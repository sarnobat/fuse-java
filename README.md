# fuse-java
Including fuse-yurl. Works with groovy

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
