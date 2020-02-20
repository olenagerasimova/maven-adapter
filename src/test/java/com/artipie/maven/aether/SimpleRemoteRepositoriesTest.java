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

import com.artipie.maven.FileCoordinates;
import java.util.List;
import org.eclipse.aether.repository.RemoteRepository;
import org.hamcrest.Matcher;
import org.hamcrest.MatcherAssert;
import org.hamcrest.beans.HasPropertyWithValue;
import org.hamcrest.collection.IsIterableContainingInAnyOrder;
import org.hamcrest.core.IsEqual;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link SimpleRemoteRepositories}.
 * @since 0.1
 */
public final class SimpleRemoteRepositoriesTest {

    @Test
    public void shouldReturnOnUploading() {
        MatcherAssert.assertThat(
            "should return value on uploading",
            new SimpleRemoteRepositories()
                .uploading(
                    new FileCoordinates("example/artifact/1.0/artifact-1.0.jar")
                ),
            this.hasAstoProtocol()
        );
    }

    @Test
    public void shouldReturnOnDownloading() {
        MatcherAssert.assertThat(
            "should return value on downloading",
            new SimpleRemoteRepositories().downloading(
                new FileCoordinates("example/artifact/2.0/artifact-2.0.jar")
            ),
            new IsIterableContainingInAnyOrder<>(
                List.of(this.hasAstoProtocol())
            )
        );
    }

    private Matcher<RemoteRepository> hasAstoProtocol() {
        return new HasPropertyWithValue<>(
            "protocol",
            new IsEqual<>("asto")
        );
    }
}
