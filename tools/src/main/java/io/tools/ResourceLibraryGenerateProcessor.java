package io.tools;

import com.google.auto.service.AutoService;
import com.google.common.base.CaseFormat;
import com.squareup.javapoet.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.stereotype.Repository;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 生成资源库接口实现类的工具
 */
@AutoService(Processor.class)
@SupportedAnnotationTypes(
        {"io.tools.BaseResourceLibrary",
        "io.tools.BaseImplResourceLibrary"
        }
)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class ResourceLibraryGenerateProcessor extends AbstractProcessor {

    private Types typeUtils;
    private Messager messager;
    private Filer filer;
    private Elements elementUtils;

    //FIXME 钊哥 这块我暂时写死了 麻烦你改成你本地的路径
    private static final File srcMainFile = new File("C:\\Users\\Fengc\\Desktop\\gencode\\system\\src\\main\\java");

    /**
     * 这个方法用于初始化处理器，方法中有一个ProcessingEnvironment类型的参数，ProcessingEnvironment是一个注解处理工具的集合。它包含了众多工具类。例如：
     * Filer可以用来编写新文件；
     * Messager可以用来打印错误信息；
     * Elements是一个可以处理Element的工具类。
     * <p>
     * 在Java语言中，Element是一个接口，表示一个程序元素，它可以指代包、类、方法或者一个变量。Element已知的子接口有如下几种：
     * <p>
     * PackageElement 表示一个包程序元素。提供对有关包及其成员的信息的访问。
     * ExecutableElement 表示某个类或接口的方法、构造方法或初始化程序（静态或实例），包括注释类型元素。
     * TypeElement 表示一个类或接口程序元素。提供对有关类型及其成员的信息的访问。注意，枚举类型是一种类，而注解类型是一种接口。
     * VariableElement 表示一个字段、enum 常量、方法或构造方法参数、局部变量或异常参数。
     * <p>
     * -----------------------------举个例子---------------------------------------
     * package com.zhpan.mannotation.factory;  //    PackageElement
     * <p>
     * public class Circle {  //  TypeElement
     * <p>
     * private int i; //   VariableElement
     * private Triangle triangle;  //  VariableElement
     * <p>
     * public Circle() {} //    ExecuteableElement
     * <p>
     * public void draw(   //  ExecuteableElement
     * String s)   //  VariableElement
     * {
     * System.out.println(s);
     * }
     *
     * @param processingEnv
     * @Override public void draw() {    //  ExecuteableElement
     * System.out.println("Draw a circle");
     * }
     * }
     * --------------------------------------------------------------------------
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
     *
     * @return
     */
    @Override
    public Set<String> getSupportedAnnotationTypes() {
        HashSet<String> set = new HashSet<>();
        //本次要处理多少注解 这块就set多少个
        set.add(BaseResourceLibrary.class.getCanonicalName());
        set.add(BaseImplResourceLibrary.class.getCanonicalName());
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
        for (Element element : roundEnv.getElementsAnnotatedWith(BaseResourceLibrary.class)) {
            if (!(element.getKind() == ElementKind.INTERFACE)) return false;
            //持久化仓库的名称
            String persistenceName = element.getAnnotation(BaseResourceLibrary.class).persistence();
            //定义方法
            List<MethodSpec> methodSpecList = new ArrayList<>();
            messager.printMessage(Diagnostic.Kind.NOTE, "InterfaceName: " + element.getSimpleName().toString());
            element.getEnclosedElements().forEach(method->{
                ElementKind kind = method.getKind();
                if (kind == ElementKind.CLASS){
                    TypeElement typeElement = (TypeElement) method;
                    messager.printMessage(Diagnostic.Kind.NOTE,"=============Class======"+typeElement.getSimpleName().toString());

                }else if (kind == ElementKind.FIELD){
                    VariableElement variableElement = (VariableElement) method;
                    messager.printMessage(Diagnostic.Kind.NOTE,"=============Field======"+variableElement.getSimpleName().toString());
                }else if (kind == ElementKind.METHOD){
                    ExecutableElement executableElement = (ExecutableElement) method;
                    messager.printMessage(Diagnostic.Kind.NOTE,"=============returnType======"+ executableElement.getReturnType());
                    //获取参数信息
                    List<ParameterSpec> extracted = extracted(executableElement);
                    //包装返回值信息
                    CodeBlock codeBlock = returnData(executableElement);
                    //构造接口的所有方法
                    MethodSpec main = MethodSpec.methodBuilder(executableElement.getSimpleName().toString())
                            .addModifiers(Modifier.PUBLIC, Modifier.PUBLIC)
                            .returns(ClassName.get(executableElement.getReturnType()))
                            .addAnnotation(Override.class)
                            .addParameters(extracted)
                            .addStatement(codeBlock)
                            .build();
                    methodSpecList.add(main);
                    messager.printMessage(Diagnostic.Kind.NOTE,"=============Method======"+ executableElement.getSimpleName());
                }else if (kind == ElementKind.PARAMETER){
                    messager.printMessage(Diagnostic.Kind.NOTE,"=============方法参数======"+ method.getSimpleName());
                } else {
                    messager.printMessage(Diagnostic.Kind.NOTE,"=============othertype======"+method.getSimpleName());
                }
            });
            // 根据注解生成持久化接口类
            Class<?> persistenceInterface = genPersistenceJava(element, persistenceName);
            // 生成主资源库实现类
            genRepositoryImplJava(element, methodSpecList,persistenceInterface);

        }
        return true;
    }

    // 根据注解生成持久化接口类
    private Class<?> genPersistenceJava(Element element, String persistenceName) {
        TypeSpec persistenceClass = TypeSpec.interfaceBuilder(persistenceName)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(NoRepositoryBean.class)
                .addSuperinterface(TypeName.get(JpaRepository.class))
                .addSuperinterface(TypeName.get(JpaSpecificationExecutor.class))
                .build();

        String packageName = elementUtils.getPackageOf(element).getQualifiedName().toString();
        messager.printMessage(Diagnostic.Kind.NOTE,"=============packageName======"+packageName);
        try {

            JavaFile javaFile = JavaFile.builder(packageName, persistenceClass)
                    .addFileComment(" This codes are generated automatically. Do not modify!")
                    .build();
            javaFile.writeTo(srcMainFile);
            return Class.forName(packageName + "." + persistenceName);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    // 生成主资源库实现类
    private void genRepositoryImplJava(Element element, List<MethodSpec> methodSpecList,Class<?> persistenceInterfaceClass) {
        //FIXME 王钊 =构造资源库实现类中的jpa局部变量参数时的类型无法获取
        FieldSpec fieldSpec = FieldSpec.builder(TypeName.get(persistenceInterfaceClass), CaseFormat.LOWER_HYPHEN.to(CaseFormat.LOWER_CAMEL, persistenceInterfaceClass.getSimpleName()), Modifier.PRIVATE)
                .addAnnotation(Autowired.class).build();

        TypeSpec classData = TypeSpec.classBuilder(element.getSimpleName().toString()+"Impl")
                .addModifiers(Modifier.PUBLIC)
                .addMethods(methodSpecList)
                .addSuperinterface(element.asType())
                .addField(fieldSpec)
                .build();

        String packageName = elementUtils.getPackageOf(element).getQualifiedName().toString();
        messager.printMessage(Diagnostic.Kind.NOTE,"=============packageName======"+packageName);
        try {
            JavaFile javaFile = JavaFile.builder(packageName, classData)
                    .addFileComment(" This codes are generated automatically. Do not modify!")
                    .build();
            javaFile.writeTo(srcMainFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private CodeBlock returnData(ExecutableElement executableElement) {
        if (executableElement.getReturnType().getKind() == TypeKind.VOID) {
            return CodeBlock.builder().build();
        }else {
            return CodeBlock.builder().add("return new $T()",executableElement.getReturnType()).build();
        }
    }

    private List<ParameterSpec> extracted(ExecutableElement executableElement) {
        //取得方法参数列表
        List<? extends VariableElement> methodParameters = executableElement.getParameters();
        //参数类型列表
        List<ParameterSpec> types = new ArrayList<>();
        for (VariableElement variableElement : methodParameters) {

            TypeMirror methodParameterType = variableElement.asType();
            if (methodParameterType instanceof TypeVariable) {
                TypeVariable typeVariable = (TypeVariable) methodParameterType;
                methodParameterType = typeVariable.getUpperBound();

            }
            //参数名
            String parameterName = variableElement.getSimpleName().toString();
            types.add(ParameterSpec.builder(TypeName.get(methodParameterType),parameterName).build());
        }
        return types;
    }

}

