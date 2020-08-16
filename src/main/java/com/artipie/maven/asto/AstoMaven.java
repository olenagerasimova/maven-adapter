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
package com.artipie.maven.asto;

import com.artipie.asto.Concatenation;
import com.artipie.asto.Key;
import com.artipie.asto.Remaining;
import com.artipie.asto.Storage;
import com.artipie.maven.Maven;
import com.artipie.maven.metadata.ArtifactsMetadata;
import com.artipie.maven.metadata.MavenMetadata;
import com.jcabi.xml.XMLDocument;
import hu.akarnokd.rxjava2.interop.SingleInterop;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import org.xembly.Directives;

/**
 * Maven front for artipie maven adaptor.
 *
 * @since 0.2
 * @todo #91:30min Generate valid checksums on metadata update.
 *  When updating the maven-metadata.xml, then we should find all checksum files in the root of
 *  repository by prefix maven-metadata.xml (it can be done using storage.list), there will be
 *  few checksum files like `maven-metadata.xml.md5`, `maven-metadata.xml.sha1`,
 *  `maven-metadata.xml.sha256`, `maven-metadata.xml.sha512`. We need to update all these files
 *  with checksums of `maven-metadata.xml` data using checksum file extension as digest algorithm.
 *  If we found some unsupported algorithm, then delete checksum file. I'd start with
 *  `MD5`, `SHA-256`, `SHA-1` and `SHA-512`. After this, enable testes in
 *  MavenTest.
 */
public final class AstoMaven implements Maven {

    /**
     * Repository storage.
     */
    private final Storage storage;

    /**
     * Update metadata executor.
     */
    private final Executor exec;

    /**
     * Ctor.
     * @param storage Maven repo storage
     */
    public AstoMaven(final Storage storage) {
        this(storage, Executors.newSingleThreadExecutor());
    }

    /**
     * Constructor.
     * @param storage Storage used by this class.
     * @param exec Executor
     */
    public AstoMaven(final Storage storage, final Executor exec) {
        this.storage = storage;
        this.exec = exec;
    }

    @Override
    public CompletionStage<Void> update(final Key upload, final Key artifact) {
        return this.storage.value(new Key.From(upload, "maven-metadata.xml"))
            .thenComposeAsync(
                pub -> new Concatenation(pub).single().to(SingleInterop.get()), this.exec
            )
            .thenApplyAsync(buf -> new String(new Remaining(buf).bytes(), StandardCharsets.UTF_8))
            .thenApply(XMLDocument::new)
            .thenApply(doc -> new MavenMetadata(Directives.copyOf(doc.node())))
            .thenCompose(
                doc -> this.storage.list(artifact).thenApply(
                    items -> items.stream()
                        .map(
                            item -> item.string()
                                .replaceAll(String.format("%s/", artifact.string()), "")
                                .split("/")[0]
                        )
                        .filter(item -> !item.startsWith("maven-metadata"))
                        .collect(Collectors.toSet())
                ).thenCompose(
                    versions -> new ArtifactsMetadata(this.storage).release(upload)
                        .thenApply(
                            latest -> {
                                versions.add(latest);
                                return doc.versions(versions);
                            }
                    )
                )
            ).thenCompose(doc -> doc.save(this.storage, upload));
    }
}
