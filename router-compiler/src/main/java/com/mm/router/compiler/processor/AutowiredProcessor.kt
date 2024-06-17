package com.mm.router.compiler.processor

import com.mm.router.annotation.Autowired
import com.mm.router.compiler.BaseAbstractProcessor
import com.mm.router.compiler.inter.IProcessor
import com.mm.router.compiler.util.TypeKind
import com.mm.router.compiler.util.TypeUtils
import com.squareup.javapoet.AnnotationSpec
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.ParameterSpec
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.TypeSpec
import javax.annotation.processing.Filer
import javax.annotation.processing.Messager
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeMirror
import javax.lang.model.util.Elements
import javax.lang.model.util.Types
import javax.tools.Diagnostic


/**
 * Processor used to create autowired helper
 * @since 1.0
 *
 */
class AutowiredProcessor : IProcessor {
    private lateinit var messager: Messager
    private lateinit var elementUtils: Elements
    private lateinit var typeUtils: TypeUtils
    private lateinit var types: Types
    private lateinit var filer: Filer

    private val TAG = "Router"
    private val NAME_OF_AUTOWIRED = "\$\$Autowired"
    private val WARNING_TIPS = "DO NOT EDIT THIS FILE!!! IT WAS GENERATED BY ROUTER."
    private val AndroidLog = ClassName.get("android.util", "Log")

    override fun process(roundEnv: RoundEnvironment, abstractProcessor: BaseAbstractProcessor) {
        messager = abstractProcessor.mMessager
        elementUtils = abstractProcessor.mElements
        typeUtils = abstractProcessor.mTypeUtils
        types = abstractProcessor.mTypes
        filer = abstractProcessor.mFiler
        val elements: Set<Element>? = roundEnv.getElementsAnnotatedWith(Autowired::class.java)
        if (elements?.isNotEmpty() == true) {
            val categories = categories(roundEnv.getElementsAnnotatedWith(Autowired::class.java))

            val typeISyringe = elementUtils.getTypeElement(IProcessor.ISYRINGE)
            val activityTm: TypeMirror = elementUtils.getTypeElement(IProcessor.ACTIVITY).asType()
            val fragmentX: TypeMirror = elementUtils.getTypeElement(IProcessor.FRAGMENT_X).asType()
            val fragmentTm: TypeMirror = elementUtils.getTypeElement(IProcessor.FRAGMENT).asType()
            val objectParamSpec = ParameterSpec.builder(TypeName.OBJECT, "target").build()

            for (entry in categories) {
                // Build method : 'inject'
                val parent = entry.key
                val child: List<Element> = entry.value
                val qualifiedName = parent.qualifiedName.toString()
                val packageName = qualifiedName.substring(0, qualifiedName.lastIndexOf("."))
                val fileName = "${parent.simpleName}$NAME_OF_AUTOWIRED"

                val helper: TypeSpec.Builder =
                    TypeSpec.classBuilder(fileName).addJavadoc(WARNING_TIPS).addSuperinterface(ClassName.get(typeISyringe))
                        .addModifiers(Modifier.PUBLIC)

                val injectMethodBuilder =
                    MethodSpec.methodBuilder("inject")
                        .addAnnotation(AnnotationSpec.builder(SuppressWarnings::class.java).addMember("value","\$S","DEPRECATION").build())
                        .addAnnotation(Override::class.java).addModifiers(Modifier.PUBLIC)
                        .addParameter(objectParamSpec)
                        .addStatement("\$T substitute = (\$T)target", ClassName.get(parent), ClassName.get(parent))
                // Generate method body, start inject.
                for (element in child) {
                    val fieldConfig = element.getAnnotation(Autowired::class.java)
                    val fieldName = element.simpleName.toString()
                    val originalValue = "substitute.$fieldName"
                    var statement = "substitute." + fieldName + " = " + buildCastCode(element) + "substitute."
                    var isActivity = false
                    if (types.isSubtype(parent.asType(), activityTm)) {  // Activity, then use getIntent()
                        isActivity = true
                        statement += "getIntent()."
                    } else if (types.isSubtype(parent.asType(), fragmentX) || types.isSubtype(
                            parent.asType(), fragmentTm
                        )
                    ) {   // Fragment, then use getArguments()
                        statement += "getArguments()."
                    } else {
                        error("The field [$fieldName] need autowired from intent, its parent must be activity or fragment!")
                    }
                    statement = buildStatement(originalValue, statement, typeUtils.typeExchange(element), isActivity)
                    injectMethodBuilder.addStatement(statement, fieldConfig.name.ifEmpty { fieldName })
                    // Validator
                    if (fieldConfig.required && !element.asType().kind.isPrimitive) {  // Primitive wont be check.
                        injectMethodBuilder.beginControlFlow("if (null == substitute.$fieldName)")
                        injectMethodBuilder.addStatement(
                            "throw new IllegalArgumentException(\"The field '$fieldName' is required and cannot be null," +
                                    " in class \" + \$T.class)", ClassName.get(parent))
                        injectMethodBuilder.endControlFlow()
                    }
                }
                helper.addMethod(injectMethodBuilder.build())
                JavaFile.builder(packageName, helper.build()).build().writeTo(filer)
            }
        }
    }

    private fun buildCastCode(element: Element): String {
        return if (typeUtils.typeExchange(element) == TypeKind.SERIALIZABLE.ordinal) {
            CodeBlock.builder().add("(\$T) ", ClassName.get(element.asType())).build().toString()
        } else ""
    }

    /**
     * Build param inject statement
     */
    private fun buildStatement(originalValue: String, statement: String, type: Int, isActivity: Boolean): String {
        var code: String = statement
        code += when (TypeKind.values()[type]) {
            TypeKind.BOOLEAN -> "getBoolean" + (if (isActivity) "Extra" else "") + "(\$S, " + originalValue + ")"
            TypeKind.BYTE -> "getByte" + (if (isActivity) "Extra" else "") + "(\$S, " + originalValue + ")"
            TypeKind.SHORT -> "getShort" + (if (isActivity) "Extra" else "") + "(\$S, " + originalValue + ")"
            TypeKind.INT -> "getInt" + (if (isActivity) "Extra" else "") + "(\$S, " + originalValue + ")"
            TypeKind.LONG -> "getLong" + (if (isActivity) "Extra" else "") + "(\$S, " + originalValue + ")"
            TypeKind.CHAR -> "getChar" + (if (isActivity) "Extra" else "") + "(\$S, " + originalValue + ")"
            TypeKind.FLOAT -> "getFloat" + (if (isActivity) "Extra" else "") + "(\$S, " + originalValue + ")"
            TypeKind.DOUBLE -> "getDouble" + (if (isActivity) "Extra" else "") + "(\$S, " + originalValue + ")"
            TypeKind.STRING -> (if (isActivity) "getExtras() == null ? $originalValue : substitute.getIntent().getExtras().getString(\$S" else "getString(\$S") + ", " + originalValue + ")"
            TypeKind.SERIALIZABLE -> (if (isActivity) "getSerializableExtra(\$S)" else "getSerializable(\$S)")
            TypeKind.PARCELABLE -> (if (isActivity) "getParcelableExtra(\$S)" else "getParcelable(\$S)")
            TypeKind.UNKNOWN -> throw IllegalArgumentException("this is unsupported type")
        }
        return code
    }


    /**
     * Categories field, find his papa.
     *
     * @param elements Field need autowired
     */
    @Throws(IllegalAccessException::class)
    private fun categories(elements: Set<Element>): HashMap<TypeElement, ArrayList<Element>> {
        val parentAndChild: HashMap<TypeElement, ArrayList<Element>> = HashMap()
        if (elements.isNotEmpty()) {
            for (element: Element in elements) {
                val enclosingElement = element.enclosingElement as TypeElement
                if (element.modifiers.contains(Modifier.PRIVATE)) {
                    throw IllegalAccessException(
                        "The inject fields CAN NOT BE 'private'!!! please check field [" + element.simpleName + "] in class [" + enclosingElement.qualifiedName + "]," +
                                "please add @JvmField"
                    )
                }
                if (parentAndChild.containsKey(enclosingElement)) { // Has categries
                    parentAndChild[enclosingElement]?.add(element)

                } else {
                    parentAndChild[enclosingElement] = arrayListOf(element)
                }
            }
        }
        return parentAndChild
    }

    private fun error(error: String?) {
        messager.printMessage(Diagnostic.Kind.ERROR, this.javaClass.canonicalName + " : " + error)
    }

    private fun info(info: String) {
        messager.printMessage(Diagnostic.Kind.WARNING, this.javaClass.canonicalName + " : " + info)
    }

}