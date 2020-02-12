# maven-adapter
Maven (remote) repository adapter

## General overview

The basic premise is - adapting official Maven libraries
(`org.apache.maven` and `org.eclipse.aether` packages)
to Artipie API.

## Files

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

## Layout

(Default) naming convention is - in groupId replace all dots with directory separator ('/')
then put artifactId, version and then go files.

Example layout (not fully exhausting):
```
$ROOT
|-- org/
    `-- example/
        `-- artifact/
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

`com.artipie.maven.Repository` is central facade for all operations.
The only implementation is `com.artipie.maven.aether.AetherRepository`.

#### `Repository#upload(String path,InputStream content)`
Uploads a given artifact defined by its path.
A client (mvn) usually makes at least two subsequent requests - for a JAR file and for a POM file
and maybe more for classifier files.

`path` - is expected to be a HTTP request URI path.
`content` - an artifact payload.

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
