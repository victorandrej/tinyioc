package io.github.victorandrej.tinyioc;

import io.github.victorandrej.tinyioc.config.BeanMetadado;
import io.github.victorandrej.tinyioc.exception.DuplicatedBeanException;
import io.github.victorandrej.tinyioc.exception.NoSuchBeanException;
import io.github.victorandrej.tinyioc.exception.UnresolvableBeanException;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class BeanNode {


    public static BeanNode newInstance() {
        return new BeanNode();
    }

    private final Class<?> nodeClass;
    private final Map<Class<?>, BeanNode> nodes;
    private final Set<BeanNode> parent;
    private final Map<Class<?>, BeanNode> globalNodes;

    private final Map<Class<?>, BeanNode> childrenNodes = new HashMap<>();

    private final Map<String, Set<BeanMetadado>> namedChildrenBeanInstance = new HashMap<>();
    private final Map<String, BeanMetadado> beanInstance = new HashMap<>();
    private final Set<BeanMetadado> globalBeanInstance;
    private final Set<BeanMetadado> childrenBeanInstance = new HashSet<>();

    private BeanNode() {
        this(Object.class, null, null, null);
        this.globalNodes.put(Object.class, this);
    }

    private BeanNode(Class<?> nodeCLass, Set<BeanNode> parent, Map<Class<?>, BeanNode> globalNodes, Set<BeanMetadado> globalBeanInstance) {
        this.nodeClass = nodeCLass;
        this.parent = Objects.requireNonNullElse(parent, new HashSet<>());
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


        for (var inter : interfaces)
            interFacesNode.add(newNode(inter));

        return interFacesNode;


    }

    private void putNode(Class<?> clazz, BeanNode node) {
        this.nodes.put(clazz, node);
        for (var parent : this.parent) {
            parent.childrenNodes.put(clazz, node);
        }

        this.globalNodes.put(clazz, node);
    }

    public void addInstance(String name, Object instance) {
        var beanMetadado = new BeanMetadado() {{
            setBeanInstance(instance);
            setName(name);
            setBeanClass(instance.getClass());
        }};
        var node = newNode(instance.getClass());
        node.addInstance(beanMetadado);
    }

    private void addInstance(BeanMetadado beanMetadado) {
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

        if (Objects.nonNull(superNode)) {
            superNode.putNode(clazz, newNode);
        }

        this.globalNodes.put(clazz, newNode);
        return newNode;
    }

    private <T extends Collection> BeanMetadado getBean(Class<?> beanClass, T collection, Function<T, BeanMetadado> function) {
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

    public <T> T getInstance(Class<T> beanClass) {
        var result = getBeanNode(beanClass);

        var response = getBean(beanClass, result.beanInstance.values(), (c) -> c.iterator().next());

        if (Objects.isNull(response))
            response = getBean(beanClass, result.childrenBeanInstance, (c) -> c.iterator().next());


        return (T) response.getBeanInstance();


    }
    public <T> List<T> getInstancesCollection(Class<T> collectionType){
        Objects.requireNonNull(collectionType);
        var result = getBeanNode(collectionType);
        return (List<T>) result.beanInstance.values().stream().map(b -> b.getBeanInstance()).toList();

    }

    public <T> T getInstance(Class<T> beanClass, String name) {
        Objects.requireNonNull(beanClass);
        Objects.requireNonNull(name);
        var result = getBeanNode(beanClass);


        var response = getBean(beanClass, result.beanInstance.values(), (c) -> result.beanInstance.get(name));

        if (Objects.isNull(response)) {

            var childrenNameMap = result.namedChildrenBeanInstance.get(name);
            response = getBean(beanClass, childrenNameMap, (c) -> c.iterator().next());
        }
        return (T) response.getBeanInstance();
    }


    public Class<?> getNodeClass() {
        return nodeClass;
    }
}