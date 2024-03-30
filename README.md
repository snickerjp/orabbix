# Orabbix support Zabbix 4.0,5.0,6.0

- Stable Branch is `main`
- Not support branch `convert_maven`

[![Codacy Badge](https://api.codacy.com/project/badge/Grade/56a9164c812c4ea1a0fca04469616c7c)](https://app.codacy.com/app/snickerjp/orabbix?utm_source=github.com&utm_medium=referral&utm_content=snickerjp/orabbix&utm_campaign=Badge_Grade_Settings)

## How to build

```sh
# Git clone `main` branch.
git clone https://github.com/snickerjp/orabbix.git -b main

cd orabbix
# download orabbix
#from: https://sourceforge.net/projects/orabbix/

unzip orabbix-1.2.3.zip
OR
unzip -d orabbix-1.2.3 orabbix-1.2.3.zip
ls -d orabbix-1.2.3

# compile
# require. java-1.8.0-openjdk-devel(example RHEL yum system)
javac -cp "$(for _JAR in orabbix-1.2.3/lib/*.jar;do echo -n $_JAR:;done)orabbix-1.2.3/orabbix-1.2.3.jar" com/smartmarmot/orabbix/Sender.java
mkdir -p ./build
cp orabbix-1.2.3/orabbix-1.2.3.jar ./build
cd build
jar -xvf orabbix-1.2.3.jar com
cp ../com/smartmarmot/orabbix/Sender.class com/smartmarmot/orabbix/Sender.class
jar -uf orabbix-1.2.3.jar com
```

## Test

### Zabbix

- Work with Zabbix 4.0
- Work with Zabbix 5.0
- Work with Zabbix 6.0
- Work with Zabbix 7.0 (Now testing)

### Java

- Work with Java7
- Work with Java8
- Work with Java17
- Work with Java21 (Now testing)

ref.

- JP [Oracle Java SE Supportロードマップ \| Oracle 日本](https://www.oracle.com/jp/java/technologies/java-se-support-roadmap.html)
- EN [Oracle Java SE Support Roadmap \| Oracle](https://www.oracle.com/java/technologies/java-se-support-roadmap.html)

### Oracle Database

- Work with 11g
- Work with 12c
- Work with 21c XE
- Work with 23c Free

ex.
snickerjp/orabbix#9

## Screenshot

![image](https://github.com/snickerjp/orabbix/assets/1247622/4494419e-2a2d-41cd-a7cf-b8565b689cf9)
