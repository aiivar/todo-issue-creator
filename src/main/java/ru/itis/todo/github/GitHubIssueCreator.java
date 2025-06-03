package ru.itis.todo.github;

import lombok.RequiredArgsConstructor;
import org.kohsuke.github.GHIssue;
import org.kohsuke.github.GHIssueBuilder;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.springframework.stereotype.Component;
import ru.itis.todo.api.IssueCreator;
import ru.itis.todo.api.TodoConfig;
import ru.itis.todo.api.TodoItem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class GitHubIssueCreator implements IssueCreator {
    private final TodoConfig config;
    private GitHub gitHub;
    private GHRepository repository;

    private void init() throws IOException {
        if (gitHub == null) {
            gitHub = GitHub.connectUsingOAuth(config.getGithub().getToken());
            String[] repoParts = config.getGithub().getRepo().split("/");
            repository = gitHub.getRepository(config.getGithub().getRepo());
        }
    }

    @Override
    public int createIssues(List<TodoItem> items, boolean dryRun) {
        int count = 0;
        try {
            init();

            for (TodoItem item : items) {
                if (issueExists(item)) {
                    continue;
                }

                String title = formatTitle(item);
                String body = formatBody(item);
                List<String> labels = formatLabels(item);

                if (dryRun) {
                    System.out.println("\nIssue для создания:");
                    System.out.println("Заголовок: " + title);
                    System.out.println("Описание:\n" + body);
                    System.out.println("Метки: " + String.join(", ", labels));
                    if (item.getAssignee() != null) {
                        System.out.println("Назначено: " + item.getAssignee());
                    }
                } else {
                    GHIssueBuilder issueBuilder = repository.createIssue(title)
                            .body(body)
                            .label(labels.toArray(new String[0]));

                    if (item.getAssignee() != null) {
                        issueBuilder.assignee(item.getAssignee().substring(1)); // Убираем @ из имени
                    }

                    issueBuilder.create();
                }
                count++;
            }
        } catch (IOException e) {
            throw new RuntimeException("Ошибка при создании issues: " + e.getMessage(), e);
        }
        return count;
    }

    @Override
    public boolean issueExists(TodoItem item) {
        try {
            init();
            // Ищем issue по заголовку и описанию файла
            String query = String.format("repo:%s \"%s\" in:title \"%s\" in:body",
                    config.getGithub().getRepo(),
                    formatTitle(item),
                    item.getFilePath());
            
            return gitHub.searchIssues().q(query).list().iterator().hasNext();
        } catch (IOException e) {
            // В случае ошибки считаем, что issue не существует
            return false;
        }
    }

    private String formatTitle(TodoItem item) {
        String template = config.getIssueTemplate().getTitle();
        if (template == null) {
            return item.getDescription();
        }
        return template
                .replace("{description}", item.getDescription())
                .replace("{category}", item.getCategory())
                .replace("{priority}", item.getPriority());
    }

    private String formatBody(TodoItem item) {
        String template = config.getIssueTemplate().getBody();
        if (template == null) {
            return String.format("""
                **Задача**: %s
                **Файл**: %s
                **Строка**: %d
                **Приоритет**: %s
                **Категория**: %s%s%s
                """,
                item.getDescription(),
                item.getFilePath(),
                item.getLineNumber(),
                item.getPriority(),
                item.getCategory(),
                item.getAssignee() != null ? "\n**Назначено**: " + item.getAssignee() : "",
                item.getTags().length > 0 ? "\n**Теги**: " + String.join(", ", item.getTags()) : "");
        }
        return template
                .replace("{description}", item.getDescription())
                .replace("{file}", item.getFilePath().toString())
                .replace("{line}", String.valueOf(item.getLineNumber()))
                .replace("{priority}", item.getPriority())
                .replace("{category}", item.getCategory())
                .replace("{assignee}", item.getAssignee() != null ? item.getAssignee() : "")
                .replace("{tags}", item.getTags().length > 0 ? String.join(", ", item.getTags()) : "");
    }

    private List<String> formatLabels(TodoItem item) {
        List<String> labels = new ArrayList<>();
        labels.add("good first issue");
        labels.add(item.getCategory());
        labels.add(item.getPriority());
        if (item.getTags() != null) {
            labels.addAll(List.of(item.getTags()));
        }
        return labels;
    }
} 