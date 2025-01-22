package io.github.victorandrej.tinyioc.order;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Priodidade de um bean
 */
public abstract class Priority {

    public static Class<? extends Priority> getNext(Class<? extends Priority> tClass) {
        try {
            Constructor<? extends Priority> c = tClass.getConstructor();
            var i = c.newInstance();
            var m = tClass.getMethod("next");
            return (Class<? extends Priority>) m.invoke(i);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(tClass + " Sem construtor padrao publico");

        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException | IllegalAccessException e) {
            throw new Error(e);
            //nao ocorrera em condicoes normais da jvm
        }

    }

    public abstract   Class<? extends  Priority> next();

}
