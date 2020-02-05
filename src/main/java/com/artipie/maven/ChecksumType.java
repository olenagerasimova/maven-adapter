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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Locale;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;

/**
 * Well known checksum algorithms.
 * @since 0.1
 */
public enum ChecksumType {
    /**
     * MD5 algorithm.
     */
    MD5,

    /**
     * SHA1 algorithm.
     */
    SHA1;

    /**
     * File extension NOT starting with dot.
     * @return File extension
     */
    public String extension() {
        return this.name().toLowerCase(Locale.getDefault());
    }

    /**
     * {@link java.security.MessageDigest} algorithm.
     * @return Checksum algorithm
     */
    public String algorithm() {
        return this.name();
    }

    /**
     * {@link MessageDigest} instance.
     * @return MessageDigest instance.
     */
    public MessageDigest messageDigest() {
        try {
            return MessageDigest.getInstance(this.algorithm());
        } catch (final NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Checksums given byte array.
     * @param bytes Payload
     * @return Bytes digest
     */
    public Digest digest(final byte[] bytes) {
        return this.digest(new ByteArrayInputStream(bytes));
    }

    /**
     * Checksums given string with default charset.
     * @param str Payload
     * @return String digest.
     */
    public Digest digest(final String str) {
        return this.digest(str.getBytes());
    }

    /**
     * InputStream digest.
     * @param payload Payload
     * @return InputStream digest
     */
    public Digest digest(final InputStream payload) {
        try {
            return new Digest(DigestUtils.digest(this.messageDigest(), payload));
        } catch (final IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    /**
     * File digest.
     * @param path Payload file
     * @param options File open options
     * @return File digest
     */
    public Digest digest(final Path path, final OpenOption... options) {
        try (var is = Files.newInputStream(path, options)) {
            return this.digest(is);
        } catch (final IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    /**
     * Digest result.
     * @since 0.1
     */
    public static final class Digest {

        /**
         * MessageDigest bytes.
         */
        private final byte[] bytes;

        /**
         * All args constructor.
         * @param bytes MessageDigest bytes
         */
        private Digest(final byte[] bytes) {
            this.bytes = Arrays.copyOf(bytes, bytes.length);
        }

        /**
         * HEX-encoded digest.
         * @return HEX-encoded digest
         */
        public String hex() {
            return Hex.encodeHexString(this.bytes);
        }

        /**
         * Defensive copy of the digest bytes.
         * @return Digest byte array
         */
        public byte[] byteArray() {
            return Arrays.copyOf(this.bytes, this.bytes.length);
        }
    }
}
