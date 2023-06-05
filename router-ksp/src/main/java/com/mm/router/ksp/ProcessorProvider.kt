package com.mm.router.ksp

import com.google.auto.service.AutoService
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.mm.router.ksp.processor.AutowiredProcessor
import com.mm.router.ksp.processor.InterceptorProcessor
import com.mm.router.ksp.processor.ProviderProcessor
import com.mm.router.ksp.processor.RouterProcessor


/**
 * [com.mm.router.annotation.Autowired]
 */
@AutoService(SymbolProcessorProvider::class)
class AutowiredProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return AutowiredProcessor(environment.logger, environment.codeGenerator)
    }
}

/**
 * [com.mm.router.annotation.ServiceProvider]
 */
@AutoService(SymbolProcessorProvider::class)
class ProviderProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return ProviderProcessor(environment.logger, environment.codeGenerator, environment.options)
    }
}

/**
 * [com.mm.router.annotation.RouterPath]
 */
@AutoService(SymbolProcessorProvider::class)
class RouterProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return RouterProcessor(environment.logger, environment.codeGenerator, environment.options)
    }
}

/**
 * [com.mm.router.annotation.RouterInterceptor]
 */
@AutoService(SymbolProcessorProvider::class)
class InterceptorProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return InterceptorProcessor(environment.logger, environment.codeGenerator, environment.options)
    }
}