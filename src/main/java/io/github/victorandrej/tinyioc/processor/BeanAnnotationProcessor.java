package io.github.victorandrej.tinyioc.processor;


import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import io.github.victorandrej.tinyioc.config.Const;
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
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import  io.github.victorandrej.tinyioc.processor.Processor;


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
    public void process(File generetedSourceDir, List<Class<?>> classes, Log log) throws Exception {
        createClasScan(generetedSourceDir,classes,log);
    }



    private void createClasScan(File generetedSourceDir, List<Class<?>> classes, Log log) {
        if (classes.isEmpty())
            return;
        var method = MethodSpec.methodBuilder(Const.SCAN_METHOD_NAME)
                .addModifiers(javax.lang.model.element.Modifier.PUBLIC, javax.lang.model.element.Modifier.STATIC)
                .returns(void.class)
                .addException(Exception.class);

        for (var clazz : classes) {

            if (clazz.isAnnotationPresent(Bean.class)) {

                log.info("Gerando MetaDado para a " + clazz);
                method.addStatement("$T.addClass("+clazz.getCanonicalName()+".class)", ClassScanner.class);

            }
        }
        TypeSpec generatedClass = TypeSpec.classBuilder(Const.CLASS_SCAN_CLASS)
                .addModifiers(javax.lang.model.element.Modifier.PUBLIC)
                .addMethod(method.build())
                .build();

        JavaFile javaFile = JavaFile.builder(Const.SCAN_PACKAGE, generatedClass).build();
        ClassUtil.sneakyThrow(() -> javaFile.writeTo(generetedSourceDir));

    }


}
