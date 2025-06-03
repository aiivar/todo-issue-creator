package ru.itis.todo.parser;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.comments.Comment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.itis.todo.api.TodoComment;
import ru.itis.todo.api.TodoParser;
import ru.itis.todo.config.TodoConfig;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JavaTodoParser implements TodoParser {
    private static final Pattern TODO_PATTERN = Pattern.compile(
            "(?i)todo:\\s*([^|]+)(?:\\s*\\|\\s*(.+))?",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern METADATA_PATTERN = Pattern.compile(
            "(\\w+):\\s*([^|,]+)"
    );

    private final TodoConfig config;

    @Override
    public boolean canHandle(Path filePath) {
        return filePath.toString().endsWith(".java");
    }

    @Override
    public List<TodoComment> parse(Path filePath) {
        try {
            CompilationUnit cu = new JavaParser().parse(filePath).getResult().orElse(null);
            if (cu == null) {
                return List.of();
            }

            List<TodoComment> comments = new ArrayList<>();
            for (Comment comment : cu.getAllContainedComments()) {
                String content = comment.getContent().trim();
                Matcher todoMatcher = TODO_PATTERN.matcher(content);

                if (todoMatcher.find()) {
                    String description = todoMatcher.group(1).trim();
                    String metadata = todoMatcher.group(2);

                    TodoComment.TodoCommentBuilder builder = TodoComment.builder()
                            .description(description)
                            .filePath(filePath.toString())
                            .lineNumber(comment.getBegin().get().line)
                            .priority(config.getDefaults().getPriority())
                            .category(config.getDefaults().getCategory());

                    if (metadata != null) {
                        Map<String, String> metadataMap = parseMetadata(metadata);
                        applyMetadata(builder, metadataMap);
                    }

                    comments.add(builder.build());
                }
            }

            return comments;
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse file: " + filePath, e);
        }
    }

    private Map<String, String> parseMetadata(String metadata) {
        Matcher matcher = METADATA_PATTERN.matcher(metadata);
        Map<String, String> result = new java.util.HashMap<>();
        
        while (matcher.find()) {
            String key = matcher.group(1).toLowerCase();
            String value = matcher.group(2).trim();
            result.put(key, value);
        }
        
        return result;
    }

    private void applyMetadata(TodoComment.TodoCommentBuilder builder, Map<String, String> metadata) {
        metadata.forEach((key, value) -> {
            switch (key) {
                case "priority":
                    try {
                        builder.priority(TodoComment.Priority.valueOf(value.toUpperCase()));
                    } catch (IllegalArgumentException e) {
                        // Keep default priority if invalid value
                    }
                    break;
                case "category":
                    try {
                        builder.category(TodoComment.Category.valueOf(value.toUpperCase()));
                    } catch (IllegalArgumentException e) {
                        // Keep default category if invalid value
                    }
                    break;
                case "assignee":
                    builder.assignee(value);
                    break;
                case "tags":
                    List<String> tags = Arrays.stream(value.split(","))
                            .map(String::trim)
                            .collect(Collectors.toList());
                    builder.tags(tags);
                    break;
                default:
                    Map<String, String> additionalFields = builder.build().getAdditionalFields();
                    if (additionalFields == null) {
                        additionalFields = new java.util.HashMap<>();
                    }
                    additionalFields.put(key, value);
                    builder.additionalFields(additionalFields);
            }
        });
    }
} 