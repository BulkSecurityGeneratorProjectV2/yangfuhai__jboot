# Jboot API 文档生成

在 Jboot 中内置了 3 个注解，用于生成帮助开发者生成 API 文档。它们分别是

- @Api  ：给 Controller 配置，一个 Controller 生成一个文档文件。
- @ApiOper ： 给 Controller 的方法配置。
- @ApiPara ：给 Controller 的参数进行配置。

## 基本使用

生成文档的过程，需要自己写一个 `main()` 方法，然后通过 ApiDocManager 来生成，代码如下：

```java
public class ApiDocGenerator {

    public static void main(String[] args) {

        ApiDocConfig config = new ApiDocConfig();
        config.setBasePath("./doc/api");

        ApiDocManager.me().genDocs(config);
    }
}
```

Controller 代码如下：

```java
@RequestMapping("/api/user")
@Api("用户相关API")
public class UserApiController extends ApiControllerBase {

    @Inject
    private UserService userService;


    @PostRequest
    @ApiOper("用户登录")
    public Ret login(@ApiPara(value = "登录账户", notes = "可以是邮箱") @NotNull String loginAccount
            , @ApiPara("登录密码") @NotNull String password) {
        //....
    }


    @ApiOper("用户详情")
    public Ret detail(@ApiPara("用户ID") @NotNull Long id) {
        //....
    }


    @ApiOper("更新用户信息")
    public Ret update(@ApiPara("用户 json 信息") @JsonBody @NotNull User user) {
        //....
    }
}    
```

默认情况下，` ApiDocManager.me().genDocs(config)` 生成的是 Markdown 文档，内容如下：

![](./static/images/apidoc.jpg)

## 不同的文档生成在不同的目录

一般情况下，如下的代码会去找到所有带有 `@Api` 注解的 `Controller` 生成文档，并生成在同一个目录：

```java
public class ApiDocGenerator {

    public static void main(String[] args) {

        ApiDocConfig config = new ApiDocConfig();
        config.setBasePath("./doc/api");

        ApiDocManager.me().genDocs(config);
    }
}
```

不同的 `Controller` 生成在不同的目录，代码如下：

```java
public class ApiDocGenerator {

    public static void main(String[] args) {

        ApiDocConfig config1 = new ApiDocConfig();
        config1.setBasePath("./doc/api1");
        config1.setPackagePrefix("com.xxx.package1");

        ApiDocManager.me().genDocs(config1);



        ApiDocConfig config2 = new ApiDocConfig();
        config2.setBasePath("./doc/api2");
        config2.setPackagePrefix("com.xxx.package2");

        ApiDocManager.me().genDocs(config2);
    }
}
```

## 多个 `Controller` 生成一个文档

`@Api` 注解提供了一个 `collect` 的配置，用于汇总其他 `Controller` 的接口。 例如：

```java
@RequestMapping("/api/user")
@Api(value="用户相关API",collect={Controler1.class, Controller2.class})
public class UserApiController extends ApiControllerBase {

    
    @PostRequest
    @ApiOper("用户登录")
    public Ret login(@ApiPara(value = "登录账户", notes = "可以是邮箱") @NotNull String loginAccount
            , @ApiPara("登录密码") @NotNull String password) {
        //....
    }


}    
```

`@Api(value="用户相关API",collect={Controler1.class,Controller2.class})` 表示 `UserApiController` 生成
的文档会把 `Controler1` 和  `Controller2` 的接口也汇总到此文档里来。

> 注意：此时，`Controler1` 和  `Controller2` 不再需要添加 `@Api` 注解。