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

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

/**
 * Tests for {@link ChecksumType}.
 * @since 0.1
 */
public final class ChecksumTypeTest {

    /**
     * Arbitrary random byte array length.
     */
    private static final int ARRAY_LENGTH = 8192;

    @TestFactory
    public Collection<DynamicTest> checksumTestFactory() throws Exception {
        return Arrays.stream(ChecksumType.values())
            .map(this::createDynamicTest)
            .collect(Collectors.toList());
    }

    private DynamicTest createDynamicTest(final ChecksumType type) {
        return DynamicTest.dynamicTest(
            String.format("test%s", type.algorithm()),
            () -> {
                final var bytes = new byte[ChecksumTypeTest.ARRAY_LENGTH];
                ThreadLocalRandom.current().nextBytes(bytes);
                Assertions.assertArrayEquals(
                    DigestUtils.digest(type.messageDigest(), bytes),
                    type.digest(bytes).byteArray()
                );
            }
        );
    }
}
