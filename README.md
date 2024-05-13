# fuse-java
Including fuse-yurl. Works with groovy

### 2024-04

Unfortunately I couldn't get FuseShellScriptCallouts working with java native images. 
* I guess a jar file is the only way to go
* ~~I'll have to revert to python (since golang doesn't have support I vaguely recall). Wait, seems like fuse isn't supported on Windows.~~
```
Exception in thread "main" java.lang.UnsatisfiedLinkError: No awt in java.library.path
	at org.graalvm.nativeimage.builder/com.oracle.svm.core.jdk.NativeLibrarySupport.loadLibraryRelative(NativeLibrarySupport.java:136)
	at java.base@21.0.2/java.lang.ClassLoader.loadLibrary(ClassLoader.java:106)
	at java.base@21.0.2/java.lang.Runtime.loadLibrary0(Runtime.java:916)
	at java.base@21.0.2/java.lang.System.loadLibrary(System.java:2063)
	at java.desktop@21.0.2/java.awt.Toolkit$2.run(Toolkit.java:1384)
	at java.desktop@21.0.2/java.awt.Toolkit$2.run(Toolkit.java:1382)
	at java.base@21.0.2/java.security.AccessController.executePrivileged(AccessController.java:129)
	at java.base@21.0.2/java.security.AccessController.doPrivileged(AccessController.java:319)
	at java.desktop@21.0.2/java.awt.Toolkit.loadLibraries(Toolkit.java:1381)
	at java.desktop@21.0.2/java.awt.Toolkit.initStatic(Toolkit.java:1419)
	at java.desktop@21.0.2/java.awt.Toolkit.<clinit>(Toolkit.java:1393)
	at java.desktop@21.0.2/java.awt.Component.<clinit>(Component.java:624)
	at com.sun.jna.Platform.<clinit>(Platform.java:64)
	at net.fusejna.Platform.init(Platform.java:39)
	at net.fusejna.Platform.fuse(Platform.java:26)
	at net.fusejna.FuseJna.init(FuseJna.java:133)
	at net.fusejna.FuseJna.mount(FuseJna.java:193)
	at net.fusejna.FuseFilesystem.mount(FuseFilesystem.java:571)
	at net.fusejna.FuseFilesystem.mount(FuseFilesystem.java:558)
	at FuseShellScriptCallouts.main(FuseShellScriptCallouts.java:81)
	at java.base@21.0.2/java.lang.invoke.LambdaForm$DMH/sa346b79c.invokeStaticInit(LambdaForm$DMH)
```

### Ideas
* callout to bash
* oracle
* csv (from mint or fidelity) with sizes
* graphml
* yurl

### 2023-01-31
```
Exception in thread "main" java.lang.UnsatisfiedLinkError: Unable to load library 'fuse': dlopen(libfuse.dylib, 0x0009): tried: 'libfuse.dylib' (no such file), '/System/Volumes/Preboot/Cryptexes/OSlibfuse.dylib' (no such file), '/Library/Java/JavaVirtualMachines/jdk1.8.0_333.jdk/Contents/Home/bin/./libfuse.dylib' (no such file), '/usr/lib/libfuse.dylib' (no such file, not in dyld cache), 'libfuse.dylib' (no such file), '/usr/lib/libfuse.dylib' (no such file, not in dyld cache)
	at com.sun.jna.NativeLibrary.loadLibrary(NativeLibrary.java:169)
	at com.sun.jna.NativeLibrary.getInstance(NativeLibrary.java:242)
	at com.sun.jna.Library$Handler.<init>(Library.java:140)
	at com.sun.jna.Native.loadLibrary(Native.java:368)
	at com.sun.jna.Native.loadLibrary(Native.java:353)
	at net.fusejna.Platform.init(Platform.java:77)
	at net.fusejna.Platform.fuse(Platform.java:26)
	at net.fusejna.FuseJna.init(FuseJna.java:133)
	at net.fusejna.FuseJna.mount(FuseJna.java:193)
	at net.fusejna.FuseFilesystem.mount(FuseFilesystem.java:571)
	at net.fusejna.FuseFilesystem.mount(FuseFilesystem.java:577)
	at App.main(App.java:233)
```
### HelloFS

https://github.com/sarnobat/fuse-java/blob/master/proj/src/main/java/HelloFS.java
