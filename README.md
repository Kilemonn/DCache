# DCache
[![CI Build](https://github.com/Kilemonn/DCache/actions/workflows/gradle.yml/badge.svg)](https://github.com/Kilemonn/DCache/actions/workflows/gradle.yml) [![Coverage](.github/badges/jacoco.svg)](https://github.com/Kilemonn/DCache/actions/workflows/gradle.yml)

## Overview
DCache is a property driven caching framework that allows you to configure multi-storage-medium backed caching framework for Spring.
Allowing you to simply define your preferred cache ID, backing mechanism, key and value types and just autowiring in the cache and using it.

## Quick Start

Below are some quick start configuration snippets and matching code snippets.

### More detailed documentation can be found in the [Wiki](https://github.com/Kilemonn/DCache/wiki)!
Including remote cache configuration and fallback cache configuration.

### Including as a dependency

This can be included by making sure that you have [JitPack](https://jitpack.io) setup as a dependency repository within your project.
You can refer to the hosted versions of this library at [DCache](https://jitpack.io/#Kilemonn/DCache).

Here is an example in Gradle to include the dependency:
```
implementation("com.github.Kilemonn:dcache:0.1.0")
```

### Defining cache instances

The cache instances are dynamically configured via the application properties.
The expected format is: `dcache.cache.<cache-id>.<property>=<value>` where `cache-id` is an arbitrary id that you decide.

For example defining an in memory cache with String as the key and String as the value, requires the following properties:
```properties
dcache.cache.my-in-memory-cache.type=IN_MEMORY
dcache.cache.my-in-memory-cache.key_type=java.lang.String
dcache.cache.my-in-memory-cache.value_type=java.lang.String
```

In your application code you can auto wire this cache using the following code:
```java
public class YourClass {
    @Autowired
    @Qualifier("my-in-memory-cache")
    private DCache<String, String> cache;
}
```

### Wiring in the cache manager

All the constructed caches are stored in the DCacheManager. This can be wired in as required.
The manager can be used to retrieve multiple cache instances by their ID.
For completeness see below:

```java
public class YourClass {
    @Autowired
    private DCacheManager cacheManager;
}
```
