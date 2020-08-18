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
package com.artipie.maven.metadata;

import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.Stream;

/**
 * Versions comparator.
 * @since 0.5
 */
public final class Versions implements Comparator<String> {

    @Override
    public int compare(final String first, final String second) {
        return Arrays.compare(
            Versions.stringVersionToIntArray(first), Versions.stringVersionToIntArray(second)
        );
    }

    /**
     * Transforms.
     * @param version Version to clean
     * @return Version without snapshot
     */
    private static Integer[] stringVersionToIntArray(final String version) {
        return Stream.of(version.replace("-SNAPSHOT", "").split("\\."))
            .map(Integer::parseInt).toArray(Integer[]::new);
    }
}
