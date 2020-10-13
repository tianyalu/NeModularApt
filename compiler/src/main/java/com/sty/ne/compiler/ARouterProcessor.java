package com.sty.ne.compiler;

import com.google.auto.service.AutoService;
import com.sty.ne.annotation.ARouter;

import java.io.IOException;
import java.io.Writer;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

/**
 * @Author: tian
 * @UpdateDate: 2020/10/12 9:13 PM
 */
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
