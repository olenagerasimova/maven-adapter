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

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.AllOf;
import org.hamcrest.core.IsEqual;
import org.hamcrest.core.StringEndsWith;
import org.hamcrest.core.StringStartsWith;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

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
     * Test temporary directory.
     * By JUnit annotation contract it should not be private
     * @checkstyle VisibilityModifierCheck (3 lines)
     */
    @TempDir
    Path temp;

    @ParameterizedTest
    @EnumSource(ChecksumType.class)
    public void shouldResolveName(final ChecksumType type) throws Exception {
        final var path = this.randomFile();
        MatcherAssert.assertThat(
            "ChecksumAttribute should resolve attribute path",
            new ChecksumAttribute(path)
                .resolveName(type)
                .toString(),
            new AllOf<>(
                List.of(
                    new StringStartsWith(false, path.toString()),
                    new StringEndsWith(true, type.toString())
                )
            )
        );
    }

    @ParameterizedTest
    @EnumSource(ChecksumType.class)
    public void shouldCalcChecksum(final ChecksumType type) throws Exception {
        final var path = this.randomFile();
        MatcherAssert.assertThat(
            "checksums should match",
            new ChecksumAttribute(path).readHex(type),
            new IsEqual<>(
                Hex.encodeHexString(
                    DigestUtils.digest(
                        MessageDigest.getInstance(type.algorithm()), path.toFile()
                    )
                )
            )
        );
    }

    @ParameterizedTest
    @EnumSource(ChecksumType.class)
    public void shouldReadChecksumFromFile(final ChecksumType type) throws Exception {
        final var path = this.randomFile();
        final var checksum = new ChecksumAttribute(path);
        final var line = this.randomString();
        Files.writeString(checksum.resolveName(type), line);
        MatcherAssert.assertThat(
            "should read checksum from checksum file",
            checksum.readHex(type),
            new IsEqual<>(line)
        );
    }

    private Path randomFile() throws Exception {
        final var bytes = new byte[ChecksumAttributeTest.ARRAY_LENGTH];
        ThreadLocalRandom.current().nextBytes(bytes);
        return Files.write(
            this.temp.resolve(
                String.format("%s.bin", this.randomString())
            ),
            bytes
        );
    }

    private String randomString() {
        return UUID.randomUUID().toString();
    }
}
