package com.mm.router.service

import com.mm.router.IProvider


/**
 * Date : 2023/5/17
 * 自动绑定数据服务接口
 */
interface AutowiredService : IProvider {

    fun autoWired(instance: Any)
}