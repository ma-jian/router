# router
Activity Result API 方式启动的路由管理器


```
implementation 'io.github.ma-jian:router-api:1.0.1'
kapt 'io.github.ma-jian:router-compiler:1.0'
```

### **CHANGELOG**
#### v1.0
1. [RouterPath](router_annotation/src/main/java/com/mm/annotation/RouterPath.kt) 路由地址注册页面路径
2. [ServiceProvider](router_annotation/src/main/java/com/mm/annotation/ServiceProvider.kt) 提供对外接口能力
3. [Autowired](router_annotation/src/main/java/com/mm/annotation/Autowired.kt) 对标记字段自动赋值,需要在赋值页面注册 Router.init(this).autoWired(this)

路由启动页面

startActivity
``` kotlin
Router.init(this).open("com.mm.second").navigation()
```
startActivityForResult
``` kotlin
Router.init().open(Router.Path.ACTION_CONTENT).navigation() {
    if (it.resultCode == RESULT_OK) {                
        textView.text = textView.text.toString() + "\n ${it.data}"
    }
}
```

两种获取接口的方式

@RouterPath
``` kotlin
@RouterPath(value = "/router/service/autowired", des = "自动注册赋值")
class AutowiredServiceImpl : AutowiredService {
    ...
}

val autowiredService = Router.init(this).open("/router/service/autowired").doProvider<AutowiredService>()
```

@ServiceProvider
``` kotlin
/**
 * 标记对外接口
 * params 构造参数类型，通过doProvider() 传入参数
 */
@ServiceProvider(returnType = IServiceProvider::class, para = [])
class ServiceProvider : IServiceProvider {
    ...
}

//获取接口实例
val provider = Router.init().open(IServiceProvider::class.java).doProvider<IServiceProvider>()
```


