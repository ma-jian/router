package com.mm.router_ksp

import com.google.auto.service.AutoService
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.mm.router_ksp.processor.AutowiredProcessor
import com.mm.router_ksp.processor.InterceptorProcessor
import com.mm.router_ksp.processor.ProviderProcessor
import com.mm.router_ksp.processor.RouterProcessor


/**
 * Date : 2023/6/1
 */
/**
 * [com.mm.annotation.Autowired]
 */
@AutoService(SymbolProcessorProvider::class)
class AutowiredProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return AutowiredProcessor(environment.logger, environment.codeGenerator)
    }
}

/**
 * [com.mm.annotation.ServiceProvider]
 */
@AutoService(SymbolProcessorProvider::class)
class ProviderProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return ProviderProcessor(environment.logger, environment.codeGenerator, environment.options)
    }
}

/**
 * [com.mm.annotation.RouterPath]
 */
@AutoService(SymbolProcessorProvider::class)
class RouterProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return RouterProcessor(environment.logger, environment.codeGenerator, environment.options)
    }
}

/**
 * [com.mm.annotation.RouterInterceptor]
 */
@AutoService(SymbolProcessorProvider::class)
class InterceptorProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return InterceptorProcessor(environment.logger, environment.codeGenerator, environment.options)
    }
}