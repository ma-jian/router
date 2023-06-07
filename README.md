# router

Activity Result API 方式启动的路由管理器

* 支持解析标准URL进行跳转，并自动注入参数到目标页面中
* 支持多模块使用
* 支持添加多个拦截器，自定义拦截顺序
* 支持获取Fragment
* 支持获取服务接口，方便多模块间通信
* 支持两种注解处理方式ksp(kt代码)、kapt(java代码)
* 页面、拦截器、服务等组件均自动注册到框架

[**CHANGELOG**](CHANGELOG.md)

1、添加依赖和配置
* apt方式
```groovy
android {
    defaultConfig {
        //...
        javaCompileOptions {
            annotationProcessorOptions {
                arguments = [moduleName: project.getName()]
            }
        }
    }
}

kapt 'io.github.ma-jian:router-compiler:1.0.3'
implementation 'io.github.ma-jian:router-api:1.0.3'
```
* ksp方式 (kt代码优先使用ksp方式提升编译速度)
```
plugins {
    id 'com.google.devtools.ksp'
}

ksp {
    arg("moduleName", project.getName())
}

ksp 'io.github.ma-jian:router-ksp:1.0.3'
implementation 'io.github.ma-jian:router-api:1.0.3'
```

2、路由启动页面

* startActivity

```kotlin
Router.init(this).open("com.mm.second").navigation()
```

* startActivityForResult

```kotlin
Router.init().open(Router.Path.ACTION_CONTENT).navigation() {
    if (it.resultCode == RESULT_OK) {
        textView.text = textView.text.toString() + "\n ${it.data}"
    }
}
```
* 拦截器Result回调
```kotlin
Router.init().open("com.mm.second").navigationResult {
    //路由执行完毕
    it.onArrival { result ->
        //...
    }
    //路由被中断、通过在[Interceptor]中执行chain.interrupt()方法
    it.onInterrupt {
        //...
    }
}
```

3、获取服务接口

* @RouterPath 方式获取Service，接口必须继承[IProvider](router-api/src/main/java/com/mm/router/IProvider.kt)

```kotlin
@RouterPath(value = "/router/service/autowired", des = "自动注册赋值")
class AutowiredServiceImpl : AutowiredService {
    //...
}

val autowiredService = Router.init(this).open("/router/service/autowired").doProvider<AutowiredService>()
```

* @ServiceProvider 方式无需接口继承IProvider，但必须是接口实现类

```kotlin
/**
 * 标记对外接口
 */
@ServiceProvider("/service/provider")
class ServiceProviderImpl constructor(
    private val string: String,
    private val int: Int,
    private val log: Long,
    private val bol: Boolean
) : IServiceProvider {
    //...
}

//获取接口实例
val provider = Router.init().open("/service/provider").doProvider<IServiceProvider>()
```

4、自定义路由拦截器 @RouterInterceptor

```kotlin
@RouterInterceptor("/router/path/match", priority = 1, des = "路由拦截器")
class PathInterceptor : Interceptor {
    /**
     * @param chain 拦截器信息
     * @param intent 路由跳转intent
     */
    override fun intercept(chain: Interceptor.Chain, intent: Intent) {
        //拦截跳转
        if (meta.path == "com.mm.second") {
            chain.interrupt()
            return
        }
        //继续执行下一个拦截器，没有则进行跳转
        chain.proceed(meta, intent)
    }
}
```

5、添加混淆规则
```
-keep class * implements com.mm.router.interceptor.Interceptor { *; }
-keep class * implements com.mm.router.IRouterRulesCreator { *; }
-keep class * implements com.mm.router.IRouterInterceptor { *; }
-keep class * implements com.mm.router.ISyringe { *; }
-keep class * implements com.mm.router.IProvider { *; }
```