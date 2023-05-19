package com.mm.router_compiler.processor

import com.google.auto.service.AutoService
import com.mm.annotation.ServiceProvider
import com.mm.annotation.model.RouterMeta
import com.mm.annotation.model.RouterType
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
import javax.lang.model.type.TypeMirror
import javax.lang.model.util.ElementFilter
import javax.tools.Diagnostic


/**
 * 处理[ServiceProvider] 可根据地址获取服务接口
 */

class ServiceProviderProcessor : IProcessor {
    private val WARNING_TIPS = "DO NOT EDIT THIS FILE!!! IT WAS GENERATED BY ROUTER."
    private val NAME_OF_PROVIDER = "\$\$ServiceProvider"
    private lateinit var messager: Messager

    override fun process(roundEnv: RoundEnvironment, abstractProcessor: BaseAbstractProcessor) {
        val types = abstractProcessor.mTypes
        val elementUtils = abstractProcessor.mElements
        messager = abstractProcessor.mMessager

        try {
            val elements: Set<Element>? = roundEnv.getElementsAnnotatedWith(ServiceProvider::class.java)
            if (elements.isNullOrEmpty()) {
                return
            }
            val parameterizedTypeName: ParameterizedTypeName = ParameterizedTypeName.get(
                ClassName.get(HashMap::class.java), ClassName.get(String::class.java), ClassName.get(RouterMeta::class.java)
            )
            val parameterSpec: ParameterSpec = ParameterSpec.builder(parameterizedTypeName, "rules").build()
            val methodSpecBuilder: MethodSpec.Builder =
                MethodSpec.methodBuilder("initRule").addAnnotation(Override::class.java).addModifiers(Modifier.PUBLIC)
                    .addParameter(parameterSpec)
            val iProvider = elementUtils.getTypeElement(IProcessor.IPROVIDER).asType()
            var packageName: String = javaClass.getPackage().name
            for (typeElement in ElementFilter.typesIn(roundEnv.getElementsAnnotatedWith(ServiceProvider::class.java))) {
                if (typeElement.kind != ElementKind.CLASS) {
                    error("Only Classes can be annotated with " + ServiceProvider::class.java.canonicalName)
                    return
                }
                val qualifiedName = typeElement.qualifiedName.toString()
                packageName = qualifiedName.substring(0, qualifiedName.lastIndexOf("."))
                val provider: ServiceProvider = typeElement.getAnnotation(ServiceProvider::class.java)
                val tm: TypeMirror = typeElement.asType()
                if (types.isSubtype(tm, iProvider)) {
                    // IProvider
                    val returnType = getClassFromAnnotation(typeElement, "returnType")
                    val params = getClassFromAnnotation(typeElement, "params")
                    val routerType = RouterType.PROVIDER
                    methodSpecBuilder.addCode(
                        CodeBlock.of(
                            "rules.put(\$S, \$T.build(\$T.$routerType,\$S,\$T.class,\$S,new Class[]{$params}));",
                            returnType,
                            ClassName.get(RouterMeta::class.java),
                            ClassName.get(RouterType::class.java),
                            returnType,
                            typeElement,
                            provider.des
                        )
                    )
                } else {
                    throw RuntimeException("The @ServiceProvider is marked on unsupported class, look at [$tm].")
                }
            }
            val methodSpec: MethodSpec = methodSpecBuilder.build()
            val moduleName = abstractProcessor.moduleName
            val className = moduleName + NAME_OF_PROVIDER
            val typeSpec: TypeSpec = TypeSpec.classBuilder(className).addModifiers(Modifier.PUBLIC).addAnnotation(
                AnnotationSpec.builder(AutoService::class.java).addMember(
                    "value", "\$T.class", elementUtils.getTypeElement(BaseAbstractProcessor.ROUTER_INTERFACE_PATH)
                ).build()
            ).addSuperinterface(ClassName.get(elementUtils.getTypeElement(BaseAbstractProcessor.ROUTER_INTERFACE_PATH)))
                .addJavadoc(CodeBlock.of("$WARNING_TIPS\n自动收集 {@link \$T} 服务接口信息", ServiceProvider::class.java))
                .addMethod(methodSpec).build()
            JavaFile.builder(packageName, typeSpec).build().writeTo(abstractProcessor.mFiler)
        } catch (e: IOException) {
            error(e.localizedMessage)
        } catch (e: Exception) {
            error(e.localizedMessage)
        }
    }


    private fun getClassFromAnnotation(key: Element, name: String): String {
        val annotationMirrors = key.annotationMirrors
        for (annotationMirror in annotationMirrors) {
            if (ServiceProvider::class.java.name == annotationMirror.annotationType.toString()) {
                annotationMirror.elementValues.forEach { (k, v) ->
                    if (k.simpleName.toString() == name) {
                        return v.value?.toString() ?: ""
                    }
                }
            }
        }
        return ""
    }

    private fun error(error: String?) {
        messager.printMessage(Diagnostic.Kind.ERROR, this.javaClass.canonicalName + " : " + error)
    }

    private fun info(error: String) {
        messager.printMessage(Diagnostic.Kind.WARNING, this.javaClass.canonicalName + " : " + error)
    }
}