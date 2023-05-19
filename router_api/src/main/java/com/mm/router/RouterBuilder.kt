package com.mm.router

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentFactory
import androidx.fragment.app.FragmentManager
import com.mm.annotation.model.RouterMeta
import com.mm.annotation.model.RouterType
import java.io.Serializable
import java.lang.reflect.Type

/**
 * Created by : m
 * Date : 2022/3/23
 * Process the data and open the specified page with result API
 * 处理数据并利用 Result Api 打开指定页面
 * @since 1.0
 */

class RouterBuilder(
    activity: FragmentActivity?, fragment: Fragment?, intent: Intent, meta: RouterMeta
) {
    private lateinit var componentActivity: FragmentActivity
    private var fragment: Fragment? = null
    private var intent: Intent
    private var bundle: Bundle = Bundle()

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
            this.bundle = bundle
        }
        return this
    }

    /**
     * Inserts an String value into the mapping of this Bundle
     * @param key
     * @param value
     */
    fun withString(key: String, value: String?): RouterBuilder {
        bundle.putString(key, value)
        return this
    }

    /**
     * Inserts an String Array value into the mapping of this Bundle
     * @param key
     * @param value
     */
    fun withStringArray(key: String, value: Array<String>?): RouterBuilder {
        bundle.putStringArray(key, value)
        return this
    }

    /**
     * Inserts an Int value into the mapping of this Bundle
     * @param key
     * @param value
     */
    fun withInt(key: String, value: Int): RouterBuilder {
        bundle.putInt(key, value)
        return this
    }

    /**
     * Inserts an Boolean value into the mapping of this Bundle
     * @param key
     * @param value
     */
    fun withBoolean(key: String, value: Boolean): RouterBuilder {
        bundle.putBoolean(key, value)
        return this
    }

    /**
     * Inserts an ArrayList<String> values into the mapping of this Bundle
     * @param key
     * @param value
     */
    fun withStringArrayList(key: String, value: ArrayList<String>?): RouterBuilder {
        bundle.putStringArrayList(key, value)
        return this
    }

    /**
     * Inserts a Parcelable value into the mapping of this Bundle
     * @param key
     * @param value
     */
    fun withParcelable(key: String, value: Parcelable?): RouterBuilder {
        bundle.putParcelable(key, value)
        return this
    }

    /**
     * Inserts a List of Parcelable values into the mapping of this Bundle
     * @param key
     * @param value
     */
    fun withParcelableArrayList(key: String, value: ArrayList<out Parcelable?>?): RouterBuilder {
        bundle.putParcelableArrayList(key, value)
        return this
    }

    /**
     * Inserts a Serializable value into the mapping of this Bundle
     * @param key
     * @param value
     */
    fun withSerializable(key: String, value: Serializable?): RouterBuilder {
        bundle.putSerializable(key, value)
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
     * Launch the navigation by type
     * @param args 构造参数
     * @return 返回
     */
    fun <T> doProvider(vararg args: Any): T? {
        return distributeRouter(null, args)
    }

    /**
     * 路由跳转到指定页面
     * execute route and jump to the specified page
     */
    fun navigation(): Boolean = distributeRouter(null, arrayOf<Any>()) ?: false

    /**
     * 执行路由跳转到指定页面并返回结果
     * execute the route, jump to the specified page and return the result
     * @param callback the activity result callback
     */
    fun navigation(callback: ActivityResultCallback<ActivityResult>): Boolean =
        distributeRouter<Boolean>(callback, arrayOf<Any>()) ?: false

    private fun <T> distributeRouter(callback: ActivityResultCallback<ActivityResult>?, vararg args: Any): T? {
        when (meta.type) {
            RouterType.ACTIVITY, RouterType.SYSTEM_ACTIVITY -> {
                try {
                    return intent.let {
                        val resultCallback = ActivityResultCallback<ActivityResult> { result ->
                            callback?.onActivityResult(result)
                            removeRouterFragment()
                        }
                        filterIntent(it, resultCallback)
                    } as T
                } catch (e: Exception) {
                    throw IllegalArgumentException("The activity return type is fixed to the bool type")
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

            else -> {
                if (meta.destination == null) return null
                val any = if (meta.params.isEmpty()) {
                    val constructor = meta.destination!!.getDeclaredConstructor()
                    constructor.isAccessible = true
                    constructor.newInstance()
                } else {
                    val constructor = meta.destination!!.getDeclaredConstructor(*meta.params)
                    constructor.isAccessible = true
                    constructor.newInstance(args)
                }
                try {
                    if (any is IProvider) {
                        any.init(componentActivity)
                    }
                    return any as T
                } catch (e: Exception) {
                    throw IllegalArgumentException("The current path is of type${intent.action}, check whether the generic parameters are correct")
                }
            }
        }
    }


    private fun filterIntent(intent: Intent, callback: ActivityResultCallback<ActivityResult>): Boolean {
        intent.putExtras(bundle)
        var result = true
        if (meta.type == RouterType.ACTIVITY) {
            result = routerFragment.navigation(intent, callback)
        } else if (meta.type == RouterType.SYSTEM_ACTIVITY) {
            when (meta.path) {
                Router.Path.ACTION_CONTENT -> routerFragment.navigationContent(intent) { uri ->
                    val intent1 = Intent()
                    uri?.let {
                        intent1.data = it
                    }
                    val activityResult =
                        ActivityResult(if (uri != null) Activity.RESULT_OK else Activity.RESULT_CANCELED, intent1)
                    callback.onActivityResult(activityResult)
                }

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

                Router.Path.ACTION_TAKE_PICTURE -> routerFragment.navigationTakePicture(intent) {
                    callback.onActivityResult(
                        ActivityResult(
                            if (it) Activity.RESULT_OK else Activity.RESULT_CANCELED, null
                        )
                    )
                }

                Router.Path.ACTION_TAKE_VIDEO -> routerFragment.navigationTakeVideo(intent) { bitmap ->
                    val intent1 = Intent()
                    bitmap?.let {
                        intent1.putExtra("data", it)
                    }
                    val activityResult =
                        ActivityResult(if (bitmap != null) Activity.RESULT_OK else Activity.RESULT_CANCELED, intent1)
                    callback.onActivityResult(activityResult)
                }

                Router.Path.ACTION_PICK_CONTACT -> routerFragment.navigationPickContact { uri ->
                    val intent1 = Intent()
                    uri?.let {
                        intent1.data = uri
                    }
                    val activityResult =
                        ActivityResult(if (uri != null) Activity.RESULT_OK else Activity.RESULT_CANCELED, intent1)
                    callback.onActivityResult(activityResult)
                }

                Router.Path.ACTION_SEND_EMAIL -> routerFragment.sendEmail(intent, callback)

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
     * generic types error
     */
    private fun throwGeneric(action: String, type: Type?, error: Type?) {
        throw IllegalArgumentException("The current action $action generic type should be ${type},but now is $error")
    }
}