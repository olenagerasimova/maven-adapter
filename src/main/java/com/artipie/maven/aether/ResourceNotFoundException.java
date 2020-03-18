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

import org.eclipse.aether.spi.connector.transport.Transporter;

/**
 * A marker exception indicating that given resource does not exist.
 * Maven relies on exceptions to handle this logic.
 * May be thrown in {@link Transporter#peek(org.eclipse.aether.spi.connector.transport.PeekTask)}
 * And be classified in {@link Transporter#classify(java.lang.Throwable)}
 * as {@link Transporter#ERROR_NOT_FOUND}
 * @since 0.1
 * @deprecated Outdated due architectural changes in 0.2
 */
@Deprecated
public class ResourceNotFoundException extends RuntimeException {

    /**
     * Super constructor.
     * @param message Detail message
     */
    public ResourceNotFoundException(final String message) {
        super(message);
    }

    /**
     * Super constructor.
     * @param message Detail message
     * @param cause Exception cause
     */
    public ResourceNotFoundException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
