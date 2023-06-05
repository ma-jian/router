package com.mm.router.ksp.processor

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.mm.router.annotation.Autowired
import com.mm.router.annotation.model.RouterType
import com.mm.router.ksp.utils.ISYRINGE
import com.mm.router.ksp.utils.TypeKind
import com.mm.router.ksp.utils.WARNING_TIPS
import com.mm.router.ksp.utils.findAnnotationWithType
import com.mm.router.ksp.utils.getKotlinPoetTTypeGeneric
import com.mm.router.ksp.utils.quantifyNameToClassName
import com.mm.router.ksp.utils.routeType
import com.mm.router.ksp.utils.typeExchange
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.ksp.writeTo


/**
 * 处理[com.mm.router.annotation.Autowired]
 */
class AutowiredProcessor(private val logger: KSPLogger, private val codeGenerator: CodeGenerator) :
    SymbolProcessor {
    private val NAME_OF_AUTOWIRED = "\$\$Autowired"
    private val TAG = "Router"
    private val AndroidLog = ClassName("android.util", "Log")

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbol = resolver.getSymbolsWithAnnotation(Autowired::class.qualifiedName!!)
        val elements = symbol.filterIsInstance<KSPropertyDeclaration>().toList()
        if (elements.isNotEmpty()) {
            try {
                parseAutowired(elements)
            } catch (e: Exception) {
                logger.exception(e)
            }
        }
        return emptyList()
    }


    private fun parseAutowired(elements: List<KSPropertyDeclaration>) {
        val categories = categories(elements)
        val any = ClassName("kotlin", "Any")
        val objParameterSpec = ParameterSpec.builder("target", any.copy(nullable = false)).build()
        var packageName: String
        val fileDependencies = mutableSetOf<KSFile>()
        categories.forEach { (key, value) ->
            packageName = key.qualifiedName!!.asString().substring(0, key.qualifiedName!!.asString().lastIndexOf("."))
            val fileName = "${key.simpleName.asString()}$NAME_OF_AUTOWIRED"
            val typeSpecBuild = TypeSpec.classBuilder(fileName).addSuperinterface(ISYRINGE.quantifyNameToClassName())
                .addKdoc(CodeBlock.of(WARNING_TIPS))

            val target = key.qualifiedName!!.asString().quantifyNameToClassName()
            val funSpecBuild = FunSpec.builder("inject").addModifiers(KModifier.OVERRIDE).addParameter(objParameterSpec)
                .addStatement("val substitute = target as %T", target)
            val routeType = key.routeType
            value.forEach { field ->
                val autowired = field.findAnnotationWithType<Autowired>()
                val fieldName = field.simpleName.asString()
                val fieldType = field.getKotlinPoetTTypeGeneric().copy(nullable = false)
                val isActivity: Boolean
                if (routeType == RouterType.ACTIVITY) {  // Activity, then use getIntent()
                    isActivity = true
                } else if (routeType == RouterType.FRAGMENT) {   // Fragment, then use getArguments()
                    isActivity = false
                } else {
                    error("The field [$fieldName] need autowired from intent, its parent must be activity or fragment!")
                }
                val isNullable = field.type.resolve().isMarkedNullable
                val intent = if (isActivity) "substitute.intent.extras" else "substitute.arguments"
                val name = autowired!!.name.ifEmpty { fieldName }
                val originalValue = "substitute.$fieldName"
                val codeBlock =
                    buildCodeBlock(intent, originalValue, field.typeExchange(), isActivity, isNullable, fieldType, name)
                funSpecBuild.addCode(codeBlock)
                // Validator
                if (autowired.required) {  // Primitive wont be check.
                    funSpecBuild.beginControlFlow("if (null == substitute.$fieldName)")
                    funSpecBuild.addStatement(
                        "%T.e(\"$TAG\", \"The field '$fieldName' is null, in class %T\")",
                        AndroidLog,
                        target
                    )
                    funSpecBuild.endControlFlow()
                }
            }
            typeSpecBuild.addFunction(funSpecBuild.build())
            key.containingFile?.let {
                fileDependencies.add(it)
            }
            val file = FileSpec.builder(packageName, fileName).addType(typeSpecBuild.build()).build()
            file.writeTo(codeGenerator, true, fileDependencies)
        }
    }

    /**
     * Categories field, find his papa.
     *
     * @param elements Field need autowired
     */
    private fun categories(elements: List<KSPropertyDeclaration>): MutableMap<KSClassDeclaration, MutableList<KSPropertyDeclaration>> {
        val parentAndChildren = mutableMapOf<KSClassDeclaration, MutableList<KSPropertyDeclaration>>()
        for (element in elements) {
            // Class of the member
            if (element.parentDeclaration !is KSClassDeclaration) {
                error("Property annotated with @Autowired 's enclosingElement(property's class) must be non-null!")
            }
            val parent = element.parentDeclaration as KSClassDeclaration

            if (element.modifiers.contains(com.google.devtools.ksp.symbol.Modifier.PRIVATE)) {
                throw IllegalAccessException(
                    "The inject fields CAN NOT BE 'private'!!! please check field ["
                            + element.simpleName.asString() + "] in class [" + parent.qualifiedName?.asString() + "]"
                )
            }
            if (parentAndChildren.containsKey(parent)) {
                parentAndChildren[parent]!!.add(element)
            } else {
                parentAndChildren[parent] = mutableListOf(element)
            }
        }
        return parentAndChildren
    }

    /**
     * Build param inject codeBlock
     */
    private fun buildCodeBlock(
        intent: String, originalValue: String, type: Int,
        isActivity: Boolean, isNullable: Boolean, fieldType: TypeName, name: String
    ): CodeBlock {
        val build = ClassName("android.os", "Build")
        return when (TypeKind.values()[type]) {
            TypeKind.BOOLEAN ->
                CodeBlock.builder().addStatement(
                    "$originalValue = $intent?.getBoolean(%S, " + originalValue + if (isNullable) " ?: false)" else ") ?: false",
                    name
                ).build()

            TypeKind.BYTE ->
                CodeBlock.builder().addStatement(
                    "$originalValue = $intent?.getByte(%S, " + originalValue + if (isNullable) " ?: 0)" else ") ?: 0", name
                ).build()

            TypeKind.SHORT ->
                CodeBlock.builder().addStatement(
                    "$originalValue = $intent?.getShort(%S, " + originalValue + if (isNullable) " ?: 0)" else ") ?: 0", name
                ).build()

            TypeKind.INT ->
                CodeBlock.builder().addStatement(
                    "$originalValue = $intent?.getInt(%S, " + originalValue + if (isNullable) " ?: 0)" else ") ?: 0", name
                ).build()

            TypeKind.LONG ->
                CodeBlock.builder().addStatement(
                    "$originalValue = $intent?.getLong(%S, " + originalValue + if (isNullable) " ?: 0L)" else ") ?: 0L", name
                ).build()

            TypeKind.CHAR ->
                CodeBlock.builder().addStatement(
                    "$originalValue = $intent?.getChar(%S, " + originalValue + if (isNullable) " ?: 0)" else ") ?: 0", name
                ).build()

            TypeKind.FLOAT ->
                CodeBlock.builder().addStatement(
                    "$originalValue = $intent?.getFloat(%S, " + originalValue + if (isNullable) " ?: 0F)" else ") ?: 0F", name
                ).build()

            TypeKind.DOUBLE ->
                CodeBlock.builder().addStatement(
                    "$originalValue = $intent?.getDouble(%S, " + originalValue + if (isNullable) " ?: 0.0)" else ") ?: 0.0", name
                ).build()

            TypeKind.STRING ->
                CodeBlock.builder().addStatement(
                    "$originalValue = $intent?.getString(%S " + if (isActivity) ") ?: \"\"" else ", $originalValue)" + if (isNullable) "" else " ?: \"\"",
                    name
                ).build()

            TypeKind.SERIALIZABLE ->
                CodeBlock.builder()
                    .addStatement("$originalValue =")
                    .beginControlFlow("if (%T.VERSION.SDK_INT >= 33)", build)
                    .addStatement("$intent?.getSerializable(%S, %T::class.java)" + if (isNullable) "" else " ?: $originalValue", name, fieldType)
                    .nextControlFlow("else")
                    .addStatement("$intent?.getSerializable(%S)?.let { it as %T }" + if (isNullable) "" else " ?: $originalValue", name, fieldType)
                    .endControlFlow()
                    .build()

            TypeKind.PARCELABLE ->
                CodeBlock.builder()
                    .addStatement("$originalValue =")
                    .beginControlFlow("if (%T.VERSION.SDK_INT >= 33)", build)
                    .addStatement("$intent?.getParcelable(%S, %T::class.java)" + if(isNullable) "" else " ?: $originalValue", name, fieldType)
                    .nextControlFlow("else")
                    .addStatement("$intent?.getParcelable<%T>(%S)" + if(isNullable) "" else " ?: $originalValue",  fieldType, name)
                    .endControlFlow()
                    .build()

            TypeKind.UNKNOWN -> throw IllegalArgumentException("this is unsupported type")
        }
    }
}