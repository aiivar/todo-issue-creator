package ru.itis.todo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import ru.itis.todo.api.TodoConfig;

import java.nio.file.Path;

@SpringBootApplication
public class TodoIssueCreatorApplication {
    public static void main(String[] args) {
        SpringApplication.run(TodoIssueCreatorApplication.class, args);
    }

    @Bean
    public TodoConfig todoConfig() {
        try {
            return TodoConfig.fromYaml(Path.of(".todo-to-issue.yaml"));
        } catch (Exception e) {
            System.err.println("Ошибка загрузки конфигурации: " + e.getMessage());
            System.err.println("Используем конфигурацию по умолчанию");
            return new TodoConfig();
        }
    }
} 