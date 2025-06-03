package ru.itis.todo.api;

import java.nio.file.Path;
import java.util.List;

public interface TodoParser {
    /**
     * Checks if this parser can handle the given file type
     * @param filePath path to the file
     * @return true if this parser can handle the file
     */
    boolean canHandle(Path filePath);

    /**
     * Parse TODO comments from the given file
     * @param filePath path to the file
     * @return list of found TODO comments
     */
    List<TodoComment> parse(Path filePath);
} 