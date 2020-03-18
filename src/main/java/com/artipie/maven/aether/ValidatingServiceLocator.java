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

import java.util.List;
import java.util.Optional;
import org.eclipse.aether.spi.locator.ServiceLocator;

/**
 * A decorator over {@link ServiceLocator}.
 * Overrides {@link ServiceLocator#getService(Class)} contract to return null
 * if the service cannot be located or initialized.
 * It throws {@link ServiceLocatorException} instead.
 * Provides a new method wrapping nullable value with {@link Optional}.
 * @since 0.1
 * @deprecated Outdated due architectural changes in 0.2
 */
@Deprecated
public final class ValidatingServiceLocator implements ServiceLocator {

    /**
     * Actual ServiceLocator.
     */
    private final ServiceLocator locator;

    /**
     * All args constructor.
     * @param locator ServiceLocator instance
     */
    public ValidatingServiceLocator(final ServiceLocator locator) {
        this.locator = locator;
    }

    /**
     * Returns the original ServiceLocator instance.
     * @return ServiceLocator instance
     */
    public ServiceLocator unwrap() {
        return this.locator;
    }

    /**
     * Wraps the original nullable return value with {@link Optional}.
     * @param type Service class
     * @param <T> Service
     * @return Present if the service can be located and initialized
     */
    public <T> Optional<T> getServiceOpt(final Class<T> type) {
        return Optional.ofNullable(this.locator.getService(type));
    }

    @Override
    public <T> T getService(final Class<T> type) {
        return this.getServiceOpt(type)
            .orElseThrow(() -> new ServiceLocatorException(type));
    }

    @Override
    public <T> List<T> getServices(final Class<T> type) {
        return this.locator.getServices(type);
    }
}
