package ru.itis.todo.api;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class TodoComment {
    private String description;
    private String filePath;
    private int lineNumber;
    private Priority priority;
    private Category category;
    private String assignee;
    private List<String> tags;
    private Map<String, String> additionalFields;

    public enum Priority {
        LOW, MEDIUM, HIGH
    }

    public enum Category {
        BUG, FEATURE, REFACTOR
    }
} 