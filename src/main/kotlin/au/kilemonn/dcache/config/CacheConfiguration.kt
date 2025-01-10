package au.kilemonn.dcache.config

import au.kilemonn.dcache.cache.DCache
import au.kilemonn.dcache.cache.inmemory.InMemoryDCache
import au.kilemonn.dcache.cache.memcached.MemcachedDCache
import au.kilemonn.dcache.cache.redis.RedisDCache
import java.io.Serializable
import java.util.Optional

/**
 * Holds the defined configuration of a specific cache. These values are retrieved from the system properties.
 *
 * @author github.com/Kilemonn
 */
class CacheConfiguration<K, V: Serializable>
{
    companion object
    {
        const val TYPE: String = "type"
        const val KEY_CLASS: String = "key_class"
        const val VALUE_CLASS: String = "value_class"
        const val PREFIX: String = "prefix"
        const val ENDPOINT: String = "endpoint"
        const val PORT: String = "port"
        const val MAX_ENTRIES: String = "max_entries"
        const val EXPIRATION_FROM_WRITE: String = "expiration_from_write"
        const val IN_MEMORY_FALLBACK: String = "in_memory_fallback"
    }

    val id: String
    val type: DCacheType
    val keyClass: Class<K>
    val valueClass: Class<V>

    private var prefix: String = ""
    private var endpoint: String = ""
    private var port: Int = 0
    private var maxEntries: Long = 0
    private var expirationFromWrite: Long = 0 // In seconds
    private var inMemoryFallback: Boolean = false

    constructor(id: String, type: DCacheType, keyClass: Class<K>, valueClass: Class<V>, options: Map<String, Any>)
    {
        this.id = id
        this.type = type
        this.keyClass = keyClass
        this.valueClass = valueClass

        this.withPrefix(Optional<String>.ofNullable(options[PREFIX]).orElse("").toString())
            .withEndpoint(Optional<String>.ofNullable(options[ENDPOINT]).orElse("").toString())
            .withPort((Optional<Int>.ofNullable(options[PORT]).orElse(0)).toString().toInt())
            .withMaxEntries((Optional<Long>.ofNullable(options[MAX_ENTRIES]).orElse(0L)).toString().toLong())
            .withExpirationFromWrite((Optional<Int>.ofNullable(options[EXPIRATION_FROM_WRITE]).orElse(0)).toString().toLong())
            .withInMemoryFallback((Optional<String>.ofNullable(options[IN_MEMORY_FALLBACK]).orElse("")).toString().toBoolean())
    }

    fun withPrefix(prefix: String): CacheConfiguration<K, V>
    {
        this.prefix = prefix
        return this
    }

    fun getPrefix(): String
    {
        return prefix
    }

    fun withEndpoint(endpoint: String): CacheConfiguration<K, V>
    {
        this.endpoint = endpoint
        return this
    }

    fun getEndpoint(): String
    {
        return endpoint
    }

    fun withPort(port: Int): CacheConfiguration<K, V>
    {
        this.port = port
        return this
    }

    fun getPort(): Int
    {
        return port
    }

    fun withMaxEntries(maxEntries: Long): CacheConfiguration<K, V>
    {
        this.maxEntries = maxEntries
        return this
    }

    fun getMaxEntries(): Long
    {
        return maxEntries
    }

    fun withExpirationFromWrite(expirationFromWrite: Long): CacheConfiguration<K, V>
    {
        this.expirationFromWrite = expirationFromWrite
        return this
    }

    fun getExpirationFromWrite(): Long
    {
        return expirationFromWrite
    }

    fun withInMemoryFallback(inMemoryFallback: Boolean): CacheConfiguration<K, V>
    {
        this.inMemoryFallback = inMemoryFallback
        return this
    }

    fun getInMemoryFallback(): Boolean
    {
        return inMemoryFallback
    }

    fun buildCache(): DCache<K, V>
    {
        return when(type)
        {
            DCacheType.IN_MEMORY -> InMemoryDCache(keyClass, valueClass, this)
            DCacheType.REDIS -> RedisDCache(keyClass, valueClass, this)
            DCacheType.MEMCACHED -> MemcachedDCache(keyClass, valueClass, this)
        }
    }
}
