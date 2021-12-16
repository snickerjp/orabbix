# Orabbix support Zabbix 4.0

## How to build

```sh
git clone https://github.com/snickerjp/orabbix.git

cd orabbix
# download orabbix
#from: https://sourceforge.net/projects/orabbix/

unzip -d orabbix-1.2.3 orabbix-1.2.3.zip

# compile
# require. java-1.7.0-openjdk-devel,java-1.8.0-openjdk-devel
javac -cp "orabbix-1.2.3/orabbix-1.2.3.jar:orabbix-1.2.3/lib/*" com/smartmarmot/orabbix/Sender.java
mkdir -p ./build
cp orabbix-1.2.3/orabbix-1.2.3.jar ./build
cd build
jar -xvf orabbix-1.2.3.jar com
cp ../com/smartmarmot/orabbix/Sender.class com/smartmarmot/orabbix/Sender.class
jar -uf orabbix-1.2.3.jar com
```
