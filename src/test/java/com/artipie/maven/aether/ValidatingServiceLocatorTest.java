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

import com.artipie.maven.test.OptionalAssertions;
import java.util.List;
import org.eclipse.aether.spi.locator.ServiceLocator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link ValidatingServiceLocator}.
 * @since 0.1
 */
public final class ValidatingServiceLocatorTest {

    /**
     * Always returns null.
     */
    private static final ServiceLocator NULL = new ServiceLocator() {
        @Override
        public <T> T getService(final Class<T> type) {
            return null;
        }

        @Override
        public <T> List<T> getServices(final Class<T> type) {
            return null;
        }
    };

    /**
     * Instantiates a service by its default constructor.
     */
    private static final ServiceLocator NEW_INSTANCE = new ServiceLocator() {
        @Override
        public <T> T getService(final Class<T> type) {
            try {
                return type.getDeclaredConstructor().newInstance();
            } catch (final ReflectiveOperationException | SecurityException ex) {
                throw new ServiceLocatorException(type, ex);
            }
        }

        @Override
        public <T> List<T> getServices(final Class<T> type) {
            return null;
        }
    };

    @Test
    public void shouldReturnEmptyOnNull() {
        OptionalAssertions.empty(
            new ValidatingServiceLocator(ValidatingServiceLocatorTest.NULL)
                .getServiceOpt(ValidatingServiceLocatorTest.class)
        );
    }

    @Test
    public void shouldReturnPresent() {
        OptionalAssertions.present(
            new ValidatingServiceLocator(ValidatingServiceLocatorTest.NEW_INSTANCE)
                .getServiceOpt(ValidatingServiceLocatorTest.class)
        );
    }

    @Test
    public void shouldThrowOnNull() {
        Assertions.assertThrows(
            ServiceLocatorException.class,
            () -> new ValidatingServiceLocator(ValidatingServiceLocatorTest.NULL)
                .getService(ValidatingServiceLocatorTest.class));
    }
}
