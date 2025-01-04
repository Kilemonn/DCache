package au.kilemonn.dcache.cache

/**
 * A custom [RuntimeException] used to indicate the provided value is of the wrong type.
 *
 * @author github.com/Kilemonn
 */
class InvalidValueException(providedClass: Class<*>, expectedClass: Class<*>) : RuntimeException("Provided value of class [${providedClass.name}] is invalid. Expected value of type [${expectedClass.name}].")
