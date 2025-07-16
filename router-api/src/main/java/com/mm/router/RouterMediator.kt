package com.mm.router

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.text.TextUtils
import android.util.ArrayMap
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.mm.router.annotation.model.RouterMeta
import com.mm.router.annotation.model.RouterType
import com.mm.router.cache.RouterCache
import com.mm.router.service.AutowiredService

/**
 * 路由中间层
 * 负责处理具体的路由操作，保持高性能和可读性
 */
class RouterMediator {
    companion object {
        const val INTENT_KEY_ROUTE_URL = "router_route_url"
        const val INTENT_EXTRA_ROUTE_URL = "intent_extra_route_url"
    }

    private var activity: FragmentActivity? = null
    private var fragment: Fragment? = null
    private val cache: RouterCache

    /**
     * 使用Activity创建路由中介者
     */
    constructor(activity: FragmentActivity, cache: RouterCache) {
        this.activity = activity
        this.cache = cache
    }

    /**
     * 使用Fragment创建路由中介者
     */
    constructor(fragment: Fragment, cache: RouterCache) {
        this.fragment = fragment
        this.cache = cache
    }

    private val currentContext: FragmentActivity?
        get() {
            return fragment?.requireActivity() ?: activity
        }

    /**
     * 对当前FragmentActivity页面自动赋值
     */
    fun autoWired(activity: FragmentActivity) = autoService(activity)

    /**
     * 对当前Fragment页面自动赋值
     */
    fun autoWired(fragment: Fragment) = autoService(fragment)

    private fun autoService(any: Any) {
        val autowiredService = open("/router/service/autowired").doProvider<AutowiredService>()
        Router.LogW("RouterMediator autoService: $autowiredService")
        autowiredService?.init(currentContext!!)
        autowiredService?.autoWired(any)
    }

    /**
     * 打开指定Intent页面
     */
    fun open(intent: Intent): RouterBuilder {
        Router.LogW("RouterMediator open intent: $intent")
        return RouterBuilder(
            activity,
            fragment,
            intent,
            RouterMeta.build(RouterType.ACTIVITY, Class.forName(intent.component!!.className)),
            cache)
    }

    /**
     * 打开指定路径的页面
     */
    fun open(url: String): RouterBuilder {
        val meta = findRouterMeta(url)
        Router.LogW("RouterMediator open url: $url;meta: $meta")
        val intent = openLocalUrl(currentContext, meta)
        return RouterBuilder(activity, fragment, intent, meta, cache)
    }

    /**
     * 打开Activity页面或者获取ServiceProvider标记的接口
     */
    fun open(clazz: Class<*>): RouterBuilder {
        val meta = findRouterMeta(clazz)
        Router.LogW("RouterMediator open clazz: $clazz;meta: $meta")
        val intent = openLocalUrl(currentContext, meta)
        return RouterBuilder(activity, fragment, intent, meta, cache)
    }

    /**
     * 打开指定页面,支持从app后台启动
     *
     * will open activity, It can be opened from the app background
     * @param url an address to be opened
     */
    @SuppressLint("QueryPermissionsNeeded")
    @Suppress("DEPRECATION")
    fun openWithAppRunning(url: String): RouterBuilder {
        val meta = findRouterMeta(url)
        return currentContext?.let {
            if (isAppAlive(it) != 0) {
                val intent = openLocalUrl(currentContext, meta)
                RouterBuilder(activity, fragment, intent, meta, cache)
            } else {
                val launchIntent = it.packageManager.getLaunchIntentForPackage(it.packageName)
                if (launchIntent == null) {
                    val packageInfo = it.packageManager.getPackageInfo(it.packageName, 0)
                    val resolveIntent = Intent(Intent.ACTION_MAIN, null)
                    resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER)
                    resolveIntent.setPackage(packageInfo.packageName)
                    val apps = it.packageManager.queryIntentActivities(resolveIntent, 0)
                    val ri = apps.iterator().next()
                    if (ri != null) {
                        val packageName = ri.activityInfo.packageName
                        val className = ri.activityInfo.name
                        val intent = Intent(Intent.ACTION_MAIN)
                        intent.addCategory(Intent.CATEGORY_LAUNCHER)
                        intent.component = ComponentName(packageName, className)
                        intent.putExtra(INTENT_EXTRA_ROUTE_URL, url)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        RouterBuilder(activity, fragment, intent, RouterMeta.build(RouterType.ACTIVITY, Class.forName(className)), cache)
                    } else {
                        RouterBuilder(
                            activity,
                            fragment,
                            Intent(Intent.ACTION_MAIN),
                            RouterMeta.build(RouterType.ACTIVITY, null),
                            cache
                        )
                    }
                } else {
                    launchIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    launchIntent.putExtra(INTENT_EXTRA_ROUTE_URL, url)
                    RouterBuilder(activity, fragment, launchIntent, RouterMeta.build(RouterType.ACTIVITY, null), cache)
                }
            }
        } ?: RouterBuilder(
            activity,
            fragment,
            Intent(Intent.ACTION_VIEW),
            RouterMeta.build(RouterType.ACTIVITY, null),
            cache
        )
    }

    /**
     * 查找路由元数据
     */
    private fun findRouterMeta(url: String): RouterMeta {
        // 1. 检查是否为系统默认路径
        if (Router.systemPath.contains(url)) {
            return RouterMeta.build(RouterType.SYSTEM_ACTIVITY, url, null)
        }

        // 2. 使用Router的优化查找方法
        Router.findRouteMeta(url)?.let { return it }

        // 3. 兼容旧的查找逻辑
        val matchRuleKey = getMatchRuleKey(url)
        Router.rules[matchRuleKey]?.let { return it }

        return RouterMeta.build(RouterType.UNKNOWN, url,null)
    }

    /**
     * find [RouterMeta] by class
     */
    private fun findRouterMeta(clazz: Class<*>): RouterMeta {
        Router.rules.forEach { (_, value) ->
            value.destination?.let { destination ->
                if (clazz.isAssignableFrom(destination)) {
                    return value
                }
            }
        }
        return RouterMeta.build(RouterType.UNKNOWN, clazz)
    }


    /**
     * Open the specified URL of the local
     * @param meta data of the page
     */
    private fun openLocalUrl(context: Context?, meta: RouterMeta?): Intent {
        if (context == null || meta == null) return Intent(Intent.ACTION_VIEW)
        //1检查类型
        try {
            when (meta.type) {
                RouterType.ACTIVITY, RouterType.SERVICE -> {
                    // 2 匹配到已注册的url - class，构造intent
                    val intent = Intent(context, meta.destination)
                    intent.putExtra(INTENT_KEY_ROUTE_URL, meta.path)
                    if (context is Application) {
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    getAllQueryParameter(meta.path).forEach {
                        intent.putExtra(it.key, it.value)
                    }
                    return intent
                }

                RouterType.FRAGMENT, RouterType.PROVIDER -> {
                    return Intent(context, meta.destination).apply {
                        getAllQueryParameter(meta.path).forEach {
                            putExtra(it.key, it.value)
                        }
                    }
                }

                else -> {
                    val intent = meta.destination?.let { Intent(context, it) } ?: Intent()
                    return intent.apply {
                        getAllQueryParameter(meta.path).forEach {
                            putExtra(it.key, it.value)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return Intent(Intent.ACTION_VIEW)
    }

    /**
     * matching path rules
     * @param url path of page
     */
    private fun getMatchRuleKey(url: String?): String {
        if (url == null) return ""
        val checkUrl = url.toUri()
        val checkScheme = checkUrl.scheme
        val checkHost = checkUrl.host
        val checkPath = checkUrl.path
        val checkParameterNames = checkUrl.queryParameterNames
        for (ruleKey in Router.allRuleKeys) {
            val ruleUri = ruleKey.toUri()

            if (ruleUri.scheme?.equals(checkScheme, true) != true || ruleUri.host?.equals(
                    checkHost, true
                ) != true || ruleUri.path?.equals(checkPath, true) != true
            ) {
                continue
            }
            val queryParameterNames = ruleUri.queryParameterNames
            val ruleParameterNames: MutableSet<String> = HashSet(queryParameterNames)
            // queryParameterNames不可编辑
            val iterator = ruleParameterNames.iterator()
            while (iterator.hasNext()) {
                val key = iterator.next()
                // 如果是可选key，则从规则中移除
                if (key.startsWith("[") && key.endsWith("]")) {
                    iterator.remove()
                }
            }
            if (checkParameterNames.containsAll(ruleParameterNames)) {
                return ruleKey
            }
        }
        return url
    }

    /**
     * get all params
     * @param url
     */
    private fun getAllQueryParameter(url: String?): Map<String, String?> {
        val params: MutableMap<String, String?> = ArrayMap()
        if (TextUtils.isEmpty(url)) {
            return params
        }
        val parse = url!!.toUri()
        parse.queryParameterNames.forEach { key ->
            params[key] = parse.getQueryParameter(key)
        }
        return params
    }


    /**
     * Return app running status
     * @param context
     * @return int 1:前台 2:后台 0:不存在
     */
    @Suppress("DEPRECATION")
    private fun isAppAlive(context: Context): Int {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val runningTaskInfo = activityManager.getRunningTasks(10)
        val topActivity = runningTaskInfo[0].topActivity
        // it is at the top of the stack return 1
        if (isAppActivity(context.packageName, topActivity)) {
            return 1
        } else {
            // whether it is in the stack return 2
            for (i in 1 until runningTaskInfo.size) {
                val info = runningTaskInfo[i]
                if (isAppActivity(context.packageName, info.topActivity)) {
                    return 2
                }
            }
            return 0 // not found return 0
        }
    }

    /**
     * whether the current activity within this ths app、and excludes the umeng MfrMessageActivity
     * @param packageName app包名
     * @param componentName topActivity
     */
    private fun isAppActivity(packageName: String, componentName: ComponentName?): Boolean {
        return componentName?.packageName == packageName && !componentName.className.contains("MfrMessageActivity")
    }

}