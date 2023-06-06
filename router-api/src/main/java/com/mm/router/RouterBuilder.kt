package com.mm.router

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentFactory
import androidx.fragment.app.FragmentManager
import com.mm.router.annotation.model.RouterMeta
import com.mm.router.annotation.model.RouterType
import com.mm.router.interceptor.ActivityResultBuilder
import com.mm.router.interceptor.Interceptor
import com.mm.router.interceptor.InterceptorBuilder
import com.mm.router.interceptor.RealInterceptorChain
import java.io.Serializable
import java.lang.reflect.Type

/**
 *
 * Process the data and open the specified page with result API
 *
 * 处理数据并利用 Result Api 打开指定页面
 * @since 1.0
 */
class RouterBuilder(
    activity: FragmentActivity?, fragment: Fragment?, intent: Intent, meta: RouterMeta
) {
    private lateinit var componentActivity: FragmentActivity
    private var fragment: Fragment? = null
    private var intent: Intent
    private var meta: RouterMeta

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
                fragmentManager.beginTransaction().add(routerFragment, FRAGMENT_TAG).commitNowAllowingStateLoss()
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
     * Inserts an String Array value into the mapping of this Bundle
     * @param key
     * @param value
     */
    fun withStringArray(key: String, value: Array<String>?): RouterBuilder {
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
        intent.putExtra(key, value)
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
    fun withParcelableArrayList(key: String, value: ArrayList<out Parcelable?>?): RouterBuilder {
        intent.putExtra(key, value)
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

    /**
     * get the provider by type
     * @param args 构造参数
     * @return 返回服务实体类
     */
    fun <T> doProvider(vararg args: Any): T? {
        return distributeRouter(meta, null, *args)
    }

    /**
     * 路由跳转到指定页面并返回拦截状态
     */
    fun navigationResult(resultBuilder: (ActivityResultBuilder) -> Unit) = interceptorRouter {
        val also = ActivityResultBuilder().also(resultBuilder)
        it.proceed { distributeRouter(this, { result -> also.arrivaled?.invoke(result) }, arrayOf<Any>()) ?: false }
        it.interrupt { also.interrupt?.invoke() }
    }

    /**
     * 路由跳转到指定页面
     *
     * execute route and jump to the specified page
     */
    fun navigation() = interceptorRouter {
        it.proceed { distributeRouter(this, null, arrayOf<Any>()) ?: false }
    }

    /**
     * 执行路由跳转到指定页面并返回结果
     *
     * execute the route, jump to the specified page and return the result
     * @param callback the activity result callback
     */
    fun navigation(callback: ActivityResultCallback<ActivityResult>) = interceptorRouter {
        it.proceed { distributeRouter<Boolean>(this, callback, arrayOf<Any>()) }
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> distributeRouter(meta: RouterMeta, callback: ActivityResultCallback<ActivityResult>?, vararg args: Any): T? {
        when (meta.type) {
            RouterType.ACTIVITY, RouterType.SYSTEM_ACTIVITY -> {
                return try {
                    intent.let {
                        val resultCallback = ActivityResultCallback<ActivityResult> { result ->
                            callback?.onActivityResult(result)
                            removeRouterFragment()
                        }
                        filterIntent(it, resultCallback)
                    } as T
                } catch (e: ActivityNotFoundException) {
                    e.printStackTrace()
                    false as T
                } catch (e: Exception) {
                    throw IllegalArgumentException("The activity return type [$] is fixed to the bool type")
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
                val clazz = FragmentFactory.loadFragmentClass(componentActivity.classLoader, meta.destination!!.name)
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
                return try {
                    return meta.destination!!.declaredConstructors.find { it.parameterTypes.size == args.size }?.let {
                        it.isAccessible = true
                        val provider = it.newInstance(*args)
                        if (provider is IProvider) {
                            provider.init(componentActivity)
                        }
                        provider as T
                    }
                        ?: throw IllegalArgumentException("can`t find this Constructor of class [${meta.destination}],args:[${args.joinToString {it.toString()}}]")
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
            }

            RouterType.INTERCEPTOR -> {
                return meta.destination!!.declaredConstructors.find { it.parameterTypes.size == args.size }?.let {
                    it.isAccessible = true
                    val interceptor = it.newInstance(*args)
                    interceptor as T
                }
            }

            RouterType.UNKNOWN -> return null
        }
    }

    private fun filterIntent(intent: Intent, callback: ActivityResultCallback<ActivityResult>): Boolean {
        var result = true
        if (meta.type == RouterType.ACTIVITY) {
            result = routerFragment.navigation(intent, callback)
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
                    val activityResult =
                        ActivityResult(if (list != null) Activity.RESULT_OK else Activity.RESULT_CANCELED, intent1)
                    callback.onActivityResult(activityResult)
                }

                Router.Path.ACTION_TAKE_PIC_PREVIEW -> routerFragment.navigationTakePicPreview { bitmap ->
                    val intent1 = Intent()
                    bitmap?.let {
                        intent1.putExtra("data", it)
                    }
                    val activityResult =
                        ActivityResult(if (bitmap != null) Activity.RESULT_OK else Activity.RESULT_CANCELED, intent1)
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

                else -> {}
            }
        }
        return result
    }

    /**
     * 过滤当前路由拦截器
     */
    private fun interceptorRouter(interceptorBuilder: (InterceptorBuilder) -> Unit) {
        val filter = Router.interceptors.filter { meta.interceptors.isEmpty() || meta.interceptors.contains(it.key) }
        val list =
            filter.map { it.value }.sortedByDescending { it.priority }.map {
                val constructor = it.destination!!.getDeclaredConstructor()
                constructor.isAccessible = true
                constructor.newInstance() as Interceptor
            }
        RealInterceptorChain(meta, list, 0, InterceptorBuilder().also(interceptorBuilder)).proceed(this.meta, this.intent)
    }

    /**
     * generic types error
     */
    private fun throwGeneric(action: String, type: Type?, error: Type?) {
        throw IllegalArgumentException("The current action $action generic type should be ${type},but now is $error")
    }
}