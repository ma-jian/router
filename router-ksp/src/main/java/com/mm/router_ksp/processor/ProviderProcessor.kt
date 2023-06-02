package com.mm.router_ksp.processor

import com.google.auto.service.AutoService
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFile
import com.mm.annotation.ServiceProvider
import com.mm.annotation.model.RouterMeta
import com.mm.annotation.model.RouterType
import com.mm.router_ksp.utils.IPROVIDER
import com.mm.router_ksp.utils.IROUTER_CREATOR
import com.mm.router_ksp.utils.WARNING_TIPS
import com.mm.router_ksp.utils.findModuleName
import com.mm.router_ksp.utils.pairServices
import com.mm.router_ksp.utils.pairString
import com.mm.router_ksp.utils.quantifyNameToClassName
import com.mm.router_ksp.utils.routeType
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
 * Date : 2023/5/29
 */
class ProviderProcessor(private val logger: KSPLogger, private val codeGenerator: CodeGenerator, options: Map<String, String>) :
    SymbolProcessor {
    private val moduleName = options.findModuleName(logger)
    private val NAME_OF_PROVIDER = "\$\$ServiceProvider"

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbol = resolver.getSymbolsWithAnnotation(ServiceProvider::class.qualifiedName!!)
        val elements = symbol.filterIsInstance<KSClassDeclaration>().toList()
        if (elements.isNotEmpty()) {
            try {
                parseProvider(elements)
            } catch (e: Exception) {
                logger.exception(e)
            }
        }
        return emptyList()
    }

    private fun parseProvider(elements: List<KSClassDeclaration>) {
        val map = ClassName("java.util", "HashMap")
        val parameterSpec = ParameterSpec.builder(
            "rules", map.parameterizedBy(
                STRING, RouterMeta::class.asTypeName()
            ).copy(nullable = false)
        ).build()

        val funSpecBuild = FunSpec.builder("initRule").addModifiers(KModifier.OVERRIDE).addParameter(parameterSpec)
        var packageName: String = javaClass.getPackage().name

        val groupFileDependencies = mutableSetOf<KSFile>()
        elements.forEach {
            if (it.classKind != ClassKind.CLASS) {
                error("Only Classes can be annotated with " + ServiceProvider::class.java.canonicalName)
            }
            val qualifiedName = it.qualifiedName?.asString() ?: error("local variable can not be annotated with @ServiceProvider")
            packageName = qualifiedName.substring(0, qualifiedName.lastIndexOf("."))
            if (it.routeType == RouterType.PROVIDER) {
                val services = it.pairServices("returnType")
                val des = it.pairString("des")
                val returnType = services.declaration.qualifiedName!!.asString()
                funSpecBuild.addStatement(
                    "rules.put(%S, %T.build(%T.${it.routeType},%S,%T::class.java,%S))",
                    returnType,
                    RouterMeta::class,
                    RouterType::class,
                    returnType,
                    it.qualifiedName!!.asString().quantifyNameToClassName(),
                    des
                )
            } else {
                throw RuntimeException("The @ServiceProvider is marked on unsupported class, look at [$qualifiedName],implement the current interface [$IPROVIDER]")
            }
            it.containingFile?.let { file ->
                groupFileDependencies.add(file)
            }
        }
        val className = moduleName + NAME_OF_PROVIDER
        val file = FileSpec.builder(packageName, className)
            .addType(
                TypeSpec.classBuilder(ClassName(packageName, className))
                    .addSuperinterface(IROUTER_CREATOR.quantifyNameToClassName())
                    .addFunction(funSpecBuild.build())
                    .addKdoc(CodeBlock.of("$WARNING_TIPS\n自动收集 [${ServiceProvider::class.qualifiedName}] 服务接口信息"))
                    .addAnnotation(
                        AnnotationSpec.builder(AutoService::class)
                            .addMember(
                                CodeBlock.of(
                                    "%T::class",
                                    IROUTER_CREATOR.quantifyNameToClassName()
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