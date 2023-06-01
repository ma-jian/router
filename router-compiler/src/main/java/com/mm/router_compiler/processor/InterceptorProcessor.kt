package com.mm.router_compiler.processor

import com.google.auto.service.AutoService
import com.mm.annotation.RouterInterceptor
import com.mm.annotation.model.InterceptorMeta
import com.mm.router_compiler.BaseAbstractProcessor
import com.mm.router_compiler.inter.IProcessor
import com.squareup.javapoet.AnnotationSpec
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.ParameterSpec
import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.javapoet.TypeSpec
import java.io.IOException
import javax.annotation.processing.Messager
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.Modifier
import javax.lang.model.util.ElementFilter
import javax.tools.Diagnostic

/**
 * 注解处理器处理[RouterPathProcessor]
 */

class InterceptorProcessor : IProcessor {
    private val WARNING_TIPS = "DO NOT EDIT THIS FILE!!! IT WAS GENERATED BY ROUTER."
    private val NAME_OF_INTERCEPTOR = "\$\$InterceptorCreator"

    private lateinit var messager: Messager

    override fun process(roundEnv: RoundEnvironment, abstractProcessor: BaseAbstractProcessor) {
        messager = abstractProcessor.mMessager
        try {
            val elements: Set<Element>? = roundEnv.getElementsAnnotatedWith(RouterInterceptor::class.java)
            if (elements.isNullOrEmpty()) {
                return
            }
            val parameterizedTypeName: ParameterizedTypeName = ParameterizedTypeName.get(
                ClassName.get(HashMap::class.java),
                ClassName.get(String::class.java),
                ClassName.get(InterceptorMeta::class.java)
            )
            val parameterSpec: ParameterSpec = ParameterSpec.builder(parameterizedTypeName, "interceptors").build()
            val methodSpecBuilder: MethodSpec.Builder = MethodSpec.methodBuilder("intercept")
                .addAnnotation(Override::class.java)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(parameterSpec)
            var packageName: String = javaClass.getPackage().name
            for (typeElement in ElementFilter.typesIn(roundEnv.getElementsAnnotatedWith(RouterInterceptor::class.java))) {
                if (typeElement.kind != ElementKind.CLASS) {
                    error("Only Classes can be annotated with " + RouterInterceptor::class.java.canonicalName)
                    return
                }
                val qualifiedName = typeElement.qualifiedName.toString()
                packageName = qualifiedName.substring(0, qualifiedName.lastIndexOf("."))
                val router: RouterInterceptor = typeElement.getAnnotation(RouterInterceptor::class.java)
                methodSpecBuilder.addStatement(
                    "interceptors.put(\$S, \$T.build(\$S, \$L, new \$T(), \$S))",
                    router.value,
                    ClassName.get(InterceptorMeta::class.java),
                    router.value,
                    router.priority,
                    typeElement.asType(),
                    router.des
                )
            }
            val methodSpec: MethodSpec = methodSpecBuilder.build()
            val moduleName = abstractProcessor.moduleName
            val className = moduleName + NAME_OF_INTERCEPTOR
            val typeSpec: TypeSpec = TypeSpec.classBuilder(className)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(
                    AnnotationSpec.builder(AutoService::class.java)
                        .addMember(
                            "value",
                            "\$T.class",
                            abstractProcessor.mElements.getTypeElement(BaseAbstractProcessor.ROUTER_INTERCEPTOR_PATH)
                        )
                        .build()
                )
                .addSuperinterface(ClassName.get(abstractProcessor.mElements.getTypeElement(BaseAbstractProcessor.ROUTER_INTERCEPTOR_PATH)))
                .addMethod(methodSpec)
                .addJavadoc(CodeBlock.of("$WARNING_TIPS\n自动收集 {@link \$T} 路由", RouterInterceptor::class.java))
                .build()
            JavaFile.builder(packageName, typeSpec).build().writeTo(abstractProcessor.mFiler)
        } catch (e: IOException) {
            error(e.toString())
        } catch (e: Exception) {
            error(e.printStackTrace())
        }
    }

    private fun error(error: String?) {
        messager.printMessage(Diagnostic.Kind.ERROR, this.javaClass.canonicalName + " : " + error)
    }

    private fun info(error: String) {
        messager.printMessage(Diagnostic.Kind.WARNING, this.javaClass.canonicalName + " : " + error)
    }
}