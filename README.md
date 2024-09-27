# router

Activity Result API 方式启动的路由管理器

```groovy
implementation 'io.github.ma-jian:router-api:1.0.5'

// kapt处理
kapt 'io.github.ma-jian:router-compiler:1.0.5'
// or ksp  kt代码优先使用ksp方式提升编译速度
plugins {
    id 'com.google.devtools.ksp'
}

ksp 'io.github.ma-jian:router-ksp:1.0.5'
```

### **CHANGELOG**

#### v1.0.5

1. 修改@Autowired 字段为必填时的错误提示
2. 修改其他bug

#### v1.0.3

1. 新增注解[RouterInterceptor](router-annotation/src/main/java/com/mm/router/annotation/RouterInterceptor.kt) 路由拦截器，支持路由的自定义拦截
   和路由拦截回调
2. 新增 ksp 注解处理逻辑，原生生成kotlin代码提升编译速度
3. 修改注解[ServiceProvider](router-annotation/src/main/java/com/mm/router/annotation/ServiceProvider.kt)处理逻辑，移除接口必须继承 IProvider 的限制
4. 修改其他bug

#### v1.0

1. [RouterPath](router-annotation/src/main/java/com/mm/router/annotation/RouterPath.kt) 路由地址注册页面路径
2. [ServiceProvider](router-annotation/src/main/java/com/mm/router/annotation/ServiceProvider.kt) 提供对外接口能力
3. [Autowired](router-annotation/src/main/java/com/mm/router/annotation/Autowired.kt) 对标记字段自动赋值,需要在赋值页面注册
   Router.init(this).autoWired(this)

路由启动页面

startActivity

```kotlin
Router.init(this).open("com.mm.second").navigation()
```

startActivityForResult

```kotlin
Router.init().open(Router.Path.ACTION_CONTENT).navigation() {
    if (it.resultCode == RESULT_OK) {
        textView.text = textView.text.toString() + "\n ${it.data}"
    }
}
```
获取拦截器结果
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

两种获取接口的方式

@RouterPath 该方式获取Service 接口必须继承[IProvider](router-api/src/main/java/com/mm/router/IProvider.kt)

```kotlin
@RouterPath(value = "/router/service/autowired", des = "自动注册赋值")
class AutowiredServiceImpl : AutowiredService {
    //...
}

val autowiredService = Router.init(this).open("/router/service/autowired").doProvider<AutowiredService>()
```

@ServiceProvider 该方式无需接口继承IProvider，但必须是接口实现类

```kotlin
/**
 * 标记对外接口
 */
@ServiceProvider("/service/provider")
class ServiceProviderImpl (
    private val string: String,
    private val int: Int,
    private val log: Long,
    private val bol: Boolean
) : IServiceProvider {
    //...
}

//获取接口实例
val provider = Router.init().open("/service/provider").doProvider<IServiceProvider>("",1,2L,false)
```

@RouterInterceptor

```kotlin
@RouterInterceptor("/router/path/match", priority = 1, des = "路由拦截器")
class PathInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain, intent: Intent) {

    }
}
```


