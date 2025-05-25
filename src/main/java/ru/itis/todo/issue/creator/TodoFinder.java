package ru.itis.todo.issue.creator;

import java.nio.file.*;
import java.util.*;
import java.io.IOException;

public class TodoFinder {

    public static List<TodoItem> findTodos(Path repoPath) throws IOException {
        List<TodoItem> todos = new ArrayList<>();

        Files.walk(repoPath)
                .filter(path -> Files.isRegularFile(path) && path.toString().endsWith(".java"))
                .forEach(file -> {
                    try {
                        List<String> lines = Files.readAllLines(file);
                        for (int i = 0; i < lines.size(); i++) {
                            String line = lines.get(i);
                            if (line.contains("// TODO:")) {
                                String todoText = line.split("// TODO:")[1].trim();
                                Path relativePath = repoPath.relativize(file);
                                todos.add(new TodoItem(
                                        relativePath.toString(),
                                        i + 1,
                                        todoText
                                ));
                            }
                        }
                    } catch (IOException e) {
                        System.err.println("Error reading file: " + file);
                    }
                });

        return todos;
    }
}