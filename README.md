# 组件化APT介绍与使用

[TOC]

## 一、概念

### 1.1 什么是`APT` 

`APT(Annotation Processing Tool)` 是一种处理 注释的工具，它对源代码文件进行检测并找出其中的`Annotation`，根据注解自动生成代码，如果想要自定义的注解处理器能够正常运行，必须要通过`APT`工具来进行处理。即**根据规则，帮我们生成代码、生成类文件**。

也可以这样理解：只有通过声明`APT`工具后，程序在编译期间自定义注解解释器才能执行。

### 1.2 结构体语言

`element`组成的结构体：

```xml
<html>
	<body>
  	<div>...</div>
  </body>
</html>
```

对应`Java`源文件来说，它同样也是一种结构体语言：

```java
package com.sty.ne.modularapt;	//PackageElement 包元素/节点
public class Main {	//TypeElement 类元素/节点
  private int x;		//VariableElement 属性元素/节点
  private Main() { 	//ExecuteableElement 方法元素/节点
    
  }
  private void print(String msg) {
    
  }
}
```

### 1.3 `Element`程序元素

* `PackageElement`：表示一个包程序元素，提供对有关包及其成员的信息的访问；
* `ExecutableElement`：表示某个类或接口的方法、构造方法或初始化程序（静态或实例）；
* `TypeElement`：表示一个类或接口程序元素，提供对有关类型及其成员的信息的访问；
* `VariableElement`：表示一个字段、`enum`常量、方法或构造方法参数、局部变量或异常参数。

### 1.4 需要掌握的`API`

> 1. `getEnclosedElements()`：返回该元素直接包含的子元素；
> 2. `getEnclosingElement()`：返回包含该`element`的父`element`，与上一个方法相反；
> 3. `getKind()`：返回`element`的类型，判断是哪种`element`；
> 4. `getModifiers()`：获取修饰关键字，如`public static final`等关键字；
> 5. `getSimpleName()`：获取名字，不带包名；
> 6. `getQualifiedName()`：获取全名，如果是类的话，包含完整的包名路径；
> 7. `getParameters()`：获取方法的参数元素，每个元素是一个`VariableElement`；
> 8. `getReturnType()`：获取方法元素的返回值；
> 9. `getConstantValue()`：如果属性变量被`final`修饰，则可以使用该方法获取它的值。

## 二、实现 

### 2.1 实现思路

利用`APT`技术对于每个被`@ARouter`注解的类自动生成一个类似如下的`Class`文件：

```java
public class MainActivity$$ARouter {
    public static Class<?> findTargetClass(String path) {
        if (path.equalsIgnoreCase("/app/MainActivity")) {
            return MainActivity.class;
        }
        return null;
    }
}
```

使用时直接通过路径参数调用`findTargetClass(String path)`方法即可找到对应的类名，从而实现`Activity`之间的跳转。

### 2.2 实现步骤

#### 2.2.1 `annotation`模块

`ARouter`注解声明文件：

```java
/**
 * <strong>Activity使用的布局文件注解</strong>
 * <ul>
 *  <li>@Target(ElementType.TYPE) //接口、类、枚举、注解</li>
 *  <li>@Target(ElementType.FIELD) //属性、枚举的常量</li>
 *  <li>@Target(ElementType.METHOD) //方法</li>
 *  <li>@Target(ElementType.PARAMETER) //方法参数</li>
 *  <li>@Target(ElementType.CONSTRUCTOR) //构造函数</li>
 *  <li>@Target(ElementType.LOCAL_VARIABLE) //局部变量</li>
 *  <li>@Target(ElementType.ANNOTATION_TYPE) //该注解使用在另一个注解上</li>
 *  <li>@Target(ElementType.PACKAGE) //包</li>
 *  <li>@Retention(RetentionPolicy.RUNTIME) //注解会在class字节码文件中存在，jvm加载时可以通过反射获取到该注解的内容</li>
 * </ul>
 *
 * 生命周期：SOURCE < CLASS < RUNTIME
 * 1. 一般如果需要在运行时去动态获取注解信息，用RUNTIME注解
 * 2. 要在编译时进行一些预处理操作，如ButterKnife，用CLASS注解，注解会在class文件中存在，但是在运行时会被丢弃
 * 3. 做一些检查性的操作，如@Override，用SOURCE源码注解，注解仅存在在源码级别，在编译的时候丢弃该注解
 * @Author: tian
 * @UpdateDate: 2020/10/12 8:48 PM
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface ARouter {
    //详细的路由路径（必填），如："/app/MainActivity"
    String path();

    //从path中截取出来，规范开发者的编码
    String group() default "";
}
```

#### 2.2.2 `compiler`模块

`build.gradle`文件：

```groovy
apply plugin: 'java-library'

dependencies {
    implementation fileTree(dir: 'libs', include: ['*.jar'])

    // AS-3.4.1 + gradle5.1.1-all + auto-service:1.0-rc4
    // 注册注解，并对其生成META-INF的配置信息
    compileOnly 'com.google.auto.service:auto-service:1.0-rc4'
    annotationProcessor 'com.google.auto.service:auto-service:1.0-rc4'

    //引入annotation,让注解处理器处理注解
    implementation project(':annotation')
}

//java 控制台输出中文乱码
tasks.withType(JavaCompile) {
    options.encoding = "UTF-8"
}

//jdk编译的版本1.7
sourceCompatibility = "1.7"
targetCompatibility = "1.7"
```

注解处理器`ARouterProcessor`文件：

```java
//通过AutoService来自动生成注解处理器，用来做注册，类似在Manifest中注册Activity
//build/classes/java/main/META-INF/services/javax.annotation.processing.Processor
@AutoService(Processor.class)
//该注解处理器需要处理哪一种注解的类型
@SupportedAnnotationTypes("com.sty.ne.annotation.ARouter")
//需要用什么样的JDK版本来编译，来进行文件的生成
@SupportedSourceVersion(SourceVersion.RELEASE_7)
//注解处理器能够接受的参数  在app 的build.gradle文件中配置
@SupportedOptions("content")
public class ARouterProcessor extends AbstractProcessor {
    //操作Element工具类
    private Elements elementUtils;

    //type(类信息) 工具类
    private Types typeUtils;

    //用来输出警告、错误等日志
    private Messager messager;

    //文件生成器
    private Filer filer;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        elementUtils = processingEnvironment.getElementUtils();
        typeUtils = processingEnvironment.getTypeUtils();
        messager = processingEnvironment.getMessager();
        filer = processingEnvironment.getFiler();

        String content = processingEnvironment.getOptions().get("content");
        //不能像Android中Log.e的写法-->会报错
        //messager.printMessage(Diagnostic.Kind.ERROR, content);
        messager.printMessage(Diagnostic.Kind.NOTE, content);
    }

    /**
     * 相当于main函数，开始处理注解
     * 注解处理器的核心方法，处理具体的注解，生成Java文件
     * @param set 使用了支持处理注解的节点集合（类上面写了注解）
     * @param roundEnvironment 当前或是之前的运行环境，可以通过该对象查找找到的注解
     * @return true 表示后续处理器不会再处理（已经处理完成）
     */
    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        if(set.isEmpty()) {
            return false;
        }

        //获取项目中所有使用了ARouter注解的节点
        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(ARouter.class);
        //遍历所有的类节点
        for (Element element : elements) {
            //类节点之上就是包节点
            String packageName = elementUtils.getPackageOf(element).getQualifiedName().toString();
            //获取简单类名
            String className = element.getSimpleName().toString();
            messager.printMessage(Diagnostic.Kind.NOTE, "被注解的类有：" + className);
            //最终我们想要生成的类文件，如：MainActivity$$ARouter
            String finalClassName = className + "$$ARouter";

            try {
                JavaFileObject sourceFile = filer.createSourceFile(packageName + "."
                        + finalClassName);
                Writer writer = sourceFile.openWriter();
                //设置包名
                writer.write("package " + packageName + ";\n");
                writer.write("public class " + finalClassName + " {\n");
                writer.write("public static Class<?> findTargetClass(String path) {\n");
                //获取类之上@ARouter注解的path值
                ARouter aRouter = element.getAnnotation(ARouter.class);
                writer.write("if (path.equalsIgnoreCase(\"" + aRouter.path() + "\")) {\n");
                writer.write("return " + className + ".class;\n}\n");
                writer.write("return null;\n");
                writer.write("}\n}");

                //非常重要
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }
}
```

### 2.2.3 `app`模块

`build.gradle`文件：

```groovy
android {   
  //...
	defaultConfig {
        applicationId "com.sty.ne.modularapt"
        minSdkVersion 21
        targetSdkVersion 30
        versionCode 1
        versionName "1.0"
        testInstrumentationRunner "androidx.test.runner.AndroidJUnitRunner"

        //在gradle文件中配置选项参数（用于APT传参接收）
        //切记：必须写在defaultConfig节点下
        javaCompileOptions {
            annotationProcessorOptions {
                arguments = [content : 'hello apt']
            }
        }
    }
  //...
}

dependencies {
		//...

    // 依赖注解
    implementation project(':annotation')
    // 注解处理器
    annotationProcessor project(':compiler')
}
```

`MainActivity`中使用：

```java
    public void jumpToOrder(View view) {
        Class<?> targetClass = OrderActivity$$ARouter.findTargetClass("/app/OrderActivity");
        startActivity(new Intent(this, targetClass));
    }
```

