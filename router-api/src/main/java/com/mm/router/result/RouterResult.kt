package com.mm.router.result

import android.os.Bundle
import androidx.activity.result.ActivityResult
import com.mm.router.Router

/**
 * 路由执行结果封装
 * 统一管理路由的成功、失败、拦截等状态
 * @since 1.1
 */
sealed class RouterResult {

    /**
     * 路由执行成功
     */
    data class Success(val result: ActivityResult, val path: String) : RouterResult()

    /**
     * 路由被拦截
     */
    data class Intercepted(val path: String, val interceptorName: String, val reason: String? = null) : RouterResult()

    /**
     * 路由执行失败
     */
    data class Failure(val error: Exception, val path: String,val bundle: Bundle?) : RouterResult()
}

/**
 * 路由结果回调接口
 *
 * 统一处理所有路由执行结果
 */
fun interface RouterResultCallback {

    /**
     * 处理路由结果
     *
     * @param result 路由执行结果
     */
    fun onResult(result: RouterResult)
}

/**
 * 路由结果构建器
 *
 * 提供DSL风格的结果处理配置
 */
class RouterResultBuilder {

    private var onSuccess: ((RouterResult.Success) -> Unit)? = null
    private var onIntercepted: ((RouterResult.Intercepted) -> Unit)? = null
    private var onFailure: ((RouterResult.Failure) -> Boolean)? = null

    /**
     * 配置成功回调
     */
    fun onSuccess(action: (RouterResult.Success) -> Unit) {
        this.onSuccess = action
    }

    /**
     * 配置拦截回调
     */
    fun onIntercepted(action: (RouterResult.Intercepted) -> Unit) {
        this.onIntercepted = action
    }

    /**
     * 配置失败回调
     */
    fun onFailure(action: (RouterResult.Failure) -> Boolean) {
        this.onFailure = action
    }

    /**
     * 构建结果回调
     */
    internal fun build(): RouterResultCallback {
        return RouterResultCallback { result ->
            when (result) {
                is RouterResult.Success -> onSuccess?.invoke(result)
                is RouterResult.Intercepted -> onIntercepted?.invoke(result)
                is RouterResult.Failure -> {
                    Router.LogE("路由执行失败：${result.error.message}")
                    val invoke = onFailure?.invoke(result) ?: false
                    if (!invoke && Router.enableFallback && Router.fallbackHandler != null) {
                        Router.fallbackHandler?.handleFallback(result.path,result.bundle)
                    }
                }
            }
        }
    }
}
