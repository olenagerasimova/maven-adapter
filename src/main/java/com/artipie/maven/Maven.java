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

import com.artipie.asto.Key;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * Maven front for artipie maven adaptor.
 * @since 0.5
 */
public interface Maven {

    /**
     * Updates the metadata of a maven package.
     * @param pkg Maven package key
     * @return Completion stage
     */
    CompletionStage<Void> update(Key pkg);

    /**
     * Fake {@link Maven} implementation.
     * @since 0.5
     */
    class Fake implements Maven {

        /**
         * Was maven updated?
         */
        private boolean updated;

        @Override
        public CompletionStage<Void> update(final Key pkg) {
            this.updated = true;
            return CompletableFuture.allOf();
        }

        /**
         * Was maven updated?
         * @return True is was, false - otherwise
         */
        public boolean wasUpdated() {
            return this.updated;
        }
    }
}
