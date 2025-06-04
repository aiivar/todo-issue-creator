package ru.itis.todo.api.cli;

import lombok.Builder;
import lombok.Data;
import lombok.SneakyThrows;
import org.springframework.boot.ApplicationArguments;

import java.nio.file.Path;
import java.nio.file.Paths;

@Data
@Builder
public class TodoCliCommand {
    private Path configPath;
    private String repo;
    private boolean dryRun;
    private Path sourceDir;
    private boolean verbose;

    @SneakyThrows
    public static TodoCliCommand fromArgs(ApplicationArguments args) {
        return TodoCliCommand.builder()
                .configPath(args.containsOption("config") 
                        ? Path.of(args.getOptionValues("config").get(0))
                        : Paths.get(ClassLoader.getSystemResource(".todo-to-issue.yaml").toURI()))
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