#router
-keep class * implements com.mm.router.interceptor.Interceptor { *; }
-keep class * implements com.mm.router.IRouterRulesCreator { *; }
-keep class * implements com.mm.router.IRouterInterceptor { *; }
-keep class * implements com.mm.router.ISyringe { *; }
-keep class * implements com.mm.router.IProvider { *; }
# 保持所有被 @ServiceProvider 标注的类不被混淆
-keep @interface com.mm.router.annotation.ServiceProvider
-keep @com.mm.router.annotation.ServiceProvider class * {
    *;
}