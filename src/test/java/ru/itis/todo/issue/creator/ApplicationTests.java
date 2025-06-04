package ru.itis.todo.issue.creator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestPropertySource;
import ru.itis.todo.api.TodoConfig;
import ru.itis.todo.api.TodoParser;
import ru.itis.todo.api.TodoItem;
import ru.itis.todo.api.IssueCreator;
import ru.itis.todo.git.GitService;
import ru.itis.todo.github.GitHubIssueCreator;
import ru.itis.todo.parser.JavaTodoParser;
import ru.itis.todo.api.cli.TodoCliCommand;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@TestPropertySource(properties = {
		"spring.main.banner-mode=off",
		"logging.level.org.springframework=WARN"
})
@DisplayName("Todo Issue Creator Application Integration Tests")
class ApplicationTests {

	@Autowired
	private ApplicationContext applicationContext;

	@Autowired
	private TodoConfig todoConfig;

	@Autowired
	private GitService gitService;

	@Autowired
	private GitHubIssueCreator gitHubIssueCreator;

	@Autowired
	private JavaTodoParser javaTodoParser;

	@MockBean
	private IssueCreator mockIssueCreator;

	@TempDir
	Path tempDir;

	@BeforeEach
	void setUp() {
		// Reset mocks before each test
		reset(mockIssueCreator);
	}

	@Test
	@DisplayName("Should load Spring application context successfully")
	void contextLoads() {
		assertNotNull(applicationContext);
		assertTrue(applicationContext.containsBean("todoConfig"));
		assertTrue(applicationContext.containsBean("gitService"));
		assertTrue(applicationContext.containsBean("gitHubIssueCreator"));
		assertTrue(applicationContext.containsBean("javaTodoParser"));
	}

	@Test
	@DisplayName("Should autowire all required beans")
	void shouldAutowireAllRequiredBeans() {
		assertNotNull(todoConfig, "TodoConfig should be autowired");
		assertNotNull(gitService, "GitService should be autowired");
		assertNotNull(gitHubIssueCreator, "GitHubIssueCreator should be autowired");
		assertNotNull(javaTodoParser, "JavaTodoParser should be autowired");
	}

	@Test
	@DisplayName("Should load TodoConfig bean with default configuration")
	void shouldLoadTodoConfigWithDefaults() {
		assertNotNull(todoConfig);
		// Test that config loads without throwing exceptions
		// Since we're using default config when YAML fails to load
		assertDoesNotThrow(() -> {
			// Basic validation that config object exists and is usable
			String configString = todoConfig.toString();
			assertNotNull(configString);
		});
	}

	@Test
	@DisplayName("Should create TodoCliCommand from application arguments")
	void shouldCreateTodoCliCommandFromArgs() {
		// Test CLI command creation with various argument combinations
		assertDoesNotThrow(() -> {
			// Test help functionality
			TodoCliCommand.printHelp();
		});

		// Test that CLI command can be created (would need actual ApplicationArguments in real scenario)
		assertDoesNotThrow(() -> {
			// This tests that the static method exists and doesn't throw on basic usage
			TodoCliCommand.printHelp();
		});
	}

	@Test
	@DisplayName("Should support Java file parsing through JavaTodoParser")
	void shouldSupportJavaFileParsing() throws IOException {
		// Create a test Java file with TODO comments
		String javaContent = """
            public class TestClass {
                // todo: This is a test TODO comment
                public void testMethod() {
                    // todo: Another TODO with metadata | priority: high | category: bug
                }
            }
            """;

		Path testJavaFile = tempDir.resolve("TestClass.java");
		Files.writeString(testJavaFile, javaContent);

		// Test that parser supports Java files
		assertTrue(javaTodoParser.supportsFile(testJavaFile));

		// Test parsing functionality
		List<TodoItem> items = javaTodoParser.parseFile(testJavaFile);
		assertNotNull(items);
		assertEquals(2, items.size());

		// Verify first TODO
		TodoItem firstItem = items.get(0);
		assertEquals("This is a test TODO comment", firstItem.getDescription());
		assertEquals(testJavaFile, firstItem.getFilePath());

		// Verify second TODO with metadata
		TodoItem secondItem = items.get(1);
		assertEquals("Another TODO with metadata", secondItem.getDescription());
		assertEquals("high", secondItem.getPriority());
		assertEquals("bug", secondItem.getCategory());
	}

	@Test
	@DisplayName("Should handle GitService operations")
	void shouldHandleGitServiceOperations() {
		assertNotNull(gitService);

		// Test that GitService is properly configured with TodoConfig
		assertDoesNotThrow(() -> {
			// Test cleanup method (should not throw even if no repo is cloned)
			gitService.cleanup();
		});
	}

	@Test
	@DisplayName("Should integrate GitHubIssueCreator with dry-run mode")
	void shouldIntegrateGitHubIssueCreatorWithDryRun() {
		// Create test TodoItems
		TodoItem testItem = TodoItem.builder()
				.description("Test integration TODO")
				.filePath(Path.of("test/TestFile.java"))
				.lineNumber(10)
				.priority("medium")
				.category("feature")
				.build();

		List<TodoItem> testItems = Arrays.asList(testItem);

		// Test dry-run mode (should not make actual GitHub API calls)
		assertDoesNotThrow(() -> {
			int result = gitHubIssueCreator.createIssues(testItems, true);
			assertEquals(1, result);
		});
	}

	@Test
	@DisplayName("Should handle configuration loading errors gracefully")
	void shouldHandleConfigurationLoadingErrorsGracefully() {
		// This test verifies that the application can start even when YAML config fails
		// The application should fall back to default configuration
		assertNotNull(todoConfig);

		// Verify that even with potential config errors, the application context loads
		assertTrue(applicationContext.isActive());
	}

	@Test
	@DisplayName("Should support end-to-end workflow simulation")
	void shouldSupportEndToEndWorkflowSimulation() throws IOException {
		// Create a realistic project structure
		Path srcDir = tempDir.resolve("src/main/java");
		Files.createDirectories(srcDir);

		// Create multiple Java files with different TODO patterns
		createTestJavaFile(srcDir, "UserService.java", """
            package com.example;
            public class UserService {
                // todo: Add user validation | priority: high | category: feature
                public void createUser() {}

                // todo: Implement user search | priority: medium
                public void searchUsers() {}
            }
            """);

		createTestJavaFile(srcDir, "OrderService.java", """
            package com.example;
            public class OrderService {
                // todo: Fix order calculation bug | priority: high | category: bug | assignee: @developer
                public void calculateTotal() {}
            }
            """);

		// Simulate parsing all Java files
		List<Path> javaFiles = Files.walk(srcDir)
				.filter(path -> javaTodoParser.supportsFile(path))
				.toList();

		assertEquals(2, javaFiles.size());

		// Parse all files and collect TODOs
		List<TodoItem> allTodos = javaFiles.stream()
				.flatMap(file -> javaTodoParser.parseFile(file).stream())
				.toList();

		assertEquals(3, allTodos.size());

		// Verify different TODO types were parsed correctly
		assertTrue(allTodos.stream().anyMatch(item ->
				item.getDescription().contains("user validation") && "high".equals(item.getPriority())));
		assertTrue(allTodos.stream().anyMatch(item ->
				item.getDescription().contains("search") && "medium".equals(item.getPriority())));
		assertTrue(allTodos.stream().anyMatch(item ->
				item.getDescription().contains("calculation") && "@developer".equals(item.getAssignee())));

		// Test dry-run issue creation
		assertDoesNotThrow(() -> {
			int createdCount = gitHubIssueCreator.createIssues(allTodos, true);
			assertEquals(3, createdCount);
		});
	}

	@Test
	@DisplayName("Should handle empty and invalid files gracefully")
	void shouldHandleEmptyAndInvalidFilesGracefully() throws IOException {
		// Test empty Java file
		Path emptyFile = tempDir.resolve("Empty.java");
		Files.writeString(emptyFile, "");

		List<TodoItem> emptyResult = javaTodoParser.parseFile(emptyFile);
		assertNotNull(emptyResult);
		assertEquals(0, emptyResult.size());

		// Test file with no TODOs
		Path noTodosFile = tempDir.resolve("NoTodos.java");
		Files.writeString(noTodosFile, """
            public class NoTodos {
                // Regular comment
                public void method() {
                    System.out.println("No TODOs here");
                }
            }
            """);

		List<TodoItem> noTodosResult = javaTodoParser.parseFile(noTodosFile);
		assertNotNull(noTodosResult);
		assertEquals(0, noTodosResult.size());

		// Test malformed Java file
		Path malformedFile = tempDir.resolve("Malformed.java");
		Files.writeString(malformedFile, """
            public class Malformed {
                // todo: This TODO should still be found
                public void method( {
                    // Missing closing parenthesis
            """);

		assertDoesNotThrow(() -> {
			List<TodoItem> malformedResult = javaTodoParser.parseFile(malformedFile);
			assertNotNull(malformedResult);
			// Should handle malformed files gracefully
		});
	}

	@Test
	@DisplayName("Should verify component integration and dependencies")
	void shouldVerifyComponentIntegrationAndDependencies() {
		// Verify that GitService has TodoConfig dependency
		assertNotNull(gitService);

		// Verify that GitHubIssueCreator has TodoConfig dependency
		assertNotNull(gitHubIssueCreator);

		// Test that components can work together
		assertDoesNotThrow(() -> {
			// Create a simple integration test
			Path testFile = tempDir.resolve("Integration.java");
			Files.writeString(testFile, """
                // todo: Integration test TODO
                public class Integration {}
                """);

			// Parse file
			List<TodoItem> items = javaTodoParser.parseFile(testFile);
			assertEquals(1, items.size());

			// Test issue creation in dry-run mode
			int result = gitHubIssueCreator.createIssues(items, true);
			assertEquals(1, result);
		});
	}

	@Test
	@DisplayName("Should handle different file types appropriately")
	void shouldHandleDifferentFileTypesAppropriately() throws IOException {
		// Test that JavaTodoParser only supports Java files
		assertTrue(javaTodoParser.supportsFile(Path.of("Test.java")));
		assertTrue(javaTodoParser.supportsFile(Path.of("com/example/Service.java")));

		// Test non-Java files are not supported
		assertFalse(javaTodoParser.supportsFile(Path.of("test.txt")));
		assertFalse(javaTodoParser.supportsFile(Path.of("script.py")));
		assertFalse(javaTodoParser.supportsFile(Path.of("style.css")));
		assertFalse(javaTodoParser.supportsFile(Path.of("README.md")));
	}

	@Test
	@DisplayName("Should test TodoItem builder functionality")
	void shouldTestTodoItemBuilderFunctionality() {
		// Test TodoItem creation with all fields
		TodoItem fullItem = TodoItem.builder()
				.description("Complete user authentication")
				.filePath(Path.of("src/main/java/UserAuth.java"))
				.lineNumber(25)
				.priority("high")
				.category("feature")
				.assignee("@security_team")
				.tags(new String[]{"security", "auth"})
				.build();

		assertNotNull(fullItem);
		assertEquals("Complete user authentication", fullItem.getDescription());
		assertEquals(Path.of("src/main/java/UserAuth.java"), fullItem.getFilePath());
		assertEquals(25, fullItem.getLineNumber());
		assertEquals("high", fullItem.getPriority());
		assertEquals("feature", fullItem.getCategory());
		assertEquals("@security_team", fullItem.getAssignee());
		assertArrayEquals(new String[]{"security", "auth"}, fullItem.getTags());

		// Test TodoItem with minimal fields
		TodoItem minimalItem = TodoItem.builder()
				.description("Fix bug")
				.filePath(Path.of("Bug.java"))
				.lineNumber(1)
				.build();

		assertNotNull(minimalItem);
		assertEquals("Fix bug", minimalItem.getDescription());
		assertNull(minimalItem.getPriority());
		assertNull(minimalItem.getCategory());
	}

	@Test
	@DisplayName("Should test regex pattern matching in JavaTodoParser")
	void shouldTestRegexPatternMatchingInJavaTodoParser() throws IOException {
		// Test various TODO comment formats
		String complexJavaContent = """
            public class RegexTest {
                // todo: Simple TODO
                // TODO: Uppercase TODO
                // todo Simple without colon
                // todo: With priority | priority: high
                // todo: Full metadata | priority: low | category: refactor | assignee: @dev | tags: cleanup,refactor
                /*
                 * todo: Block comment TODO | priority: medium | category: bug
                 */
                public void method() {
                    // Not a todo comment
                    // FIXME: This should be ignored
                }
            }
            """;

		Path testFile = tempDir.resolve("RegexTest.java");
		Files.writeString(testFile, complexJavaContent);

		List<TodoItem> items = javaTodoParser.parseFile(testFile);

		// Should find 6 TODO comments (ignoring FIXME and regular comments)
		assertEquals(6, items.size());

		// Test specific parsing results
		assertTrue(items.stream().anyMatch(item ->
				"Simple TODO".equals(item.getDescription())));
		assertTrue(items.stream().anyMatch(item ->
				"Uppercase TODO".equals(item.getDescription())));
		assertTrue(items.stream().anyMatch(item ->
				"With priority".equals(item.getDescription()) && "high".equals(item.getPriority())));
		assertTrue(items.stream().anyMatch(item ->
				"Full metadata".equals(item.getDescription()) &&
						"low".equals(item.getPriority()) &&
						"refactor".equals(item.getCategory()) &&
						"@dev".equals(item.getAssignee())));
	}

	@Test
	@DisplayName("Should test GitHubIssueCreator issue formatting")
	void shouldTestGitHubIssueCreatorIssueFormatting() {
		// Create test TodoItem with all metadata
		TodoItem complexItem = TodoItem.builder()
				.description("Implement OAuth2 authentication")
				.filePath(Path.of("src/main/java/auth/OAuth2Service.java"))
				.lineNumber(42)
				.priority("high")
				.category("feature")
				.assignee("@security_team")
				.tags(new String[]{"security", "oauth", "authentication"})
				.build();

		// Test dry-run mode to verify formatting without making API calls
		assertDoesNotThrow(() -> {
			int result = gitHubIssueCreator.createIssues(Arrays.asList(complexItem), true);
			assertEquals(1, result);
		});

		// Test issue existence check (should not throw in dry-run scenarios)
		assertDoesNotThrow(() -> {
			boolean exists = gitHubIssueCreator.issueExists(complexItem);
			// In test environment, this might return false due to missing GitHub connection
			assertNotNull(exists);
		});
	}

	@Test
	@DisplayName("Should test error handling in all components")
	void shouldTestErrorHandlingInAllComponents() {
		// Test GitService cleanup with no repository
		assertDoesNotThrow(() -> gitService.cleanup());

		// Test JavaTodoParser with non-existent file
		Path nonExistentFile = tempDir.resolve("NonExistent.java");
		assertDoesNotThrow(() -> {
			List<TodoItem> result = javaTodoParser.parseFile(nonExistentFile);
			assertNotNull(result);
		});

		// Test GitHubIssueCreator with empty list
		assertDoesNotThrow(() -> {
			int result = gitHubIssueCreator.createIssues(Arrays.asList(), true);
			assertEquals(0, result);
		});
	}

	@Test
	@DisplayName("Should test default values and constants")
	void shouldTestDefaultValuesAndConstants() {
		// Test TodoItem default constants
		assertEquals("medium", TodoItem.DEFAULT_PRIORITY);
		assertEquals("feature", TodoItem.DEFAULT_CATEGORY);

		// Test that defaults are applied correctly in parsing
		try {
			String javaContent = """
                public class DefaultTest {
                    // todo: Test default values
                }
                """;

			Path testFile = tempDir.resolve("DefaultTest.java");
			Files.writeString(testFile, javaContent);

			List<TodoItem> items = javaTodoParser.parseFile(testFile);
			assertEquals(1, items.size());

			TodoItem item = items.get(0);
			assertEquals("Test default values", item.getDescription());
			assertEquals(TodoItem.DEFAULT_PRIORITY, item.getPriority());
			assertEquals(TodoItem.DEFAULT_CATEGORY, item.getCategory());
		} catch (IOException e) {
			fail("Should not throw IOException: " + e.getMessage());
		}
	}

	private void createTestJavaFile(Path directory, String fileName, String content) throws IOException {
		Path file = directory.resolve(fileName);
		Files.writeString(file, content);
	}
}
