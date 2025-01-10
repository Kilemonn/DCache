package au.kilemonn.dcache.config

import au.kilemonn.dcache.cache.DCache
import au.kilemonn.dcache.cache.DCacheInitialisationException
import au.kilemonn.dcache.cache.InvalidValueException
import au.kilemonn.dcache.manager.DCacheManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Lazy
import org.springframework.context.support.GenericApplicationContext
import org.springframework.core.env.ConfigurableEnvironment
import org.springframework.core.env.MapPropertySource
import java.io.Serializable
import kotlin.collections.iterator

/**
 * Initialisation and injection of the dynamically configured caches, defined with the prefix [DCACHE_CONFIG_PREFIX].
 *
 * @author github.com/Kilemonn
 */
@Configuration(value = DCacheConfiguration.CONFIG_NAME)
class DCacheConfiguration: ApplicationContextAware
{
    companion object
    {
        // Incase the caches are not ready you can add @DependsOn() with this configuration name
        const val CONFIG_NAME = "dcache-init"

        internal const val DCACHE_CONFIG_PREFIX = "dcache.cache."

        // Incase the caches are not ready you can add @DependsOn() with the cache manager name
        const val DCACHE_CACHE_MANAGER = "dcache.cache.manager"
    }

    @Autowired
    private lateinit var env: ConfigurableEnvironment

    private lateinit var context: GenericApplicationContext

    @Bean(name = [DCACHE_CACHE_MANAGER])
    @Lazy(false) // Ensure that this is not lazy loaded
    fun initialiseCacheManager(): DCacheManager
    {
        val props = HashMap<String, Any>()
        for (source in env.propertySources)
        {
            if (source is MapPropertySource)
            {
                props.putAll(source.source)
            }
        }

        val dcacheProps = props.filter { entry ->
                entry.key.startsWith(DCACHE_CONFIG_PREFIX)
            }.toMap()

        // TODO: How can I maintain a single connection to redis/memcache instead of creating new connections per?
        // TODO: Or how to specify a configuration to reuse an existing connection?
        val cacheEntries = getCacheIdsAndPropertiesMap(dcacheProps)
        val caches = HashMap<String, DCache<*, Serializable>>()
        for (cacheEntry in cacheEntries)
        {
            val keyClass = Class.forName(cacheEntry.value[CacheConfiguration.KEY_CLASS].toString())
            val valueClass = Class.forName(cacheEntry.value[CacheConfiguration.VALUE_CLASS].toString())
            if (!Serializable::class.java.isAssignableFrom(valueClass))
            {
                throw InvalidValueException(valueClass, Serializable::class.java)
            }
            val type = DCacheType.valueOf(cacheEntry.value[CacheConfiguration.TYPE].toString())
            val config = CacheConfiguration(cacheEntry.key, type, keyClass, valueClass as Class<Serializable>, cacheEntry.value)
            val cache = config.buildCache()
            caches.put(config.id, cache)
            context.beanFactory.registerSingleton(config.id, cache)
        }

        return DCacheManager(caches)
    }

    override fun setApplicationContext(applicationContext: ApplicationContext)
    {
        context = applicationContext as GenericApplicationContext
    }

    internal fun getCacheIdsAndPropertiesMap(map: Map<String, Any>): Map<String, Map<String, Any>>
    {
        val props = HashMap<String, HashMap<String, Any>>()
        for (key in map.keys)
        {
            val id = getPropertyID(key)
            if (id.isBlank())
            {
                continue
            }

            if (props.contains(id))
            {
                val m = props[id]
                m!!.put(getPropertyForID(key, id), map[key]!!)
            }
            else
            {
                val m = HashMap<String, Any>()
                m.put(getPropertyForID(key, id), map[key]!!)
                props.put(id, m)
            }
        }

        return props
    }

    internal fun getPropertyID(str: String): String
    {
        if (!str.startsWith(DCACHE_CONFIG_PREFIX))
        {
            return ""
        }

        val periodIndex = str.indexOf(".", DCACHE_CONFIG_PREFIX.length)
        if (periodIndex == -1)
        {
            return ""
        }
        return str.substring(DCACHE_CONFIG_PREFIX.length, periodIndex)
    }

    internal fun getPropertyForID(str: String, id: String): String
    {
        // Doing +1 to remove the training "."
        val startIndex = (DCACHE_CONFIG_PREFIX + id).length + 1
        if (!str.startsWith(DCACHE_CONFIG_PREFIX) || startIndex > str.length)
        {
            return ""
        }
        return str.substring(startIndex)
    }
}
