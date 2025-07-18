package com.mm.router_app.main

import android.annotation.SuppressLint
import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentActivity
import com.mm.router.ResultContracts
import com.mm.router.Router
import com.mm.router.annotation.Autowired
import com.mm.router.annotation.RouterPath
import com.mm.router.fallback.FallbackHandler
import com.mm.router_app.R
import com.mm.router_app.bean.ParaBean
import com.mm.router_app.bean.SerBean
import com.mm.router_app.provider.IServiceProvider
import com.mm.router_app.provider.ServiceProviderImpl


@RouterPath("com.mm.main", interceptor = ["/"], des = "主页面")
class MainActivity : FragmentActivity() {

    @Autowired
    @JvmField
    var age: Int? = 0

    @Autowired
    @JvmField
    var age1: Int = 0

    @Autowired
    @JvmField
    var bol: Boolean = false

    @Autowired
    @JvmField
    var bol1: Boolean? = false

    @Autowired(name = "name", des = "用户名称")
    @JvmField
    var name: String? = ""

    @Autowired(name = "name2", des = "用户名称1")
    @JvmField
    var name2: String = ""

    @Autowired(name = "long")
    @JvmField
    var log: Long? = 0

    @Autowired(name = "long1")
    @JvmField
    var log1: Long = 0

    @Autowired(name = "dou")
    @JvmField
    var dou: Double = 0.0

    @Autowired(name = "dou1", required = true)
    @JvmField
    var dou1: Double? = 0.0

    @Autowired
    @JvmField
    var bean: SerBean? = null

    @Autowired
    @JvmField
    var bean1: ParaBean = ParaBean()

    lateinit var textView: TextView

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        intent.putExtra("age", 1)
        intent.putExtra("string", "参数传递")
        intent.putExtra("long", 120L)
        Router.init(this).autoWired(this)
        textView = findViewById<TextView>(R.id.message)
        val editText = findViewById<EditText>(R.id.edit)
        val stringBuilder = StringBuilder()
        stringBuilder.append("age:$age; string:$name2; name:$name; long $log")
        textView.text = stringBuilder
        Router.enableFallback = true
        Router.defaultFallback (object : FallbackHandler {
            override fun handleFallback(path: String, bundle: Bundle?) {
                Log.e("defaultFallback","path:$path ; bundle:${bundle.toString()}")
                Router.init().open("com.mm.second").withBundle(bundle).navigation()
            }
        })
        /**
         * [ServiceProviderImpl]
         */
        val provider = Router.init().open("/service/provider").doProvider<IServiceProvider>("a", 1, 1L, false)
        stringBuilder.append("\n\n").append(provider?.message())
        textView.text = stringBuilder

        findViewById<Button>(R.id.finish).visibility = if (intent.getBooleanExtra("result_ok", false)) View.VISIBLE else View.GONE

        //模拟跳转后需要处理特别逻辑问题
        findViewById<Button>(R.id.finish).setOnClickListener {
            setResult(RESULT_OK)
            finish()
        }

        //startActivityForResult
        findViewById<View>(R.id.open_second).setOnClickListener {
            Router.init().open("com.mm.second2").withString("string", editText.text.toString()).withInt("age", 100)
                .withBoolean("bol", true).navigationResult {
                    //路由执行完毕并返回信息
                    it.onSuccess { result ->
                        if (result.result.resultCode == Activity.RESULT_OK) {
                            val string = result.result.data?.getStringExtra("string")
                            val bol = result.result.data?.getBooleanExtra("bol", false)
                            val age = result.result.data?.getIntExtra("age", 0)
                            val msg = "ActivityResultCallback string:$string; bol:$bol; age:$age"
                            stringBuilder.append("\n\n").append(msg)
                            textView.text = stringBuilder
                        }
                    }

                    //路由被中断、通过在[Interceptor]中执行chain.interrupt()方法
                    it.onIntercepted {
                        textView.text = textView.text.toString() + "\n onInterrupt"
                    }
                    it.onFailure {
                        val failure = "${it.path} ; ${it.error.message}"
                        textView.text = textView.text.toString() + "\nonFailure:$failure"
                        false
                    }
                }
        }

        val permission = arrayOf(
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.READ_CONTACTS
        )

        //先获取权限
        val launcher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions(),
            object : ActivityResultCallback<Map<String, Boolean>> {
                override fun onActivityResult(result: Map<String, Boolean>?) {

                }
            })
        launcher.launch(permission)

        // const val ACTION_CONTENT = "action_content" //单选相册视频返回已选择Uri ; image/\* video/\*
        findViewById<View>(R.id.open_content).setOnClickListener {
            Router.init().open(Router.Path.ACTION_CONTENT).navigation() {
                if (it.resultCode == RESULT_OK) {
                    textView.text = textView.text.toString() + "\n ${it.data}"
                }
            }
        }

        // const val ACTION_MULTI_CONTENT = "action_multi_content" //多选相册视频返回已选择列表List<Uri> image/\* video/\*
        findViewById<View>(R.id.open_multi_content).setOnClickListener {
            Router.init().open(Router.Path.ACTION_MULTI_CONTENT).navigation() {
                if (it.resultCode == RESULT_OK) {
                    textView.text = textView.text.toString() + "\n ${it.data?.extras}"
                }
            }
        }
        //  const val ACTION_TAKE_PIC_PREVIEW = "action_take_pic_preview" //拍照预览 返回Bitmap图
        findViewById<View>(R.id.take_pic_preview).setOnClickListener {
            Router.init().open(Router.Path.ACTION_TAKE_PIC_PREVIEW).navigation() {
                if (it.resultCode == RESULT_OK) {
                    textView.text = textView.text.toString() + "\n ${it.data?.extras}"
                }
            }
        }

        //  const val ACTION_TAKE_PICTURE = "action_take_picture" //拍照预览并保存至文件 返回地址
        findViewById<View>(R.id.take_picture).setOnClickListener {
            Router.init().open(Router.Path.ACTION_TAKE_PICTURE).navigation() {
                if (it.resultCode == RESULT_OK) {
                    textView.text = textView.text.toString() + "\n ${it.data}"
                }
            }
        }

        //  const val ACTION_TAKE_VIDEO = "action_take_video"//拍摄视频并保存至文件 返回视频地址
        findViewById<View>(R.id.take_video).setOnClickListener {
            Router.init().open(Router.Path.ACTION_TAKE_VIDEO).navigation() {
                if (it.resultCode == RESULT_OK) {
                    textView.text = textView.text.toString() + "\n ${it.data}"
                }
            }
        }


        //  const val ACTION_PICK_CONTACT = "action_pick_contact" //打开通讯录 返回Uri
        findViewById<View>(R.id.pick_contact).setOnClickListener {
            Router.init().open(Router.Path.ACTION_PICK_CONTACT).navigation() {
                if (it.resultCode == RESULT_OK) {
                    textView.text = textView.text.toString() + "\n ${it.data}"
                }
            }
        }


        //  const val ACTION_MAP = "action_map" //打开地图
        //  const val MAP_MODE = "map_mode" //导航方式
        //  const val DES_NAME = "dname" //终点名称
        findViewById<View>(R.id.open_map).setOnClickListener {
            Router.init().open(Router.Path.ACTION_MAP)
                .withString(ResultContracts.MapIntent.MAP_MODE, ResultContracts.MapIntent.GAODE_MAP)
                .withString(ResultContracts.MapIntent.DES_NAME, "上海").navigation() {
                    if (it.resultCode == RESULT_OK) {
                        textView.text = textView.text.toString() + "\n ${it.data}"
                    }
                }
        }

        // const val ACTION_CALL_DIAL = "action_call_dial" //打开电话拨号
        // const val PHONE = "phone"
        findViewById<View>(R.id.call_dial).setOnClickListener {
            Router.init().open(Router.Path.ACTION_CALL_DIAL).withString(ResultContracts.CallIntent.PHONE, "123456789")
                .navigation() {
                    if (it.resultCode == RESULT_OK) {
                        textView.text = textView.text.toString() + "\n ${it.data}"
                    }
                }
        }

        // const val ACTION_SEND_SMS = "action_send_sms" //发送短信
        // const val PHONE = "phone"
        // const val MESSAGE = "message"
        findViewById<View>(R.id.send_sms).setOnClickListener {
            Router.init().open(Router.Path.ACTION_SEND_SMS).withString(ResultContracts.SendSMSIntent.PHONE, "123456789")
                .withString(ResultContracts.SendSMSIntent.MESSAGE, "短信内容").navigation() {
                    if (it.resultCode == RESULT_OK) {
                        textView.text = textView.text.toString() + "\n ${it.data}"
                    }
                }
        }

        // const val ACTION_SHARE = "action_share" //系统分享
        // const val TYPE = "type"
        // const val EXTRA_TEXT = "extra_text"
        // const val EXTRA_STREAM = "extra_stream"
        findViewById<View>(R.id.open_share).setOnClickListener {
            Router.init().open(Router.Path.ACTION_SHARE)
                .withString(ResultContracts.SendShareIntent.TYPE, ResultContracts.SendShareIntent.TEXT)
                .withString(ResultContracts.SendShareIntent.EXTRA_TEXT, "分享文字")
                .withParcelable(ResultContracts.SendShareIntent.EXTRA_STREAM, Uri.parse("文件")).navigation() {
                    if (it.resultCode == RESULT_OK) {
                        textView.text = textView.text.toString() + "\n ${it.data}"
                    }
                }
        }

        // const val ACTION_MARKET = "action_market" //应用市场
        // const val PACKAGE_NAME = "package_name"
        findViewById<View>(R.id.open_market).setOnClickListener {
            Router.init().open(Router.Path.ACTION_MARKET).withString(ResultContracts.MarketIntent.PACKAGE_NAME, "com.mm.router")
                .navigation() {
                    if (it.resultCode == RESULT_OK) {
                        textView.text = textView.text.toString() + "\n ${it.data}"
                    }
                }
        }

        var setting = Settings.ACTION_SETTINGS
        // const val ACTION_SETTINGS = "action_settings" //打开系统页面
        // const val SETTINGS_ACTION = "settings_action"
        findViewById<View>(R.id.open_settings).setOnClickListener {
            Router.init().open(Router.Path.ACTION_SETTINGS).withString(ResultContracts.SettingsIntent.SETTINGS_ACTION, setting)
                .navigation() {
                    if (it.resultCode == RESULT_OK) {
                        textView.text = textView.text.toString() + "\n ${it.data}"
                    }
                }
        }

        /**
         * [Settings.ACTION_SETTINGS] 显示系统设置
         * [Settings.ACTION_APPLICATION_DETAILS_SETTINGS] app 设置页面
         * [Settings.ACTION_WIRELESS_SETTINGS] 显示设置以允许配置无线控制，例如 Wi-Fi、蓝牙和移动网络
         * [Settings.ACTION_AIRPLANE_MODE_SETTINGS] 显示设置以允许进入退出飞行模式
         * [Settings.ACTION_WIFI_SETTINGS] 显示设置以允许配置 Wi-Fi
         * [Settings.ACTION_APN_SETTINGS] 显示设置以允许配置 APN
         * [Settings.ACTION_BLUETOOTH_SETTINGS] 显示设置以允许配置蓝牙
         * [Settings.ACTION_DATE_SETTINGS] 显示设置以允许配置日期和时间
         * [Settings.ACTION_LOCALE_SETTINGS] 显示设置以允许配置区域设置
         * [Settings.ACTION_INPUT_METHOD_SETTINGS] 显示配置输入法的设置，特别是允许用户启用输入法
         * [Settings.ACTION_DISPLAY_SETTINGS] 显示设置以允许配置显示
         * [Settings.ACTION_SECURITY_SETTINGS] 显示设置以允许配置安全性和位置隐私
         * [Settings.ACTION_LOCATION_SOURCE_SETTINGS] 显示设置以允许配置当前位置源
         * [Settings.ACTION_INTERNAL_STORAGE_SETTINGS] 显示内部存储设置
         * [Settings.ACTION_MEMORY_CARD_SETTINGS] 显示存储卡存储设置
         * [Settings.ACTION_APPLICATION_SETTINGS] 应用程序列表
         * [Settings.ACTION_APP_NOTIFICATION_SETTINGS] APP的通知设置界面
         */
        val spinner = findViewById<Spinner>(R.id.spinner)
        val type = arrayListOf(
            Settings.ACTION_SETTINGS,
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Settings.ACTION_WIRELESS_SETTINGS,
            Settings.ACTION_AIRPLANE_MODE_SETTINGS,
            Settings.ACTION_WIFI_SETTINGS,
            Settings.ACTION_APN_SETTINGS,
            Settings.ACTION_BLUETOOTH_SETTINGS,
            Settings.ACTION_DATE_SETTINGS,
            Settings.ACTION_LOCALE_SETTINGS,
            Settings.ACTION_INPUT_METHOD_SETTINGS,
            Settings.ACTION_DISPLAY_SETTINGS,
            Settings.ACTION_SECURITY_SETTINGS,
            Settings.ACTION_LOCATION_SOURCE_SETTINGS,
            Settings.ACTION_INTERNAL_STORAGE_SETTINGS,
            Settings.ACTION_MEMORY_CARD_SETTINGS,
            Settings.ACTION_APPLICATION_SETTINGS,
            Settings.ACTION_APP_NOTIFICATION_SETTINGS
        )
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, type)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = spinnerAdapter
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                setting = type[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
    }
}