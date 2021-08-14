package io.tools;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;


import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.JavaFileManager;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;

@AutoService(Processor.class)
@SupportedAnnotationTypes("io.tools.Json")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class JsonProcessor extends AbstractProcessor {

    private Types typeUtils;
    private Messager messager;
    private Filer filer;
    private Elements elementUtils;
    //private Map<String, FactoryGroupedClasses> factoryClasses = new LinkedHashMap<>();


    /**
     * 这个方法用于初始化处理器，方法中有一个ProcessingEnvironment类型的参数，ProcessingEnvironment是一个注解处理工具的集合。它包含了众多工具类。例如：
     * Filer可以用来编写新文件；
     * Messager可以用来打印错误信息；
     * Elements是一个可以处理Element的工具类。
     *
     *在Java语言中，Element是一个接口，表示一个程序元素，它可以指代包、类、方法或者一个变量。Element已知的子接口有如下几种：
     *
     * PackageElement 表示一个包程序元素。提供对有关包及其成员的信息的访问。
     * ExecutableElement 表示某个类或接口的方法、构造方法或初始化程序（静态或实例），包括注释类型元素。
     * TypeElement 表示一个类或接口程序元素。提供对有关类型及其成员的信息的访问。注意，枚举类型是一种类，而注解类型是一种接口。
     * VariableElement 表示一个字段、enum 常量、方法或构造方法参数、局部变量或异常参数。
     *
     * -----------------------------举个例子---------------------------------------
     * package com.zhpan.mannotation.factory;  //    PackageElement
     *
     * public class Circle {  //  TypeElement
     *
     *     private int i; //   VariableElement
     *     private Triangle triangle;  //  VariableElement
     *
     *     public Circle() {} //    ExecuteableElement
     *
     *     public void draw(   //  ExecuteableElement
     *                         String s)   //  VariableElement
     *     {
     *         System.out.println(s);
     *     }
     *
     *     @Override
     *     public void draw() {    //  ExecuteableElement
     *         System.out.println("Draw a circle");
     *     }
     * }
     * --------------------------------------------------------------------------
     *
     *
     * @param processingEnv
     */
    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);

        this.typeUtils = processingEnv.getTypeUtils();
        this.messager = processingEnv.getMessager();
        this.filer = processingEnv.getFiler();
        this.elementUtils = processingEnv.getElementUtils();
    }

    /**
     * 指定java版本
     */
    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    /**
     * 这个方法的返回值是一个Set集合，集合中指要处理的注解类型的名称(这里必须是完整的包名+类名)
     * @return
     */
    @Override
    public Set<String> getSupportedAnnotationTypes() {
        HashSet<String> set = new HashSet<>();
        //本次要处理多少注解 这块就set多少个
        set.add(Json.class.getCanonicalName());
        return set;
    }

    /**
     * 这个方法的返回值，是一个boolean类型，返回值表示注解是否由当前Processor 处理。如果返回 true，则这些注解由此注解来处理，后续其它的 Processor 无需再处理它们；如果返回 false，则这些注解未在此Processor中处理并，那么后续 Processor 可以继续处理它们。
     * 在这个方法的方法体中，我们可以校验被注解的对象是否合法、可以编写处理注解的代码，以及自动生成需要的java文件等。因此说这个方法是AbstractProcessor 中的最重要的一个方法。我们要处理的大部分逻辑都是在这个方法中完成
     *
     * @param annotations
     * @param roundEnv
     * @return
     */
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        //获取被注解元素的集合
        for (Element element : roundEnv.getElementsAnnotatedWith(Json.class)) {

            if (!(element.getKind() == ElementKind.CLASS)) return false;

            // 在gradle的控制台打印信息
            messager.printMessage(Diagnostic.Kind.NOTE, "className: " + element.getSimpleName().toString());
            element.getEnclosedElements().forEach(method->{
                if (method.getKind() == ElementKind.METHOD) {
                    String a;
                }
            });
            // 创建main方法
            MethodSpec main = MethodSpec.methodBuilder("main")//
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)//
                    .returns(void.class)//
                    .addParameter(String[].class, "args")//
                    .addStatement("$T.out.println($S)", System.class, "自动创建的")//
                    .build();

            // 创建HelloWorld类
            TypeSpec helloWorld = TypeSpec.classBuilder("HelloWorld")//
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL)//
                    .addMethod(main)//
                    .build();

            String packageName = processingEnv.getElementUtils().getPackageOf(element).getQualifiedName().toString();
            try {
                JavaFile javaFile = JavaFile.builder(packageName, helloWorld)//
                        .addFileComment(" This codes are generated automatically. Do not modify!")//
                        .build();
                javaFile.writeTo(filer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }
}
