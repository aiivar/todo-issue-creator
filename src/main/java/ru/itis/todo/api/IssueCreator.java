package ru.itis.todo.api;

import java.util.List;

public interface IssueCreator {

    int createIssues(List<TodoItem> items, boolean dryRun);

    boolean issueExists(TodoItem item);
} 