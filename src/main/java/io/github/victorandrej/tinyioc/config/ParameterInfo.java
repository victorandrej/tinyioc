package io.github.victorandrej.tinyioc.config;

import io.github.victorandrej.tinyioc.annotation.Named;
import io.github.victorandrej.tinyioc.annotation.Optional;
import io.github.victorandrej.tinyioc.steriotypes.Bean;
import io.github.victorandrej.tinyioc.util.BeanUtil;

import javax.naming.Name;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class ParameterInfo {
    private Class<?> type;
    private Class<?> collectionType;
    private String name;
    private Parameter parameter;
    private int index;
    private Boolean isCollection;
    private Boolean required;

    public ParameterInfo(Parameter parameter, int index) {
        this.parameter = parameter;
        this.index = index;
        makeMetadado();
    }

    private Class<?> getCollectionType(Parameter parameter) {
        if (isCollection && parameter.getParameterizedType() instanceof ParameterizedType type)
            return (Class<?>) type.getActualTypeArguments()[0];

        return parameter.getType();
    }

    private void makeMetadado() {
        this.isCollection = Collection.class.equals(parameter.getType());

        this.type = parameter.getType();

        this.collectionType = getCollectionType(parameter);
        Named named = parameter.getAnnotation(Named.class);

        this.name = Objects.nonNull(named) ? named.value() : "";
        this.required = !parameter.isAnnotationPresent(Optional.class);
    }


    public Boolean isRequired() {
        return required;
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

    public Boolean isCollection() {
        return this.isCollection;
    }

    public String getName() {
        return name;
    }

    public Class<?> getCollectionType() {
        return this.collectionType;
    }
}
