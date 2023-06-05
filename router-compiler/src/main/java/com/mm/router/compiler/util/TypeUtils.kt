package com.mm.router.compiler.util

import javax.lang.model.element.Element
import javax.lang.model.type.TypeMirror
import javax.lang.model.util.Elements
import javax.lang.model.util.Types

/**
 ** @version 1.0
 */
class TypeUtils(private val types: Types, elements: Elements) {
    private val parcelableType: TypeMirror
    private val serializableType: TypeMirror

    init {
        parcelableType = elements.getTypeElement(PARCELABLE).asType()
        serializableType = elements.getTypeElement(SERIALIZABLE).asType()
    }

    /**
     * @param element Raw type
     * @return Type class of java
     */
    fun typeExchange(element: Element): Int {
        val typeMirror = element.asType()

        // Primitive
        return if (typeMirror.kind.isPrimitive) {
            element.asType().kind.ordinal
        } else when (typeMirror.toString()) {
            BYTE -> TypeKind.BYTE.ordinal
            SHORT -> TypeKind.SHORT.ordinal
            INTEGER -> TypeKind.INT.ordinal
            LONG -> TypeKind.LONG.ordinal
            FLOAT -> TypeKind.FLOAT.ordinal
            DOUBLE -> TypeKind.DOUBLE.ordinal
            BOOLEAN -> TypeKind.BOOLEAN.ordinal
            CHAR -> TypeKind.CHAR.ordinal
            STRING -> TypeKind.STRING.ordinal
            else ->
                if (types.isSubtype(typeMirror, parcelableType)) {
                    // PARCELABLE
                    TypeKind.PARCELABLE.ordinal
                } else if (types.isSubtype(typeMirror, serializableType)) {
                    // SERIALIZABLE
                    TypeKind.SERIALIZABLE.ordinal
                } else {
                    TypeKind.UNKNOWN.ordinal
                }
        }
    }

    companion object {
        //序列化
        const val PARCELABLE = "android.os.Parcelable"
        const val SERIALIZABLE = "java.io.Serializable"

        // Java type
        const val BYTE = "java.lang.Byte"
        const val SHORT = "java.lang.Short"
        const val INTEGER = "java.lang.Integer"
        const val LONG = "java.lang.Long"
        const val FLOAT = "java.lang.Float"
        const val DOUBLE = "java.lang.Double"
        const val BOOLEAN = "java.lang.Boolean"
        const val CHAR = "java.lang.Character"
        const val STRING = "java.lang.String"
    }
}