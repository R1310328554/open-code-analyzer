/**
 * Copyright (c) 2013-2026 Nikita Koksharov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.redisson.config;

import io.netty.channel.EventLoopGroup;
import org.redisson.api.RedissonNodeInitializer;
import org.redisson.client.FailedNodeDetector;
import org.redisson.client.NettyHook;
import org.redisson.client.codec.Codec;
import org.redisson.codec.Kryo5Codec;
import org.redisson.codec.ReferenceCodecProvider;
import org.redisson.connection.AddressResolverGroupFactory;
import org.redisson.connection.ConnectionListener;
import org.redisson.connection.balancer.LoadBalancer;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.introspector.BeanAccess;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.introspector.PropertyUtils;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.*;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URL;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Redisson 配置的 YAML 序列化/反序列化支持类。
 * <p>
 * 封装 SnakeYAML 自定义构造器与表示器，支持 DelayStrategy、Kryo5Codec 等
 * 复杂类型的读写，以及环境变量占位符 {@code ${VAR:-default}} 解析。
 *
 * @author Nikita Koksharov
 *
 */
public class ConfigSupport {

    /** 加载类时使用的 ClassLoader（可为 null 表示默认）。 */
    private final ClassLoader classLoader;
    /** YAML 属性名是否大小写不敏感。 */
    private final boolean useCaseInsensitive;

    /** 默认构造：系统 ClassLoader，属性名区分大小写。 */
    public ConfigSupport() {
        this(false);
    }

    /** 指定属性名是否大小写不敏感。 */
    public ConfigSupport(boolean useCaseInsensitive) {
        this(null, useCaseInsensitive);
    }

    /** 指定 ClassLoader 与大小写敏感性。 */
    public ConfigSupport(ClassLoader classLoader, boolean useCaseInsensitive) {
        this.classLoader = classLoader;
        this.useCaseInsensitive = useCaseInsensitive;
    }

    /** 创建带自定义构造器与表示器的 Yaml 解析器。 */
    private Yaml createYamlParser(ClassLoader classLoader, boolean useCaseInsensitive) {
        LoaderOptions loaderOptions = new LoaderOptions();
        loaderOptions.setTagInspector(tag -> true);

        DumperOptions dumperOptions = new DumperOptions();
        dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        dumperOptions.setPrettyFlow(true);
        dumperOptions.setDefaultScalarStyle(DumperOptions.ScalarStyle.PLAIN);
        dumperOptions.setIndent(2);

        DelayConstructor constructor = new DelayConstructor(classLoader, loaderOptions, useCaseInsensitive);
        CustomRepresenter representer = new CustomRepresenter(dumperOptions, useCaseInsensitive);

        return new Yaml(constructor, representer, dumperOptions, loaderOptions);
    }

    /** 自定义 JavaBean 属性工具：过滤废弃字段与不可序列化类型。 */
    private static class CustomPropertyUtils extends PropertyUtils {
        /** YAML 读写时忽略的废弃或冗余属性名。 */
        private final Set<String> ignoredProperties = new HashSet<>(
                Arrays.asList("slaveNotUsed", "clusterConfig", "sentinelConfig", "singleConfig", "retryInterval")
        );

        /** 不可通过 YAML 直接序列化的 SSL 工厂类型。 */
        private final Set<Class<?>> ignoredClasses = new HashSet<>(Arrays.asList(
                KeyManagerFactory.class,
                TrustManagerFactory.class
        ));

        private final boolean useCaseInsensitive;

        CustomPropertyUtils(boolean useCaseInsensitive) {
            this.useCaseInsensitive = useCaseInsensitive;
            setSkipMissingProperties(!useCaseInsensitive);
            setAllowReadOnlyProperties(true);
        }
        @Override
        protected Map<String, Property> getPropertiesMap(Class<?> type, BeanAccess bAccess) {
            try {
                return super.getPropertiesMap(type, bAccess);
            } catch (Exception e) {
                if (e.getMessage() != null && e.getMessage().contains("No JavaBean properties found")) {
                    return new LinkedHashMap<>();
                }
                throw e;
            }
        }

        @Override
        public Property getProperty(Class<?> type, String name) {
            if (isIgnoredProperty(name)) {
                return null;
            }

            Property property = findProperty(type, name, false);
            if (property == null && useCaseInsensitive) {
                property = findProperty(type, name, true);
            }

            return property;
        }

        @Override
        public Property getProperty(Class<?> type, String name, BeanAccess bAccess) {
            return getProperty(type, name);
        }

        private boolean isIgnoredProperty(String name) {
            if (ignoredProperties.contains(name)) {
                return true;
            }

            if (useCaseInsensitive) {
                for (String ignored : ignoredProperties) {
                    if (ignored.equalsIgnoreCase(name)) {
                        return true;
                    }
                }
            }
            return false;
        }

        private Property findProperty(Class<?> type, String name, boolean caseInsensitive) {

            Set<Property> protectedProps = getProtectedProperties(type);
            List<Property> props = new ArrayList<>(protectedProps);
            Set<Property> standardProps = getStandardProperties(type);
            props.addAll(standardProps);

            for (Property property : props) {
                boolean matches;
                if (caseInsensitive) {
                    matches = property.getName().equalsIgnoreCase(name);
                } else {
                    matches = property.getName().equals(name);
                }

                if (matches) {
                    if (!ignoredClasses.contains(property.getType())) {
                        return property;
                    }
                }
            }

            return null;
        }

        private final Map<Class<?>, Set<Property>> propertiesCache = new HashMap<>();
        private final Map<Class<?>, Set<Property>> protectedPropertiesCache = new HashMap<>();

        private Set<Property> getStandardProperties(Class<?> type) {
            return propertiesCache.computeIfAbsent(type, t -> super.createPropertySet(type, BeanAccess.DEFAULT));
        }

        private Set<Property> getProtectedProperties(Class<?> type) {
            return protectedPropertiesCache.computeIfAbsent(type, t -> discoverProtectedProperties(t));
        }

        @Override
        protected Set<Property> createPropertySet(Class<?> type, BeanAccess bAccess) {
            Map<String, Property> propertyMap = new LinkedHashMap<>();

            try {
                Set<Property> properties = super.createPropertySet(type, BeanAccess.DEFAULT);
                for (Property prop : properties) {
                    propertyMap.put(prop.getName(), prop);
                }
            } catch (Exception e) {
                // 标准属性发现失败时继续尝试 protected 方法
            }

            for (Property prop : getProtectedProperties(type)) {
                propertyMap.putIfAbsent(prop.getName(), prop);
            }

            Set<Property> filtered = new LinkedHashSet<>();
            for (Property property : propertyMap.values()) {
                String name = property.getName();
                Class<?> propType = property.getType();

                if (ignoredProperties.contains(name)) {
                    continue;
                }
                if (ignoredClasses.contains(propType)) {
                    continue;
                }

                if (property.isReadable() || property.isWritable()) {
                    filtered.add(property);
                }
            }

            return filtered;
        }

        private Set<Property> discoverProtectedProperties(Class<?> type) {
            Set<Property> properties = new LinkedHashSet<>();

            Class<?> currentClass = type;
            Map<String, Method> getters = new HashMap<>();
            Map<String, Method> setters = new HashMap<>();

            while (currentClass != null && currentClass != Object.class) {
                Method[] methods = currentClass.getDeclaredMethods();

                for (Method method : methods) {
                    int modifiers = method.getModifiers();

                    boolean isAccessible = Modifier.isPublic(modifiers)
                            || Modifier.isProtected(modifiers)
                            || !Modifier.isPrivate(modifiers);

                    String name = method.getName();
                    if (name.startsWith("get") && name.length() > 3
                            && method.getParameterCount() == 0
                                && !method.getReturnType().equals(void.class)
                                    && isAccessible) {
                        String propertyName = decapitalize(name.substring(3));
                        getters.putIfAbsent(propertyName, method);
                    } else if (name.startsWith("is") && name.length() > 2
                                    && method.getParameterCount() == 0
                                        && (method.getReturnType().equals(boolean.class)
                                                || method.getReturnType().equals(Boolean.class))
                                                    && isAccessible) {
                        String propertyName = decapitalize(name.substring(2));
                        getters.putIfAbsent(propertyName, method);
                    } else if (name.startsWith("set") && name.length() > 3
                                        && method.getParameterCount() == 1) {
                        // For setters, include all visibility levels including private
                        String propertyName = decapitalize(name.substring(3));
                        setters.putIfAbsent(propertyName, method);
                    }
                }

                currentClass = currentClass.getSuperclass();
            }

            Set<String> allPropertyNames = new HashSet<>();
            allPropertyNames.addAll(getters.keySet());
            allPropertyNames.addAll(setters.keySet());

            for (String propertyName : allPropertyNames) {
                Method getter = getters.get(propertyName);
                Method setter = setters.get(propertyName);

                if (getter != null || setter != null) {
                    properties.add(new MethodProperty(propertyName, getter, setter));
                }
            }

            return properties;
        }

        private String decapitalize(String string) {
            if (string == null || string.isEmpty()) {
                return string;
            }
            char[] chars = string.toCharArray();
            chars[0] = Character.toLowerCase(chars[0]);
            return new String(chars);
        }
    }

    /** 基于 getter/setter 反射的 SnakeYAML Property 实现，支持 protected 访问。 */
    private static class MethodProperty extends Property {
        private final Method getter;
        private final Method setter;

        MethodProperty(String name, Method getter, Method setter) {
            super(name, getType(getter, setter));
            this.getter = getter;
            this.setter = setter;

            if (getter != null) {
                getter.setAccessible(true);
            }
            if (setter != null) {
                setter.setAccessible(true);
            }
        }

        private static Class<?> getType(Method getter, Method setter) {
            if (getter != null) {
                return getter.getReturnType();
            }
            if (setter != null) {
                return setter.getParameterTypes()[0];
            }
            return Object.class;
        }

        @Override
        public Class<?>[] getActualTypeArguments() {
            if (getter != null) {
                Type returnType = getter.getGenericReturnType();
                if (returnType instanceof ParameterizedType) {
                    Type[] types = ((ParameterizedType) returnType).getActualTypeArguments();
                    Class<?>[] classes = new Class<?>[types.length];
                    for (int i = 0; i < types.length; i++) {
                        if (types[i] instanceof Class) {
                            classes[i] = (Class<?>) types[i];
                        } else {
                            classes[i] = Object.class;
                        }
                    }
                    return classes;
                }
            }
            return new Class<?>[0];
        }

        @Override
        public void set(Object object, Object value) throws Exception {
            if (setter != null) {
                setter.invoke(object, value);
            }
        }

        @Override
        public Object get(Object object) {
            if (getter != null) {
                try {
                    return getter.invoke(object);
                } catch (Exception e) {
                    throw new IllegalStateException("Failed to get property: " + getName() + " from " + object.getClass(), e);
                }
            }
            return null;
        }

        @Override
        public List<java.lang.annotation.Annotation> getAnnotations() {
            List<java.lang.annotation.Annotation> annotations = new ArrayList<>();
            if (getter != null) {
                annotations.addAll(Arrays.asList(getter.getAnnotations()));
            }
            if (setter != null) {
                annotations.addAll(Arrays.asList(setter.getAnnotations()));
            }
            return annotations;
        }

        @Override
        public <A extends java.lang.annotation.Annotation> A getAnnotation(Class<A> annotationType) {
            if (getter != null) {
                A annotation = getter.getAnnotation(annotationType);
                if (annotation != null) {
                    return annotation;
                }
            }
            if (setter != null) {
                return setter.getAnnotation(annotationType);
            }
            return null;
        }

        @Override
        public boolean isWritable() {
            return setter != null;
        }

        @Override
        public boolean isReadable() {
            return getter != null;
        }
    }

    /** 扩展 SnakeYAML 构造器，注册 DelayStrategy 与 Kryo5Codec 等自定义类型。 */
    private static class DelayConstructor extends Constructor {
        private final ClassLoader classLoader;

        DelayConstructor(ClassLoader classLoader, LoaderOptions loaderOptions, boolean useCaseInsensitive) {
            super(loaderOptions);
            this.classLoader = classLoader;
            this.setPropertyUtils(new CustomPropertyUtils(useCaseInsensitive));

            ConstructDelayStrategy delayConstructor = new ConstructDelayStrategy();
            this.yamlConstructors.put(new Tag("tag:yaml.org,2002:org.redisson.config.EqualJitterDelay"), delayConstructor);
            this.yamlConstructors.put(new Tag("tag:yaml.org,2002:org.redisson.config.FullJitterDelay"), delayConstructor);
            this.yamlConstructors.put(new Tag("tag:yaml.org,2002:org.redisson.config.DecorrelatedJitterDelay"), delayConstructor);
            this.yamlConstructors.put(new Tag("tag:yaml.org,2002:org.redisson.config.ConstantDelay"), delayConstructor);

            this.yamlConstructors.put(new Tag("tag:yaml.org,2002:org.redisson.codec.Kryo5Codec"), new ConstructKryo5Codec());
        }

        @Override
        protected Class<?> getClassForName(String name) throws ClassNotFoundException {
            if (classLoader != null) {
                return Class.forName(name, true, classLoader);
            }
            return super.getClassForName(name);
        }

        private final class ConstructDelayStrategy extends ConstructMapping {
            @Override
            public Object construct(Node node) {
                MappingNode mappingNode = (MappingNode) node;

                Class<?> clazz;
                try {
                    String className = node.getTag().getValue().replace("tag:yaml.org,2002:", "");
                    clazz = getClassForName(className);
                } catch (ClassNotFoundException e) {
                    throw new IllegalStateException(e);
                }

                Duration baseDelay = null;
                Duration maxDelay = null;
                Duration delay = null;

                List<NodeTuple> tuples = mappingNode.getValue();

                if (tuples.isEmpty()) {
                    flattenMapping(mappingNode);
                    tuples = mappingNode.getValue();
                }

                // 扁平化后仍为空，可能是空对象 {}
                if (tuples.isEmpty()) {
                    // 按类型返回默认 DelayStrategy 实例
                    try {
                        if (clazz.getName().contains("ConstantDelay")) {
                            java.lang.reflect.Constructor<?> constructor = clazz.getConstructor(Duration.class);
                            return constructor.newInstance(Duration.ofMillis(100));
                        } else {
                            java.lang.reflect.Constructor<?> constructor = clazz.getConstructor(Duration.class, Duration.class);
                            return constructor.newInstance(Duration.ofMillis(100), Duration.ofSeconds(1));
                        }
                    } catch (Exception e) {
                        throw new IllegalStateException("Cannot construct " + clazz.getName() + " with empty mapping", e);
                    }
                }

                // 遍历 YAML 键值对解析 delay/baseDelay/maxDelay
                for (NodeTuple tuple : tuples) {
                    Node keyNode = tuple.getKeyNode();
                    String key = null;

                    if (keyNode instanceof org.yaml.snakeyaml.nodes.ScalarNode) {
                        key = ((org.yaml.snakeyaml.nodes.ScalarNode) keyNode).getValue();
                    }

                    if (key == null) continue;

                    // 递归构造属性值
                    Object value = constructObject(tuple.getValueNode());

                    if ("delay".equals(key)) {
                        if (value instanceof String) {
                            delay = Duration.parse((String) value);
                        } else if (value instanceof Duration) {
                            delay = (Duration) value;
                        }
                    } else if ("baseDelay".equals(key)) {
                        if (value instanceof String) {
                            baseDelay = Duration.parse((String) value);
                        } else if (value instanceof Duration) {
                            baseDelay = (Duration) value;
                        }
                    } else if ("maxDelay".equals(key)) {
                        if (value instanceof String) {
                            maxDelay = Duration.parse((String) value);
                        } else if (value instanceof Duration) {
                            maxDelay = (Duration) value;
                        }
                    }
                }

                try {
                    if (clazz.getName().contains("ConstantDelay")) {
                        if (delay == null) {
                            throw new IllegalStateException("Missing delay for " + clazz.getName());
                        }
                        java.lang.reflect.Constructor<?> constructor = clazz.getConstructor(Duration.class);
                        return constructor.newInstance(delay);
                    } else {
                        if (baseDelay == null || maxDelay == null) {
                            throw new IllegalStateException("Missing baseDelay or maxDelay for " + clazz.getName() +
                                    ". Got: baseDelay=" + baseDelay + ", maxDelay=" + maxDelay);
                        }
                        java.lang.reflect.Constructor<?> constructor = clazz.getConstructor(Duration.class, Duration.class);
                        return constructor.newInstance(baseDelay, maxDelay);
                    }
                } catch (Exception e) {
                    throw new IllegalStateException("Failed to construct " + clazz.getName(), e);
                }
            }
        }

        private final class ConstructKryo5Codec extends ConstructMapping {
            @Override
            public Object construct(Node node) {
                MappingNode mappingNode = (MappingNode) node;

                List<NodeTuple> tuples = mappingNode.getValue();
                if (tuples.isEmpty()) {
                    flattenMapping(mappingNode);
                    tuples = mappingNode.getValue();
                }

                boolean useReferences = false;
                Set<String> allowedClasses = new LinkedHashSet<>();

                for (NodeTuple tuple : tuples) {
                    Node keyNode = tuple.getKeyNode();
                    if (!(keyNode instanceof org.yaml.snakeyaml.nodes.ScalarNode)) {
                        continue;
                    }

                    String key = ((org.yaml.snakeyaml.nodes.ScalarNode) keyNode).getValue();
                    Object value = constructObject(tuple.getValueNode());

                    if ("useReferences".equals(key)) {
                        if (value instanceof Boolean) {
                            useReferences = (Boolean) value;
                        } else if (value != null) {
                            useReferences = Boolean.parseBoolean(value.toString());
                        }
                    } else if ("allowedClasses".equals(key) && value instanceof Collection) {
                        for (Object item : (Collection<?>) value) {
                            if (item != null) {
                                allowedClasses.add(item.toString());
                            }
                        }
                    }
                }

                return new Kryo5Codec(allowedClasses, useReferences);
            }
        }
    }

    /** 自定义 YAML 表示器：Duration/Enum/Set 格式化及类名标签处理。 */
    private static class CustomRepresenter extends Representer {
        private final Set<Class<?>> classTypedClasses = new HashSet<>(Arrays.asList(
                ReferenceCodecProvider.class,
                AddressResolverGroupFactory.class,
                Codec.class,
                RedissonNodeInitializer.class,
                LoadBalancer.class,
                NatMapper.class,
                NameMapper.class,
                NettyHook.class,
                CredentialsResolver.class,
                EventLoopGroup.class,
                ConnectionListener.class,
                ExecutorService.class,
                CommandMapper.class,
                FailedNodeDetector.class,
                DelayStrategy.class,
                EqualJitterDelay.class,
                FullJitterDelay.class,
                DecorrelatedJitterDelay.class,
                ConstantDelay.class
        ));

        CustomRepresenter(DumperOptions dumperOptions, boolean useCaseInsensitive) {
            super(dumperOptions);

            CustomPropertyUtils propUtils = new CustomPropertyUtils(useCaseInsensitive);
            this.setPropertyUtils(propUtils);

            // 跳过 null 值（类似 Jackson NON_NULL）
            this.setDefaultFlowStyle(dumperOptions.getDefaultFlowStyle());

            // 移除默认 Set 表示器
            this.multiRepresenters.remove(Set.class);

            // Duration 序列化为 ISO-8601 字符串
            this.addClassTag(Duration.class, Tag.STR);
            this.representers.put(Duration.class, data -> {
                Duration duration = (Duration) data;
                return representScalar(Tag.STR, duration.toString());
            });

            // Set 表示为 YAML 序列
            this.multiRepresenters.put(Set.class, data -> {
                Set<?> set = (Set<?>) data;
                List<?> list = new ArrayList<>(set);
                return representSequence(Tag.SEQ, list, DumperOptions.FlowStyle.AUTO);
            });

            // 枚举表示为带引号字符串
            this.multiRepresenters.put(Enum.class, data -> {
                return representScalar(Tag.STR, ((Enum<?>) data).name(), DumperOptions.ScalarStyle.DOUBLE_QUOTED);
            });

            // 根 Config 对象不使用全局类标签
            this.addClassTag(Config.class, Tag.MAP);
        }

        @Override
        public Node represent(Object data) {
            if (data != null && !data.getClass().isPrimitive()
                    && !data.getClass().isArray()
                    && !(data instanceof String)
                    && !(data instanceof Number)
                    && !(data instanceof Boolean)
                    && !(data instanceof Duration)
                    && !(data instanceof Enum)
                    && !(data instanceof Collection)
                    && !(data instanceof Map)) {

                Set<Property> properties = getProperties(data.getClass());
                return representJavaBean(properties, data);
            }
            return super.represent(data);
        }

        @Override
        protected MappingNode representJavaBean(Set<Property> properties, Object javaBean) {
            MappingNode node = super.representJavaBean(properties, javaBean);

            // 需显式类名的对象覆盖 YAML 标签
            if (shouldIncludeClassName(javaBean.getClass()) && !(javaBean instanceof Config)) {
                // 标签格式 !classname，后续 fixTagFormat 转为 !<classname>
                node.setTag(new Tag("!" + javaBean.getClass().getName()));
            }

            // 小型 DelayStrategy 对象使用 flow 风格
            if (javaBean instanceof EqualJitterDelay
                    || javaBean instanceof FullJitterDelay
                        || javaBean instanceof DecorrelatedJitterDelay
                            || javaBean instanceof ConstantDelay) {
                node.setFlowStyle(DumperOptions.FlowStyle.FLOW);
            } else if (shouldIncludeClassName(javaBean.getClass())
                            && !(javaBean instanceof Config)) {
                // 其他带标签的小对象也尝试 flow 风格
                if (properties.size() <= 2 && hasOnlySimpleProperties(javaBean, properties)) {
                    node.setFlowStyle(DumperOptions.FlowStyle.FLOW);
                }
            }

            return node;
        }

        @Override
        protected NodeTuple representJavaBeanProperty(Object javaBean,
                                                                               Property property, Object propertyValue, Tag customTag) {
            if (propertyValue == null) {
                return null;
            }

            // DelayStrategy 通过构造器创建，需输出只读属性
            boolean isDelayStrategy = javaBean instanceof EqualJitterDelay
                                                    || javaBean instanceof FullJitterDelay
                                                    || javaBean instanceof DecorrelatedJitterDelay
                                                    || javaBean instanceof ConstantDelay;

            // 无 setter 的只读属性默认跳过（DelayStrategy 除外）
            if (!property.isWritable() && !isDelayStrategy) {
                return null;
            }

            // 字符串值强制双引号
            if (propertyValue instanceof String) {
                Node valueNode = representScalar(Tag.STR, (String) propertyValue, DumperOptions.ScalarStyle.DOUBLE_QUOTED);
                Node keyNode = representData(property.getName());
                return new NodeTuple(keyNode, valueNode);
            }

            return super.representJavaBeanProperty(javaBean, property, propertyValue, customTag);
        }

        private boolean hasOnlySimpleProperties(Object javaBean, Set<Property> properties) {
            try {
                for (Property property : properties) {
                    Object value = property.get(javaBean);
                    if (value != null && !isSimpleType(value.getClass())) {
                        return false;
                    }
                }
                return true;
            } catch (Exception e) {
                return false;
            }
        }

        private boolean isSimpleType(Class<?> clazz) {
            return clazz.isPrimitive()
                    || clazz == String.class
                    || clazz == Integer.class
                    || clazz == Long.class
                    || clazz == Boolean.class
                    || clazz == Double.class
                    || clazz == Float.class
                    || Duration.class.isAssignableFrom(clazz);
        }

        boolean shouldIncludeClassName(Class<?> clazz) {
            for (Class<?> classTyped : classTypedClasses) {
                if (classTyped.isAssignableFrom(clazz)) {
                    return true;
                }
            }
            return false;
        }
    }

    /** 从 Readable 读取内容并解析环境变量占位符。 */
    private String resolveEnvParams(Readable in) {
        try (Scanner s = new Scanner(in).useDelimiter("\\A")) {
            if (s.hasNext()) {
                return resolveEnvParams(s.next());
            }
            return "";
        }
    }

    private static final Pattern ENV_PARAM_PATTERN =
            Pattern.compile("\\$\\{([\\w\\.]+(:-.+?)?)\\}");

    /** 将 ${ENV} 或 ${ENV:-default} 替换为系统属性或环境变量值。 */
    private String resolveEnvParams(String content) {
        Matcher m = ENV_PARAM_PATTERN.matcher(content);
        while (m.find()) {
            String[] parts = m.group(1).split(":-");
            String v = System.getenv(parts[0]);
            v = System.getProperty(parts[0], v);
            if (v != null) {
                content = content.replace(m.group(), v);
            } else if (parts.length == 2) {
                content = content.replace(m.group(), parts[1]);
            }
        }
        return content;
    }

    /** 从 YAML 字符串反序列化为指定配置类型。 */
    public <T> T fromYAML(String content, Class<T> configType) {
        content = resolveEnvParams(content);
        content = unfixTagFormat(content);
        Yaml yaml = createYamlParser(classLoader, useCaseInsensitive);
        return yaml.loadAs(content, configType);
    }

    /** 从文件读取 YAML 配置。 */
    public <T> T fromYAML(File file, Class<T> configType) throws IOException {
        return fromYAML(file, configType, null);
    }

    /** 从文件读取 YAML，并使用指定 ClassLoader 加载类。 */
    public <T> T fromYAML(File file, Class<T> configType, ClassLoader classLoader) throws IOException {
        LoaderOptions loaderOptions = new LoaderOptions();
        loaderOptions.setTagInspector(tag -> true); // Allow all tags

        DumperOptions dumperOptions = new DumperOptions();
        Yaml yamlParser = new Yaml(new DelayConstructor(classLoader, loaderOptions, useCaseInsensitive),
                new CustomRepresenter(dumperOptions, useCaseInsensitive),
                dumperOptions,
                loaderOptions);
        String content = resolveEnvParams(new FileReader(file));
        content = unfixTagFormat(content);
        return yamlParser.loadAs(content, configType);
    }

    /** 从 URL 读取 YAML 配置。 */
    public <T> T fromYAML(URL url, Class<T> configType) throws IOException {
        String content = resolveEnvParams(new InputStreamReader(url.openStream()));
        content = unfixTagFormat(content);
        Yaml yaml = createYamlParser(classLoader, useCaseInsensitive);
        return yaml.loadAs(content, configType);
    }

    /** 从 Reader 读取 YAML 配置。 */
    public <T> T fromYAML(Reader reader, Class<T> configType) throws IOException {
        String content = resolveEnvParams(reader);
        content = unfixTagFormat(content);
        Yaml yaml = createYamlParser(classLoader, useCaseInsensitive);
        return yaml.loadAs(content, configType);
    }

    /** 从 InputStream 读取 YAML 配置。 */
    public <T> T fromYAML(InputStream inputStream, Class<T> configType) {
        String content = resolveEnvParams(new InputStreamReader(inputStream));
        content = unfixTagFormat(content);
        Yaml yaml = createYamlParser(classLoader, useCaseInsensitive);
        return yaml.loadAs(content, configType);
    }

    /** 将 Config 序列化为 YAML 字符串。 */
    public String toYAML(Config config) {
        Yaml yaml = createYamlParser(classLoader, useCaseInsensitive);
        String yamlStr = yaml.dump(config);
        return fixTagFormat(yamlStr);
    }

    private static final Pattern TAG_FIX_PATTERN = Pattern.compile("!([a-zA-Z0-9_.]+)");

    private String fixTagFormat(String yaml) {
        // 将 !package.Class 转为 !<package.Class> 字面量标签格式
        Matcher matcher = TAG_FIX_PATTERN.matcher(yaml);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String className = matcher.group(1);
            matcher.appendReplacement(result, "!<" + className + ">");
        }
        matcher.appendTail(result);

        return result.toString();
    }

    private static final Pattern TAG_UNFIX_PATTERN = Pattern.compile("!<([a-zA-Z0-9_.]+)>");

    private String unfixTagFormat(String yaml) {
        // 解析前将 !<className> 转回 !!className
        Matcher matcher = TAG_UNFIX_PATTERN.matcher(yaml);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String className = matcher.group(1);
            matcher.appendReplacement(result, "!!" + className);
        }
        matcher.appendTail(result);

        return result.toString();
    }

    /** 从 Config 中提取当前生效的连接模式子配置并执行池大小校验。 */
    public static BaseConfig<?> getConfig(Config configCopy) {
        if (configCopy.getMasterSlaveServersConfig() != null) {
            validate(configCopy.getMasterSlaveServersConfig());
            return configCopy.getMasterSlaveServersConfig();
        } else if (configCopy.getSingleServerConfig() != null) {
            validate(configCopy.getSingleServerConfig());
            return configCopy.getSingleServerConfig();
        } else if (configCopy.getSentinelServersConfig() != null) {
            validate(configCopy.getSentinelServersConfig());
            return configCopy.getSentinelServersConfig();
        } else if (configCopy.getClusterServersConfig() != null) {
            validate(configCopy.getClusterServersConfig());
            return configCopy.getClusterServersConfig();
        } else if (configCopy.getReplicatedServersConfig() != null) {
            validate(configCopy.getReplicatedServersConfig());
            return configCopy.getReplicatedServersConfig();
        }

        throw new IllegalArgumentException("server(s) address(es) not defined!");
    }

    /** 校验单节点连接池大小不小于最小空闲连接数。 */
    private static void validate(SingleServerConfig config) {
        if (config.getConnectionPoolSize() < config.getConnectionMinimumIdleSize()) {
            throw new IllegalArgumentException("connectionPoolSize can't be lower than connectionMinimumIdleSize");
        }
    }

    /** 校验主从/集群等模式下主从与订阅连接池参数合法性。 */
    private static void validate(BaseMasterSlaveServersConfig<?> config) {
        if (config.getSlaveConnectionPoolSize() < config.getSlaveConnectionMinimumIdleSize()) {
            throw new IllegalArgumentException("slaveConnectionPoolSize can't be lower than slaveConnectionMinimumIdleSize");
        }
        if (config.getMasterConnectionPoolSize() < config.getMasterConnectionMinimumIdleSize()) {
            throw new IllegalArgumentException("masterConnectionPoolSize can't be lower than masterConnectionMinimumIdleSize");
        }
        if (config.getSubscriptionConnectionPoolSize() < config.getSubscriptionConnectionMinimumIdleSize()) {
            throw new IllegalArgumentException("slaveSubscriptionConnectionMinimumIdleSize can't be lower than slaveSubscriptionConnectionPoolSize");
        }
    }

}