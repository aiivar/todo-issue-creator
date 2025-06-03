package ru.itis.todo;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.yaml.snakeyaml.Yaml;
import ru.itis.todo.api.IssueCreator;
import ru.itis.todo.api.TodoComment;
import ru.itis.todo.api.TodoParser;
import ru.itis.todo.config.TodoConfig;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@SpringBootApplication
@RequiredArgsConstructor
public class TodoIssueCreatorApplication implements CommandLineRunner {
    private final List<TodoParser> parsers;
    private final IssueCreator issueCreator;

    public static void main(String[] args) {
        SpringApplication.run(TodoIssueCreatorApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        // Load configuration
        TodoConfig config;
        Path configPath = Paths.get(".todo-to-issue.yaml");
        if (Files.exists(configPath)) {
            try (InputStream input = new FileInputStream(configPath.toFile())) {
                Yaml yaml = new Yaml();
                config = yaml.loadAs(input, TodoConfig.class);
            }
        } else {
            System.err.println("Configuration file .todo-to-issue.yaml not found");
            return;
        }

        // Find all files to scan
        List<Path> filesToScan = new ArrayList<>();
        try (Stream<Path> paths = Files.walk(Paths.get("."))) {
            paths.filter(Files::isRegularFile)
                    .filter(path -> {
                        String pathStr = path.toString();
                        // Skip hidden and build directories
                        if (pathStr.contains("/.") || pathStr.contains("/target/") || pathStr.contains("/build/")) {
                            return false;
                        }
                        // Apply include/exclude filters if configured
                        if (config.getFilters().getExclude() != null) {
                            for (String exclude : config.getFilters().getExclude()) {
                                if (pathStr.matches(exclude)) {
                                    return false;
                                }
                            }
                        }
                        if (config.getFilters().getInclude() != null) {
                            for (String include : config.getFilters().getInclude()) {
                                if (pathStr.matches(include)) {
                                    return true;
                                }
                            }
                            return false;
                        }
                        return true;
                    })
                    .forEach(filesToScan::add);
        }

        // Parse TODOs from files
        List<TodoComment> allComments = new ArrayList<>();
        for (Path file : filesToScan) {
            for (TodoParser parser : parsers) {
                if (parser.canHandle(file)) {
                    try {
                        List<TodoComment> comments = parser.parse(file);
                        allComments.addAll(comments);
                    } catch (Exception e) {
                        System.err.println("Failed to parse file: " + file);
                        e.printStackTrace();
                    }
                    break;
                }
            }
        }

        // Create issues
        if (!allComments.isEmpty()) {
            int created = issueCreator.createIssues(allComments);
            System.out.printf("Created %d issues from %d TODO comments%n", created, allComments.size());
        } else {
            System.out.println("No TODO comments found");
        }
    }
} 