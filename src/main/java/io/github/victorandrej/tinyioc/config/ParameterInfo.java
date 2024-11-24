package io.github.victorandrej.tinyioc.config;

import io.github.victorandrej.tinyioc.annotation.Named;
import io.github.victorandrej.tinyioc.steriotypes.Bean;
import io.github.victorandrej.tinyioc.util.BeanUtil;

import javax.naming.Name;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class ParameterInfo {
    private Class<?> type;
    private String name;
    private Parameter parameter;
    private int index;
    private Boolean isCollection;
    public ParameterInfo(Parameter parameter, int index) {
        this.parameter = parameter;
        this.index = index;
        makeMetadado();
    }

    private Class<?> getCollectionType(Parameter parameter){
       ParameterizedType type = (ParameterizedType) parameter.getParameterizedType();

      return (Class<?>) type.getActualTypeArguments()[0];
    }
    private void makeMetadado() {
        this. isCollection = Collections.class.equals(parameter.getType()) || List.class.equals(parameter.getType());

        this.type = getCollectionType(parameter);
        Named named = parameter.getAnnotation(Named.class);

        this.name = Objects.nonNull(named) ? named.value() : "";

    }


    public Parameter getParameter() {
        return parameter;
    }

    public int getIndex() {
        return index;
    }

    public Class<?> getType() {
        return type;
    }

    public Boolean isCollection(){
        return  this.isCollection;
    }
    public String getName() {
        return name;
    }
}
