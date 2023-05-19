package com.mm.router.impl

import android.content.Context
import android.util.LruCache
import com.mm.annotation.RouterPath
import com.mm.annotation.ServiceProvider
import com.mm.router.ISyringe
import com.mm.router.service.AutowiredService

/**
 * param inject service impl.
 *
 * @since 1.0
 */
@RouterPath(value = "/router/service/autowired", des = "自动注册赋值")
class AutowiredServiceImpl : AutowiredService {
    private lateinit var classCache: LruCache<String, ISyringe>
    private lateinit var blackList: MutableList<String>
    private val NAME_OF_AUTOWIRED = "$\$Autowired"

    override fun init(context: Context) {
        classCache = LruCache(50)
        blackList = ArrayList()
    }

    override fun autoWired(instance: Any) {
        doInject(instance, null)
    }

    /**
     * Recursive injection
     *
     * @param instance who call me.
     * @param parent   parent of me.
     */
    private fun doInject(instance: Any, parent: Class<*>?) {
        val clazz = parent ?: instance.javaClass
        val syringe = getSyringe(clazz)
        syringe?.inject(instance)
        val superClazz = clazz.superclass
        // has parent and its not the class of framework.
        if (null != superClazz && !superClazz.name.startsWith("android")) {
            doInject(instance, superClazz)
        }
    }

    private fun getSyringe(clazz: Class<*>): ISyringe? {
        val className = clazz.name
        try {
            if (!blackList.contains(className)) {
                var syringeHelper = classCache[className]
                if (null == syringeHelper) {  // No cache.
                    syringeHelper = Class.forName(clazz.name + NAME_OF_AUTOWIRED).getConstructor().newInstance() as ISyringe
                }
                classCache.put(className, syringeHelper)
                return syringeHelper
            }
        } catch (e: Exception) {
            blackList.add(className) // This instance need not autowired.
        }
        return null
    }
}