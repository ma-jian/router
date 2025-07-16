package com.mm.router.ksp.processor

import com.google.auto.service.AutoService
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFile
import com.mm.router.annotation.RouterInterceptor
import com.mm.router.annotation.model.RouterMeta
import com.mm.router.annotation.model.RouterType
import com.mm.router.ksp.utils.INTERCEPTOR
import com.mm.router.ksp.utils.INTERCEPTOR_CREATOR
import com.mm.router.ksp.utils.WARNING_TIPS
import com.mm.router.ksp.utils.findAnnotationWithType
import com.mm.router.ksp.utils.findModuleName
import com.mm.router.ksp.utils.quantifyNameToClassName
import com.mm.router.ksp.utils.routeType
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.ksp.writeTo


/**
 * 处理[com.mm.router.annotation.RouterInterceptor]
 */
class InterceptorProcessor(
    private val logger: KSPLogger,
    private val codeGenerator: CodeGenerator,
    options: Map<String, String>
) :
    SymbolProcessor {
    private val moduleName = options.findModuleName(logger)
    private val NAME_OF_INTERCEPTOR = "Interceptor\$\$"

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbol = resolver.getSymbolsWithAnnotation(RouterInterceptor::class.qualifiedName!!)
        val elements = symbol.filterIsInstance<KSClassDeclaration>().toList()
        if (elements.isNotEmpty()) {
            try {
                parseRoute(elements)
            } catch (e: Exception) {
                logger.exception(e)
            }
        }
        return emptyList()
    }

    private fun parseRoute(elements: List<KSClassDeclaration>) {
        val map = ClassName("java.util.concurrent", "ConcurrentHashMap")
        val parameterSpec = ParameterSpec.builder(
            "interceptors", map.parameterizedBy(
                STRING, RouterMeta::class.asTypeName()
            ).copy(nullable = false)
        ).build()

        val funSpecBuild = FunSpec.builder("intercept").addModifiers(KModifier.OVERRIDE).addParameter(parameterSpec)
        var packageName: String = javaClass.getPackage().name

        val groupFileDependencies = mutableSetOf<KSFile>()
        elements.forEach {
            if (it.classKind != ClassKind.CLASS) {
                error("Only Classes can be annotated with " + RouterInterceptor::class.java.canonicalName)
            }
            val qualifiedName = it.qualifiedName?.asString()
                ?: error("local variable can not be annotated with @RouterInterceptor")
            packageName = qualifiedName.substring(0, qualifiedName.lastIndexOf("."))
            val interceptor =
                it.findAnnotationWithType<RouterInterceptor>() ?: error("$qualifiedName must annotated with @RouterInterceptor")
            if (it.routeType == RouterType.INTERCEPTOR) {
                funSpecBuild.addStatement(
                    "interceptors.put(%S, %T.build(%T.${it.routeType}, %S, %T::class.java, %S, %L))",
                    interceptor.value,
                    RouterMeta::class,
                    RouterType::class,
                    interceptor.value,
                    it.qualifiedName!!.asString().quantifyNameToClassName(),
                    interceptor.des,
                    interceptor.priority
                )
            } else {
                error("The @RouterInterceptor is marked on unsupported class,implement the current interface [$INTERCEPTOR]")
            }
            it.containingFile?.let { file ->
                groupFileDependencies.add(file)
            }
        }
        val className = NAME_OF_INTERCEPTOR + moduleName
        val file = FileSpec.builder(packageName, className)
            .addType(
                TypeSpec.classBuilder(ClassName(packageName, className))
                    .addSuperinterface(INTERCEPTOR_CREATOR.quantifyNameToClassName())
                    .addFunction(funSpecBuild.build())
                    .addKdoc(CodeBlock.of("$WARNING_TIPS\n自动收集 [${RouterInterceptor::class.qualifiedName}] 路由"))
                    .addAnnotation(
                        AnnotationSpec.builder(AutoService::class)
                            .addMember(
                                CodeBlock.of(
                                    "%T::class",
                                    INTERCEPTOR_CREATOR.quantifyNameToClassName()
                                )
                            )
                            .build()
                    )
                    .build()
            )
            .build()
        file.writeTo(codeGenerator, true, groupFileDependencies)
    }
}