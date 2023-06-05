package com.mm.router_app.provider

import com.mm.router.IProvider


/**
 * Date : 2023/5/18
 */
interface IServiceProvider : IProvider {

    fun message(): String
}