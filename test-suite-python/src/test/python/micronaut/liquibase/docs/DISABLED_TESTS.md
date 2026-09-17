# Python Docs Disabled Test Inventory

This file tracks the Python documentation examples of Micronaut Liquibase under `test-suite-python/src/test/python/micronaut/liquibase/docs`
that are disabled, or that carry a workaround because the direct port of the Java example does not compile or does not behave
like the Java example yet (Python compiler gaps). It is the bug-fixing task list for the Python compiler
(`micronaut-inject-python` / `micronaut-context-python`); every row references a `TODO(python)` comment in the sources.

The Python examples are compiled by every build and their tests run with `./gradlew pythonCheck -Ppython-ci`
(the "Python CI" GitHub workflow).

## Reconciliation

- Last generated active `@Disabled` count: 0.
- Last generated command: `rg -n "@Disabled\(" test-suite-python/src/test/python`.
- Last full-suite command: `./gradlew :test-suite-python:test -Ppython-ci`.
- Last full-suite result: build successful, 1 test executed (1 test class), 0 skipped.

## Migration Rules

- The snippet classes live in `io.micronaut.liquibase.docs` in every language: a Python source package cannot be the imported
  Java package `micronaut.liquibase` itself.
- The `Application` entry point is a class with a `@staticmethod main(args: list[str])` calling `Micronaut.run(Application, args)`,
  as in the core guide; `SLF4JBridgeHandler` is imported from `org.slf4j.bridge` like any other Java class.
- A Python test class is a `@MicronautTest` with `@Test` methods and plain `assert` statements; `ApplicationTest` asserts the
  JUL bridge was installed by `main` and that the Liquibase change sets of `application.yml` ran (`DATABASECHANGELOG` queried
  through `java.sql.DriverManager` on the H2 in-memory database).

## Active `@Disabled` Tests

None.

## Workarounds in the Sources

None.

## `java.type` usages

None.
