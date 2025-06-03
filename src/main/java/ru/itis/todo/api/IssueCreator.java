package ru.itis.todo.api;

import java.util.List;

/**
 * Интерфейс для создания issues в GitHub
 */
public interface IssueCreator {
    /**
     * Создает issues в GitHub на основе TODO задач
     * @param items список TODO задач
     * @param dryRun если true, только показывает что будет создано, без реального создания
     * @return количество созданных или симулированных issues
     */
    int createIssues(List<TodoItem> items, boolean dryRun);

    /**
     * Проверяет, существует ли уже issue для данной TODO задачи
     * @param item TODO задача
     * @return true если issue уже существует
     */
    boolean issueExists(TodoItem item);
} 