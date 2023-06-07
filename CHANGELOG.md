
### **CHANGELOG**

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