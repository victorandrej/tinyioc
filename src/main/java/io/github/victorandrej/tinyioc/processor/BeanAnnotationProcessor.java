package io.github.victorandrej.tinyioc.processor;


import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import io.github.victorandrej.tinyioc.config.*;
import io.github.victorandrej.tinyioc.config.scan.ClassScanner;
import io.github.victorandrej.tinyioc.order.*;
import io.github.victorandrej.tinyioc.processor.asm.JClass;
import io.github.victorandrej.tinyioc.steriotypes.Bean;


import org.apache.maven.plugin.logging.Log;


import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;

import java.nio.file.Paths;
import java.util.*;
import java.util.stream.IntStream;

import org.objectweb.asm.Type;

/**
 * Processador de anotacoes {@link    io.github.victorandrej.tinyioc.steriotypes.Bean Bean}
 * escaneia todas a classes com {@link    io.github.victorandrej.tinyioc.steriotypes.Bean Bean}
 */
@SupportedAnnotationTypes(Const.BEAN_ANNOTATION)
@SupportedSourceVersion(SourceVersion.RELEASE_17)
public class BeanAnnotationProcessor implements Processor {
    private static final String USER_DIR_PROPERTY = "user.dir";
    private static final String TEST_FILE_LOCK = ".test-phase";
    private static final String IGNORE_PROCESSOR = ".ignore-processor";
    private static final String BEAN_ANNOTATION = "io.github.victorandrej.tinyioc.steriotypes.Bean";

    private boolean ignore() {
        return Paths.get(System.getProperty(USER_DIR_PROPERTY), IGNORE_PROCESSOR).toFile().exists();
    }


    @Override
    public void process(Compiler compiler, Log log) throws Exception {
        createClasScan(compiler, log);
    }


    private void createClasScan(Compiler compiler, Log log) throws ClassNotFoundException, NoSuchMethodException {

        var classes = compiler.getSourceClasses();
        if (classes.isEmpty())
            return;


        List<String> filteredClasses = filterAndOrganize(classes, log);

        var method = MethodSpec.methodBuilder(Const.SCAN_METHOD_NAME)
                .addModifiers(javax.lang.model.element.Modifier.PUBLIC, javax.lang.model.element.Modifier.STATIC)
                .returns(void.class)
                .addException(Exception.class);

        log.info("Escanenado beans");
        for (var clazz : filteredClasses) {

            log.info("Bean encontrado: " + clazz);
            method.addStatement("$T.addClass(" + clazz + ".class)", ClassScanner.class);
        }
        TypeSpec generatedClass = TypeSpec.classBuilder(Const.CLASS_SCAN_CLASS)
                .addModifiers(javax.lang.model.element.Modifier.PUBLIC)
                .addMethod(method.build())
                .build();

        compiler.compile(Const.SCAN_PACKAGE, generatedClass);
    }

    private List<String> filterAndOrganize(List<JClass> paramClasses, Log log) throws ClassNotFoundException, NoSuchMethodException {
        List<JClass> classes = new ArrayList<>();


        for (var c : paramClasses) {
            var v = ValidacaoClassEnum.checkClass(c);

            if (v.equals(ValidacaoClassEnum.SEM_ERRO))
                classes.add(c);
            else
                log.warn(c.getName() + " ignorada, Motivo: " + v.getDescricao());

        }

        Map<String, List<JClass>> priorities = new LinkedHashMap<>();

        Class<? extends Priority> p = Ring4.class;

        do {
            priorities.put(p.getName(), new ArrayList<>());
        } while (Objects.nonNull(p = Priority.getNext(p)));

        filterPriorities(priorities, classes);
        orderPriorities(priorities);
        var orderedClasses = new LinkedList<String>();
        var  lEntries = new ArrayList<>( priorities.entrySet());
        Collections.reverse(lEntries);
       lEntries.forEach(e ->
                {
                    orderedClasses.add(e.getKey());
                    e.getValue().stream().map(el -> el.getName()).forEach(orderedClasses::add);
                }
        );
        return orderedClasses;
    }

    private void orderPriorities(Map<String, List<JClass>> priorities) throws NoSuchMethodException {

        for (var classes : priorities.values())
            for (var i = 0; i < classes.size(); i++) {
                var clazz = classes.get(i);
                classes.remove(i);

                var b = clazz.getAnnotation(Bean.class.getName());


                final String classOrder;
                String classOrder1;

                try {
                    classOrder1 = ((Type) b.get("classOrder").getValue()).getClassName();
                } catch (Exception e) {
                    classOrder1 = ((Class) Bean.class.getMethod("classOrder").getDefaultValue()).getName();
                }

                classOrder = classOrder1;
                BeanOrder order;


                try {

                    order = BeanOrder.valueOf((String) b.get("order").getValue());
                } catch (Exception e) {
                    order = (BeanOrder) Bean.class.getMethod("order").getDefaultValue();
                }

                if (classOrder.equals(None.class.getName())) {
                    classes.add(i, clazz);
                    continue;
                }

                var index = IntStream.range(0, classes.size())
                        .filter(in -> classes.get(in).getName().equals(classOrder)).findFirst().orElse(-1);

                if (index == -1 || classOrder.equals(clazz.getName()) || clazz.isAnnotation()){
                    classes.add(i, clazz);
                    continue;
                }



                switch (order) {
                    case AFTER -> {
                        if (i >= index + 1) {
                            classes.add(i, clazz);
                            continue;
                        }

                        classes.add(index + 1, clazz);

                    }
                    case BEFORE -> {
                        if (i <= index) {
                            classes.add(i, clazz);
                            continue;
                        }

                        classes.add(index, clazz);
                    }
                    default -> {
                    }
                }

            }


    }

    private void filterPriorities(Map<String, List<JClass>> priorities, List<JClass> classes) throws NoSuchMethodException {
        for (var i = 0; i < classes.size(); i++) {
            var clazz = classes.get(i);

            var b = clazz.getAnnotation(Bean.class.getName());

            String priority;

            try {
                priority = ((Type) b.get("priority").getValue()).getClassName();
            } catch (Exception e) {
                priority = ((Class) Bean.class.getMethod("priority").getDefaultValue()).getName();
            }
            priorities.get(priority).add(clazz);
        }
    }
}