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

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import java.util.Optional;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Encapsulates checksum file logic.
 * @since 0.1
 */
public final class ChecksumAttribute {

    /**
     * Class logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(ChecksumAttribute.class);

    /**
     * File to calculate checksums for.
     */
    private final Path path;

    /**
     * All args constructor.
     * @param path A file
     */
    public ChecksumAttribute(final Path path) {
        this.path = path;
    }

    /**
     * Simply appends given algorithm name to the path.
     * @param type Checksum algorithm
     * @return Checksum file path
     */
    public Path resolveName(final ChecksumType type) {
        return this.path.resolveSibling(
            String.format(
                "%s.%s",
                this.path.getFileName(),
                type.name().toLowerCase(Locale.getDefault())
            )
        );
    }

    /**
     * Reads a checksum for a path.
     * @param type Checksum algorithm
     * @return HEX-encoded checksum
     * @throws NoSuchAlgorithmException Invalid {@link ChecksumType}
     */
    public String readHex(final ChecksumType type) throws NoSuchAlgorithmException {
        final var digest = MessageDigest.getInstance(type.algorithm());
        return this.readAttribute(type)
            .orElseGet(() -> this.calcHex(digest));
    }

    /**
     * Tries to read a checksum attribute file.
     * @param type Checksum file
     * @return Present hex-encoded checksum if attribute exists.
     */
    private Optional<String> readAttribute(final ChecksumType type) {
        return Optional.of(this.resolveName(type))
            .filter(
                file -> {
                    final var exists = Files.exists(file);
                    if (!exists) {
                        LOG.warn("checksum file does not exist {}", file);
                    }
                    return exists;
                }
            ).map(
                file -> {
                    try {
                        return Files.readString(file);
                    } catch (final IOException ex) {
                        throw new UncheckedIOException(ex);
                    }
                }
            );
    }

    /**
     * Calculates a checksum for a path.
     * @param digest MessageDigest
     * @return HEX-encoded checksum
     */
    private String calcHex(final MessageDigest digest) {
        try (var read = Files.newInputStream(this.path)) {
            return Hex.encodeHexString(
                DigestUtils.digest(digest, read)
            );
        } catch (final IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
}
