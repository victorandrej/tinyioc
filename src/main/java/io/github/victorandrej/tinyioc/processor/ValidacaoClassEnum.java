package io.github.victorandrej.tinyioc.processor;

import io.github.victorandrej.tinyioc.processor.asm.JClass;
import io.github.victorandrej.tinyioc.steriotypes.Bean;

import java.lang.reflect.Modifier;

/**
 * Enum de valiadao de bean
 */
public enum ValidacaoClassEnum {
    SEM_ERRO("Sem erro"),
    SEM_ANOTACAO("Classe não anotada"),
    INTERFACE("Classe é uma interface"),
    ABSTRATA("Classe abstrata");

    private String descricao;

    ValidacaoClassEnum(String descricao) {
        this.descricao = descricao;
    }


    public String getDescricao() {
        return descricao;
    }

    public static ValidacaoClassEnum checkClass(JClass c) {

        if (!c.hasAnnotation(Bean.class))
            return SEM_ANOTACAO;
        if (Modifier.isInterface(c.getModifier()))
            return INTERFACE;
        if (Modifier.isAbstract(c.getModifier()))
            return ABSTRATA;

        return SEM_ERRO;
    }


}
