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
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.junit.jupiter.api.Assertions;

/**
 * Custom assertions for Optional.
 *
 * @since 0.1
 */
@SuppressWarnings("PMD.ProhibitPublicStaticMethods")
public final class OptionalAssertions {

    /**
     * Static utilities class.
     */
    private OptionalAssertions() {
    }

    /**
     * Asserts if given Optional is empty.
     *
     * @param optional Actual argument
     * @param msg Assertion message supplier
     */
    public static void empty(final Optional<?> optional, final Supplier<String> msg) {
        Assertions.assertTrue(optional.isEmpty(), msg);
    }

    /**
     * Asserts if given Optional is empty.
     *
     * @param optional Test assertion actual argument
     */
    public static void empty(final Optional<?> optional) {
        final Supplier<String> msg = () -> {
            final var present = optional.map(Object::toString)
                .orElseGet(() -> "present");
            return String.format("expected empty, actual %s", present);
        };
        Assertions.assertTrue(optional.isEmpty(), msg);
    }

    /**
     * Asserts if given Optional is empty.
     *
     * @param optional Test assertion actual argument
     * @param assertion Present value assertion
     * @param <T> Present value type
     */
    public static <T> void present(final Optional<T> optional, final Consumer<T> assertion) {
        optional.ifPresentOrElse(
            assertion, () -> Assertions.fail("expected present, actual empty")
        );
    }

    /**
     * Asserts if given Optional is empty.
     *
     * @param optional Actual argument
     * @param assertion Present value assertion
     * @param msg Assertion message supplier
     * @param <T> Present value type
     */
    public static <T> void present(final Optional<T> optional,
        final Consumer<T> assertion, final Supplier<String> msg) {
        optional.ifPresentOrElse(
            assertion, () -> Assertions.fail(msg.get())
        );
    }
}
