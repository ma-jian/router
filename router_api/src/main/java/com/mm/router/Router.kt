package com.mm.router

import android.util.Log
import androidx.annotation.StringDef
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.mm.annotation.model.RouterMeta

/**
 * Created by : m
 *
 * 自定义url路由 采用 Result Api 的方式打开指定页面
 *
 * 规则：
 * scheme  host  path  params
 *
 * app://app.com/login?name={name}&age={age}
 *
 * 1、注册添加规则 [Router.addRouterRule]
 *
 * 2、打开指定的url [RouterMediator.open]
 *
 * 3、支持通过 [RouterMediator.open] 打开非路由管理页面
 *
 * 4、支持通过 [RouterMediator.open] 打开intent
 *
 * 5、默认提供的系统页面 [RouterMediator.open]
 *
 * @since 1.0 Activity 跳转升级为 Activity Result API 的方式
 */
object Router {
    /**
     * Router默认提供系统功能
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
        Path.ACTION_SEND_EMAIL,
        Path.ACTION_MAP,
        Path.ACTION_CALL_DIAL,
        Path.ACTION_SEND_SMS,
        Path.ACTION_SHARE,
        Path.ACTION_MARKET,
        Path.ACTION_SETTINGS,
    )
    annotation class Path {
        companion object {
            const val ACTION_CONTENT = "action_content" //单选相册视频返回已选择Uri ; image/\* video/\*
            const val ACTION_MULTI_CONTENT = "action_multi_content" //多选相册视频返回已选择列表List<Uri> image/\* video/\*
            const val ACTION_TAKE_PIC_PREVIEW = "action_take_pic_preview" //拍照预览 返回Bitmap图
            const val ACTION_TAKE_PICTURE = "action_take_picture" //拍照预览并保存至文件 返回 Boolean
            const val ACTION_TAKE_VIDEO = "action_take_video"//拍摄视频并保存至文件 返回bitmap图
            const val ACTION_PICK_CONTACT = "action_pick_contact" //打开通讯录 返回Uri
            const val ACTION_SEND_EMAIL = "action_send_email" //发送邮件
            const val ACTION_MAP = "action_map" //打开地图
            const val ACTION_CALL_DIAL = "action_call_dial" //打开电话拨号
            const val ACTION_SEND_SMS = "action_send_sms" //发送短信
            const val ACTION_SHARE = "action_share" //系统分享
            const val ACTION_MARKET = "action_market" //应用市场
            const val ACTION_SETTINGS = "action_settings" //打开系统页面
        }
    }

    internal const val TAG = "Router_"

    /**
     * Route interception 路由拦截
     */
    internal var routerInterceptor: IRouterInterceptor? = null

    /**
     * A collection of storage rules
     * url --- activity clazz 映射
     */
    internal val rules = HashMap<String, RouterMeta>()
    internal val allRuleKeys: MutableSet<String> = HashSet()

    /**
     * Init Router to make everything prepare to work.
     *
     * @param activity An instance of FragmentActivity
     */
    @JvmStatic
    fun init(activity: FragmentActivity): RouterMediator {
        "".isBlank()
        return RouterMediator(activity)
    }

    /**
     * Init Router to make everything prepare to work.
     *
     * @param fragment An instance of Fragment
     */
    @JvmStatic
    fun init(fragment: Fragment): RouterMediator {
        return RouterMediator(fragment)
    }

    /**
     * Init Router to make everything prepare to work.
     * user current task topActivity
     */
    @JvmStatic
    fun init(): RouterMediator {
        val delegate = RouterActivityLifecycle.delegate.get()
        if (delegate?.activity != null) {
            return if (delegate.activity is FragmentActivity) {
                RouterMediator((delegate.activity as FragmentActivity))
            } else {
                throw IllegalArgumentException(
                    "The current Stack Top Activity:[" + delegate.activity!!.javaClass.name + "] Not extends FragmentActivity," +
                            " Use a different initialization method Or modify the current ${delegate.activity!!.javaClass.name} extends FragmentActivity"
                )
            }
        }
        throw IllegalArgumentException("ActivityDelegate:[" + RouterActivityLifecycle::class.java.name + "] Not initialized")
    }

    /**
     * add router rules for annotation [com.mm.annotation.RouterPath]
     *
     * @param creator spi RouterCreator
     */
    @JvmStatic
    fun addRouterRule(creator: IRouterRulesCreator) {
        creator.initRule(rules)
        allRuleKeys.addAll(rules.keys)
    }

    /**
     * add routerInterceptor and custom routing rules
     *
     * @param interceptor router interceptor
     */
    @JvmStatic
    fun addRuleInterceptor(interceptor: IRouterInterceptor?) {
        routerInterceptor = interceptor
    }

    @JvmStatic
    internal fun LogE(s: String) {
        Log.e(TAG, s)
    }
}