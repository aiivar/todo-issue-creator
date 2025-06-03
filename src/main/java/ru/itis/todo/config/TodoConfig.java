package ru.itis.todo.config;

import lombok.Data;
import ru.itis.todo.api.TodoComment;

import java.util.List;
import java.util.Map;

@Data
public class TodoConfig {
    private GitHub github = new GitHub();
    private Defaults defaults = new Defaults();
    private Filters filters = new Filters();
    private IssueTemplate issueTemplate = new IssueTemplate();

    @Data
    public static class GitHub {
        private String repo;
        private String token;
    }

    @Data
    public static class Defaults {
        private TodoComment.Priority priority = TodoComment.Priority.MEDIUM;
        private TodoComment.Category category = TodoComment.Category.FEATURE;
    }

    @Data
    public static class Filters {
        private List<String> include;
        private List<String> exclude;
    }

    @Data
    public static class IssueTemplate {
        private String title = "{description}";
        private String body = """
            **Task**: {description}
            **File**: {file}
            **Line**: {line}
            **Priority**: {priority}
            **Category**: {category}
            """;
        private List<String> labels = List.of("good first issue", "{category}", "{priority}");
    }
}