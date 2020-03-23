cd /sarnobat.garagebandbroken/Desktop/github-repositories/fuse-java/proj/

mvn install:install-file -DgroupId=net.fusejna -DartifactId=fuse-jna -Dversion=0.0.1-SNAPSHOT  -Dpackaging=jar -Dfile=/sarnobat.garagebandbroken/Desktop/github-repositories/fuse-java/fuse-jna-0.0.1-SNAPSHOT.jar

mvn compile assembly:single
java -jar target/myproj-1.0-SNAPSHOT-jar-with-dependencies.jar

# This has to be compiled in Eclipse unfortunately. Fuse-jna is not a maven dependency
#/Library/Java/JavaVirtualMachines/jdk1.7.0_25.jdk/Contents/Home/bin/java -Dfile.encoding=UTF-8 -classpath /sarnobat.garagebandbroken/Desktop/github-repositories/fuse-java/proj/target/classes:/sarnobat.garagebandbroken/.m2/repository/junit/junit/3.8.1/junit-3.8.1.jar:/sarnobat.garagebandbroken/Desktop/github-repositories/fuse-java/fuse-jna-0.0.1-SNAPSHOT.jar:/sarnobat.garagebandbroken/.m2/repository/net/java/dev/jna/jna/3.4.0/jna-3.4.0.jar:/sarnobat.garagebandbroken/.m2/repository/com/google/guava/guava/19.0/guava-19.0.jar:/sarnobat.garagebandbroken/.m2/repository/commons-io/commons-io/2.4/commons-io-2.4.jar App