package io.github.victorandrej.tinyioc.util;

import java.util.Objects;

public class BeanUtil {
    private  BeanUtil(){}
    public static String resolveBeanName(String nome, Class<?> clazz) {

        if (Objects.nonNull(nome) && !"".equals(nome.trim()))
            return nome;

        var className = clazz.getSimpleName();
        return className.substring(0, 1).toLowerCase() + className.substring(1);
    }
}
