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

import com.artipie.asto.Content;
import com.artipie.asto.Key;
import com.artipie.asto.Storage;
import com.artipie.asto.memory.InMemoryStorage;
import java.nio.charset.StandardCharsets;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.IsEqual;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link FromMetadata}.
 * @since 0.5
 */
class FromMetadataTest {

    @Test
    void readsVersion() {
        final Storage storage = new InMemoryStorage();
        final Key key = new Key.From("com/artipie/example");
        storage.save(
            new Key.From(key, "maven-metadata.xml"),
            new Content.From(
                String.join(
                    "\n",
                    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>",
                    "<metadata modelVersion=\"1.1.0\">",
                    "  <groupId>com.test</groupId>",
                    "  <artifactId>logger</artifactId>",
                    "  <version>1.1</version>",
                    "  <versioning>",
                    "    <latest>1.1</latest>",
                    "    <release>1.1</release>",
                    "    <versions>",
                    "      <version>0.9</version>",
                    "      <version>0.8</version>",
                    "    </versions>",
                    "    <lastUpdated>20200804141715</lastUpdated>",
                    "  </versioning>",
                    "</metadata>"
                ).getBytes(StandardCharsets.UTF_8)
            )
        ).join();
        MatcherAssert.assertThat(
            new FromMetadata(storage).version(key).toCompletableFuture().join(),
            new IsEqual<>("1.1")
        );
    }

}
