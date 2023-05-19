package com.mm.router

/**
 * 注册指定页面，并对[Autowired]字段进行赋值
 * @since 1.0
 */

interface ISyringe {

    fun inject(target: Any)
}