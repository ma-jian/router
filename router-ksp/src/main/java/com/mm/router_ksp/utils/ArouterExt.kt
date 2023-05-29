package com.mm.router_ksp.utils


import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.mm.annotation.model.RouterType

//序列化
const val PARCELABLE = "android.os.Parcelable"
const val SERIALIZABLE = "java.io.Serializable"
const val KBYTE = "kotlin.Byte"
const val KSHORT = "kotlin.Short"
const val KINTEGER = "kotlin.Int"
const val KLONG = "kotlin.Long"
const val KFLOAT = "kotlin.Float"
const val KDOUBEL = "kotlin.Double"
const val KBOOLEAN = "kotlin.Boolean"
const val KCHAR = "kotlin.Char"
const val KSTRING = "kotlin.String"

const val ACTIVITY = "android.app.Activity"
const val ACTIVITY_ANDROIDX = "androidx.appcompat.app.AppCompatActivity"
const val FRAGMENT_ANDROIDX = "androidx.fragment.app.Fragment"
const val FRAGMENT = "android.app.Fragment"
const val FRAGMENT_V4 = "android.support.v4.app.Fragment"
const val SERVICE = "android.app.Service"
const val ISYRINGE = "com.mm.router.ISyringe"
const val IPROVIDER = "com.mm.router.IProvider"
const val IROUTER_CREATOR = "com.mm.router.IRouterRulesCreator"

const val KEY_MODULE_NAME = "moduleName"

const val NO_MODULE_NAME_TIPS_KSP = "These no module name, at 'build.gradle', like :\n" +
        "ksp {\n" +
        "    arg(\"moduleName\", project.getName())\n" +
        "}\n"

/**
 * AutoWire Inject Field Type check and convert
 * */
internal fun KSPropertyDeclaration.typeExchange(): Int {
    val type = this.type.resolve()
    return when (type.declaration.qualifiedName?.asString()) {
        KBYTE -> TypeKind.BYTE.ordinal
        KSHORT -> TypeKind.SHORT.ordinal
        KINTEGER -> TypeKind.INT.ordinal
        KLONG -> TypeKind.LONG.ordinal
        KFLOAT -> TypeKind.FLOAT.ordinal
        KDOUBEL -> TypeKind.DOUBLE.ordinal
        KBOOLEAN -> TypeKind.BOOLEAN.ordinal
        KCHAR -> TypeKind.CHAR.ordinal
        KSTRING -> TypeKind.STRING.ordinal
        else -> {
            when (this.isSubclassOf(listOf(PARCELABLE, SERIALIZABLE))) {
                0 -> TypeKind.PARCELABLE.ordinal
                1 -> TypeKind.SERIALIZABLE.ordinal
                else -> TypeKind.UNKNOWN.ordinal
            }
        }
    }
}

/**
 *  Find module name from ksp arguments, please add this config
 *  " ksp { arg("moduleName", project.getName()) } "
 *  in your module's build.gradle
 * */
@Suppress("SpellCheckingInspection")
internal fun Map<String, String>.findModuleName(logger: KSPLogger): String {
    val name = this[KEY_MODULE_NAME]
    return if (!name.isNullOrEmpty()) {
        @Suppress("RegExpSimplifiable") name.replace("[^0-9a-zA-Z_]+".toRegex(), "")
    } else {
        logger.error(NO_MODULE_NAME_TIPS_KSP)
        throw RuntimeException("Router::Compiler >>> No module name, for more information, look at gradle log.")
    }
}

private val ROUTE_TYPE_LIST = listOf(
    ACTIVITY,// 0
    ACTIVITY_ANDROIDX, // 1
    FRAGMENT, // 2
    FRAGMENT_V4, // 3
    FRAGMENT_ANDROIDX, // 4
    SERVICE, // 5
    IPROVIDER // 6
)

internal val KSClassDeclaration.routeType: RouterType
    get() = when (isSubclassOf(ROUTE_TYPE_LIST)) {
        0, 1 -> RouterType.ACTIVITY
        2, 3, 4 -> RouterType.FRAGMENT
        5 -> RouterType.SERVICE
        6 -> RouterType.PROVIDER
        else -> RouterType.UNKNOWN
    }
