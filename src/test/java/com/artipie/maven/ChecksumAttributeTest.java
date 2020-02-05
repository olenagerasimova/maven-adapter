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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.Comparator;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.slf4j.LoggerFactory;

/**
 * Tests for {@link ChecksumAttribute}.
 * @since 0.1
 */
public final class ChecksumAttributeTest {

    /**
     * Random byte array length.
     */
    private static final int ARRAY_LENGTH = 8192;

    /**
     * Temporary directory.
     */
    private Path temp;

    @ParameterizedTest
    @EnumSource(ChecksumType.class)
    public void testResolveName(final ChecksumType type) throws Exception {
        final var path = this.random();
        Assertions.assertEquals(
            path.resolveSibling(
                String.join(
                    ".",
                    path.getFileName().toString(),
                    type.name().toLowerCase(Locale.getDefault())
                )
            ),
            new ChecksumAttribute(path)
                .resolveName(type)
        );
    }

    @ParameterizedTest
    @EnumSource(ChecksumType.class)
    public void testReadHex(final ChecksumType type) throws Exception {
        final var path = this.random();
        Assertions.assertEquals(
            Hex.encodeHexString(
                DigestUtils.digest(
                    MessageDigest.getInstance(type.algorithm()),
                    Files.newInputStream(path)
                )
            ),
            new ChecksumAttribute(path)
                .readHex(type)
        );
    }

    @BeforeEach
    public void createTemp() throws Exception {
        this.temp = Files.createTempDirectory("junit");
        Files.createDirectories(this.temp);
    }

    @AfterEach
    public void cleanup() throws Exception {
        Files.walk(this.temp).sorted(
            Comparator.<Path, Boolean>comparing(Files::isDirectory, Boolean::compare)
                .thenComparing(Comparator.comparingInt(Path::getNameCount).reversed())
        ).forEachOrdered(
            path -> {
                try {
                    LoggerFactory.getLogger(ChecksumAttributeTest.class).debug("deleting {}", path);
                    Files.deleteIfExists(path);
                } catch (final IOException ex) {
                    LoggerFactory.getLogger(ChecksumAttributeTest.class).warn("on cleanup", ex);
                }
            }
        );
    }

    private Path random() throws Exception {
        final var bytes = new byte[ChecksumAttributeTest.ARRAY_LENGTH];
        ThreadLocalRandom.current().nextBytes(bytes);
        return Files.write(
            this.temp.resolve(
                String.format("%s.bin", UUID.randomUUID().toString())
            ),
            bytes
        );
    }
}
