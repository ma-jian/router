package com.mm.router_app.provider

import com.mm.router.annotation.ServiceProvider


/**
 * Date : 2023/5/18
 */
@ServiceProvider("/service/provider")
class ServiceProviderImpl constructor(
    private val string: String,
    private val int: Int,
    private val log: Long,
    private val bol: Boolean
) : IServiceProvider {

    constructor() : this("", 1, 1, false)

    override fun message(): String {
        return "ServiceProvider message $string :$int :$log :$bol"
    }
}