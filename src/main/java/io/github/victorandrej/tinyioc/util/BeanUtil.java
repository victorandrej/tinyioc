package io.github.victorandrej.tinyioc.util;

import io.github.victorandrej.tinyioc.steriotypes.Bean;

import java.util.Objects;

/**
 * Utilitario d ebean
 */
public class BeanUtil {
    private  BeanUtil(){}

    /**
     * Resolve o nome de um bean, caso nao passado um nome utiliza o nome da classe em camelCase
     * @param nome
     * @param clazz
     * @return
     */
    public static String resolveBeanName(String nome, Class<?> clazz) {

        if (Objects.nonNull(nome) && !"".equals(nome.trim()))
            return nome;

        var className = clazz.getSimpleName();
        return className.substring(0, 1).toLowerCase() + className.substring(1);
    }
}
