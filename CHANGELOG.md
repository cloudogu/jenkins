# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [v2.222.1-1] - 2020-04-21
### Added
- Added scm-manager plugin
- Volume for /tmp directory. This volume is not included in backups!

### Changed
- Upgrade to Jenkins 2.222.1; #52
- Upgrade java base image to 8u242-1

## [v2.190.3-3] - 2020-02-26
### Added
- config key additional.plugins which may contain a comma separated list with plugin names that are installed on startup

## [v2.190.3-2] - 2020-01-24
### Changed
* Apply updateSite changes on every start (#45)

## [v2.190.3-1] - 2019-12-10
### Changed
- Changed Jenkins version to 2.190.3
- Changed Java version to 8u222-1
