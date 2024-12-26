package au.kilemonn.dcache.config

import au.kilemonn.dcache.cache.Cache
import au.kilemonn.dcache.manager.CacheManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Lazy
import org.springframework.context.support.GenericApplicationContext
import org.springframework.core.env.ConfigurableEnvironment
import org.springframework.core.env.MapPropertySource
import kotlin.collections.iterator

/**
 * Initialisation and injection of the dynamically configured caches, defined with the prefix [DCACHE_CONFIG_PREFIX].
 *
 * @author github.com/Kilemonn
 */
@Configuration(value = ContextListener.CONFIG_NAME)
class ContextListener: ApplicationContextAware
{
    companion object
    {
        // Incase the caches are not ready you can add @DependsOn() with this configuration name
        const val CONFIG_NAME = "dcache-init"

        private const val DCACHE_CONFIG_PREFIX = "dcache.cache."

        // Incase the caches are not ready you can add @DependsOn() with the cache manager name
        const val DCACHE_CACHE_MANAGER = "dcache.cache.manager"
    }

    @Autowired
    private lateinit var env: ConfigurableEnvironment

    private lateinit var context: GenericApplicationContext

    @Bean(name = [DCACHE_CACHE_MANAGER])
    @Lazy(false) // Ensure that this is not lazy loaded
    fun initialiseCacheManager(): CacheManager
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

        val configurations = initialiseCacheConfigurations(dcacheProps)
        val caches = HashMap<String, Cache<*,*>>()
        for (configuration in configurations)
        {
            val cache = configuration.buildCache()
            caches.put(configuration.id, cache)
            context.beanFactory.registerSingleton(configuration.id, cache)
        }

        return CacheManager(caches)
    }

    override fun setApplicationContext(applicationContext: ApplicationContext)
    {
        context = applicationContext as GenericApplicationContext
    }

    private fun initialiseCacheConfigurations(map: Map<String, Any>): List<CacheConfiguration>
    {
        val cacheEntries = getCacheIdsAndPropertiesMap(map)
        val configs = ArrayList<CacheConfiguration>()

        for (entry in cacheEntries)
        {
            configs.add(CacheConfiguration.from(entry.key, entry.value))
        }

        return configs
    }

    private fun getCacheIdsAndPropertiesMap(map: Map<String, Any>): Map<String, Map<String, Any>>
    {
        val props = HashMap<String, HashMap<String, Any>>()
        for (key in map.keys)
        {
            val id = getPropertyID(key)
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

    private fun getPropertyID(str: String): String
    {
        return str.substring(DCACHE_CONFIG_PREFIX.length, str.indexOf(".", DCACHE_CONFIG_PREFIX.length))
    }

    private fun getPropertyForID(str: String, id: String): String
    {
        // Doing +1 to remove the training "."
        return str.substring((DCACHE_CONFIG_PREFIX + id + 1).length)
    }
}
