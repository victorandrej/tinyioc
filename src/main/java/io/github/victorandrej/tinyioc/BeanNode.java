package io.github.victorandrej.tinyioc;

import io.github.victorandrej.tinyioc.collection.ParentMap;
import io.github.victorandrej.tinyioc.collection.ParentSet;
import io.github.victorandrej.tinyioc.collection.ProxyCollection;
import io.github.victorandrej.tinyioc.config.BeanMetadado;
import io.github.victorandrej.tinyioc.config.BeanMetadadoImpl;
import io.github.victorandrej.tinyioc.exception.DuplicatedBeanException;
import io.github.victorandrej.tinyioc.exception.NoSuchBeanException;
import io.github.victorandrej.tinyioc.exception.UnresolvableBeanException;

import java.util.*;

public class BeanNode {




    public static BeanNode newInstance() {
        return new BeanNode();
    }

    private final Class<?> nodeClass;

    private final Map<Class<?>, BeanNode> nodes;
    private final Map<String, BeanMetadadoImpl> beanInstances = new HashMap<>();
    private final Set<BeanMetadadoImpl> parentBeanInstances;
    private final Set<BeanMetadadoImpl> childrenBeanInstances;
    private final Set<Object> instances = new HashSet<>();
    private final Set<Object> parentInstances;
    private final Set<Object> childrenInstances;

    private final Map<Class<?>, BeanNode> childrenNodes;

    private final BeanNode objectInstance;


    private BeanNode() {
        this(Object.class, null, null);

    }

    private BeanNode(Class<?> nodeClass, Set<BeanNode> parent, BeanNode objectInstance) {
        parent = Objects.requireNonNullElse(parent, new HashSet<>());

        this.objectInstance = nodeClass.equals(Object.class) ? this : objectInstance;
        this.nodeClass = nodeClass;

        for (var p : parent)
            p.putNode(nodeClass, this);

        this.parentInstances = new ParentSet<>(parent.stream().map(p->p.childrenInstances).toList());
        this.childrenInstances = new ParentSet<>(List.of(parentInstances));

        this.parentBeanInstances = new ParentSet<>(parent.stream().map(p -> p.childrenBeanInstances).toList());

        this.childrenBeanInstances = new ParentSet<>(List.of(parentBeanInstances));

        var childrenNodeParent = parent.stream().map(p -> p.childrenNodes).toList();
        this.childrenNodes = new ParentMap<>(childrenNodeParent);
        this.nodes = new ParentMap<>(childrenNodeParent);

    }

    private BeanNode resolveSupersNewNode(Class<?> clazz) {
        if (Object.class.equals(clazz))
            return objectInstance;

        var superClass = clazz.getSuperclass();
        if (Objects.isNull(superClass))
            return null;

        var node = nodes.get(clazz);
        if (Objects.isNull(node))
            node = childrenNodes.get(clazz);

        if (Objects.nonNull(node))
            return node;

        return newNode(superClass);
    }

    private Set<BeanNode> resolveInterfaceNewNode(Class<?> clazz) {
        Set<BeanNode> interFacesNode = new HashSet<>();
        var interfaces = clazz.getInterfaces();
        if (Objects.isNull(interfaces) || interfaces.length == 0)
            return interFacesNode;


        for (var inter : interfaces) {
            var v = newNode(inter);
            objectInstance.nodes.put(inter, v);
            interFacesNode.add(v);
        }
        return interFacesNode;


    }

    private void putNode(Class<?> clazz, BeanNode node) {
        this.nodes.put(clazz, node);
    }

    public void addInstance(String name, Object instance) {
        var beanMetadado = new BeanMetadadoImpl() {{
            setBeanInstance(instance);
            setName(name);
            setBeanClass(instance.getClass());
        }};
        var node = newNode(instance.getClass());
        node.addInstance(beanMetadado);
    }

    private void addInstance(BeanMetadadoImpl beanMetadado) {
        if (this.beanInstances.containsKey(beanMetadado.getName()))
            throw new DuplicatedBeanException("Bean duplicado nome: {" + beanMetadado.getName()
                    + "} classe: {" + beanMetadado.getBeanClass().getName() + "}");
        this.beanInstances.put(beanMetadado.getName(), beanMetadado);
        this.parentBeanInstances.add(beanMetadado);
        this.instances.add(beanMetadado.getBeanInstance());
        this.parentInstances.add(beanMetadado.getBeanInstance());

    }

    private BeanNode newNode(Class<?> clazz) {
        var beanNode = nodes.get(clazz);
        if (Objects.isNull(beanNode))
            beanNode = childrenNodes.get(clazz);

        if (Objects.nonNull(beanNode)) {
            return beanNode;
        }
        var superNode = resolveSupersNewNode(clazz);

        var parent = resolveInterfaceNewNode(clazz);

        if (Objects.nonNull(superNode))
            parent.add(superNode);

        var newNode = new BeanNode(clazz, parent, this.objectInstance);


        return newNode;
    }


    public <T> Collection<T> getInstancesCollection(Class<T> collectionType) {
        Objects.requireNonNull(collectionType);
        var result = this.nodes.get(collectionType);
        if (Objects.isNull(result))
            result = this.childrenNodes.get(collectionType);

        if (Objects.isNull(result))
            throw new NoSuchBeanException("nao existe bean para classe " + collectionType);

        Collection<T> c = new ProxyCollection(result.instances,result.childrenInstances);

        return c;
    }
    public   Collection<BeanMetadado> getInstancesCollectionMetadado(Class<?> collectionType) {
        Objects.requireNonNull(collectionType);
        var result = this.nodes.get(collectionType);
        if (Objects.isNull(result))
            result = this.childrenNodes.get(collectionType);

        if (Objects.isNull(result))
            throw new NoSuchBeanException("nao existe bean para classe " + collectionType);

        Collection<BeanMetadado> c = new ProxyCollection(result.beanInstances.values(),result.childrenBeanInstances);

        return c;
    }

    public <T> T getInstance(Class<T> beanClass) {
        if (this.nodeClass.equals(beanClass) && Objects.nonNull(this.beanInstances)) {
            var values = this.beanInstances.values();
            if (values.size() > 1)
                throw new UnresolvableBeanException("mais de 1 bean para a classe " + beanClass);
            if (!values.isEmpty())
                return (T) values.iterator().next().getBeanInstance();
        }

        var result = this.nodes.get(beanClass);

        if (Objects.isNull(result))
            result = this.childrenNodes.get(beanClass);

        if (Objects.isNull(result))
            throw new NoSuchBeanException("nao existe bean para classe " + beanClass);


        var values = result.beanInstances.values();
        if (Objects.isNull(values) || values.isEmpty())
            values = result.childrenBeanInstances;
        var valuesIterator = values.iterator();
        var value = valuesIterator.hasNext() ? valuesIterator.next() : null;

        if (Objects.isNull(value))
            throw new NoSuchBeanException("nao existe bean para classe " + beanClass);
        else if (values.size() > 1) {
            throw new UnresolvableBeanException("mais de 1 bean para a classe " + beanClass);

        }
        return (T) value.getBeanInstance();
    }

    public <T> T getInstance(Class<T> beanClass, String name) {
        Objects.requireNonNull(beanClass);
        Objects.requireNonNull(name);

        if (this.nodeClass.equals(beanClass) && Objects.nonNull(this.beanInstances)) {
            var value = this.beanInstances.get(name);
            if (Objects.nonNull(value))
                return (T) value.getBeanInstance();
        }


        var node = this.nodes.get(beanClass);


        if (Objects.isNull(node))
            node = this.childrenNodes.get(beanClass);


        if (Objects.isNull(node))
            throw new NoSuchBeanException("nao existe bean para classe " + beanClass);


        var instance = node.beanInstances.get(name);


        return Objects.nonNull(instance) ? (T) instance.getBeanInstance() : (T) node
                .childrenBeanInstances
                .stream()
                .filter(b -> b.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new NoSuchBeanException("nao existe bean para classe " + beanClass))
                .getBeanInstance();

    }


    public Class<?> getNodeClass() {
        return nodeClass;
    }


}