package com.mm.router

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.mm.annotation.Autowired


/**
 * Date : 2023/5/27
 */
class FragmentTest : Fragment() {

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

    @Autowired(name = "name")
    @JvmField
    var name: String? = ""
    @Autowired(name = "name")
    @JvmField
    var name2: String = ""

    @Autowired(name = "long")
    @JvmField
    var log: Long? = 0
    @Autowired(name = "long")
    @JvmField
    var log1: Long = 0

    @Autowired(name = "long")
    @JvmField
    var dou: Double = 0.0
    @Autowired(name = "long")
    @JvmField
    var dou1: Double? = 0.0

    @Autowired(name = "bean")
    @JvmField
    var bean: SerBean = SerBean()

    @Autowired(name = "bean")
    @JvmField
    var bean1: ParaBean? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.getString("", "")
        arguments?.getShort("", 0)

        arguments?.getSerializable("")

        val extra = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getSerializable("bean", SerBean::class.java)
        } else {
            arguments?.getSerializable("bean") as SerBean
        }
    }
}