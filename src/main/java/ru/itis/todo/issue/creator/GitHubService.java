package ru.itis.todo.issue.creator;

import org.springframework.stereotype.Service;
import java.net.http.*;
import java.net.*;

@Service
public class GitHubService {

    private static final String GITHUB_API_URL = "https://api.github.com/repos/%s/%s/issues";

    public void createIssue(String owner, String repo, String token, TodoItem todo) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        String url = String.format(GITHUB_API_URL, owner, repo);

        String jsonBody = String.format(
                "{ \"title\": \"%s\", \"body\": \"%s\" }",
                escapeJson(String.format("TODO in %s: %s",
                        todo.getFilePath(),
                        truncate(todo.getTodoText(), 50))),
                escapeJson(String.format("**File:** %s\n**Line:** %d\n**TODO:** %s",
                        todo.getFilePath(),
                        todo.getLineNumber(),
                        todo.getTodoText()))
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "token " + token)
                .header("Accept", "application/vnd.github.v3+json")
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 201) {
            throw new RuntimeException("Failed to create issue: " + response.body());
        }
    }

    private String escapeJson(String input) {
        return input.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private String truncate(String input, int maxLength) {
        return input.length() > maxLength ? input.substring(0, maxLength) + "..." : input;
    }
}