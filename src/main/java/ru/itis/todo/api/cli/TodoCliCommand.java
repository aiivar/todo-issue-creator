package ru.itis.todo.api.cli;

import lombok.Builder;
import lombok.Data;
import org.springframework.boot.ApplicationArguments;

import java.nio.file.Path;

/**
 * Представляет параметры командной строки
 */
@Data
@Builder
public class TodoCliCommand {
    private Path configPath;
    private String repo;
    private boolean dryRun;
    private Path sourceDir;
    private boolean verbose;

    /**
     * Создает команду из аргументов Spring Boot
     */
    public static TodoCliCommand fromArgs(ApplicationArguments args) {
        return TodoCliCommand.builder()
                .configPath(args.containsOption("config") 
                        ? Path.of(args.getOptionValues("config").get(0))
                        : Path.of(".todo-to-issue.yaml"))
                .repo(args.containsOption("repo") 
                        ? args.getOptionValues("repo").get(0)
                        : null)
                .dryRun(args.containsOption("dry-run"))
                .sourceDir(args.containsOption("source") 
                        ? Path.of(args.getOptionValues("source").get(0))
                        : Path.of("."))
                .verbose(args.containsOption("verbose"))
                .build();
    }

    /**
     * Выводит справку по использованию
     */
    public static void printHelp() {
        System.out.println("Использование: todo-issue-creator [опции]");
        System.out.println("Опции:");
        System.out.println("  --config <путь>    Путь к файлу конфигурации (по умолчанию: .todo-to-issue.yaml)");
        System.out.println("  --repo <repo>      Репозиторий GitHub (например: username/repository)");
        System.out.println("  --dry-run          Показать, что будет создано, без реального создания issues");
        System.out.println("  --source <путь>    Путь к исходному коду (по умолчанию: текущая директория)");
        System.out.println("  --verbose          Подробный вывод");
        System.out.println("  --help             Показать эту справку");
    }
} 