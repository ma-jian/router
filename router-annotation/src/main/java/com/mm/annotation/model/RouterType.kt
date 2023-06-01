package com.mm.annotation.model


/**
 * 路由的类型
 * @since 1.0 open router types
 */
enum class RouterType(val int: Int, val className: String) {

    ACTIVITY(0, "android.app.Activity"),

    SERVICE(1, "android.app.Service"),

    PROVIDER(2, "com.mm.router.IProvider"),

    FRAGMENT(3, "android.app.Fragment"),

    SYSTEM_ACTIVITY(4, "android.app.Activity"),

    UNKNOWN(-1, "Unknown route type")
}