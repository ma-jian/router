package com.mm.router

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.annotation.AnimRes
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentFactory
import androidx.fragment.app.FragmentManager
import com.mm.router.annotation.model.RouterMeta
import com.mm.router.annotation.model.RouterType
import com.mm.router.cache.RouterCache
import com.mm.router.interceptor.Interceptor
import com.mm.router.interceptor.InterceptorBuilder
import com.mm.router.interceptor.RealInterceptorChain
import com.mm.router.result.RouterResult
import com.mm.router.result.RouterResultBuilder
import com.mm.router.result.RouterResultCallback
import java.io.Serializable
import java.lang.reflect.Type

/**
 *
 * 提供链式调用API，支持参数设置、拦截器处理、多种跳转方式
 */
class RouterBuilder(
    activity: FragmentActivity?,
    fragment: Fragment?,
    intent: Intent,
    meta: RouterMeta,
    cache: RouterCache,
) {
    private lateinit var componentActivity: FragmentActivity
    private var fragment: Fragment? = null
    private var intent: Intent
    private var meta: RouterMeta
    private var cache: RouterCache

    // 动画相关属性
    private var activityOptions: ActivityOptionsCompat? = null

    companion object {
        private const val FRAGMENT_TAG = "RouterFragment"
    }

    init {
        if (activity != null) {
            this.componentActivity = activity
        }
        // activity and fragment must not be null at same time
        if (activity == null && fragment != null) {
            componentActivity = fragment.requireActivity()
        }
        this.fragment = fragment
        this.intent = intent
        this.meta = meta
        this.cache = cache
    }

    private val fragmentManager: FragmentManager
        get() {
            return fragment?.childFragmentManager ?: componentActivity.supportFragmentManager
        }

    private val routerFragment: RouterFragment
        get() {
            val existedFragment = fragmentManager.findFragmentByTag(FRAGMENT_TAG)
            return if (existedFragment != null) {
                existedFragment as RouterFragment
            } else {
                val routerFragment = RouterFragment()
                fragmentManager.beginTransaction().add(routerFragment, FRAGMENT_TAG)
                    .commitNowAllowingStateLoss()
                routerFragment
            }
        }

    internal fun removeRouterFragment() {
        val existedFragment = fragmentManager.findFragmentByTag(FRAGMENT_TAG)
        if (existedFragment != null) {
            fragmentManager.beginTransaction().remove(existedFragment).commitAllowingStateLoss()
        }
    }

    /**
     * @param bundle new bundle with intent
     */
    fun withBundle(bundle: Bundle?): RouterBuilder {
        if (bundle != null) {
            intent.putExtras(bundle)
        }
        return this
    }

    /**
     * Inserts an String value into the mapping of this Bundle
     * @param key
     * @param value
     */
    fun withString(key: String, value: String?): RouterBuilder {
        intent.putExtra(key, value)
        return this
    }

    /**
     * Inserts an Array value into the mapping of this Bundle
     * @param key
     * @param value
     */
    fun withArray(key: String, value: Array<*>): RouterBuilder {
        intent.putExtra(key, value)
        return this
    }

    /**
     * Inserts an Int value into the mapping of this Bundle
     * @param key
     * @param value
     */
    fun withInt(key: String, value: Int): RouterBuilder {
        intent.putExtra(key, value)
        return this
    }

    /**
     * Inserts an Boolean value into the mapping of this Bundle
     * @param key
     * @param value
     */
    fun withBoolean(key: String, value: Boolean): RouterBuilder {
        intent.putExtra(key, value)
        return this
    }

    /**
     * Inserts an ArrayList<String> values into the mapping of this Bundle
     * @param key
     * @param value
     */
    fun withStringArrayList(key: String, value: ArrayList<String>?): RouterBuilder {
        intent.putStringArrayListExtra(key, value)
        return this
    }

    /**
     * Inserts a Parcelable value into the mapping of this Bundle
     * @param key
     * @param value
     */
    fun withParcelable(key: String, value: Parcelable?): RouterBuilder {
        intent.putExtra(key, value)
        return this
    }

    /**
     * Inserts a List of Parcelable values into the mapping of this Bundle
     * @param key
     * @param value
     */
    fun withParcelableArrayList(key: String, value: ArrayList<Parcelable>): RouterBuilder {
        intent.putParcelableArrayListExtra(key, value)
        return this
    }

    /**
     * Inserts a Serializable value into the mapping of this Bundle
     * @param key
     * @param value
     */
    fun withSerializable(key: String, value: Serializable?): RouterBuilder {
        intent.putExtra(key, value)
        return this
    }

    /**
     * 批量设置参数
     */
    @Suppress("KotlinConstantConditions", "UNCHECKED_CAST")
    fun withParams(params: Map<String, Any?>): RouterBuilder {
        params.forEach { (key, value) ->
            when (value) {
                is Char -> intent.putExtra(key, value)
                is String -> withString(key, value)
                is Int -> withInt(key, value)
                is Boolean -> withBoolean(key, value)
                is Long -> withLong(key, value)
                is Float -> withFloat(key, value)
                is Double -> withDouble(key, value)
                is Parcelable -> withParcelable(key, value)
                is Serializable -> withSerializable(key, value)
                is Array<*> -> {
                    when {
                        value.isArrayOf<Parcelable>() -> {
                            // 将Array转换为ArrayList
                            val arrayList = ArrayList<Parcelable>()
                            arrayList.addAll(value as Array<Parcelable>)
                            withParcelableArrayList(key, arrayList)
                        }
                        value.isArrayOf<Serializable>() -> withSerializable(key, value)
                        else -> withArray(key, value)
                    }
                }

                null -> intent.putExtra(key, null as String?)
                else -> Router.LogE("Unsupported parameter type for key: $key, type: ${value.javaClass.simpleName}")
            }
        }
        return this
    }

    /**
     * 添加Long类型参数
     */
    fun withLong(key: String, value: Long): RouterBuilder {
        intent.putExtra(key, value)
        return this
    }

    /**
     * 添加Float类型参数
     */
    fun withFloat(key: String, value: Float): RouterBuilder {
        intent.putExtra(key, value)
        return this
    }

    /**
     * 添加Double类型参数
     */
    fun withDouble(key: String, value: Double): RouterBuilder {
        intent.putExtra(key, value)
        return this
    }

    /**
     * 参数验证
     */
    fun validateParams(validator: (Bundle?) -> Boolean): RouterBuilder {
        if (!validator(intent.extras)) {
            throw IllegalArgumentException("Parameter validation failed for route: ${meta.path}")
        }
        return this
    }

    /**
     * Add additional flags to the intent
     * @param flags new flag
     */
    fun addFlags(flags: Int): RouterBuilder {
        intent.addFlags(flags)
        return this
    }

    /**
     * Set the general action to be performed.
     * @param action An action name
     */
    fun setAction(action: String): RouterBuilder {
        intent.action = action
        return this
    }

    // ========== 动画设置方法 ==========

    /**
     * 设置页面跳转动画
     * @param enterAnim 进入动画
     * @param exitAnim 退出动画
     */
    fun withTransition(@AnimRes enterAnim: Int, @AnimRes exitAnim: Int): RouterBuilder {
        this.activityOptions = ActivityOptionsCompat.makeCustomAnimation(componentActivity, enterAnim, exitAnim)
        return this
    }

    /**
     * 设置滑动进入动画（从右到左）
     */
    fun withSlideInTransition(): RouterBuilder {
        this.activityOptions = ActivityOptionsCompat.makeCustomAnimation(componentActivity,
            android.R.anim.slide_in_left, android.R.anim.slide_out_right)
        return this
    }

    /**
     * 设置淡入淡出动画
     */
    fun withFadeTransition(): RouterBuilder {
        this.activityOptions = ActivityOptionsCompat.makeCustomAnimation(componentActivity,
            android.R.anim.fade_in, android.R.anim.fade_out)
        return this
    }

    /**
     * 设置场景转换动画
     */
    fun withSceneTransition(): RouterBuilder {
        this.activityOptions = ActivityOptionsCompat.makeSceneTransitionAnimation(componentActivity)
        return this
    }

    /**
     * 设置共享元素转换动画
     * @param sharedElement 共享的View元素
     * @param sharedElementName 共享元素的名称
     */
    fun withSharedElementTransition(sharedElement: View, sharedElementName: String, ): RouterBuilder {
        this.activityOptions = ActivityOptionsCompat.makeSceneTransitionAnimation(componentActivity, sharedElement, sharedElementName)
        return this
    }

    /**
     * 设置缩放动画
     * @param source 动画起始的View
     * @param startX 起始X坐标
     * @param startY 起始Y坐标
     * @param startWidth 起始宽度
     * @param startHeight 起始高度
     */
    fun withScaleTransition(source: View, startX: Int, startY: Int, startWidth: Int, startHeight: Int, ): RouterBuilder {
        this.activityOptions = ActivityOptionsCompat.makeScaleUpAnimation(source, startX, startY, startWidth, startHeight)
        return this
    }

    /**
     * 设置缩略图缩放动画
     * @param source 动画起始的View
     * @param thumbnail 缩略图Bitmap
     * @param startX 起始X坐标
     * @param startY 起始Y坐标
     */
    fun withThumbnailTransition(source: View, thumbnail: Bitmap, startX: Int, startY: Int, ): RouterBuilder {
        this.activityOptions = ActivityOptionsCompat.makeThumbnailScaleUpAnimation(source, thumbnail, startX, startY)
        return this
    }

    /**
     * 禁用动画
     */
    fun withNoAnimation(): RouterBuilder {
        this.activityOptions = ActivityOptionsCompat.makeCustomAnimation(componentActivity, 0, 0)
        return this
    }

    /**
     * 设置多个共享元素转换动画
     * @param sharedElements 共享元素对（View到名称的映射）
     */
    fun withSharedElementsTransition(vararg sharedElements: Pair<View, String>): RouterBuilder {
        this.activityOptions = ActivityOptionsCompat.makeSceneTransitionAnimation(
            componentActivity,
            *sharedElements.map { androidx.core.util.Pair(it.first, it.second) }.toTypedArray()
        )
        return this
    }

    /**
     * 设置任务栈转换动画
     */
    fun withTaskStackTransition(): RouterBuilder {
        this.activityOptions = ActivityOptionsCompat.makeTaskLaunchBehind()
        return this
    }

    /**
     * 直接设置ActivityOptionsCompat
     */
    fun withActivityOptions(options: ActivityOptionsCompat): RouterBuilder {
        this.activityOptions = options
        return this
    }

    /**
     * get the provider by type
     * @param args 构造参数
     * @return 返回服务实体类
     */
    fun <T> doProvider(vararg args: Any): T? {
        return distributeRouter(meta, null, *args)
    }

    /**
     * 路由跳转到指定页面并返回统一结果,支持页面拦截降级处理
     * 支持成功、失败、拦截等状态
     */
    fun navigationResult(resultBuilder: (RouterResultBuilder) -> Unit) {
        val builder = RouterResultBuilder().also(resultBuilder)
        val callback = builder.build()

        interceptorRouter { interceptorBuilder ->
            interceptorBuilder.proceed {
                Router.LogW("RouterBuilder proceed: ${this.path}")
                try {
                  distributeRouter(this, { result -> callback.onResult(result) }, arrayOf<Any>()) ?: false
                } catch (e: Exception) {
                    e.printStackTrace()
                    callback.onResult(RouterResult.Failure(e, meta.path,intent.extras))
                }
            }
            interceptorBuilder.interrupt { reason, inceptorName ->
                Router.LogW("RouterBuilder interrupt path: ${meta.path};reason: $reason;name: $inceptorName")
                callback.onResult(RouterResult.Intercepted(meta.path, inceptorName, reason))
            }
        }
    }

    /**
     * 路由跳转到指定页面
     *
     * execute route and jump to the specified page
     */
    fun navigation() = interceptorRouter {
        it.proceed { val bool = distributeRouter(this, null, arrayOf<Any>()) ?: false }
    }

    /**
     * 执行路由跳转到指定页面并返回结果
     *
     * execute the route, jump to the specified page and return the result
     * @param callback the activity result callback
     */
    fun navigation(callback: ActivityResultCallback<ActivityResult>) = interceptorRouter {
        val callbackResult = RouterResultBuilder().also { builder->
            builder.onSuccess { result ->
                callback.onActivityResult(result.result)
            }
        }.build()
        it.proceed { distributeRouter<Boolean>(meta, callbackResult,arrayOf<Any>()) ?: false }
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> distributeRouter(meta: RouterMeta, callback: RouterResultCallback?, vararg args: Any, ): T? {
        Router.LogW("RouterBuilder distributeRouter: ${meta.path}")
        when (meta.type) {
            RouterType.ACTIVITY, RouterType.SYSTEM_ACTIVITY -> {
                try {
                    return intent.let {
                        val resultCallback = ActivityResultCallback<ActivityResult> { result ->
                            callback?.onResult(RouterResult.Success(result, meta.path))
                            removeRouterFragment()
                        }
                        filterIntent(it, resultCallback)
                    } as T
                } catch (e: ActivityNotFoundException) {
                    callback?.onResult(RouterResult.Failure(e, meta.path,intent.extras))
                } catch (e: Exception) {
                    callback?.onResult(RouterResult.Failure(e, meta.path,intent.extras))
                }
            }

            RouterType.SERVICE -> {
                try {
                    return intent as T
                } catch (e: Exception) {
                    throw IllegalArgumentException("The service return type is fixed to the intent type")
                }
            }

            RouterType.FRAGMENT -> {
                if (meta.destination == null) return null
                val clazz = FragmentFactory.loadFragmentClass(
                    componentActivity.classLoader,
                    meta.destination!!.name
                )
                val f = clazz.getConstructor().newInstance()
                f.arguments = intent.extras
                try {
                    return f as T
                } catch (e: Exception) {
                    throw IllegalArgumentException("The current path is of type fragment, check whether the generic parameters are correct")
                }
            }

            RouterType.PROVIDER -> {
                if (meta.destination == null) return null
                try {
                    return meta.destination!!.declaredConstructors.find { it.parameterTypes.size == args.size }
                        ?.let {
                            it.isAccessible = true
                            val provider = it.newInstance(*args)
                            if (provider is IProvider) {
                                provider.init(componentActivity)
                            }
                            provider as T
                        } ?: throw IllegalArgumentException("Can`t find this Constructor of class [${meta.destination}],args:[${args.joinToString { it.toString() }}]")
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            RouterType.INTERCEPTOR -> {
                return meta.destination!!.declaredConstructors.find { it.parameterTypes.size == args.size }
                    ?.let {
                        it.isAccessible = true
                        val interceptor = it.newInstance(*args)
                        interceptor as T
                    }
            }

            RouterType.UNKNOWN -> callback?.onResult(RouterResult.Failure(Exception("The current route path was not found."),
                meta.path,intent.extras))
        }
        return null
    }

    private fun filterIntent(intent: Intent, callback: ActivityResultCallback<ActivityResult>, ): Boolean {
        var result = true
        if (meta.type == RouterType.ACTIVITY) {
            // 统一使用ActivityOptions进行动画跳转
            result = routerFragment.navigation(intent, callback, activityOptions)
        } else if (meta.type == RouterType.SYSTEM_ACTIVITY) {
            when (meta.path) {
                Router.Path.ACTION_CONTENT -> routerFragment.navigationContent(intent, callback)

                Router.Path.ACTION_MULTI_CONTENT -> routerFragment.navigationMultipleContent(intent) { list ->
                    val intent1 = Intent()
                    list?.let {
                        val data = arrayListOf<Uri>()
                        data.addAll(it)
                        intent1.putExtra("data", data)
                    }
                    val activityResult = ActivityResult(if (list != null) Activity.RESULT_OK else Activity.RESULT_CANCELED, intent1)
                    callback.onActivityResult(activityResult)
                }

                Router.Path.ACTION_TAKE_PIC_PREVIEW -> routerFragment.navigationTakePicPreview { bitmap ->
                    val intent1 = Intent()
                    bitmap?.let { intent1.putExtra("data", it) }
                    val activityResult = ActivityResult(if (bitmap != null) Activity.RESULT_OK else Activity.RESULT_CANCELED, intent1)
                    callback.onActivityResult(activityResult)
                }

                Router.Path.ACTION_TAKE_PICTURE -> routerFragment.navigationTakePicture(intent, callback)

                Router.Path.ACTION_TAKE_VIDEO -> routerFragment.navigationTakeVideo(intent, callback)

                Router.Path.ACTION_PICK_CONTACT -> routerFragment.navigationPickContact(callback)

                Router.Path.ACTION_MAP -> routerFragment.openMap(intent, callback)

                Router.Path.ACTION_CALL_DIAL -> routerFragment.openCall(intent, callback)

                Router.Path.ACTION_SEND_SMS -> routerFragment.sendSms(intent, callback)

                Router.Path.ACTION_SHARE -> routerFragment.sendShare(intent, callback)

                Router.Path.ACTION_MARKET -> routerFragment.openMarket(intent, callback)

                Router.Path.ACTION_SETTINGS -> routerFragment.openSettings(intent, callback)

                else -> result = false
            }
        }
        return result
    }

    /**
     * 执行拦截器链
     */
    private fun interceptorRouter(interceptorBuilder: (InterceptorBuilder) -> Unit) {
        try {
            // 过滤适用的拦截器
            val filter = Router.interceptors.filter { (key, _) ->
                meta.interceptors.isEmpty() || meta.interceptors.contains(key)
            }
            Router.LogW("RouterBuilder interceptorRouter filter: $filter")

            if (filter.isEmpty()) {
                val builder = InterceptorBuilder().also(interceptorBuilder)
                builder.proceed?.invoke(meta)
                return
            }

            // 创建拦截器实例，优化：使用双重检查锁定避免重复创建
            val interceptors = filter.values
                .sortedByDescending { it.priority }
                .mapNotNull { interceptorMeta ->
                    createInterceptorInstance(interceptorMeta)
                }

            Router.LogW("RouterBuilder interceptorRouter interceptors: $interceptors")
            if (interceptors.isEmpty()) {
                val builder = InterceptorBuilder().also(interceptorBuilder)
                builder.proceed?.invoke(meta)
                return
            }

            val builder = InterceptorBuilder().also(interceptorBuilder)
            RealInterceptorChain(meta, interceptors, 0, builder).proceed(meta, intent)
        } catch (e: Exception) {
            Router.LogE("Interceptor chain execution failed for path: ${meta.path}", e)
        }
    }

    /**
     * 创建拦截器实例
     */
    private fun createInterceptorInstance(interceptorMeta: RouterMeta): Interceptor? {
        return try {
            // 尝试从缓存获取拦截器实例
            cache.getInterceptor(interceptorMeta.path) ?: synchronized(this) {
                // 双重检查锁定
                cache.getInterceptor(interceptorMeta.path) ?: run {
                    val destination = interceptorMeta.destination
                        ?: throw IllegalArgumentException("Interceptor destination is null for path: ${interceptorMeta.path}")

                    val constructor = destination.getDeclaredConstructor()
                    constructor.isAccessible = true
                    val interceptor = constructor.newInstance() as Interceptor

                    // 缓存拦截器实例
                    cache.putInterceptor(interceptorMeta.path, interceptor)
                    interceptor
                }
            }
        } catch (e: Exception) {
            Router.LogE("Failed to create interceptor: ${interceptorMeta.path}", e)
            null
        }
    }

    /**
     * generic types error
     */
    private fun throwGeneric(action: String, type: Type?, error: Type?) {
        throw IllegalArgumentException("The current action $action generic type should be ${type},but now is $error")
    }
}