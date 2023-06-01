package com.mm.annotation.model


/**
 * Date : 2023/5/17
 * @param type Type of route
 * @param path Path of route
 * @param destination  destination class
 * @param des desc
 */
class RouterMeta private constructor(
    val type: RouterType,
    val path: String,
    val destination: Class<*>?,
    val des: String? = "",
    val interceptors: Array<String> = arrayOf(),
) {
    companion object {
        @JvmStatic
        fun build(type: RouterType, path: String, destination: Class<*>?, des: String?, params: Array<String>): RouterMeta {
            return RouterMeta(type, path, destination, des, params)
        }

        @JvmStatic
        fun build(type: RouterType, path: String, destination: Class<*>?, des: String?): RouterMeta {
            return RouterMeta(type, path, destination, des, arrayOf())
        }

        @JvmStatic
        fun build(type: RouterType, path: String, destination: Class<*>?): RouterMeta {
            return RouterMeta(type, path, destination, "", arrayOf())
        }

        @JvmStatic
        fun build(type: RouterType, destination: Class<*>?): RouterMeta {
            return RouterMeta(type, destination?.name ?: "", destination, "", arrayOf())
        }
    }
}