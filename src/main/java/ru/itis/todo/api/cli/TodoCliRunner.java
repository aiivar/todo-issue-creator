package ru.itis.todo.api.cli;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import ru.itis.todo.api.IssueCreator;
import ru.itis.todo.api.TodoConfig;
import ru.itis.todo.api.TodoItem;
import ru.itis.todo.api.TodoParser;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Основной класс для запуска CLI приложения
 */
@Component
@RequiredArgsConstructor
public class TodoCliRunner implements ApplicationRunner {
    private final List<TodoParser> parsers;
    private final IssueCreator issueCreator;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (args.containsOption("help")) {
            TodoCliCommand.printHelp();
            return;
        }

        TodoCliCommand command = TodoCliCommand.fromArgs(args);
        TodoConfig config = TodoConfig.fromYaml(command.getConfigPath());

        // Переопределяем репозиторий из командной строки, если указан
        if (command.getRepo() != null) {
            config.getGithub().setRepo(command.getRepo());
        }

        List<TodoItem> items = new ArrayList<>();
        
        // Рекурсивно обходим все файлы в исходной директории
        try (Stream<Path> paths = Files.walk(command.getSourceDir())) {
            paths.filter(Files::isRegularFile)
                .forEach(path -> {
                    // Находим подходящий парсер для файла
                    TodoParser parser = parsers.stream()
                        .filter(p -> p.supportsFile(path))
                        .findFirst()
                        .orElse(null);

                    if (parser != null) {
                        if (command.isVerbose()) {
                            System.out.println("Обрабатываем файл: " + path);
                        }
                        items.addAll(parser.parseFile(path));
                    }
                });
        }

        if (items.isEmpty()) {
            System.out.println("TODO комментарии не найдены");
            return;
        }

        System.out.printf("Найдено %d TODO комментариев%n", items.size());
        
        if (command.isDryRun()) {
            System.out.println("\nРежим dry-run - показываем, что будет создано:");
        }

        int created = issueCreator.createIssues(items, command.isDryRun());
        
        if (command.isDryRun()) {
            System.out.printf("%nБудет создано %d issues%n", created);
        } else {
            System.out.printf("Создано %d issues%n", created);
        }
    }
} 