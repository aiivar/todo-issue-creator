package ru.itis.todo.api;

import java.util.List;

public interface IssueCreator {
    /**
     * Create GitHub issues from TODO comments
     * @param comments list of TODO comments to create issues from
     * @return number of successfully created issues
     */
    int createIssues(List<TodoComment> comments);
} 