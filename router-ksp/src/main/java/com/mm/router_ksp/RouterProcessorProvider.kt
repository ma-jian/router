package com.mm.router_ksp

import com.google.auto.service.AutoService
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.mm.router_ksp.processor.RouterProcessor


/**
 * 路由注册处理器
 */
@AutoService(SymbolProcessorProvider::class)
class RouterProcessorProvider : SymbolProcessorProvider {

    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return RouterProcessor(environment.logger, environment.codeGenerator, environment.options)
    }
}