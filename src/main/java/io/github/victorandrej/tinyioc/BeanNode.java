package io.github.victorandrej.tinyioc;

import io.github.victorandrej.tinyioc.config.BeanMetadado;
import io.github.victorandrej.tinyioc.config.BeanMetadadoImpl;
import io.github.victorandrej.tinyioc.exception.DuplicatedBeanException;
import io.github.victorandrej.tinyioc.exception.NoSuchBeanException;
import io.github.victorandrej.tinyioc.exception.UnresolvableBeanException;

import java.util.*;
import java.util.function.Function;

public class BeanNode {


    public static BeanNode newInstance() {
        return new BeanNode();
    }

    private final Class<?> nodeClass;
    private final Map<Class<?>, BeanNode> nodes;
    private final Set<BeanNode> parent;
    private final Map<Class<?>, BeanNode> globalNodes;

    private final Map<Class<?>, BeanNode> childrenNodes = new HashMap<>();

    private final Map<String, Set<BeanMetadadoImpl>> namedChildrenBeanInstance = new HashMap<>();
    private final Map<String, BeanMetadadoImpl> beanInstance = new HashMap<>();
    private final Set<BeanMetadadoImpl> globalBeanInstance;
    private final Set<BeanMetadadoImpl> childrenBeanInstance = new HashSet<>();

    private BeanNode() {
        this(Object.class, null, null, null);
        this.globalNodes.put(Object.class, this);
    }

    private BeanNode(Class<?> nodeCLass, Set<BeanNode> parent, Map<Class<?>, BeanNode> globalNodes, Set<BeanMetadadoImpl> globalBeanInstance) {
        this.nodeClass = nodeCLass;
        this.parent = Objects.requireNonNullElse(parent, new HashSet<>());
        for (var p : this.parent)
            p.putNode(nodeCLass, this);
        this.globalNodes = Objects.requireNonNullElse(globalNodes, new HashMap<>());
        this.nodes = new HashMap<>();
        this.globalBeanInstance = Objects.requireNonNullElse(globalBeanInstance, new HashSet<>());
    }

    private BeanNode resolveSupersNewNode(Class<?> clazz) {
        var superClass = clazz.getSuperclass();
        if (Objects.isNull(superClass))
            return null;

        var node = globalNodes.get(clazz);

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
            this.globalNodes.get(Object.class).putNode(inter, v);
            interFacesNode.add(v);
        }
        return interFacesNode;


    }

    private void putNode(Class<?> clazz, BeanNode node) {
        this.nodes.put(clazz, node);
        this.childrenNodes.put(clazz, node);
        for (var parent : this.parent) {
            parent.childrenNodes.put(clazz, node);
        }

        this.globalNodes.put(clazz, node);
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
        if (this.beanInstance.containsKey(beanMetadado.getName()))
            throw new DuplicatedBeanException("Bean duplicado nome: {" + beanMetadado.getName()
                    + "} classe: {" + beanMetadado.getBeanClass().getName() + "}");
        this.beanInstance.put(beanMetadado.getName(), beanMetadado);

        for (var parent : this.parent) {
            parent.childrenBeanInstance.add(beanMetadado);
            var set = parent.namedChildrenBeanInstance.get(beanMetadado.getName());
            if (Objects.isNull(set)) {
                set = new LinkedHashSet<>();
                parent.namedChildrenBeanInstance.put(beanMetadado.getName(), set);
            }
            set.add(beanMetadado);
        }
        this.globalBeanInstance.add(beanMetadado);
    }

    private BeanNode newNode(Class<?> clazz) {
        var beanNode = globalNodes.get(clazz);
        if (Objects.nonNull(beanNode)) {
            return beanNode;
        }
        var superNode = resolveSupersNewNode(clazz);

        var globalNodes = Objects.nonNull(superNode) ? superNode.globalNodes : null;
        var globalInstance = Objects.nonNull(superNode) ? superNode.globalBeanInstance : null;

        var parent = resolveInterfaceNewNode(clazz);

        if (Objects.nonNull(superNode))
            parent.add(superNode);

        var newNode = new BeanNode(clazz, parent, globalNodes, globalInstance);

        this.globalNodes.put(clazz, newNode);
        return newNode;
    }

    private <T extends Collection> BeanMetadadoImpl getBean(Class<?> beanClass, T collection, Function<T, BeanMetadadoImpl> function) {
        var beanSize = collection.size();

        if (beanSize > 1)
            throw new UnresolvableBeanException("multiplos beans para classe " + beanClass);
        else if (beanSize == 1)
            return function.apply(collection);
        return null;
    }

    private BeanNode getBeanNode(Class<?> beanClass) {
        var result = globalNodes.get(beanClass);
        if (Objects.isNull(result))
            throw new NoSuchBeanException("não ha instancia para para a classe " + beanClass);
        return result;
    }


    public List<BeanMetadado> getInstancesCollection(Class<?> collectionType) {
        Objects.requireNonNull(collectionType);
        var result = getBeanNode(collectionType);
        List<BeanMetadado> list = new LinkedList<>();
        list.addAll(result.beanInstance.values());
        list.addAll(result.childrenBeanInstance);
        return Collections.unmodifiableList(list);
    }


    public <T> T getInstance(Class<T> beanClass) {
        if (this.nodeClass.equals(beanClass) && Objects.nonNull(this.beanInstance)) {
            var values = this.beanInstance.values();
            if (values.size() > 1)
                throw new UnresolvableBeanException("mais de 1 bean para a classe " + beanClass);
            if (!values.isEmpty())
                return (T) values.iterator().next().getBeanInstance();
        }

        var result = this.childrenNodes.get(beanClass);

        if (Objects.isNull(result))
            throw new NoSuchBeanException("nao existe bean para classe " + beanClass);


        var values = result.beanInstance.values();
        var valuesIterator = values.iterator();
        var value = valuesIterator.hasNext() ? valuesIterator.next() : null;

        if (Objects.isNull(value)) {
            values = result.childrenBeanInstance;
            valuesIterator = values.iterator();
            value = valuesIterator.hasNext() ? valuesIterator.next() : null;
        }


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

        if (this.nodeClass.equals(beanClass) && Objects.nonNull(this.beanInstance)) {
            var value = this.beanInstance.get(name);
            if (Objects.nonNull(value))
                return (T) value.getBeanInstance();
        }


        var node = this.nodes.get(beanClass);


        if (Objects.isNull(node))
            node = this.childrenNodes.get(beanClass);


        if (Objects.isNull(node))
            throw new NoSuchBeanException("nao existe bean para classe " + beanClass);


        var instance = node.beanInstance.get(name);

        if (Objects.nonNull(instance))
            return (T) instance.getBeanInstance();


        Object value = null;


        for (var c : node.childrenNodes.values())
            try {
                value = c.getInstance(beanClass, name);
            } catch (NoSuchBeanException e) {
            }


        if (Objects.isNull(value))
            throw new NoSuchBeanException("nao existe bean para classe " + beanClass);

        return (T) value;
    }


    public Class<?> getNodeClass() {
        return nodeClass;
    }
}