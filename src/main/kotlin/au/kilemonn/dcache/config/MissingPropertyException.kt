package au.kilemonn.dcache.config

/**
 * A custom [RuntimeException] used to indicate that there are missing required properties in the cache configuration.
 *
 * @author github.com/Kilemonn
 */
class MissingPropertyException(id: String, property: String): RuntimeException("Property [$property] missing for cache with ID [$id].")
