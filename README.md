<img src="https://www.artipie.com/logo.svg" width="64px" height="64px"/>

[![Build Status](https://img.shields.io/travis/yegor256/maven-files/master.svg)](https://travis-ci.org/yegor256/maven-files)
[![License](https://img.shields.io/badge/license-MIT-green.svg)](https://github.com/com.artipie/maven-adapter/blob/master/LICENSE.txt)
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

It is a WIP project. Read [developer's guide](DOCS.md)
