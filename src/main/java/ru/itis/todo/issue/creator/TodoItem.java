package ru.itis.todo.issue.creator;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TodoItem {
    private String filePath;
    private int lineNumber;
    private String todoText;
}