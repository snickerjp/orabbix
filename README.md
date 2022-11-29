# Orabbix support Zabbix 4.0

[![Codacy Badge](https://api.codacy.com/project/badge/Grade/56a9164c812c4ea1a0fca04469616c7c)](https://app.codacy.com/app/snickerjp/orabbix?utm_source=github.com&utm_medium=referral&utm_content=snickerjp/orabbix&utm_campaign=Badge_Grade_Settings)

## How to build

```sh
git clone https://github.com/snickerjp/orabbix.git

cd orabbix
# download orabbix
#from: https://sourceforge.net/projects/orabbix/

unzip orabbix-1.2.3.zip
OR
unzip -d orabbix-1.2.3 orabbix-1.2.3.zip
ls -d orabbix-1.2.3

# compile
# require. java-1.7.0-openjdk-devel or java-1.8.0-openjdk-devel
javac -cp "$(for i in orabbix-1.2.3/lib/*.jar;do echo -n $i:;done)orabbix-1.2.3/orabbix-1.2.3.jar" com/smartmarmot/orabbix/Sender.java
mkdir -p ./build
cp orabbix-1.2.3/orabbix-1.2.3.jar ./build
cd build
jar -xvf orabbix-1.2.3.jar com
cp ../com/smartmarmot/orabbix/Sender.class com/smartmarmot/orabbix/Sender.class
jar -uf orabbix-1.2.3.jar com
```

## Test

	- Work with Zabbix 5.0
	- Work with Zabbix 6.0

ex. 
snickerjp/orabbix#9
