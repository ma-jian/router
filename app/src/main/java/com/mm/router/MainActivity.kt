package com.mm.router

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import com.mm.annotation.Autowired
import com.mm.annotation.RouterPath


@RouterPath("com.mm.main")
class MainActivity : FragmentActivity() {

    @Autowired
    @JvmField
    var age: Int? = 0

    @Autowired
    @JvmField
    var string: String = ""

    @Autowired(name = "name")
    @JvmField
    var name: String = ""

    @Autowired(name = "long")
    @JvmField
    var log: Long = 0

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
        stringBuilder.append("age:$age; string:$string; name:$name; long $log")
        textView.text = stringBuilder

        val provider = Router.init().open(IServiceProvider::class.java).doProvider<IServiceProvider>()
        stringBuilder.append("\n\n").append(provider?.message())
        textView.text = stringBuilder

        findViewById<View>(R.id.open_second).setOnClickListener {
            Router.init().open("com.mm.second").withString("string", editText.text.toString())
                .withInt("age", 100).withBoolean("bol", true).navigation() {
                    val string = it.data?.getStringExtra("string")
                    val bol = it.data?.getBooleanExtra("bol", false)
                    val age = it.data?.getIntExtra("age", 0)
                    val result = "ActivityResultCallback string:$string; bol:$bol; age:$age"
                    stringBuilder.append("\n\n").append(result)
                    textView.text = stringBuilder
                }
        }
    }
}