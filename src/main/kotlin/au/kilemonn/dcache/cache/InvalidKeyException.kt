package au.kilemonn.dcache.cache

/**
 * A custom [RuntimeException] used to indicate the provided key is of the wrong type.
 *
 * @author github.com/Kilemonn
 */
class InvalidKeyException(providedClass: Class<*>, expectedClass: Class<*>) : RuntimeException("Provided key of class [${providedClass.name}] is invalid. Expected key of type [${expectedClass.name}].")
