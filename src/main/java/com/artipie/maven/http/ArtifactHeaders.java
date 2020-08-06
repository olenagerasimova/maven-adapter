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
package com.artipie.maven.http;

import com.artipie.asto.Key;
import com.artipie.http.Headers;
import com.artipie.http.headers.Header;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import org.eclipse.jetty.http.MimeTypes;

/**
 * Artifact response headers for {@code GET} and {@code HEAD} requests.
 * <p>
 * Maven client supports {@code X-Checksum-*} headers for different hash algorithms,
 * {@code ETag} header for caching, {@code Content-Type} and {@code Content-Disposition}.
 * </p>
 * @since 0.5
 */
final class ArtifactHeaders extends Headers.Wrap {

    /**
     * Headers from artifact key and checksums.
     * @param location Artifact location
     * @param checksums Artifact checksums
     */
    ArtifactHeaders(final Key location, final Map<String, String> checksums) {
        super(
            new Headers.From(
                checksumsHeader(checksums),
                contentDisposition(location),
                contentType(location)
            )
        );
    }

    /**
     * Content disposition header.
     * @param location Artifact location
     * @return Headers with content disposition
     */
    private static Header contentDisposition(final Key location) {
        final String string = location.string();
        final String name = string.substring(string.lastIndexOf('/') + 1);
        return new Header(
            "Content-Disposition",
            String.format("attachment; filename=\"%s\"", name)
        );
    }

    /**
     * Checksum headers.
     * @param checksums Artifact checksums
     * @return Checksum header and {@code ETag} header
     */
    private static Headers checksumsHeader(final Map<String, String> checksums) {
        final ArrayList<Map.Entry<String, String>> headers =
            new ArrayList<>(checksums.size() + 1);
        for (final Map.Entry<String, String> entry : checksums.entrySet()) {
            headers.add(
                new Header(String.format("X-Checksum-%s", entry.getKey()), entry.getValue())
            );
        }
        Optional.ofNullable(checksums.get("sha1"))
            .ifPresent(sha -> headers.add(new Header("ETag", sha)));
        return new Headers.From(headers);
    }

    /**
     * Artifact content type header.
     * @param key Artifact key
     * @return Content type header
     */
    private static Header contentType(final Key key) {
        final String type;
        final String src = key.string();
        switch (extension(key)) {
            case "jar":
                type = "application/java-archive";
                break;
            case "pom":
                type = "application/x-maven-pom+xml";
                break;
            default:
                type = MimeTypes.getDefaultMimeByExtension(src);
                break;
        }
        return new Header("Content-Type", type);
    }

    /**
     * Artifact extension.
     * @param key Artifact key
     * @return Lowercased extension without dot char.
     */
    private static String extension(final Key key) {
        final String src = key.string();
        return src.substring(src.lastIndexOf('.') + 1).toLowerCase(Locale.US);
    }
}
