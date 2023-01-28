#!/bin/sh

#----------------------------------------------------------------------------
# DESCRIPTION		
# DATE				[:VIM_EVAL:]strftime('%Y-%m-%d')[:END_EVAL:]
# AUTHOR			ss401533@gmail.com                                           
#----------------------------------------------------------------------------

set -e

test $# -gt 0 && echo "args given" || echo "no args"

cat <<EOF | \batcat --plain --paging=never --language sh --theme TwoDark
/Library/Java/JavaVirtualMachines/openjdk-17.jdk/Contents/Home/bin/java -cp ../jna-3.4.0.jar:../fuse-jna-0.0.1-SNAPSHOT.jar:/Users/srsarnob/.gradle/caches/modules-2/files-2.1/com.google.guava/guava/21.0/3a3d111be1be1b745edfa7d91678a12d7ed38709/guava-21.0.jar /Volumes/git/github/fuse-java/graphml/src/main/java/App.java
EOF



