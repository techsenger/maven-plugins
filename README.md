# Techsenger Maven Plugins

A collection of useful Maven plugins.

## Table of Contents
* [Usage](#usage)
    * [bom-properties](#usage-bom-properties)
* [Requirements](#requirements)
* [Code building](#code-building)
* [License](#license)
* [Contributing](#contributing)
* [Support Us](#support-us)

## Usage <a name="usage"></a>

### bom-properties-maven-plugin <a name="usage-bom-properties"></a>

The BOM Properties Maven Plugin copies properties from Bill of Materials (BOM) files to your project's properties with
configurable prefix and filtering.

Configuration Parameters:

| Parameter | Required | Default | Description |
|-----------|----------|---------|-------------|
| `groupId` | Yes | - | BOM artifact group ID |
| `artifactId` | Yes | - | BOM artifact ID |
| `version` | Yes | - | BOM version |
| `prefix` | Yes | - | Prefix for copied properties (e.g., `bom.property.name`) |
| `includes` | Yes | - | List of wildcard patterns for properties to include |
| `excludes` | No | - | List of wildcard patterns for properties to exclude |
| `caseSensitive` | No | `true` | Whether pattern matching is case-sensitive |

Example:

```xml
<plugin>
    <groupId>com.techsenger.maven.plugins</groupId>
    <artifactId>bom-properties-maven-plugin</artifactId>
    <version>${version}</version>
    <executions>
        <execution>
            <id>copy-bom-properties</id>
            <phase>initialize</phase>
            <goals>
                <goal>copy-properties</goal>
            </goals>
        </execution>
    </executions>
    <configuration>
        <boms>
            <bom>
                <groupId>...</groupId>
                <artifactId>...</artifactId>
                <version>...</version>
                <prefix>bom</prefix>
                <caseSensitive>false</caseSensitive>
                <includes>
                    <include>*.version</include>
                </includes>
                <excludes>
                    <exclude>slf?j.version</exclude>
                </excludes>
            </bom>
        </boms>
    </configuration>
</plugin>
```

## Requirements <a name="requirements"></a>

Java 11+.

## Code Building <a name="code-building"></a>

To build all plugins use standard Git and Maven commands:

    git clone https://github.com/techsenger/maven-plugins
    cd maven-plugins
    mvn clean install

## License <a name="license"></a>

Techsenger Maven Plugins are licensed under the Apache License, Version 2.0.

## Contributing <a name="contributing"></a>

We welcome all contributions. You can help by reporting bugs, suggesting improvements, or submitting pull requests
with fixes and new features. If you have any questions, feel free to reach out — we’ll be happy to assist you.

## Support Us <a name="support-us"></a>

You can support our open-source work through [GitHub Sponsors](https://github.com/sponsors/techsenger).
Your contribution helps us maintain projects, develop new features, and provide ongoing improvements.
Multiple sponsorship tiers are available, each offering different levels of recognition and benefits.