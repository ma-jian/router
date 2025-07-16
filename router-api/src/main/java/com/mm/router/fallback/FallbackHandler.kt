package com.mm.router.fallback

import android.os.Bundle

/**
 * 降级接口
 * @since 1.1
 */
interface FallbackHandler {
    fun handleFallback(path: String, bundle: Bundle? = null)
}