package io.github.victorandrej.tinyioc.config;

import io.github.victorandrej.tinyioc.annotation.Named;
import io.github.victorandrej.tinyioc.util.BeanUtil;

import javax.naming.Name;
import java.lang.reflect.Parameter;
import java.util.Objects;

public class ParameterInfo {
    private Class<?> type;
    private String name;
    private Parameter parameter;
    private int index;

    public ParameterInfo(Parameter parameter, int index) {
        this.parameter = parameter;
        this.index = index;
        makeMetadado();
    }

    private void makeMetadado() {
        this.type = parameter.getType();
        Named named = parameter.getAnnotation(Named.class);

        this.name = BeanUtil.resolveBeanName(Objects.nonNull(named) ? named.value() : "", type);

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

    public String getName() {
        return name;
    }
}
