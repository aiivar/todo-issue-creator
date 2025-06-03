package ru.itis.todo.github;

import lombok.RequiredArgsConstructor;
import org.kohsuke.github.GHIssue;
import org.kohsuke.github.GHIssueBuilder;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.springframework.stereotype.Component;
import ru.itis.todo.api.IssueCreator;
import ru.itis.todo.api.TodoComment;
import ru.itis.todo.config.TodoConfig;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class GitHubIssueCreator implements IssueCreator {
    private final TodoConfig config;

    @Override
    public int createIssues(List<TodoComment> comments) {
        try {
            GitHub github = GitHub.connect(null, config.getGithub().getToken());
            GHRepository repository = github.getRepository(config.getGithub().getRepo());
            
            int created = 0;
            for (TodoComment comment : comments) {
                try {
                    String title = formatTemplate(config.getIssueTemplate().getTitle(), comment);
                    String body = formatTemplate(config.getIssueTemplate().getBody(), comment);
                    List<String> labels = config.getIssueTemplate().getLabels().stream()
                            .map(label -> formatTemplate(label, comment))
                            .toList();

                    GHIssueBuilder issueBuilder = repository.createIssue(title)
                            .body(body);
                    
                    // Add labels one by one
                    for (String label : labels) {
                        issueBuilder.label(label);
                    }
                    
                    GHIssue issue = issueBuilder.create();

                    if (comment.getAssignee() != null) {
                        String assignee = comment.getAssignee().replace("@", "");
                        issue.addAssignees(repository.getRoot().getUser(assignee));
                    }

                    created++;
                } catch (Exception e) {
                    // Log error but continue with other issues
                    System.err.println("Failed to create issue for comment: " + comment);
                    e.printStackTrace();
                }
            }
            return created;
        } catch (IOException e) {
            throw new RuntimeException("Failed to connect to GitHub", e);
        }
    }

    private String formatTemplate(String template, TodoComment comment) {
        Map<String, String> replacements = Map.of(
                "{description}", comment.getDescription(),
                "{file}", comment.getFilePath(),
                "{line}", String.valueOf(comment.getLineNumber()),
                "{priority}", comment.getPriority().toString().toLowerCase(),
                "{category}", comment.getCategory().toString().toLowerCase()
        );

        String result = template;
        for (Map.Entry<String, String> entry : replacements.entrySet()) {
            result = result.replace(entry.getKey(), entry.getValue());
        }

        return result;
    }
} 