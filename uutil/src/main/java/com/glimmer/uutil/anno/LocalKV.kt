package com.glimmer.uutil.anno

@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
annotation class LocalKV(val key: String, val defaultValue: String)