package ru.itis.todo.api.cli;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import ru.itis.todo.api.IssueCreator;
import ru.itis.todo.api.TodoConfig;
import ru.itis.todo.api.TodoItem;
import ru.itis.todo.api.TodoParser;
import ru.itis.todo.git.GitService;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class TodoCliRunner implements ApplicationRunner {
    private final List<TodoParser> parsers;
    private final IssueCreator issueCreator;
    private final GitService gitService;
    private final TodoConfig config;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (args.containsOption("help")) {
            TodoCliCommand.printHelp();
            return;
        }

        TodoCliCommand command = TodoCliCommand.fromArgs(args);

        if (command.getRepo() != null) {
            config.getGithub().setRepo(command.getRepo());
        }

        Path sourceDir = gitService.cloneRepository();
        if (command.isVerbose()) {
            System.out.println("Клонирован репозиторий: " + config.getGithub().getRepo());
        }

        List<TodoItem> items = new ArrayList<>();

        try (Stream<Path> paths = Files.walk(sourceDir)) {
            paths.filter(Files::isRegularFile)
                .forEach(path -> {
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
            gitService.cleanup();
            return;
        }

        System.out.printf("Найдено %d TODO комментариев%n", items.size());
        
        if (command.isDryRun()) {
            System.out.println("\nРежим dry-run - показываем, что будет создано:");
        }

        int created = issueCreator.createIssues(items, command.isDryRun());

        gitService.cleanup();

        if (command.isDryRun()) {
            System.out.printf("%nБудет создано %d issues%n", created);
        } else {
            System.out.printf("Создано %d issues%n", created);
        }
    }
} 