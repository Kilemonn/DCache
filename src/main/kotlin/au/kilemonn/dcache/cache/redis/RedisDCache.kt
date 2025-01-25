package au.kilemonn.dcache.cache.redis

import au.kilemonn.dcache.cache.DCache
import au.kilemonn.dcache.cache.DCacheInitialisationException
import au.kilemonn.dcache.config.CacheConfiguration
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.StringRedisSerializer
import java.io.Serializable
import java.time.Duration

/**
 * Wraps the [RedisTemplate].
 *
 * @author github.com/Kilemonn
 */
class RedisDCache<K, V: Serializable>(keyClass: Class<K>, valueClass: Class<V>, val config: CacheConfiguration<K, V>) : DCache<K, V>(keyClass, valueClass)
{
    companion object
    {
        const val DEFAULT_REDIS_PORT: Int = 6379
    }

    private val template: RedisTemplate<K, V> = RedisTemplate()

    init {
        initialiseRedisTemplate()
    }

    private fun initialiseRedisConfiguration(): RedisStandaloneConfiguration
    {
        if (config.getEndpoint().isBlank())
        {
            throw DCacheInitialisationException(config.id, "No endpoint provided for REDIS cache.")
        }

        val split = config.getEndpoint().split(":")
        var port = DEFAULT_REDIS_PORT
        if (split.size > 1)
        {
            port = split[1].toInt()
        }

        val redisConfiguration = RedisStandaloneConfiguration()
        redisConfiguration.hostName = split[0]
        redisConfiguration.port = port
        return redisConfiguration
    }

    private fun initialiseRedisTemplate()
    {
        val builder = LettuceClientConfiguration.builder()
        builder.commandTimeout(Duration.ofMillis(config.getTimeout()))

        val connFactory = LettuceConnectionFactory(initialiseRedisConfiguration(), builder.build())
        connFactory.start()
        template.connectionFactory = connFactory
        template.keySerializer = StringRedisSerializer()
        template.afterPropertiesSet()
    }

    override fun getInternal(key: K): Result<V?>
    {
        return runCatching {
            return Result.success(template.opsForValue().get(key))
        }
    }

    override fun putInternal(key: K, value: V): Result<Boolean>
    {
        return runCatching {
            template.opsForValue().set(key, value)
            return Result.success(true)
        }
    }

    /**
     * Overriding, since redis has a proper call for this method.
     *
     * Since this is overriding we need to ensure we call the ensure functions.
     */
    override fun putIfAbsent(key: K, value: V): Boolean
    {
        ensureKeyType(key)
        ensureValueType(value)

        val result = runCatching {
            val res = template.opsForValue().setIfAbsent(withPrefix(key), value)
            return res
        }
        if (hasFallback() && !result.getOrDefault(false))
        {
            return fallbackCache!!.putIfAbsent(key, value)
        }
        return result.getOrDefault(false)
    }

    override fun putWithExpiry(key: K, value: V, duration: Duration): Boolean
    {
        val result = put(key, value)
        if (!result)
        {
            if (hasFallback())
            {
                return fallbackCache!!.putWithExpiry(key, value, duration)
            }
            return result
        }

        val res = runCatching {
            return template.expire(withPrefix(key), duration)
        }
        if (hasFallback() && !res.getOrDefault(false))
        {
            return fallbackCache!!.putWithExpiry(key, value, duration)
        }
        return res.getOrDefault(false)
    }

    override fun invalidateInternal(key: K): Result<Unit>
    {
        return runCatching {
            template.opsForValue().getAndDelete(withPrefix(key))
            return Result.success(Unit)
        }
    }

    override fun getPrefix(): String
    {
        return config.getPrefix()
    }

    override fun getCacheName(): String
    {
        return config.id
    }
}
