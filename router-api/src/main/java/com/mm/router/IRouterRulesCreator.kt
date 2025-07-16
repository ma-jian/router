package com.mm.router

import com.mm.router.annotation.model.RouterMeta
import java.util.concurrent.ConcurrentHashMap


/**
 * 路由注册接口 此接口为系统spi自动收集路由地址，用于apt处理 [com.mm.router.annotation.RouterPath] 注解地址
 * @since 1.0
 */
interface IRouterRulesCreator {
    fun initRule(rules: ConcurrentHashMap<String, RouterMeta>, allRuleKeys: MutableSet<String>)
}