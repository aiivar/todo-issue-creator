package ru.itis.todo.api;

import lombok.Data;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * Конфигурация для todo-issue-creator
 */
@Data
public class TodoConfig {
    private GitHubConfig github;
    private DefaultsConfig defaults;
    private FiltersConfig filters;
    private IssueTemplateConfig issueTemplate;

    @Data
    public static class GitHubConfig {
        private String repo;
        private String token;
    }

    @Data
    public static class DefaultsConfig {
        private String priority = TodoItem.DEFAULT_PRIORITY;
        private String category = TodoItem.DEFAULT_CATEGORY;
    }

    @Data
    public static class FiltersConfig {
        private List<String> include;
        private List<String> exclude;
    }

    @Data
    public static class IssueTemplateConfig {
        private String title;
        private String body;
        private List<String> labels;
    }

    /**
     * Загружает конфигурацию из YAML файла
     * @param configPath путь к файлу конфигурации
     * @return объект конфигурации
     * @throws IOException если файл не найден или не может быть прочитан
     */
    public static TodoConfig fromYaml(Path configPath) throws IOException {
        try (FileInputStream fis = new FileInputStream(configPath.toFile())) {
            Yaml yaml = new Yaml();
            return yaml.loadAs(fis, TodoConfig.class);
        }
    }
} 