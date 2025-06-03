package ru.itis.todo.api;

import java.nio.file.Path;
import java.util.List;

/**
 * Интерфейс для парсинга TODO комментариев из исходного кода
 */
public interface TodoParser {
    /**
     * Парсит TODO комментарии из файла
     * @param filePath путь к файлу
     * @return список найденных TODO задач
     */
    List<TodoItem> parseFile(Path filePath);

    /**
     * Проверяет, поддерживается ли данный тип файла парсером
     * @param filePath путь к файлу
     * @return true если файл поддерживается
     */
    boolean supportsFile(Path filePath);
} 