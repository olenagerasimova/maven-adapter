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
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.cactoos.list.ListOf;

/**
 * Maven artifact metadata xml.
 * @since 0.5
 */
public final class MetadataXml {

    /**
     * Group id.
     */
    private final String group;

    /**
     * Artifact id.
     */
    private final String artifact;

    /**
     * Ctor.
     * @param group Group id
     * @param artifact Artifact id
     */
    public MetadataXml(final String group, final String artifact) {
        this.group = group;
        this.artifact = artifact;
    }

    /**
     * Adds xml to storage.
     * @param storage Where to add
     * @param key Key to save xml by
     * @param versions Version to generage xml
     */
    public void addXmlToStorage(final Storage storage, final Key key, final VersionTags versions) {
        storage.save(key, new Content.From(this.get(versions).getBytes(StandardCharsets.UTF_8)))
            .join();
    }

    /**
     * Get xml as string.
     * @param versions Versions info
     * @return Maven metadata xml
     */
    public String get(final VersionTags versions) {
        return String.join(
            "\n",
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>",
            "<metadata>",
            String.format("  <groupId>%s</groupId>", this.group),
            String.format("  <artifactId>%s</artifactId>", this.artifact),
            "  <versioning>",
            versions.latest.map(val -> String.format("    <latest>%s</latest>", val)).orElse(""),
            versions.release.map(val -> String.format("    <release>%s</release>", val)).orElse(""),
            "    <versions>",
            versions.list.stream().map(val -> String.format("      <version>%s</version>", val))
                .collect(Collectors.joining("\n")),
            "    </versions>",
            "    <lastUpdated>20200804141716</lastUpdated>",
            "  </versioning>",
            "</metadata>"
        );
    }

    /**
     * Maven metadata tags with versions: latest, release, versions list.
     * @since 0.5
     */
    public static final class VersionTags {

        /**
         * Latest version.
         */
        private final Optional<String> latest;

        /**
         * Release version.
         */
        private final Optional<String> release;

        /**
         * Versions list.
         */
        private final List<String> list;

        /**
         * Ctor.
         * @param latest Latest version
         * @param release Release version
         * @param list Versions list
         */
        public VersionTags(final Optional<String> latest, final Optional<String> release,
            final List<String> list) {
            this.latest = latest;
            this.release = release;
            this.list = list;
        }

        /**
         * Ctor.
         * @param latest Latest version
         * @param release Release version
         * @param list Versions list
         */
        public VersionTags(final String latest, final String release, final List<String> list) {
            this(Optional.of(latest), Optional.of(release), list);
        }

        /**
         * Ctor.
         * @param list Versions list
         */
        public VersionTags(final String... list) {
            this(Optional.empty(), Optional.empty(), new ListOf<>(list));
        }
    }
}
