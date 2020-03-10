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

package com.artipie.maven.repository;

import com.artipie.asto.Storage;
import com.artipie.maven.artifact.Artifact;

/**
 * General abstraction over Maven (remote) repository.
 *
 * @since 0.2
 * @todo #54:30min Implement Repository interface with Aether.
 *  Implement repository interface which will read artifacts from a maven
 *  repository using Aether repository libs. See
 *  com.artipie.maven.aether.repository.AetherRepository. Don't forget the
 *  tests. After implementing the class, remove AetherRepository and related
 *  classes and tests.
 */
public interface Repository extends Iterable<Artifact> {

    /**
     * Repository abstract storage.
     * @return Repository abstract storage.
     */
    Storage storage();

    /**
     * Repository id.
     * @return Repository id.
     */
    String repoId();

    /**
     * Repository url.
     * @return Repository url.
     */
    String url();
}
