package io.github.victorandrej.tinyioc.config.scan;

import io.github.victorandrej.tinyioc.config.Const;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;


public final class ClassScanner {
    private ClassScanner() {
    }

    static {
        try {
            Class<?> clazz = Class.forName(Const.SCAN_PACKAGE +"."+ Const.CLASS_SCAN_CLASS);
            Method m = clazz.getMethod(Const.SCAN_METHOD_NAME);
            m.invoke(null);
        } catch (Exception e) {

        }
    }

    private static List<Class<?>> classes = new LinkedList<>();

    public static List<Class<?>> getClasses() {
        return Collections.unmodifiableList(classes);
    }

    static void addClass(Class<?> clazz) {
        classes.add(clazz);
    }


}
