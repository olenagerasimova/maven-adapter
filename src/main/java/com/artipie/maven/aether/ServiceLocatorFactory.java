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

import com.artipie.asto.Storage;
import com.artipie.asto.blocking.BlockingStorage;
import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.spi.connector.transport.TransporterFactory;
import org.eclipse.aether.spi.locator.ServiceLocator;

/**
 * Creates {@link ServiceLocator} instances, as it is not thread-safe by design.
 * Basically a wrapper over {@link MavenRepositorySystemUtils}.
 * Also it helps wiring services with different lifecycles -
 * it propagates natural singletons inside one-shot instances.
 * @since 0.1
 */
public final class ServiceLocatorFactory {

    /**
     * Asto.
     */
    private final Storage asto;

    /**
     * All args constructor.
     * @param asto Asto
     */
    public ServiceLocatorFactory(final Storage asto) {
        this.asto = asto;
    }

    /**
     * Creates {@link ServiceLocator} instance.
     * @return A ServiceLocator instance
     * @todo #10:30min Inject a LocalRepository into a ServiceLocator.
     *  Current design made ServiceLocatorFactory and LocalRepository coupled together.
     *  We can retrieve a LocalRepository directly from the ServiceLocator
     */
    public ValidatingServiceLocator serviceLocator() {
        return new ValidatingServiceLocator(
            MavenRepositorySystemUtils.newServiceLocator()
                .setService(
                    RepositoryConnectorFactory.class,
                    BasicRepositoryConnectorFactory.class
                )
                .setServices(
                    TransporterFactory.class,
                    new AstoTransporterFactory(new BlockingStorage(this.asto))
                )
        );
    }
}
