package com.mm.annotation

/**
 * router path
 * @param value path of router
 * @param des description
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CLASS)
annotation class RouterPath(val value: String, val des: String = "")