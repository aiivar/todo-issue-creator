package ru.itis.todo.api;

import java.nio.file.Path;
import java.util.List;

public interface TodoParser {

    List<TodoItem> parseFile(Path filePath);

    boolean supportsFile(Path filePath);
} 