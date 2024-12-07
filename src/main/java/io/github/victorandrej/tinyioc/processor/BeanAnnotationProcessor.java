package io.github.victorandrej.tinyioc.processor;


import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import io.github.victorandrej.tinyioc.config.Const;
import io.github.victorandrej.tinyioc.config.LastBean;
import io.github.victorandrej.tinyioc.config.NormalBean;
import io.github.victorandrej.tinyioc.config.PrimaryBean;
import io.github.victorandrej.tinyioc.config.scan.ClassScanner;
import io.github.victorandrej.tinyioc.util.ClassUtil;
import io.github.victorandrej.tinyioc.steriotypes.Bean;
import io.github.victorandrej.tinyioc.util.InstanceNavigator;
import org.apache.maven.plugin.logging.Log;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.lang.reflect.Modifier;
import java.nio.file.Paths;
import java.util.*;

import io.github.victorandrej.tinyioc.processor.Processor;


@SupportedAnnotationTypes(Const.BEAN_ANNOTATION)
@SupportedSourceVersion(SourceVersion.RELEASE_17)
public class BeanAnnotationProcessor implements Processor {
    private static final String USER_DIR_PROPERTY = "user.dir";
    private static final String TEST_FILE_LOCK = ".test-phase";
    private static final String IGNORE_PROCESSOR = ".ignore-processor";

    private boolean ignore() {
        return Paths.get(System.getProperty(USER_DIR_PROPERTY), IGNORE_PROCESSOR).toFile().exists();
    }



    @Override
    public void process(Compiler compiler, Log log) throws Exception {
        createClasScan( compiler, log);
    }


    private void createClasScan(Compiler compiler , Log log) {

        List<Class<?>> classes = compiler.getSourceClasses();
        if (classes.isEmpty())
            return;

        classes = filterAndOrganize(classes);

        var method = MethodSpec.methodBuilder(Const.SCAN_METHOD_NAME)
                .addModifiers(javax.lang.model.element.Modifier.PUBLIC, javax.lang.model.element.Modifier.STATIC)
                .returns(void.class)
                .addException(Exception.class);

        log.info("Escanenado beans");
        for (var clazz : classes) {
            if(!clazz.isAnnotationPresent(Bean.class) || clazz.isInterface() || Modifier.isAbstract(clazz.getModifiers())){
                log.warn(clazz +" ignorada");
                continue;
            }

            log.info("Bean encontrado: " + clazz);
            method.addStatement("$T.addClass(" + clazz.getCanonicalName() + ".class)", ClassScanner.class);
        }
        TypeSpec generatedClass = TypeSpec.classBuilder(Const.CLASS_SCAN_CLASS)
                .addModifiers(javax.lang.model.element.Modifier.PUBLIC)
                .addMethod(method.build())
                .build();

        compiler.compile(Const.SCAN_PACKAGE,generatedClass);
    }

    private List<Class<?>> filterAndOrganize(List<Class<?>> classes) {



        classes = new LinkedList<>(classes.stream().filter(c->c.isAnnotationPresent(Bean.class)).toList());
        classes.add(0,PrimaryBean.class);
        classes.add(1,NormalBean.class);
        classes.add(classes.size(),LastBean.class);

        for(var i = 0; i< classes.size();i++){
            var clazz =classes.get(i);
            Bean b = clazz.getAnnotation(Bean.class);
            var index = classes.indexOf(b.classOrder());

            if(index == -1 || b.classOrder().equals(clazz) || clazz.isAnnotation())
                continue;

            classes.remove(i);


            switch (b.order()){
                case AFTER -> {
                    if(i >= index+1){
                        classes.add(i,clazz);
                        continue;
                    }

                    classes.add(index+1,clazz);

                }
                case BEFORE -> {
                    if(i <= index){
                        classes.add(i,clazz);
                        continue;
                    }

                    classes.add(index,clazz);
                }
                default -> {}
            }

            i =-1;
        }

        return  classes;
    }



}
