package au.kilemonn.dcache.manager

import au.kilemonn.dcache.cache.Cache
import java.util.Optional

/**
 * The cache manager that holds all instantiated [Cache] instances.
 *
 * @author github.com/Kilemonn
 */
class CacheManager(private val caches: Map<String, Cache<*,*>>)
{
    val size: Int
        get() = caches.size

    fun getCache(id: String): Optional<Cache<*, *>>
    {
        return Optional.ofNullable(caches[id])
    }
}
