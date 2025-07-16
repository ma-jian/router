package com.mm.router

import android.util.Log
import androidx.annotation.StringDef
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.mm.router.annotation.model.RouterMeta
import com.mm.router.cache.RouterCache
import com.mm.router.fallback.FallbackHandler
import java.util.concurrent.ConcurrentHashMap

/**
 * 路由框架 - 基于 ActivityResult API
 * 核心特性：
 * - 🚀 基于注解的路由注册，编译时生成路由表
 * - 🔄 拦截器系统，支持路由拦截和自定义处理
 * - 💉 自动依赖注入，简化参数传递
 * - 🏭 服务提供者模式，解耦模块间通信
 * - 📱 系统功能集成，统一API调用系统服务
 * - 🚨 错误处理和降级策略
 * - ⚡ 高性能缓存机制
 * - 🔒 线程安全设计
 * - 📝 调试日志输出
 *
 * 使用示例：
 * ```kotlin
 * // 基本路由跳转
 * Router.init(this).open("/user/detail").withString("userId", "123").navigation()
 *
 * // 带结果回调的跳转
 * Router.init(this).open("/user/edit").navigation { result ->
 *     if (result.resultCode == RESULT_OK) {
 *         // 处理返回结果
 *     }
 * }
 * // 带路由执行结果的跳转
 * Router.init().open("/user/mine").navigationResult {
 *      //路由执行完毕并返回信息
 *      it.onSuccess { result ->
 *
 *      }
 *      //路由被中断、通过在[Interceptor]中执行chain.interrupt()方法
 *      it.onIntercepted {
 *
 *      }
 *      //路由执行失败,返回值:true执行当前降级逻辑，false执行默认降级逻辑
 *      it.onFailure {
 *         false
 *      }
 *   }
 *
 * // 服务提供者获取
 * val userService = Router.init().open("/service/user").doProvider<UserService>()
 * ```
 *
 * @since 1.0 Activity 跳转升级为 ActivityResult API 的方式
 * @since 1.1 新增缓存、错误处理、降级策略
 */
object Router {
    /**
     * Router默认提供的系统功能
     *
     * Router.init(this).open(Router.Path.ACTION_CONTENT).navigation()
     */
    @Retention(AnnotationRetention.SOURCE)
    @StringDef(
        Path.ACTION_CONTENT,
        Path.ACTION_MULTI_CONTENT,
        Path.ACTION_TAKE_PIC_PREVIEW,
        Path.ACTION_TAKE_PICTURE,
        Path.ACTION_TAKE_VIDEO,
        Path.ACTION_PICK_CONTACT,
        Path.ACTION_MAP,
        Path.ACTION_CALL_DIAL,
        Path.ACTION_SEND_SMS,
        Path.ACTION_SHARE,
        Path.ACTION_MARKET,
        Path.ACTION_SETTINGS,
    )
    annotation class Path {
        companion object {
            const val ACTION_CONTENT = "action_content" //单选相册视频返回已选择Uri ; image\* video\*
            const val ACTION_MULTI_CONTENT = "action_multi_content" //多选相册视频返回已选择列表List<Uri> image\* video\*
            const val ACTION_TAKE_PIC_PREVIEW = "action_take_pic_preview" //拍照预览 返回Bitmap图
            const val ACTION_TAKE_PICTURE = "action_take_picture" //拍照预览并保存至文件 返回地址
            const val ACTION_TAKE_VIDEO = "action_take_video"//拍摄视频并保存至文件 返回视频地址
            const val ACTION_PICK_CONTACT = "action_pick_contact" //打开通讯录 返回Uri
            const val ACTION_MAP = "action_map" //打开地图
            const val ACTION_CALL_DIAL = "action_call_dial" //打开电话拨号
            const val ACTION_SEND_SMS = "action_send_sms" //发送短信
            const val ACTION_SHARE = "action_share" //系统分享
            const val ACTION_MARKET = "action_market" //应用市场
            const val ACTION_SETTINGS = "action_settings" //打开系统页面
        }
    }

    /**
     * 默认支持的系统跳转路径
     */
    internal val systemPath = setOf(
        Path.ACTION_CONTENT,
        Path.ACTION_MULTI_CONTENT,
        Path.ACTION_TAKE_PIC_PREVIEW,
        Path.ACTION_TAKE_PICTURE,
        Path.ACTION_TAKE_VIDEO,
        Path.ACTION_PICK_CONTACT,
        Path.ACTION_MAP,
        Path.ACTION_CALL_DIAL,
        Path.ACTION_SEND_SMS,
        Path.ACTION_SHARE,
        Path.ACTION_MARKET,
        Path.ACTION_SETTINGS
    )

    internal const val TAG = "Router_"

    /**
     * 路由规则存储
     */
    internal val rules = ConcurrentHashMap<String, RouterMeta>()

    /**
     * 所有路由路径的集合
     */
    internal val allRuleKeys = mutableSetOf<String>()

    /**
     * 路由拦截器存储
     */
    internal val interceptors = ConcurrentHashMap<String, RouterMeta>()

    /**
     * 路由缓存管理器
     */
    private val cache: RouterCache by lazy { RouterCache(100) }

    /**
     * 调试模式开关
     */
    @Volatile
    var debugMode: Boolean = false

    /**
     * 降级策略开关
     */
    @Volatile
    var enableFallback: Boolean = false

    //降级策略
    @Volatile
    internal var fallbackHandler: FallbackHandler? = null

    /**
     * 使用指定Activity初始化路由器
     */
    @JvmStatic
    fun init(activity: FragmentActivity): RouterMediator {
        return RouterMediator(activity, cache)
    }

    /**
     * 使用指定Fragment初始化路由器
     */
    @JvmStatic
    fun init(fragment: Fragment): RouterMediator {
        return RouterMediator(fragment, cache)
    }

    /**
     * 使用当前栈顶Activity初始化路由器
     */
    @JvmStatic
    fun init(): RouterMediator {
        val delegate = RouterActivityLifecycle.delegate.get()
        if (delegate?.activity != null) {
            if (delegate.activity is FragmentActivity) {
                return RouterMediator(delegate.activity as FragmentActivity, cache)
            } else {
                throw IllegalArgumentException(
                    "The current Stack Top Activity: [${delegate.activity!!.javaClass.name}] " +
                            "does not extend FragmentActivity."
                )
            }
        }
        throw IllegalArgumentException("ActivityDelegate:[${RouterActivityLifecycle::class.java.name}] Not initialized")
    }

    /**
     * 添加路由规则
     */
    @JvmStatic
    internal fun addRouterRule(creator: IRouterRulesCreator) {
        creator.initRule(rules, allRuleKeys)
    }

    /**
     * 添加路由拦截器
     */
    @JvmStatic
    internal fun addRouterInterceptors(interceptor: IRouterInterceptor) {
        interceptor.intercept(interceptors)
    }

    /**
     * 查找路由元数据
     */
    @JvmStatic
    internal fun findRouteMeta(path: String): RouterMeta? {
        // 首先尝试从缓存获取
        cache.getRouteMeta(path)?.let { return it }
        // 直接查找
        rules[path]?.let { meta ->
            cache.putRouteMeta(path, meta)
            return meta
        }
        return null
    }

    /**
     * 添加路由降级策略
     */
    @JvmStatic
    fun defaultFallback(handler: FallbackHandler) {
        this.fallbackHandler = handler
    }

    /**
     * 清除所有缓存
     */
    @JvmStatic
    fun clearCache() {
        cache.clear()
        LogW("Router cache cleared")
    }

    // ========== 日志方法 ==========
    @JvmStatic
    internal fun LogW(message: String) {
        if (debugMode) {
            Log.w(TAG, message)
        }
    }

    @JvmStatic
    internal fun LogE(message: String) {
        if (debugMode) {
            Log.e(TAG, message)
        }
    }

    @JvmStatic
    internal fun LogE(message: String, throwable: Throwable) {
        if (debugMode) {
            Log.e(TAG, message, throwable)
        }
    }
}