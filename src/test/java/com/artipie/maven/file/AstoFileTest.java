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
package com.artipie.maven.file;

import com.artipie.asto.Content;
import com.artipie.asto.Key;
import com.artipie.asto.Storage;
import com.artipie.asto.memory.InMemoryStorage;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.IsEqual;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link File.Asto}.
 *
 * @since 0.2
 */
public class AstoFileTest {

    @Test
    @Disabled
    public void readContent() {
        final Storage asto = new InMemoryStorage();
        final Key key = new Key.From("key");
        final String content = "a sample content for file";
        asto.save(
            key,
            new Content.From(content.getBytes())
        );
        final File file = new File.Asto(key, asto);
        MatcherAssert.assertThat(
            file.content(),
            new IsEqual<>(content.getBytes())
        );
    }

    @Test
    @Disabled
    public void readFileName() throws Exception {
        final Storage asto = new InMemoryStorage();
        final Key key = new Key.From("anotherKey");
        asto.save(
            key,
            new Content.From(new byte[0])
        );
        final File file = new File.Asto(key, asto);
        MatcherAssert.assertThat(
            file.name().asString(),
            new IsEqual<>(
                key.string()
            )
        );
    }
}
