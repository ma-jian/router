@file:OptIn(KspExperimental::class)

package com.mm.router.ksp.utils

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.symbol.Origin.KOTLIN
import com.google.devtools.ksp.symbol.Origin.KOTLIN_LIB
import com.mm.router.annotation.ServiceProvider
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.toTypeParameterResolver
import com.squareup.kotlinpoet.tags.TypeAliasTag

@OptIn(KspExperimental::class)
@Suppress("unused")
internal fun KSClassDeclaration.isKotlinClass(): Boolean {
    return origin == KOTLIN ||
            origin == KOTLIN_LIB ||
            isAnnotationPresent(Metadata::class)
}

internal inline fun <reified T : Annotation> KSAnnotated.findAnnotationWithType(): T? {
    return getAnnotationsByType(T::class).firstOrNull()
}

internal inline fun KSPLogger.check(condition: Boolean, message: () -> String) {
    check(condition, null, message)
}

internal inline fun KSPLogger.check(condition: Boolean, element: KSNode?, message: () -> String) {
    if (!condition) {
        error(message(), element)
    }
}

internal fun KSClassDeclaration.isClassKind(kind: ClassKind): Boolean {
    when (val declaration = superTypes.toMutableList().first().resolve().declaration) {
        is KSClassDeclaration -> {
            return declaration.classKind == kind
        }
    }
    return false
}

internal fun KSClassDeclaration.kindType(): String {
    when (val declaration = superTypes.toMutableList().first().resolve().declaration) {
        is KSClassDeclaration -> {
            return declaration.classKind.type
        }
        is KSFunctionDeclaration -> {
            return declaration.functionKind.name
        }
    }
    return "null"
}

/**
 * Judge whether a class [KSClassDeclaration] is a subclass of another class [superClassName]
 * https://www.raywenderlich.com/33148161-write-a-symbol-processor-with-kotlin-symbol-processing
 * */
internal fun KSClassDeclaration.isSubclassOf(
    superClassName: String,
): Boolean {
    val superClasses = superTypes.toMutableList()
    while (superClasses.isNotEmpty()) {
        val current: KSTypeReference = superClasses.first()
        val declaration: KSDeclaration = current.resolve().declaration
        when {
            declaration is KSClassDeclaration && declaration.qualifiedName?.asString() == superClassName -> {
                return true
            }

            declaration is KSClassDeclaration -> {
                superClasses.removeAt(0)
                superClasses.addAll(0, declaration.superTypes.toList())
            }

            else -> {
                superClasses.removeAt(0)
            }
        }
    }
    return false
}

internal fun KSClassDeclaration.isSubclassOf(superClassNames: List<String>): Int {
    val superClasses = superTypes.toMutableList()
    while (superClasses.isNotEmpty()) {
        val current: KSTypeReference = superClasses.first()
        val declaration: KSDeclaration = current.resolve().declaration
        when {
            declaration is KSClassDeclaration && (superClassNames.indexOf(declaration.qualifiedName?.asString())) != -1 -> {
                return superClassNames.indexOf(declaration.qualifiedName?.asString())
            }

            declaration is KSClassDeclaration -> {
                superClasses.removeAt(0)
                superClasses.addAll(0, declaration.superTypes.toList())
            }

            else -> {
                superClasses.removeAt(0)
            }
        }
    }
    return -1
}

internal fun KSPropertyDeclaration.isSubclassOf(superClassName: String): Boolean {
    val propertyType = type.resolve().declaration
    return if (propertyType is KSClassDeclaration) {
        propertyType.isSubclassOf(superClassName)
    } else {
        false
    }
}

internal fun KSPropertyDeclaration.isSubclassOf(superClassNames: List<String>): Int {
    val propertyType = type.resolve().declaration
    return if (propertyType is KSClassDeclaration) {
        propertyType.isSubclassOf(superClassNames)
    } else {
        -1
    }
}

internal fun String.quantifyNameToClassName(): ClassName {
    val index = lastIndexOf(".")
    return ClassName(substring(0, index), substring(index + 1, length))
}

/**
 *  such: val map = Map<String, String> ==> Map<String, String> (used for kotlinpoet for %T)
 * */
internal fun KSPropertyDeclaration.getKotlinPoetTTypeGeneric(): TypeName {
    val classTypeParams = this.typeParameters.toTypeParameterResolver()
    val typeName = this.type.toTypeName(classTypeParams)
    // Fix: typealias-handling
    // https://square.github.io/kotlinpoet/interop-ksp/#typealias-handling
    // Alias class -> such as: var a = arrayList<String>() -> ArrayList<String>
    typeName.tags[TypeAliasTag::class]?.let {
        val typeAliasTag = (it as? TypeAliasTag)?.abbreviatedType
        if (typeAliasTag != null) {
            return typeAliasTag
        }
    }
    return typeName
}

/**
 *  such: val map = Map<String, String> ==> Map (used for kotlinpoet for %T)
 * */
@Suppress("unused")
internal fun KSPropertyDeclaration.getKotlinPoetTType(): TypeName {
    return this.type.resolve().toTypeName()
}

fun KSClassDeclaration.pairServices(name: String): KSType {
    val provider = annotations.find {
        it.shortName.asString() == ServiceProvider::class.java.simpleName && it.annotationType.resolve().declaration
            .qualifiedName?.asString() == ServiceProvider::class.java.canonicalName
    }
    return provider?.arguments?.find { it.name!!.getShortName() == name }?.value as KSType
}

@Suppress("UNCHECKED_CAST")
fun KSClassDeclaration.pairParams(name: String): List<KSType> {
    val provider = annotations.find {
        it.shortName.asString() == ServiceProvider::class.java.simpleName && it.annotationType.resolve().declaration
            .qualifiedName?.asString() == ServiceProvider::class.java.canonicalName
    }
    return provider?.arguments?.find { it.name!!.getShortName() == name }!!.value as List<KSType>
}

fun KSClassDeclaration.pairString(name: String): String {
    val provider = annotations.find {
        it.shortName.asString() == ServiceProvider::class.java.simpleName && it.annotationType.resolve().declaration
            .qualifiedName?.asString() == ServiceProvider::class.java.canonicalName
    }
    return provider?.arguments?.find { it.name!!.getShortName() == name }?.value?.toString() ?: ""
}