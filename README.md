<img src="https://www.artipie.com/logo.svg" width="64px" height="64px"/>

[![Maven Build](https://github.com/artipie/maven-adapter/workflows/Maven%20Build/badge.svg)](https://github.com/artipie/maven-adapter/actions?query=workflow%3A%22Maven+Build%22)
[![License](https://img.shields.io/badge/license-MIT-green.svg)](https://github.com/artipie/maven-adapter/blob/master/LICENSE.txt)
[![Hits-of-Code](https://hitsofcode.com/github/artipie/maven-adapter)](https://hitsofcode.com/view/github/artipie/maven-adapter)
[![PDD status](http://www.0pdd.com/svg?name=artipie/maven-adapter)](http://www.0pdd.com/p?name=artipie/maven-adapter)
![GitHub commit activity](https://img.shields.io/github/commit-activity/m/artipie/maven-adapter?style=plastic)

# maven-adapter
Maven (remote) repository adapter

The basic premise is - adapting official Maven libraries
(`org.apache.maven` and `org.eclipse.aether` packages)
to Artipie API.

## How to contribute

Fork repository, make changes, send us a pull request. We will review
your changes and apply them to the `master` branch shortly, provided
they don't violate our quality standards. To avoid frustration, before
sending us your pull request please run full Maven build:

```
$ mvn clean install -Pqulice
```
To avoid build errors use Maven 3.2+.

## How it works

When the Maven client downloads a dependency, it relies on normal HTTP GET and PUT on well-known files (see below for details).

With respect to this, Artipie responds to HTTP requests for each of the type of files like a normal HTTP server.

When the Maven client uploads a dependency, it relies on the maven-deploy-plugin being configured, a
repository being configure in the `distributionManagement` section of the `settings.xml` and authentication
credentials configured in the `servers` section of the`settings.xml`.

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

## A developer's entrypoint

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

#### Storage lifecycle

Before starting you need to defined _three_ storages:

- __Staging directory__ (`com.artipie.maven.util.AutoCloseablePath`) -
a transient local file storage where uploading artifacts should be placed
right after starting to handle the incoming request.
The analog to your project's `target` or `build` directories.
- __Local repository__ - (`org.eclipse.aether.repository.LocalRepository`) -
yet another intermediate local file storage, like `~/.m2/repository` after `mvn install`.
You may consider to refactor it to omit the staging directory and
use the local repository instead of it, though in my opinion it's not _"Maven way"_.
You have to work with local file system anyway as Maven is strongly tied to file system.
- __Asto__ (`com.artipie.asto.blocking.BlockingStorage`) - final destination like a remote repository
where your `mvn deploy` ends. It is _blocking_ because
it is deeply integrated in Maven framework which is not really reactive-friendly.

Uploading artifacts pass from one storage to another sequentially:
```
>--(receive)--> [Staging] >--(install)--> [LocalRepository] >--(deploy)--> [Asto]
```

## Maven library abstract

See [Maven architectural diagram](https://maven.apache.org/ref/3.6.3/)

Library classes are defined in packages `org.apache.maven` and `org.eclipse.aether`.

Internal services are wired by a dependency injector implementation `Plexus`
or by a _Service Locator_ pattern implementation `org.eclipse.aether.spi.locator.ServiceLocator`.
This `maven-adapter` uses the latter for easier debugging.

See also `org.apache.maven.repository.internal.MavenRepositorySystemUtils`
for convenient static factory methods creating services.

`org.eclipse.aether.RepositorySystem` is a general facade for all operations.
