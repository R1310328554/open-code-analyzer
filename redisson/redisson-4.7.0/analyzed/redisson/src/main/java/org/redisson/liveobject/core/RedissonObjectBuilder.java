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
package org.redisson.liveobject.core;

import org.redisson.*;
import org.redisson.api.*;
import org.redisson.api.annotation.REntity;
import org.redisson.api.annotation.RObjectField;
import org.redisson.client.codec.Codec;
import org.redisson.client.protocol.ScoredEntry;
import org.redisson.client.protocol.decoder.ListScanResult;
import org.redisson.client.protocol.decoder.MapScanResult;
import org.redisson.codec.DefaultReferenceCodecProvider;
import org.redisson.codec.ReferenceCodecProvider;
import org.redisson.config.Config;
import org.redisson.liveobject.misc.ClassUtils;
import org.redisson.liveobject.resolver.NamingScheme;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentMap;

/**
 * Live Object 引用与嵌套 {@link RObject} 的构建、序列化与反序列化中心。
 * <p>
 * 负责：根据 Java 集合/队列类型映射到 Redisson 实现类；
 * 在 {@code @REntity} 字段上创建命名正确的子对象；
 * 在 {@link RedissonReference} 与运行时对象之间双向转换（含 List/Set/Map 递归解引用）。
 *
 * @author Rui Gu
 * @author Nikita Koksharov
 *
 */
public class RedissonObjectBuilder {

    /** 反序列化引用时使用的客户端风格。 */
    public enum ReferenceType {RXJAVA, REACTIVE, DEFAULT}

    /** Java 接口/抽象类型 → Redisson 同步实现类的映射表。 */
    private static final Map<Class<?>, Class<? extends RObject>> SUPPORTED_CLASS_MAPPING = new LinkedHashMap<>();
    /** 各 RObject 接口在默认 Codec 下的 {@code getXxx(String)} 工厂方法。 */
    private static final Map<Class<?>, Method> DEFAULT_CODEC_REFERENCES = new HashMap<>();
    /** 各 RObject 接口在自定义 Codec 下的 {@code getXxx(String, Codec)} 工厂方法。 */
    private static final Map<Class<?>, Method> CUSTOM_CODEC_REFERENCES = new HashMap<>();

    /** 静态初始化：填充类型映射并扫描 Client 上的 get* 工厂方法。 */
    static {
        SUPPORTED_CLASS_MAPPING.put(SortedSet.class,      RedissonSortedSet.class);
        SUPPORTED_CLASS_MAPPING.put(Set.class,            RedissonSet.class);
        SUPPORTED_CLASS_MAPPING.put(ConcurrentMap.class,  RedissonMap.class);
        SUPPORTED_CLASS_MAPPING.put(Map.class,            RedissonMap.class);
        SUPPORTED_CLASS_MAPPING.put(BlockingDeque.class,  RedissonBlockingDeque.class);
        SUPPORTED_CLASS_MAPPING.put(Deque.class,          RedissonDeque.class);
        SUPPORTED_CLASS_MAPPING.put(BlockingQueue.class,  RedissonBlockingQueue.class);
        SUPPORTED_CLASS_MAPPING.put(Queue.class,          RedissonQueue.class);
        SUPPORTED_CLASS_MAPPING.put(List.class,           RedissonList.class);
        
        fillCodecMethods(RedissonClient.class, RObject.class);
        fillCodecMethods(RedissonReactiveClient.class, RObjectReactive.class);
        fillCodecMethods(RedissonRxClient.class, RObjectRx.class);
    }

    /** Redisson 全局配置（含默认 Codec）。 */
    private final Config config;
    /** 同步客户端（可为 null，取决于构造方式）。 */
    private RedissonClient redisson;
    /** Reactive 客户端。 */
    private RedissonReactiveClient redissonReactive;
    /** RxJava 客户端。 */
    private RedissonRxClient redissonRx;
    
    /** 引用序列化时按注解/字段解析 Codec 的提供者。 */
    private final ReferenceCodecProvider codecProvider = new DefaultReferenceCodecProvider();
    
    /** 基于同步 {@link RedissonClient} 构造，并注册配置中的默认 Codec。 */
    public RedissonObjectBuilder(RedissonClient redisson) {
        super();
        this.config = redisson.getConfig();
        this.redisson = redisson;

        Codec codec = config.getCodec();
        codecProvider.registerCodec((Class<Codec>) codec.getClass(), codec);
    }

    /** 基于 Reactive 客户端构造。 */
    public RedissonObjectBuilder(RedissonReactiveClient redissonReactive) {
        super();
        this.config = redissonReactive.getConfig();
        this.redissonReactive = redissonReactive;

        Codec codec = config.getCodec();
        codecProvider.registerCodec((Class<Codec>) codec.getClass(), codec);
    }

    /** 基于 RxJava 客户端构造。 */
    public RedissonObjectBuilder(RedissonRxClient redissonRx) {
        super();
        this.config = redissonRx.getConfig();
        this.redissonRx = redissonRx;

        Codec codec = config.getCodec();
        codecProvider.registerCodec((Class<Codec>) codec.getClass(), codec);
    }

    /** 异步将嵌套 RObject 引用写入 Live Object 的 liveMap 字段。 */
    public void storeAsync(RObject ar, String fieldName, RMap<String, Object> liveMap) {
        liveMap.fastPutAsync(fieldName, ar);
    }

    /** 同步将嵌套 RObject 引用写入 Live Object 的 liveMap 字段。 */
    public void store(RObject ar, String fieldName, RMap<String, Object> liveMap) {
        liveMap.fastPut(fieldName, ar);
    }
    
    /**
     * 为 {@code @REntity} 的嵌套集合/队列字段创建对应的 {@link RObject}。
     * <p>
     * 根据 fieldType 查映射表，用 {@link NamingScheme} 生成 Redis key 后调用 Client 工厂。
     */
    public RObject createObject(Object id, Class<?> clazz, Class<?> fieldType, String fieldName) {
        Class<? extends RObject> mappedClass = getMappedClass(fieldType);
        try {
            if (mappedClass != null) {
                Codec fieldCodec = getFieldCodec(clazz, mappedClass, fieldName);
                NamingScheme fieldNamingScheme = getNamingScheme(clazz, fieldCodec);
                String referenceName = fieldNamingScheme.getFieldReferenceName(clazz, id, mappedClass, fieldName);
                
                return createRObject(redisson, mappedClass, referenceName, fieldCodec);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
        return null;
    }
    
    /**
     * 解析字段级或实体级 Codec。
     * <p>
     * WARNING: rEntity 必须是 {@code @This} 对象声明的实体类。
     */
    private Codec getFieldCodec(Class<?> rEntity, Class<? extends RObject> rObjectClass, String fieldName) throws ReflectiveOperationException {
        Field field = ClassUtils.getDeclaredField(rEntity, fieldName);
        if (field.isAnnotationPresent(RObjectField.class)) {
            RObjectField anno = field.getAnnotation(RObjectField.class);
            return codecProvider.getCodec(anno, rEntity, rObjectClass, fieldName, config);
        } else {
            REntity anno = ClassUtils.getAnnotation(rEntity, REntity.class);
            return codecProvider.getCodec(anno, rEntity, config);
        }
    }
    
    /** 根据实体 {@code @REntity} 注解实例化其 namingScheme 策略。 */
    public NamingScheme getNamingScheme(Class<?> entityClass) {
        REntity anno = ClassUtils.getAnnotation(entityClass, REntity.class);
        Codec codec = codecProvider.getCodec(anno, entityClass, config);
        return getNamingScheme(entityClass, codec);
    }
    
    /** 使用指定 Codec 构造 namingScheme 实例。 */
    public NamingScheme getNamingScheme(Class<?> rEntity, Codec c) {
        REntity anno = ClassUtils.getAnnotation(rEntity, REntity.class);
        try {
            return anno.namingScheme().getDeclaredConstructor(Codec.class).newInstance(c);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    /** 按 assignableFrom 顺序查找 fieldType 对应的 Redisson 实现类。 */
    private Class<? extends RObject> getMappedClass(Class<?> cls) {
        for (Entry<Class<?>, Class<? extends RObject>> entrySet : SUPPORTED_CLASS_MAPPING.entrySet()) {
            if (entrySet.getKey().isAssignableFrom(cls)) {
                return entrySet.getValue();
            }
        }
        return null;
    }
    
    /** 扫描 Client 上返回 RObject 的 get* 方法，登记默认/自定义 Codec 两种签名。 */
    private static void fillCodecMethods(Class<?> clientClazz, Class<?> objectClazz) {
        for (Method method : clientClazz.getDeclaredMethods()) {
            if (!method.getReturnType().equals(Void.TYPE)
                    && objectClazz.isAssignableFrom(method.getReturnType())
                    && method.getName().startsWith("get")) {

                Class<?> cls = method.getReturnType();
                if (method.getParameterTypes().length == 2 // 第一参数为 name，第二参数为 codec
                        && String.class == method.getParameterTypes()[0]
                            && Codec.class.isAssignableFrom(method.getParameterTypes()[1])) {
                    CUSTOM_CODEC_REFERENCES.put(cls, method);
                } else if (method.getParameterTypes().length == 1
                            && String.class == method.getParameterTypes()[0]) {
                    DEFAULT_CODEC_REFERENCES.put(cls, method);
                }
            }
        }
    }

    /** 按客户端类型将 {@link RedissonReference} 还原为 Live Object 或 RObject。 */
    public Object fromReference(RedissonReference rr, ReferenceType type) throws ReflectiveOperationException {
        if (type == ReferenceType.REACTIVE) {
            return fromReference(redissonReactive, rr);
        } else if (type == ReferenceType.RXJAVA) {
            return fromReference(redissonRx, rr);
        }
        return fromReference(redisson, rr);
    }
    
    /** 同步客户端路径：REntity 走 LiveObjectService，否则反射调用 get* 工厂。 */
    private Object fromReference(RedissonClient redisson, RedissonReference rr) throws ReflectiveOperationException {
        Class<?> type = rr.getType();
        if (ClassUtils.isAnnotationPresent(type, REntity.class)) {
            RedissonLiveObjectService liveObjectService = (RedissonLiveObjectService) redisson.getLiveObjectService();

            NamingScheme ns = getNamingScheme(type);
            Object id = ns.resolveId(rr.getKeyName());
            return liveObjectService.createLiveObject(type, id);
        }

        return getObject(redisson, rr, type, codecProvider);
    }

    /** 根据引用中的类型与 Codec 信息，反射调用 RedissonClient 上匹配的 get* 方法。 */
    private Object getObject(Object redisson, RedissonReference rr, Class<?> type,
            ReferenceCodecProvider codecProvider) throws ReflectiveOperationException {
        if (type != null) {
            if (!DEFAULT_CODEC_REFERENCES.containsKey(type) && type.getInterfaces().length > 0) {
                type = type.getInterfaces()[0];
            }

            if (isDefaultCodec(rr)) {
                Method m = DEFAULT_CODEC_REFERENCES.get(type);
                if (m != null) {
                    return m.invoke(redisson, rr.getKeyName());
                }
            } else {
                Method m = CUSTOM_CODEC_REFERENCES.get(type);
                if (m != null) {
                    return m.invoke(redisson, rr.getKeyName(), codecProvider.getCodec(rr.getCodecType()));
                }
            }
        }
        throw new ClassNotFoundException("No RObject is found to match class type of " + rr.getTypeName() + " with codec type of " + rr.getCodec());
    }
    
    /** 判断引用是否使用全局默认 Codec（null 或与 config 中类名相同）。 */
    private boolean isDefaultCodec(RedissonReference rr) {
        return rr.getCodec() == null
                || rr.getCodec().equals(config.getCodec().getClass().getName());
    }

    private Object fromReference(RedissonRxClient redisson, RedissonReference rr) throws ReflectiveOperationException {
        Class<?> type = rr.getRxJavaType();
        /** RxJava 客户端暂不支持从引用还原 Live Object，仅还原普通 RObject。 */
        return getObject(redisson, rr, type, codecProvider);
    }
    
    private Object fromReference(RedissonReactiveClient redisson, RedissonReference rr) throws ReflectiveOperationException {
        Class<?> type = rr.getReactiveType();
        /** Reactive 客户端暂不支持从引用还原 Live Object，仅还原普通 RObject。 */
        return getObject(redisson, rr, type, codecProvider);
    }

    /**
     * 将运行时对象转为可序列化的 {@link RedissonReference}。
     * <p>
     * 未 attach 的 {@code @REntity} 禁止直接序列化；{@link RLiveObject} 使用 NamingScheme 生成 key。
     */
    public RedissonReference toReference(Object object) {
        if (object != null && ClassUtils.isAnnotationPresent(object.getClass(), REntity.class)) {
            throw new IllegalArgumentException("REntity should be attached to Redisson before save");
        }
        
        if (object instanceof RObject && !(object instanceof RLiveObject)) {
            Class<?> clazz = object.getClass().getInterfaces()[0];
            
            RObject rObject = (RObject) object;
            if (rObject.getCodec() != null) {
                codecProvider.registerCodec((Class) rObject.getCodec().getClass(), rObject.getCodec());
            }
            return new RedissonReference(clazz, rObject.getName(), rObject.getCodec());
        }
        if (object instanceof RObjectReactive && !(object instanceof RLiveObject)) {
            Class<?> clazz = object.getClass().getInterfaces()[0];

            RObjectReactive rObject = (RObjectReactive) object;
            if (rObject.getCodec() != null) {
                codecProvider.registerCodec((Class) rObject.getCodec().getClass(), rObject.getCodec());
            }
            return new RedissonReference(clazz, rObject.getName(), rObject.getCodec());
        }
        if (object instanceof RObjectRx && !(object instanceof RLiveObject)) {
            Class<?> clazz = object.getClass().getInterfaces()[0];

            RObjectRx rObject = (RObjectRx) object;
            if (rObject.getCodec() != null) {
                codecProvider.registerCodec((Class) rObject.getCodec().getClass(), rObject.getCodec());
            }
            return new RedissonReference(clazz, rObject.getName(), rObject.getCodec());
        }

        try {
            if (object instanceof RLiveObject) {
                Class<?> rEntity = object.getClass().getSuperclass();
                NamingScheme ns = getNamingScheme(rEntity);

                return new RedissonReference(rEntity,
                        ns.getName(rEntity, ((RLiveObject) object).getLiveObjectId()));
            }
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
        return null;
    }

    /** 按 expectedType 的接口与 Codec 是否默认，反射调用 Client 工厂创建 RObject。 */
    private <T extends RObject, K extends Codec> T createRObject(RedissonClient redisson, Class<T> expectedType, String name, K codec) throws ReflectiveOperationException {
        Class<?>[] interfaces = expectedType.getInterfaces();
        for (Class<?> iType : interfaces) {
            boolean isDefaultCodec = codec.getClass() == config.getCodec().getClass();

            if (isDefaultCodec) {
                Method builder = DEFAULT_CODEC_REFERENCES.get(iType);
                if (builder != null) {
                    return (T) builder.invoke(redisson, name);
                }
            } else {
                Method builder = CUSTOM_CODEC_REFERENCES.get(iType);
                if (builder != null) {
                    return (T) builder.invoke(redisson, name, codec);
                }
            }
        }
        
        String codecName = null;
        if (codec != null) {
            codecName = codec.getClass().getName();
        }
        throw new ClassNotFoundException("No RObject is found to match class type of " + expectedType.getName() + " with codec type of " + codecName);
    }

    /**
     * 递归遍历容器结构，将其中 {@link RedissonReference} 替换为真实对象。
     * <p>
     * 支持 List、Set、Map、{@link ListScanResult}、{@link MapScanResult} 及 {@link ScoredEntry}。
     */
    public Object tryHandleReference(Object o, ReferenceType type) throws ReflectiveOperationException {
        boolean hasConversion = false;
        if (o instanceof List) {
            List<Object> r = (List<Object>) o;
            for (int i = 0; i < r.size(); i++) {
                Object ref = tryHandleReference0(r.get(i), type);
                if (ref != r.get(i)) {
                    r.set(i, ref);
                }
            }
            return o;
        } else if (o instanceof Set) {
            Set<Object> set;
            Set<Object> r = (Set<Object>) o;
            boolean useNewSet = o instanceof LinkedHashSet;
            try {
                set = (Set<Object>) o.getClass().getConstructor().newInstance();
            } catch (Exception exception) {
                set = new LinkedHashSet<>();
            }
            for (Object i : r) {
                Object ref = tryHandleReference0(i, type);
                // 此处不检测 ref 是否变化：下方 r.add(ref) 需在首次失败时触发回退；
                // 假设失败是系统性的（如 Set 不支持 add），而非偶发单元素错误。
                if (useNewSet) {
                    set.add(ref);
                } else {
                    try {
                        r.add(ref);
                        set.add(i);
                    } catch (Exception e) {
                        // 原 Set 不支持 add（如 LinkedHashMap$LinkedEntrySet），回退为新建 Set
                        useNewSet = true;
                        set.add(ref);
                    }
                }
                hasConversion |= ref != i;
            }

            if (!hasConversion) {
                return o;
            } else if (useNewSet) {
                return set;
            } else if (!set.isEmpty()) {
                r.removeAll(set);
            }
            return o;
        } else if (o instanceof Map) {
            Map<Object, Object> r = (Map<Object, Object>) o;
            for (Map.Entry<Object, Object> e : r.entrySet()) {
                if (e.getKey() instanceof RedissonReference
                        || e.getValue() instanceof RedissonReference) {
                    Object key = e.getKey();
                    Object value = e.getValue();
                    if (e.getKey() instanceof RedissonReference) {
                        key = fromReference((RedissonReference) e.getKey(), type);
                        r.remove(e.getKey());
                    }
                    if (e.getValue() instanceof RedissonReference) {
                        value = fromReference((RedissonReference) e.getValue(), type);
                    }
                    r.put(key, value);
                }
            }

            return o;
        } else if (o instanceof ListScanResult) {
            tryHandleReference(((ListScanResult) o).getValues(), type);
            return o;
        } else if (o instanceof MapScanResult) {
            MapScanResult scanResult = (MapScanResult) o;
            Map oldMap = ((MapScanResult) o).getMap();
            Map map = (Map) tryHandleReference(oldMap, type);
            if (map != oldMap) {
                MapScanResult<Object, Object> newScanResult
                        = new MapScanResult<Object, Object>(scanResult.getPos(), map);
                newScanResult.setRedisClient(scanResult.getRedisClient());
                return newScanResult;
            } else {
                return o;
            }
        } else {
            return tryHandleReference0(o, type);
        }
    }

    /** 单对象层级的引用解包：Reference、ScoredEntry 内引用、Map.Entry 键值。 */
    private Object tryHandleReference0(Object o, ReferenceType type) throws ReflectiveOperationException {
        if (o instanceof RedissonReference) {
            return fromReference((RedissonReference) o, type);
        } else if (o instanceof ScoredEntry && ((ScoredEntry) o).getValue() instanceof RedissonReference) {
            ScoredEntry<?> se = (ScoredEntry<?>) o;
            return new ScoredEntry(se.getScore(), fromReference((RedissonReference) se.getValue(), type));
        } else if (o instanceof Map.Entry) {
            Map.Entry old = (Map.Entry) o;
            Object key = tryHandleReference0(old.getKey(), type);
            Object value = tryHandleReference0(old.getValue(), type);
            if (value != old.getValue() || key != old.getKey()) {
                return new AbstractMap.SimpleEntry(key, value);
            }
        }
        return o;
    }

}
