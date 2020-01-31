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
package com.artipie.maven.test;

import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

/**
 * Test class for {@link OptionalAssertions}.
 *
 * @since 0.1
 */
public class OptionalAssertionsTest {

    @Test
    public void testEmptyPositive() {
        Assertions.assertDoesNotThrow(
            () -> OptionalAssertions.empty(Optional.empty())
        );
    }

    @Test
    public void testEmptyNegative() {
        Assertions.assertThrows(
            AssertionFailedError.class,
            () -> OptionalAssertions.empty(Optional.of(""))
        );
    }

    @Test
    public void testPresentPositive() {
        Assertions.assertDoesNotThrow(
            () -> OptionalAssertions.present(Optional.of("present"))
        );
    }

    @Test
    public void testPresentNegative() {
        Assertions.assertThrows(
            AssertionFailedError.class,
            () -> OptionalAssertions.present(Optional.empty())
        );
    }

    @Test
    public void testPresentValuePositive() {
        Assertions.assertDoesNotThrow(
            () -> OptionalAssertions.present(
                Optional.of("a"),
                value -> Assertions.assertEquals("a", value)
            )
        );
    }

    @Test
    public void testPresentValueNegative() {
        Assertions.assertThrows(
            AssertionFailedError.class,
            () -> OptionalAssertions.present(
                Optional.of("a"),
                value -> Assertions.assertEquals("b", value)
            )
        );
    }
}
