package au.kilemonn.dcache.config

import au.kilemonn.dcache.cache.Cache
import au.kilemonn.dcache.cache.inmemory.InMemoryCache
import java.util.Optional


/**
 * Holds the defined configuration of a specific cache. These values are retrieved from the system properties.
 *
 * @author github.com/Kilemonn
 */
class CacheConfiguration
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

        fun from(id: String, map: Map<String, Any>): CacheConfiguration
        {
            Optional<String>.ofNullable(map[TYPE]).orElseThrow { MissingPropertyException(id, TYPE) }
            Optional<String>.ofNullable(map[KEY_CLASS]).orElseThrow { MissingPropertyException(id, KEY_CLASS) }
            Optional<String>.ofNullable(map[VALUE_CLASS]).orElseThrow { MissingPropertyException(id, VALUE_CLASS) }

            // TODO: Add invalid cache type handling
            val type = CacheType.valueOf(map[TYPE].toString())
            val config = CacheConfiguration(id, type, map[KEY_CLASS].toString(), map[VALUE_CLASS].toString())

            config.withPrefix(Optional<String>.ofNullable(map[PREFIX]).orElse("").toString())
                .withEndpoint(Optional<String>.ofNullable(map[ENDPOINT]).orElse("").toString())
                .withPort(Optional<Int>.ofNullable(map[PORT]).orElse(0) as Int)
                .withMaxEntries(Optional<Long>.ofNullable(map[MAX_ENTRIES]).orElse(0L) as Long)
                .withExpirationFromWrite(Optional<Int>.ofNullable(map[EXPIRATION_FROM_WRITE]).orElse(0) as Int)

            return config
        }
    }

    constructor(id: String, type: CacheType, keyClassName: String, valueClassName: String)
    {
        this.id = id
        this.type = type

        this.keyClass = Class.forName(keyClassName)
        this.valueClass = Class.forName(valueClassName)
    }

    val id: String
    private val type: CacheType
    private val keyClass: Class<*>
    private val valueClass: Class<*>

    private var prefix: String = ""
    private var endpoint: String = ""
    private var port: Int = 0
    private var maxEntries: Long = 0
    private var expirationFromWrite: Int = 0

    fun withPrefix(prefix: String): CacheConfiguration
    {
        this.prefix = prefix
        return this
    }

    fun getPrefix(): String
    {
        return prefix
    }

    fun withEndpoint(endpoint: String): CacheConfiguration
    {
        this.endpoint = endpoint
        return this
    }

    fun withPort(port: Int): CacheConfiguration
    {
        this.port = port
        return this
    }

    fun withMaxEntries(maxEntries: Long): CacheConfiguration
    {
        this.maxEntries = maxEntries
        return this
    }

    fun getMaxEntries(): Long
    {
        return maxEntries
    }

    fun withExpirationFromWrite(expirationFromWrite: Int): CacheConfiguration
    {
        this.expirationFromWrite = expirationFromWrite
        return this
    }

    fun buildCache(): Cache<*, *>
    {
        if (type == CacheType.IN_MEMORY)
        {
            return InMemoryCache(keyClass, valueClass, this)
        }

        throw IllegalArgumentException("Invalid type not supported???")
    }
}
