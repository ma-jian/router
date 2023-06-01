package com.mm.router.service

import com.mm.router.IProvider


/**
 * 自动绑定数据服务接口
 * @since 1.0
 */
interface AutowiredService : IProvider {

    fun autoWired(instance: Any)
}