package ru.itis.todo.issue.creator;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.nio.file.Path;
import java.util.List;

@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Bean
	CommandLineRunner run(GitHubService gitHubService) {
		return args -> {
			if (args.length != 3) {
				System.out.println("Usage: java -jar todo-issue-creator.jar <owner> <repo> <token>");
				System.exit(1);
			}

			String owner = args[0];
			String repo = args[1];
			String token = args[2];

			List<TodoItem> todos = TodoFinder.findTodos(Path.of("."));
			System.out.println("Found " + todos.size() + " TODOs");

			for (TodoItem todo : todos) {
				gitHubService.createIssue(owner, repo, token, todo);
				Thread.sleep(1000);
			}
		};
	}

}
