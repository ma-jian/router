package com.mm.router.compiler.inter

import com.mm.router.compiler.BaseAbstractProcessor
import javax.annotation.processing.RoundEnvironment

/**
 * 注解处理器接口
 */
interface IProcessor {
    // System interface
    companion object {
        const val ACTIVITY = "android.app.Activity"
        const val FRAGMENT_X = "androidx.fragment.app.Fragment"
        const val FRAGMENT = "android.app.Fragment"
        const val SERVICE = "android.app.Service"
        const val ISYRINGE = "com.mm.router.ISyringe"
        const val IPROVIDER = "com.mm.router.IProvider"
        const val INTERCEPTOR = "com.mm.router.interceptor.Interceptor"
    }

    fun process(roundEnv: RoundEnvironment, abstractProcessor: BaseAbstractProcessor)
}