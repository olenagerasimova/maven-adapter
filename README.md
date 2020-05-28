<img src="https://www.artipie.com/logo.svg" width="64px" height="64px"/>

[![Maven Build](https://github.com/artipie/maven-adapter/workflows/Maven%20Build/badge.svg)](https://github.com/artipie/maven-adapter/actions?query=workflow%3A%22Maven+Build%22)
[![License](https://img.shields.io/badge/license-MIT-green.svg)](https://github.com/artipie/maven-adapter/blob/master/LICENSE.txt)
[![Hits-of-Code](https://hitsofcode.com/github/artipie/maven-adapter)](https://hitsofcode.com/view/github/artipie/maven-adapter)
[![PDD status](http://www.0pdd.com/svg?name=artipie/maven-adapter)](http://www.0pdd.com/p?name=artipie/maven-adapter)
![GitHub commit activity](https://img.shields.io/github/commit-activity/m/artipie/maven-adapter?style=plastic)

# maven-adapter
Maven repository adapter

`com.artipie.maven.Maven` is the central entrypoint for all operations. It uses a
`com.artipie.asto.Storage` to store maven artifacts.

Current implementation is focused on generating metadata for artifacts on repository.

Taking the repository described above in Layout section, let's suppose that a new version for the artifact
was uploaded to the repository (`2.0` for example). We generate the metadata this way: 

```java
    Metadata updated = new Maven(
        storage
    ).update("org.example.artifact");
```

It will generate the `metadata.xml` with the recently added `2.0` version info.

## Maven concepts

### Files

A dependency is identified by its _coordinates_ - groupId, artifactId and version.
JAR files may contain a classifier suffix - `sources` or `javadoc`.
By coordinates you can determine the artifact path in local or remote repository and vice versa.

Repositories must handle following types of files:
- A primary artifact - a main JAR file.
- Secondary artifacts - a POM file, files with a classifier.
- Attribute files - checksums, signatures, lastUpdate files for primary and secondary artifacts.
- Metadata files - `maven-metadata.xml`, containing information about artifact versions
including snapshot versions.

File naming convention is:
`artifactId-version[-classifier]-extension`

### Layout

(Default) naming convention is - in groupId replace all dots with directory separator ('/')
then put artifactId, version and then go files.

Example layout (not fully exhaustive):
```
$ROOT
|-- org/
    `-- example/
        `-- artifact/
            `-- maven-metadata.xml
            `-- maven-metadata.xml.sha1
            `-- 1.0/
                |-- artifact-1.0.jar
                |-- artifact-1.0.jar.sha1
                |-- artifact-1.0.pom
                |-- artifact-1.0.pom.sha1
                |-- artifact-1.0-sources.jar
                |-- artifact-1.0-sources.jar.sha1
```

For example, for an artifact `org.example:artifact:1.0` (Gradle-style notation is used for clarity)
the path would be `org/example/artifact/1.0/artifact-1.0.jar` (and other files).

## How to contribute

Fork repository, make changes, send us a pull request. We will review
your changes and apply them to the `master` branch shortly, provided
they don't violate our quality standards. To avoid frustration, before
sending us your pull request please run full Maven build:

```
$ mvn clean install -Pqulice
```
To avoid build errors use Maven 3.2+.