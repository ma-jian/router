package com.mm.router_compiler.util

/**
 * type kind
 */
enum class TypeKind {
    // Base type
    BOOLEAN, BYTE, SHORT, INT, LONG, CHAR, FLOAT, DOUBLE,  // Other type
    STRING, SERIALIZABLE, PARCELABLE, UNKNOWN
}