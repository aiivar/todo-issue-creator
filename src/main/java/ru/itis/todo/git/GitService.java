package ru.itis.todo.git;

import lombok.RequiredArgsConstructor;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.stereotype.Service;
import ru.itis.todo.api.TodoConfig;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
@RequiredArgsConstructor
public class GitService {
    private final TodoConfig config;
    private Path repoPath;

    public Path cloneRepository() throws IOException, GitAPIException {
        if (repoPath != null && Files.exists(repoPath)) {
            return repoPath;
        }

        String repo = config.getGithub().getRepo();
        if (repo == null || repo.trim().isEmpty()) {
            throw new IllegalStateException("Repository is not specified in the configuration");
        }

        repoPath = Files.createTempDirectory("todo-scanner-");
        
        String repoUrl = String.format("https://github.com/%s.git", repo);
        Git.cloneRepository()
                .setURI(repoUrl)
                .setDirectory(repoPath.toFile())
                .call();

        return repoPath;
    }

    public void cleanup() {
        if (repoPath != null && Files.exists(repoPath)) {
            deleteDirectory(repoPath.toFile());
        }
    }

    private void deleteDirectory(File directory) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
        }
        directory.delete();
    }
} 