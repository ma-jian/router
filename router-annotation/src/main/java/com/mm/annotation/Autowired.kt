package com.mm.annotation

/**
 * Annotation for field, which need autowired.
 *
 * @version 1.0
 * @param name // Mark param's name or service name.
 * @param required  // If required, app will be crash when value is null. Primitive type wont be check!
 * @param des // Description of the field
 */
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.SOURCE)
annotation class Autowired(
    val name: String = "",
    val required: Boolean = false,
    val des: String = ""
)