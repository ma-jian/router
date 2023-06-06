package com.mm.router

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContract
import java.util.*


/**
 * 系统默认 Contract
 * @since 1.0
 */

class ResultContracts private constructor() {

    /**
     * 相册参数类型
     */
    class Contents {
        companion object {
            //传递参数 类型
            const val TYPE = "type"

            //传递参数 保存路径
            const val PATH = "path"
        }
    }

    /**
     * open map
     */
    class MapIntent : ActivityResultContract<Map<String, String>, ActivityResult>() {

        companion object {
            const val MAP_MODE = "map_mode" //导航方式
            const val DES_NAME = "dname" //终点名称

            //导航类型
            const val GAODE_MAP = "gaode_map"
            const val BAIDU_MAP = "baidu_map"
            const val TENCENT_MAP = "tencent_map"
            const val GOOGLE_MAP = "google_map"
        }

        override fun parseResult(resultCode: Int, intent: Intent?): ActivityResult {
            return ActivityResult(resultCode, intent)
        }

        /**
         * geo:latitude,longitude 显示给定经度和纬度处的地图。
         */
        @SuppressLint("QueryPermissionsNeeded")
        override fun createIntent(context: Context, input: Map<String, String>): Intent {
            val dname = input[DES_NAME] ?: ""
            val intent = when (input[MAP_MODE]) {
                GAODE_MAP -> openGaoDeMap(context.packageName, dname)
                BAIDU_MAP -> openBaiduMap(context.packageName, dname)
                TENCENT_MAP -> openTencent(context.packageName, dname)
                else -> {
                    Intent(Intent.ACTION_VIEW).apply {
                        data = Uri.parse("geo:0,0?q=$dname")
                        setPackage("com.google.android.apps.maps")
                    }
                }
            }

            return if (input[MAP_MODE] == GAODE_MAP || input[MAP_MODE] == BAIDU_MAP || input[MAP_MODE] == TENCENT_MAP) {
                if (checkMapAppsIsExist(context, intent.`package` ?: "")) {
                    intent
                } else {
                    Intent(Intent.ACTION_VIEW)
                }
            } else {
                intent
            }
        }

        /**
         * 打开高德地图（公交出行，起点位置使用地图当前位置）
         * t = 0（驾车）= 1（公交）= 2（步行）= 3（骑行）= 4（火车）= 5（长途客车）
         *
         * 搜索地点 androidamap://poi?sourceApplication=softname&keywords=银行|加油站|电影院&dev=0
         * @param dname 终点名称
         */
        private fun openGaoDeMap(packageName: String, dname: String): Intent {
            return Intent(Intent.ACTION_VIEW).apply {
                setPackage("com.autonavi.minimap")
                addCategory(Intent.CATEGORY_DEFAULT)
                data =
                    Uri.parse("androidamap://poi?sourceApplication=${packageName}&keywords=${dname}")
            }
        }

        /**
         * 打开百度地图（公交出行，起点位置使用地图当前位置）
         * mode = transit（公交）、driving（驾车）、walking（步行）和riding（骑行）. 默认:driving
         * 当 mode=transit 时 ： sy = 0：推荐路线 、 2：少换乘 、 3：少步行 、 4：不坐地铁 、 5：时间短 、 6：地铁优先
         *
         * baidumap://map/geocoder?src=andr.baidu.openAPIdemo&address=北京市海淀区上地信息路9号奎科科技大厦
         * @param dname 终点名称
         */
        private fun openBaiduMap(packageName: String, dname: String): Intent {
            return Intent(Intent.ACTION_VIEW).apply {
                setPackage("com.baidu.BaiduMap")
                data = Uri.parse("baidumap://map/geocoder?src=${packageName}&address=${dname}")
            }
        }

        /**
         * 打开腾讯地图（公交出行，起点位置使用地图当前位置）
         * 公交：type=bus drive walk
         * bus policy 0：较快捷 、 1：少换乘 、 2：少步行 、 3：不坐地铁
         * drive policy 0：较快捷 1：无高速 2：距离
         * 驾车：type=drive，policy有以下取值
         * 0：较快捷 、 1：无高速 、 2：距离短
         *
         * policy的取值缺省为0
         * @param dname 终点名称
         */
        private fun openTencent(packageName: String, dname: String): Intent {
            return Intent(Intent.ACTION_VIEW).apply {
                setPackage("com.tencent.map")
                data =
                    Uri.parse("qqmap://map/routeplan?type=drive&from=我的位置&fromcoord=0,0&to=$dname&policy=0&referer=$packageName")
            }
        }

        /**
         * 检测地图应用是否安装
         * @param context
         * @param packageName 包名
         * @return
         */
        @Suppress("DEPRECATION")
        private fun checkMapAppsIsExist(context: Context, packageName: String): Boolean {
            var packageInfo: PackageInfo?
            try {
                packageInfo = context.packageManager.getPackageInfo(packageName, 0)
            } catch (e: Exception) {
                packageInfo = null
                e.printStackTrace()
            }
            return packageInfo != null
        }
    }


    /**
     * open phone
     */
    class CallIntent : ActivityResultContract<String, ActivityResult>() {
        companion object {
            const val PHONE = "phone"
        }

        /**
         * @param input phone number
         */
        override fun createIntent(context: Context, input: String): Intent {
            return Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$input")
            }
        }

        override fun parseResult(resultCode: Int, intent: Intent?): ActivityResult {
            return ActivityResult(resultCode, intent)
        }
    }

    /**
     * 发送短信
     */
    class SendSMSIntent : ActivityResultContract<Map<String, String>, ActivityResult>() {
        companion object {
            const val PHONE = "phone"
            const val MESSAGE = "message"
        }

        override fun createIntent(context: Context, input: Map<String, String>): Intent {
            return Intent(Intent.ACTION_SENDTO).apply {
                "text/plain".also { type = it }
                data = Uri.parse("smsto:${input[PHONE]}")
                putExtra("sms_body", input[MESSAGE])
            }
        }

        override fun parseResult(resultCode: Int, intent: Intent?): ActivityResult {
            return ActivityResult(resultCode, intent)
        }
    }


    /**
     * 分享
     */
    class SendShareIntent : ActivityResultContract<Map<String, Any>, ActivityResult>() {
        companion object {
            const val TYPE = "type"
            const val EXTRA_TEXT = "extra_text"
            const val EXTRA_STREAM = "extra_stream"

            //share type
            const val TEXT = "text/plain" //Share Text
            const val IMAGE = "image/*" //Share Image
            const val AUDIO = "audio/*" //Share Audio
            const val VIDEO = "video/*" // Share Video
            const val FILE = "*/*" //Share File
        }

        override fun createIntent(context: Context, input: Map<String, Any>): Intent {
            return Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                type = input[TYPE]?.toString() ?: TEXT
                if (type == TEXT) {
                    val text = input[EXTRA_TEXT]
                    if (text != null && text is String) {
                        putExtra(Intent.EXTRA_TEXT, text)
                    }
                } else {
                    val stream = input[EXTRA_STREAM]
                    if (stream != null && stream is Uri) {
                        putExtra(Intent.EXTRA_STREAM, stream)
                    }
                    addCategory(Intent.CATEGORY_DEFAULT)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            }, "分享")
        }

        override fun parseResult(resultCode: Int, intent: Intent?): ActivityResult {
            return ActivityResult(resultCode, intent)
        }
    }


    /**
     * 打开市场
     */
    class MarketIntent : ActivityResultContract<String, ActivityResult>() {
        companion object {
            const val PACKAGE_NAME = "package_name"
        }

        /**
         * @param input packageName
         */
        override fun createIntent(context: Context, input: String): Intent {
            return Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("market://details?id=$input")
            }
        }

        override fun parseResult(resultCode: Int, intent: Intent?): ActivityResult {
            return ActivityResult(resultCode, intent)
        }
    }


    /**
     * 打开系统设置
     */
    class SettingsIntent : ActivityResultContract<String, ActivityResult>() {
        companion object {
            const val SETTINGS_ACTION = "settings_action"
        }

        /**
         * [Settings.ACTION_SETTINGS] 显示系统设置
         *
         * [Settings.ACTION_APPLICATION_DETAILS_SETTINGS] app 设置页面
         *
         * [Settings.ACTION_WIRELESS_SETTINGS] 显示设置以允许配置无线控制，例如 Wi-Fi、蓝牙和移动网络
         *
         * [Settings.ACTION_AIRPLANE_MODE_SETTINGS] 显示设置以允许进入退出飞行模式
         *
         * [Settings.ACTION_WIFI_SETTINGS] 显示设置以允许配置 Wi-Fi
         *
         * [Settings.ACTION_APN_SETTINGS] 显示设置以允许配置 APN
         *
         * [Settings.ACTION_BLUETOOTH_SETTINGS] 显示设置以允许配置蓝牙
         *
         * [Settings.ACTION_DATE_SETTINGS] 显示设置以允许配置日期和时间
         *
         * [Settings.ACTION_LOCALE_SETTINGS] 显示设置以允许配置区域设置
         *
         * [Settings.ACTION_INPUT_METHOD_SETTINGS] 显示配置输入法的设置，特别是允许用户启用输入法
         *
         * [Settings.ACTION_DISPLAY_SETTINGS] 显示设置以允许配置显示
         *
         * [Settings.ACTION_SECURITY_SETTINGS] 显示设置以允许配置安全性和位置隐私
         *
         * [Settings.ACTION_LOCATION_SOURCE_SETTINGS] 显示设置以允许配置当前位置源
         *
         * [Settings.ACTION_INTERNAL_STORAGE_SETTINGS] 显示内部存储设置
         *
         * [Settings.ACTION_MEMORY_CARD_SETTINGS] 显示存储卡存储设置
         *
         * [Settings.ACTION_APPLICATION_SETTINGS] 应用程序列表
         *
         * [Settings.ACTION_APP_NOTIFICATION_SETTINGS] APP的通知设置界面
         */
        override fun createIntent(context: Context, input: String): Intent {
            val intent = Intent(input)
            if (input == Settings.ACTION_APPLICATION_DETAILS_SETTINGS) {
                intent.data = Uri.parse("package:${context.packageName}")
            } else if (input == Settings.ACTION_APP_NOTIFICATION_SETTINGS) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                } else {
                    intent.putExtra("app_package", context.packageName)
                    intent.putExtra("app_uid", context.applicationInfo.uid)
                }
            }
            return intent
        }

        override fun parseResult(resultCode: Int, intent: Intent?): ActivityResult {
            return ActivityResult(resultCode, intent)
        }
    }
}