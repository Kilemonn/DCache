package au.kilemonn.dcache.cache

/**
 * A custom [RuntimeException] used to indicate cache initialisation errors.
 *
 * @author github.com/Kilemonn
 */
class DCacheInitialisationException(id: String, reason: String) : RuntimeException("Unable to initialise cache with ID [$id] because: [$reason].")
