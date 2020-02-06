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
package com.artipie.maven.util;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests for {@link AutoCloseablePath}.
 * @since 0.1
 */
public final class AutoCloseablePathTest {

    /**
     * Test temporary directory.
     * By JUnit annotation contract it should not be private
     * @checkstyle VisibilityModifierCheck (3 lines)
     */
    @TempDir
    Path temp;

    @Test
    public void testClose() throws Exception {
        final var path = new AutoCloseablePath(
            Files.write(this.temp.resolve("close.txt"), new byte[]{})
        );
        Assumptions.assumeTrue(Files.exists(path.unwrap()));
        path.close();
        Assertions.assertFalse(Files.exists(path.unwrap()));
    }

    @Test
    public void shouldThrowOnClose() throws Exception {
        final var dir = this.temp.resolve("directory");
        Files.createDirectories(dir);
        Assumptions.assumeTrue(Files.isDirectory(dir));
        Files.write(dir.resolve("anyfile.bin"), new byte[0]);
        Assertions.assertThrows(
            FileCleanupException.class,
            () -> new AutoCloseablePath(dir).close()
        );
    }

    @Test
    public void testParentResolveChild() {
        final Path parent = this.temp.resolve("parent");
        Assertions.assertTrue(
            new AutoCloseablePath.Parent(parent)
                .resolve("file.txt")
                .unwrap()
                .startsWith(parent)
        );
    }
}
