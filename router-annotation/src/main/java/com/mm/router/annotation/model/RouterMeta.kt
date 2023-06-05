package com.mm.router.annotation.model


/**
 * 路由实体类
 * @param type Type of router
 * @param path Path of router
 * @param destination  destination class
 * @param des desc
 * @param interceptors 页面使用的拦截器，为空则全部使用
 * @param priority 路由器的优先级，数字越大优先级越高
 */
class RouterMeta private constructor(
    val type: RouterType,
    val path: String,
    val destination: Class<*>?,
    val des: String? = "",
    val interceptors: Array<String> = arrayOf(),
    val priority: Int = 0
) {
    companion object {
        @JvmStatic
        fun build(type: RouterType, path: String, destination: Class<*>?, des: String?, priority: Int): RouterMeta {
            return RouterMeta(type, path, destination, des, arrayOf(), priority)
        }

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