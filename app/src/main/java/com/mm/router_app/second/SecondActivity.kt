package com.mm.router_app.second

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import com.mm.router.Router
import com.mm.router.annotation.Autowired
import com.mm.router.annotation.RouterPath
import com.mm.router_app.provider.IServiceProvider
import com.mm.router_app.R


@RouterPath("com.mm.second", des = "第二个页面")
class SecondActivity : FragmentActivity() {

    @Autowired(name = "age")
    @JvmField
    var age: Int = 0

    @Autowired
    @JvmField
    var string: String = ""

    @Autowired
    @JvmField
    var bol: Boolean? = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second)
        Router.init(this).autoWired(this)
        val textView = findViewById<TextView>(R.id.message)
        val editText = findViewById<EditText>(R.id.edit)
        val extras = intent.extras.toString()
        val stringBuilder = StringBuilder()
        stringBuilder.append("age:$age; string:$string; bol:$bol; extras:$extras")
        textView.text = stringBuilder

        val provider = Router.init().open(IServiceProvider::class.java).doProvider<IServiceProvider>()
        stringBuilder.append("\n\n").append(provider?.message())
        textView.text = stringBuilder

        findViewById<View>(R.id.open_main).setOnClickListener {
            val intent = Intent()
            intent.putExtra("bol", true)
            intent.putExtra("string", editText.text.toString())
            intent.putExtra("age", 13)
            setResult(RESULT_OK, intent)
            finish()
        }
    }
}