cat <<EOF

Install Macfuse: https://github.com/osxfuse/osxfuse/releases/download/macfuse-4.0.5/macfuse-4.0.5.dmg

Jar
---
cd ~/github/fuse-java/proj/

mvn install:install-file -DgroupId=net.fusejna -DartifactId=fuse-jna -Dversion=0.0.1-SNAPSHOT  -Dpackaging=jar -Dfile=$HOME/github/fuse-java/fuse-jna-0.0.1-SNAPSHOT.jar

mvn compile assembly:single
java -jar target/myproj-1.1-SNAPSHOT-jar-with-dependencies.jar

# Now execute:
#
#	find -L family_tree
#


Graal native image
------------------
mvn package && JAVA_HOME=/Volumes/Apps/graalvm-ce-java11-22.2.0/Contents/Home/ mvn -Pnative -Dagent package
cp -v target/myproj fuse-java.osx
EOF