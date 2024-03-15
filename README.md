# Protocol Parser

Parses protocol docs into a clean Markdown format version.

# Dependencies

[digraph-parser](https://github.com/CloudburstMC/digraph-parser) - A library for parsing Graphviz DOT files

# Run

```bash
java -jar ProtocolParser.jar -i="directory/to/docs" -o="directory/to/markdown"
```

# Building

Requires Gradle

```
./gradlew shadowJar
```