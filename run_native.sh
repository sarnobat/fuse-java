set -e
 
GRAALVM_HOME=/Volumes/Apps/graalvm-ce-java11-22.2.0/Contents/Home/

$GRAALVM_HOME/bin/native-image -H:Class=FuseShellScriptCallouts -jar proj/target/myproj-1.1-SNAPSHOT-jar-with-dependencies.jar -H:Name=fusejava.osx
