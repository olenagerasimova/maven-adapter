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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.output.NullOutputStream;

/**
 * Encapsulates checksum file logic.
 * @since 0.1
 */
public final class ChecksumAttribute {

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
     * Calculates checksums and writes them to corresponding files.
     * @return Map of HEX-encoded checksums per algorithms
     * @throws IOException File reading failed
     * @throws NoSuchAlgorithmException Invalid {@link ChecksumType}
     */
    public Map<ChecksumType, String> write() throws IOException, NoSuchAlgorithmException {
        final Map<ChecksumType, String> map = new EnumMap<>(ChecksumType.class);
        for (final ChecksumType type : ChecksumType.values()) {
            map.put(type, this.write(type));
        }
        return map;
    }

    /**
     * Calculates checksum for a given type and writes it to a corresponding file.
     * @param type Checksum type
     * @return Map of HEX-encoded checksums per algorithms
     * @throws IOException File reading failed
     * @throws NoSuchAlgorithmException Invalid {@link ChecksumType}
     */
    public String write(final ChecksumType type) throws IOException, NoSuchAlgorithmException {
        final var hex = this.readHex(type);
        Files.writeString(
            this.resolveName(type),
            hex,
            StandardOpenOption.TRUNCATE_EXISTING,
            StandardOpenOption.CREATE
        );
        return hex;
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
     * Calculates a checksum for a path.
     * @param type Checksum algorithm
     * @return HEX-encoded checksum
     * @throws IOException File reading failed
     * @throws NoSuchAlgorithmException Invalid {@link ChecksumType}
     */
    public String readHex(final ChecksumType type) throws IOException, NoSuchAlgorithmException {
        try (var read = Files.newInputStream(this.path)) {
            final var digest = MessageDigest.getInstance(type.algorithm());
            read.transferTo(new DigestOutputStream(new NullOutputStream(), digest));
            return Hex.encodeHexString(digest.digest());
        }
    }
}
