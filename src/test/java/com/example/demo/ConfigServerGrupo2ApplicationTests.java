package com.example.demo;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ConfigServerGrupo2ApplicationTests {

    @TempDir
    static Path repository;

    @Autowired
    Environment environment;

    @DynamicPropertySource
    static void localGitRepository(DynamicPropertyRegistry registry) throws IOException, GitAPIException {
        Files.writeString(repository.resolve("ms-factura.properties"), "config.integration.message=desde-git\n");
        try (Git git = Git.init().setDirectory(repository.toFile()).setInitialBranch("main").call()) {
            git.add().addFilepattern("ms-factura.properties").call();
            git.commit().setMessage("Configuracion de prueba")
                    .setAuthor("Config Test", "config@example.test").call();
        }
        registry.add("spring.cloud.config.server.git.uri", () -> repository.toUri().toString());
        registry.add("spring.cloud.config.server.git.default-label", () -> "main");
    }

    @Test
    void servesConfigurationFromGit() throws IOException, InterruptedException {
        HttpResponse<String> response = get("/ms-factura/default/main");

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).contains("config.integration.message", "desde-git", "ms-factura.properties");
    }

    @Test
    void rejectsApplicationsWithoutConfiguration() throws IOException, InterruptedException {
        assertThat(get("/aplicacion-inexistente/default/main").statusCode()).isEqualTo(404);
    }

    private HttpResponse<String> get(String path) throws IOException, InterruptedException {
        URI uri = URI.create("http://localhost:" + environment.getProperty("local.server.port") + path);
        try (HttpClient client = HttpClient.newHttpClient()) {
            return client.send(HttpRequest.newBuilder(uri).GET().build(), HttpResponse.BodyHandlers.ofString());
        }
    }
}
