cd /sarnobat.garagebandbroken/Desktop/github-repositories/fuse-java/proj/

mvn install:install-file -DgroupId=net.fusejna -DartifactId=fuse-jna -Dversion=0.0.1-SNAPSHOT  -Dpackaging=jar -Dfile=/sarnobat.garagebandbroken/Desktop/github-repositories/fuse-java/fuse-jna-0.0.1-SNAPSHOT.jar

mvn compile assembly:single
java -jar target/myproj-1.0-SNAPSHOT-jar-with-dependencies.jar

# Now execute:
#
#	find -L family_tree
#