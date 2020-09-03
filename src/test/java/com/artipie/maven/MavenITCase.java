/*
 * MIT License
 *
 * Copyright (c) 2020 Artipie
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.artipie.maven;

import com.artipie.asto.Key;
import com.artipie.asto.Storage;
import com.artipie.asto.blocking.BlockingStorage;
import com.artipie.asto.fs.FileStorage;
import com.artipie.asto.memory.InMemoryStorage;
import com.artipie.asto.test.TestResource;
import com.artipie.http.slice.LoggingSlice;
import com.artipie.maven.http.MavenSlice;
import com.artipie.vertx.VertxSliceServer;
import io.vertx.reactivex.core.Vertx;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.cactoos.list.ListOf;
import org.cactoos.scalar.Unchecked;
import org.hamcrest.MatcherAssert;
import org.hamcrest.text.StringContainsInOrder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;
import org.testcontainers.Testcontainers;
import org.testcontainers.containers.GenericContainer;

/**
 * Maven integration test.
 * @since 0.11
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
@EnabledOnOs({OS.LINUX, OS.MAC})
public final class MavenITCase {

    /**
     * Vertx instance.
     */
    private static final Vertx VERTX = Vertx.vertx();

    /**
     * Temporary directory for all tests.
     * @checkstyle VisibilityModifierCheck (3 lines)
     */
    @TempDir
    Path tmp;

    /**
     * Vertx slice server instance.
     */
    private VertxSliceServer server;

    /**
     * Container.
     */
    private GenericContainer<?> cntn;

    /**
     * Storage.
     */
    private Storage storage;

    /**
     * Vertx slice server port.
     */
    private int port;

    @BeforeEach
    void init() throws IOException, InterruptedException {
        this.storage = new InMemoryStorage();
        this.server = new VertxSliceServer(
            MavenITCase.VERTX,
            new LoggingSlice(new MavenSlice(this.storage))
        );
        this.port = this.server.start();
        Testcontainers.exposeHostPorts(this.port);
        final Path setting = this.tmp.resolve("settings.xml");
        setting.toFile().createNewFile();
        Files.write(setting, this.settings());
        this.cntn = new GenericContainer<>("centos:centos8")
            .withCommand("tail", "-f", "/dev/null")
            .withWorkingDirectory("/home/")
            .withFileSystemBind(this.tmp.toString(), "/home");
        this.cntn.start();
        this.cntn.execInContainer("yum", "-y", "install", "maven");
    }

    @Test
    void downloadsDependency() throws Exception {
        this.addFilesToStorage();
        MatcherAssert.assertThat(
            this.exec(
                "mvn", "-s", "/home/settings.xml", "dependency:get",
                "-Dartifact=com.artipie:helloworld:0.1"
            ).replaceAll("\n", ""),
            new StringContainsInOrder(
                new ListOf<String>(
                    // @checkstyle LineLengthCheck (1 line)
                    String.format("Downloaded from my-repo: http://host.testcontainers.internal:%d/com/artipie/helloworld/0.1/helloworld-0.1.jar (11 B", this.port),
                    "BUILD SUCCESS"
                )
            )
        );
    }

    @AfterEach
    void stopContainer() {
        this.server.close();
        this.cntn.stop();
    }

    @AfterAll
    static void close() {
        MavenITCase.VERTX.close();
    }

    /**
     * Executes dnf command in container.
     * @param actions What to do
     * @return String stdout
     * @throws Exception On error
     */
    private String exec(final String... actions) throws Exception {
        return this.cntn.execInContainer(actions).getStdout();
    }

    private List<String> settings() {
        return new ListOf<String>(
            "<settings>",
            "    <profiles>",
            "        <profile>",
            "            <id>artipie</id>",
            "            <repositories>",
            "                <repository>",
            "                    <id>my-repo</id>",
            String.format("<url>http://host.testcontainers.internal:%d/</url>", this.port),
            "                </repository>",
            "            </repositories>",
            "        </profile>",
            "    </profiles>",
            "    <activeProfiles>",
            "        <activeProfile>artipie</activeProfile>",
            "    </activeProfiles>",
            "</settings>"
        );
    }

    private void addFilesToStorage() throws InterruptedException {
        final Storage resources = new FileStorage(
            new TestResource("com/artipie/helloworld").asPath()
        );
        final BlockingStorage bsto = new BlockingStorage(resources);
        bsto.list(Key.ROOT).stream()
            .map(Key::string)
            .forEach(
                item -> new Unchecked<>(
                    () -> {
                        new BlockingStorage(this.storage).save(
                            new Key.From("com", "artipie", "helloworld", item),
                            new Unchecked<>(() -> bsto.value(new Key.From(item))).value()
                        );
                        return true;
                    }
            ).value()
        );
    }
}
