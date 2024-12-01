package io.github.victorandrej.tinyioc.config.scan;

import io.github.victorandrej.tinyioc.config.Const;
import io.github.victorandrej.tinyioc.util.ClassUtil;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;


public final class ClassScanner {
    private ClassScanner() {
    }

    static {
        classes = new LinkedList<>();

        ClassUtil.sneakyThrow(() -> {
            Class<?> clazz = Class.forName(Const.SCAN_PACKAGE + "." + Const.CLASS_SCAN_CLASS);
            Method m = clazz.getMethod(Const.SCAN_METHOD_NAME);
            m.invoke(null);
        });
    }

    private final static List<Class<?>> classes;

    public static List<Class<?>> getClasses() {
        return Collections.unmodifiableList(classes);
    }

    static void addClass(Class<?> clazz) {
        classes.add(clazz);
    }


}
