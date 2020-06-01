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

import com.artipie.asto.Content;
import com.artipie.asto.Key;
import com.artipie.asto.Storage;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Comparator;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import org.xembly.Directive;
import org.xembly.Directives;
import org.xembly.Xembler;

/**
 * Maven metadata generator.
 * @since 0.3
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
final class MavenMetadata {

    /**
     * Current Xembler state.
     */
    private final Directives dirs;

    /**
     * Ctor.
     * @param source Source xembler directives
     */
    MavenMetadata(final Iterable<Directive> source) {
        this.dirs = new Directives(source);
    }

    /**
     * Update versions.
     * @param items Version names
     * @return Updated metadata
     */
    public MavenMetadata versions(final Set<String> items) {
        final Directives copy = new Directives(this.dirs);
        copy.xpath("/metadata")
            .push().xpath("versioning").remove().pop()
            .xpath("/metadata")
            .add("versioning");
        items.stream().max(Comparator.naturalOrder())
            .ifPresent(latest -> copy.add("latest").set(latest).up());
        items.stream().filter(version -> !version.endsWith("SNAPSHOT"))
            .max(Comparator.naturalOrder())
            .ifPresent(latest -> copy.add("release").set(latest).up());
        copy.add("versions");
        items.forEach(version -> copy.add("version").set(version).up());
        copy.up();
        copy.addIf("lastUpdated").set(Instant.now().toEpochMilli()).up();
        copy.up();
        return new MavenMetadata(copy);
    }

    /**
     * Save metadata to storage.
     * @param storage Storage to save
     * @param base Base key where to save
     * @return Async state
     */
    public CompletionStage<Void> save(final Storage storage, final Key base) {
        return CompletableFuture.supplyAsync(
            () -> new Xembler(this.dirs).xmlQuietly().getBytes(StandardCharsets.UTF_8)
        ).thenCompose(
            data -> CompletableFuture.allOf(
                storage.save(
                    new Key.From(base, "maven-metadata.xml"),
                    new Content.From(data)
                )
            )
        );
    }
}
