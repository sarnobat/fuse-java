set -e

cat <<EOF
Install Macfuse: https://github.com/osxfuse/osxfuse/releases/download/macfuse-4.0.5/macfuse-4.0.5.dmg
EOF

mvn install:install-file -f proj/pom.xml -DgroupId=net.fusejna -DartifactId=fuse-jna -Dversion=0.0.1-SNAPSHOT  -Dpackaging=jar -Dfile=$HOME/github/fuse-java/fuse-jna-0.0.1-SNAPSHOT.jar

mvn compile assembly:single -f proj/pom.xml
# DYLD_LIBRARY_PATH=/usr/local/lib
# LD_LIBRARY_PATH=/usr/local/lib
ls /usr/local/lib/libfuse.dylib

LD_LIBRARY_PATH=$LD_LIBRARY_PATH:/usr/local/lib java -Djava.library.path=/usr/local/lib/ -jar proj/target/myproj-1.1-SNAPSHOT-jar-with-dependencies.jar
LD_LIBRARY_PATH=$LD_LIBRARY_PATH:/usr/local/lib java -Djava.library.path=/usr/local/lib/ -classpath proj/target/myproj-1.1-SNAPSHOT-jar-with-dependencies.jar App

cat <<EOF
# Now execute:
#
#	find -L family_tree

EOF
