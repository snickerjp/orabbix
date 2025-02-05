# Orabbix - Oracle Database Monitoring for Zabbix

[![Codacy Badge](https://api.codacy.com/project/badge/Grade/56a9164c812c4ea1a0fca04469616c7c)](https://app.codacy.com/app/snickerjp/orabbix?utm_source=github.com&utm_medium=referral&utm_content=snickerjp/orabbix&utm_campaign=Badge_Grade_Settings)

Orabbix is a monitoring solution that integrates Oracle Database with Zabbix monitoring system.

## Compatibility

### Zabbix Versions
- ✅ Zabbix 4.0
- ✅ Zabbix 5.0
- ✅ Zabbix 6.0
- ✅ Zabbix 7.0

### Java Versions
- ✅ Java 8 (Recommended)
- ✅ Java 17
- ✅ Java 21 (Testing)
- ⚠️ Java 7 (Not actively tested)

For Java support information, see:
- [Oracle Java SE Support Roadmap (EN)](https://www.oracle.com/java/technologies/java-se-support-roadmap.html)
- [Oracle Java SE Support Roadmap (JP)](https://www.oracle.com/jp/java/technologies/java-se-support-roadmap.html)

### Oracle Database Versions
- ✅ Oracle 11g
- ✅ Oracle 12c
- ✅ Oracle 21c XE
- ✅ Oracle 23c Free

## Building from Source

### Prerequisites
- Java Development Kit (JDK 8 or later)
- Git
- Maven 3.x

### Build Instructions

#### Using Maven (Recommended)
1. Clone the repository:
```sh
git clone https://github.com/snickerjp/orabbix.git -b main
cd orabbix
```

2. Build with Maven:
```sh
mvn clean package
```

The built JAR files will be available in the `target` directory:
- `orabbix-1.2.3.jar`: Basic JAR file
- `orabbix-1.2.3-jar-with-dependencies.jar`: JAR with all dependencies included

#### Alternative Build Method (Legacy)

1. Clone the repository:
```sh
git clone https://github.com/snickerjp/orabbix.git -b legacy
cd orabbix
```

2. Download Orabbix:
Download from: https://sourceforge.net/projects/orabbix/
```sh
unzip orabbix-1.2.3.zip
# OR
unzip -d orabbix-1.2.3 orabbix-1.2.3.zip
```

3. Compile:
```sh
# Requires java-1.8.0-openjdk-devel (example for RHEL-based systems)
javac -cp "$(for _JAR in orabbix-1.2.3/lib/*.jar;do echo -n $_JAR:;done)orabbix-1.2.3/orabbix-1.2.3.jar" com/smartmarmot/orabbix/Sender.java
mkdir -p ./build
cp orabbix-1.2.3/orabbix-1.2.3.jar ./build
cd build
jar -xvf orabbix-1.2.3.jar com
cp ../com/smartmarmot/orabbix/Sender.class com/smartmarmot/orabbix/Sender.class
jar -uf orabbix-1.2.3.jar com
```

## Screenshots

![Orabbix Dashboard](https://github.com/snickerjp/orabbix/assets/1247622/4494419e-2a2d-41cd-a7cf-b8565b689cf9)

## Notes
- Main development branch is `main`
- Branch `legacy` is legacy style

For more details and issues, please refer to the [GitHub Issues](https://github.com/snickerjp/orabbix/issues)
