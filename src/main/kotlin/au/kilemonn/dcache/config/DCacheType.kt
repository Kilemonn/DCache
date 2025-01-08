package au.kilemonn.dcache.config

/**
 * The cache type and where the data is actually stored.
 *
 * @author github.com/Kilemonn
 */
enum class DCacheType
{
    IN_MEMORY,
    REDIS,
    MEMCACHED,
}
