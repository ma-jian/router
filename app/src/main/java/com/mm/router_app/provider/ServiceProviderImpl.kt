package com.mm.router_app.provider

import android.content.Context
import android.util.Log
import com.mm.router.annotation.ServiceProvider


/**
 * Date : 2023/5/18
 */
@ServiceProvider("/service/provider")
class ServiceProviderImpl constructor() : IServiceProvider {

    constructor(string: String, int: Int, log: Long, string1: String) : this()

    private lateinit var context: Context

    override fun init(context: Context) {
        this.context = context
        Log.e("ServiceProvider", "ServiceProvider init")
    }

    override fun message(): String {
        return "ServiceProvider message:$context "
    }
}