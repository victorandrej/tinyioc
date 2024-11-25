package io.github.victorandrej.tinyioc.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

import static io.github.victorandrej.tinyioc.util.ClassUtil.sneakyThrow;

public class InstanceNavigator {
    Object instance;
    Class<?> instanceClass;

    public InstanceNavigator(Object instance) {
        this.instance = instance;
        this.instanceClass = instance.getClass();
    }

    public InstanceNavigator navigate(String fieldName,Boolean isStatic) {
        try {
            Field f = getField(fieldName,instanceClass,isStatic);
            f.setAccessible(true);
            return new InstanceNavigator(f.get(isStatic? null: instance));
        } catch (Exception e) {
            sneakyThrow(e);
        }
        return null;
    }
    private  Field getField(String fieldName,Class<?> clazz,Boolean isStatic){
        var fields = clazz.getFields() ;
        for (var field : fields)
            if (!(isStatic ^ Modifier.isStatic(field.getModifiers()))  && field.getName().equals(fieldName))
                return field;

        Class<?> superClass = clazz.getSuperclass();
        if (Objects.nonNull(superClass))
            return getField(fieldName, superClass, isStatic);

        sneakyThrow(new NoSuchFieldException(fieldName + " nao encontrado para " + clazz));

        return null;
    }

    private Boolean compareParameters(Class<?>[] esperado, Class<?>[] avaliado) {
        if (esperado.length != avaliado.length)
            return false;

        for (var i = 0; i < esperado.length; i++)
            if (!esperado[i].equals(avaliado[i]))
                return false;
        return true;
    }

    private Method getMethod(String methodName, Class<?> clazz, Boolean isStatic, Class<?>[] paramsType) {
        var methods = clazz.getMethods();
        for (var method : methods)
            if (!(isStatic ^ Modifier.isStatic(method.getModifiers())) && method.getName().equals(method.getName()) && compareParameters(method.getParameterTypes(), paramsType))
                return method;

        Class<?> superClass = clazz.getSuperclass();
        if (Objects.nonNull(superClass))
            return getMethod(methodName, superClass, isStatic, paramsType);

        sneakyThrow(new NoSuchMethodException(methodName + " nao encontrado para " + clazz));

        return null;
    }

    public InstanceNavigator invoke(String methodName, Boolean isStatic, Object... params) {
        try {

            Method m = getMethod(methodName, instanceClass, isStatic, Arrays.stream(params).map(p -> p.getClass()).collect(Collectors.toList()).toArray(new Class<?>[0]));
            m.setAccessible(true);
            return new InstanceNavigator(m.invoke(isStatic ?null: instance, params));
        } catch (Exception e) {
            sneakyThrow(e);
        }
        return null;
    }

    public <T> T get() {
        return (T) this.instance;
    }

}
