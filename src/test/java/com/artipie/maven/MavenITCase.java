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
import com.artipie.asto.fs.FileStorage;
import com.jcabi.matchers.XhtmlMatchers;
import com.jcabi.xml.XMLDocument;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.cactoos.io.InputOf;
import org.cactoos.io.OutputTo;
import org.cactoos.io.TeeInput;
import org.cactoos.list.ListOf;
import org.cactoos.list.Mapped;
import org.cactoos.scalar.LengthOf;
import org.cactoos.text.Split;
import org.cactoos.text.TextOf;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.AllOf;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Test case for {@link Maven}.
 *
 * @since 0.3
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public final class MavenITCase {

    /**
     * Temporary directory with repository data.
     * @checkstyle VisibilityModifierCheck (5 lines)
     */
    @TempDir
    Path repo;

    @BeforeEach
    void setUp() throws Exception {
        final Path asto = this.repo.resolve("com").resolve("artipie").resolve("asto");
        final TestResource res = new TestResource(
            Thread.currentThread().getContextClassLoader(), "./com/artipie/asto"
        );
        final List<TestResource> versions = res.list().stream()
            .filter(item -> !item.toString().endsWith("maven-metadata.xml"))
            .collect(Collectors.toList());
        for (final TestResource version : versions) {
            final Path verpath = asto.resolve(version.name());
            verpath.toFile().mkdirs();
            final Collection<TestResource> files = version.list();
            for (final TestResource file : files) {
                file.copy(verpath.resolve(file.name()));
            }
        }
        final TestResource meta = res.resolve("maven-metadata.xml");
        meta.copy(asto.resolve(meta.name()));
    }

    @Test
    void generatesMetadata() throws Exception {
        final FileStorage storage = new FileStorage(this.repo);
        new Maven(storage)
            .update(new Key.From("com", "artipie", "asto"))
            .toCompletableFuture()
            .get();
        MatcherAssert.assertThat(
            new XMLDocument(
                new String(
                    Files.readAllBytes(
                        this.repo.resolve("com").resolve("artipie").resolve("asto")
                            .resolve("maven-metadata.xml")
                    ),
                    StandardCharsets.UTF_8
                )
            ),
            new AllOf<>(
                new ListOf<>(
                    // @checkstyle LineLengthCheck (20 lines)
                    XhtmlMatchers.hasXPath("/metadata/groupId[text() = 'com.artipie']"),
                    XhtmlMatchers.hasXPath("/metadata/artifactId[text() = 'asto']"),
                    XhtmlMatchers.hasXPath("/metadata/versioning/latest[text() = '1.0-SNAPSHOT']"),
                    XhtmlMatchers.hasXPath("/metadata/versioning/release[text() = '0.20.2']"),
                    XhtmlMatchers.hasXPath("/metadata/versioning/versions/version[text() = '0.18']"),
                    XhtmlMatchers.hasXPath("/metadata/versioning/versions/version[text() = '0.15']"),
                    XhtmlMatchers.hasXPath("/metadata/versioning/versions/version[text() = '0.11.1']"),
                    XhtmlMatchers.hasXPath("/metadata/versioning/versions/version[text() = '0.20.1']"),
                    XhtmlMatchers.hasXPath("/metadata/versioning/versions/version[text() = '0.20.2']"),
                    XhtmlMatchers.hasXPath("/metadata/versioning/versions/version[text() = '1.0-SNAPSHOT']"),
                    XhtmlMatchers.hasXPath("/metadata/versioning/versions/version[text() = '0.18']"),
                    XhtmlMatchers.hasXPath("/metadata/versioning/lastUpdated")
                )
            )
        );
    }

    /**
     * Test resource helper.
     * @since 0.4
     */
    static final class TestResource {

        /**
         * Class loader.
         */
        private final ClassLoader clo;

        /**
         * Resource location.
         */
        private final String location;

        /**
         * Ctor.
         * @param clo Class loader
         */
        TestResource(final ClassLoader clo) {
            this(clo, "");
        }

        /**
         * Ctor.
         * @param clo Class loader
         * @param location Resource location
         */
        TestResource(final ClassLoader clo, final String location) {
            this.clo = clo;
            this.location = location;
        }

        /**
         * Resolve child.
         * @param name Child name
         * @return Resource
         */
        public TestResource resolve(final String name) {
            return new TestResource(this.clo, String.join("/", this.location, name));
        }

        /**
         * List resources.
         * @return Collection of child resources
         * @throws IOException On error
         */
        public Collection<TestResource> list() throws IOException {
            try (InputStream src = this.clo.getResourceAsStream(this.location)) {
                return new Mapped<>(
                    name -> new TestResource(
                        this.clo,
                        String.join("/", this.location, name.asString())
                    ),
                    new Split(new TextOf(new InputOf(src)), new TextOf("\n"))
                );
            }
        }

        /**
         * Copy resource to output path.
         * @param out Output
         * @throws IOException On error
         */
        public void copy(final Path out) throws IOException {
            // @checkstyle LineLengthCheck (10 lines)
            try (
                InputStream src = new BufferedInputStream(this.clo.getResourceAsStream(this.location));
                OutputStream ous = new BufferedOutputStream(Files.newOutputStream(out, StandardOpenOption.WRITE, StandardOpenOption.CREATE))
            ) {
                new LengthOf(new TeeInput(new InputOf(src), new OutputTo(ous))).intValue();
            }
        }

        /**
         * Resource name.
         * @return Name
         */
        public String name() {
            return Paths.get(this.location).getFileName().toString();
        }

        @Override
        public String toString() {
            return this.location;
        }
    }
}
