set -e
 
# GRAALVM_HOME=/Volumes/Apps/graalvm-ce-java11-22.2.0/Contents/Home/
GRAALVM_HOME=/Volumes/Apps/graalvm-jdk-21.0.2+13.1/Contents/Home

$GRAALVM_HOME/bin/native-image  --no-fallback -H:Class=FuseShellScriptCallouts -jar proj/target/myproj-1.1-SNAPSHOT-jar-with-dependencies.jar -H:Name=fusejava.osx
