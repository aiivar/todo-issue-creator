package ru.itis.todo.api;

import lombok.Builder;
import lombok.Data;

import java.nio.file.Path;

/**
 * Представляет TODO задачу, найденную в исходном коде
 */
@Data
@Builder
public class TodoItem {
    private String description;
    private Path filePath;
    private int lineNumber;
    private String priority;
    private String category;
    private String assignee;
    private String[] tags;
    
    // Значения по умолчанию
    public static final String DEFAULT_PRIORITY = "medium";
    public static final String DEFAULT_CATEGORY = "feature";
} 