package com.mm.router_ksp

import com.google.auto.service.AutoService
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.mm.router_ksp.processor.AutowiredProcessor


/**
 * Date : 2023/5/27
 */
@AutoService(SymbolProcessorProvider::class)
class AutowiredProcessorProvider : SymbolProcessorProvider {

    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return AutowiredProcessor(environment.logger, environment.codeGenerator, environment.options)
    }
}