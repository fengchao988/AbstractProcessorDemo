package io.tools;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;


import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.tools.Diagnostic;
import javax.tools.JavaFileManager;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

@AutoService(Processor.class)
@SupportedAnnotationTypes("io.tools.Json")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class JsonProcessor extends AbstractProcessor {

    private Filer filer;

    private Messager messager;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        messager = processingEnv.getMessager();
        messager.printMessage(Diagnostic.Kind.NOTE,"已经进入init方法了........");

        filer = processingEnv.getFiler();

    }
    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        HashSet<String> set = new HashSet<>();
        set.add(Json.class.getCanonicalName());
        return set;
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        for (Element element : roundEnv.getElementsAnnotatedWith(Json.class)) {

            if (!(element.getKind() == ElementKind.CLASS)) return false;

            // 在gradle的控制台打印信息
            messager.printMessage(Diagnostic.Kind.NOTE, "className: " + element.getSimpleName().toString());

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
