package ru.itis.todo.parser;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.comments.Comment;
import org.springframework.stereotype.Component;
import ru.itis.todo.api.TodoItem;
import ru.itis.todo.api.TodoParser;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class JavaTodoParser implements TodoParser {
    private static final Pattern TODO_PATTERN = Pattern.compile(
        "(?i)todo:?\\s*([^|]+)(?:\\|\\s*priority:\\s*(\\w+))?(?:\\|\\s*category:\\s*(\\w+))?(?:\\|\\s*assignee:\\s*(@\\w+))?(?:\\|\\s*tags:\\s*([\\w,]+))?");

    @Override
    public List<TodoItem> parseFile(Path filePath) {
        List<TodoItem> items = new ArrayList<>();
        try {
            CompilationUnit cu = new JavaParser().parse(filePath).getResult().orElse(null);
            if (cu != null) {
                for (Comment comment : cu.getAllComments()) {
                    String content = comment.getContent().trim();
                    Matcher matcher = TODO_PATTERN.matcher(content);
                    
                    if (matcher.find()) {
                        String description = matcher.group(1).trim();
                        String priority = matcher.group(2) != null ? matcher.group(2).toLowerCase() : TodoItem.DEFAULT_PRIORITY;
                        String category = matcher.group(3) != null ? matcher.group(3).toLowerCase() : TodoItem.DEFAULT_CATEGORY;
                        String assignee = matcher.group(4);
                        String[] tags = matcher.group(5) != null ? matcher.group(5).split(",") : new String[0];

                        items.add(TodoItem.builder()
                            .description(description)
                            .filePath(filePath)
                            .lineNumber(comment.getBegin().get().line)
                            .priority(priority)
                            .category(category)
                            .assignee(assignee)
                            .tags(tags)
                            .build());
                    }
                }
            }
        } catch (IOException e) {
            // Логируем ошибку, но продолжаем работу
            System.err.println("Ошибка при парсинге файла " + filePath + ": " + e.getMessage());
        }
        return items;
    }

    @Override
    public boolean supportsFile(Path filePath) {
        return filePath.toString().toLowerCase().endsWith(".java");
    }
} 