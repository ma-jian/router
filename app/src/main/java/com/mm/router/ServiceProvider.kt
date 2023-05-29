package com.mm.router

import android.content.Context
import android.util.Log
import com.mm.annotation.ServiceProvider


/**
 * Date : 2023/5/18
 */
@ServiceProvider(returnType = IServiceProvider::class, params = [String::class, Int::class])
class ServiceProvider : IServiceProvider {
    private lateinit var context: Context

    override fun init(context: Context) {
        this.context = context
        Log.e("ServiceProvider", "ServiceProvider init")
    }

    override fun message(): String {
        return "ServiceProvider message:$context "
    }
}