package com.mm.router


/**
 * 注册指定页面，并对 [com.mm.router.annotation.Autowired] 字段进行赋值
 * @since 1.0
 */
interface ISyringe {

    fun inject(target: Any)
}