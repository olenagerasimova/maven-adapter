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

package com.artipie.maven.aether;

import java.nio.file.Path;
import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.internal.impl.SimpleLocalRepositoryManagerFactory;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.NoLocalRepositoryManagerException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Tests for {@link LocalArtifactResolver}.
 * @since 0.1
 */
public final class LocalArtifactResolverTest {

    /**
     * Test temporary directory.
     * By JUnit annotation contract it should not be private
     * @checkstyle VisibilityModifierCheck (3 lines)
     */
    @TempDir
    Path temp;

    @ParameterizedTest
    @CsvSource({
        "'org/example/artifact/1.0/artifact-1.0.jar', 'org.example:artifact:jar:1.0'",
        "'group/name/1.0/name-1.0-sources.jar', 'group:name:jar:sources:1.0'"
    })
    public void shouldResolve(final String path, final String coords) throws Exception {
        Assertions.assertEquals(
            this.temp.resolve(path),
            new LocalArtifactResolver(this.newSession())
                .resolve(new DefaultArtifact(coords))
        );
    }

    @Test
    public void shouldFailOnNullArtifact() throws Exception {
        Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> new LocalArtifactResolver(this.newSession())
                .resolve(null)
        );
    }

    @ParameterizedTest
    @EmptySource
    @ValueSource(strings = {"badcoords", "two:parts"})
    public void shouldFailOnBadCoords(final String coords) throws Exception {
        Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> new LocalArtifactResolver(this.newSession())
                .resolve(new DefaultArtifact(coords))
        );
    }

    private DefaultRepositorySystemSession newSession() throws NoLocalRepositoryManagerException {
        final var session = MavenRepositorySystemUtils.newSession();
        session.setLocalRepositoryManager(
            new SimpleLocalRepositoryManagerFactory()
                .newInstance(
                    session,
                    new LocalRepository(this.temp.toFile())
                )
        );
        return session;
    }
}
