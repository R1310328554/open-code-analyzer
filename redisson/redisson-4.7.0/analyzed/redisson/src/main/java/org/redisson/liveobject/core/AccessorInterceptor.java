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

import io.netty.buffer.ByteBuf;
import net.bytebuddy.implementation.bind.annotation.*;
import org.redisson.RedissonObject;
import org.redisson.RedissonReference;
import org.redisson.RedissonScoredSortedSet;
import org.redisson.RedissonSetMultimap;
import org.redisson.api.*;
import org.redisson.api.annotation.REntity;
import org.redisson.api.annotation.REntity.TransformationMode;
import org.redisson.api.annotation.RIndex;
import org.redisson.client.codec.Codec;
import org.redisson.client.protocol.RedisCommands;
import org.redisson.command.CommandAsyncExecutor;
import org.redisson.command.CommandBatchService;
import org.redisson.liveobject.misc.ClassUtils;
import org.redisson.liveobject.misc.Introspectior;
import org.redisson.liveobject.resolver.MapResolver;
import org.redisson.liveobject.resolver.NamingScheme;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

/**
 * Live Object 字段访问拦截器，由 ByteBuddy 注入为代理目标类的<b>静态</b>字段。
 * <p>
 * 每个被代理实体类对应一个实例，拦截 getter/setter 方法：
 * 从 {@code liveObjectLiveMap} 读写 Redis Hash 字段值，并维护 {@link RIndex} 索引。
 *
 * @author Rui Gu (https://github.com/jackygurui)
 * @author Nikita Koksharov
 */
public class AccessorInterceptor {

    /** 识别 getter 方法名前缀（get/is）。 */
    private static final Pattern GETTER_PATTERN = Pattern.compile("^(get|is)");
    /** 识别 setter 方法名前缀（set）。 */
    private static final Pattern SETTER_PATTERN = Pattern.compile("^(set)");
    /** 从方法名剥离前缀以推导字段名。 */
    private static final Pattern FIELD_PATTERN = Pattern.compile("^(get|set|is)");

    /** 异步命令执行器。 */
    private final CommandAsyncExecutor commandExecutor;
    /** 被代理的实体类。 */
    private final Class<?> entityClass;
    /** 解析/创建 liveObjectLiveMap 的策略。 */
    private final MapResolver mapResolver;

    /** @param entityClass 实体类；@param commandExecutor 命令执行器；@param mapResolver Map 解析器 */
    public AccessorInterceptor(Class<?> entityClass, CommandAsyncExecutor commandExecutor,
                               MapResolver mapResolver) {
        this.entityClass = entityClass;
        this.commandExecutor = commandExecutor;
        this.mapResolver = mapResolver;
    }

    /**
     * ByteBuddy 拦截入口：处理 RId getter/setter、普通字段 getter/setter。
     * <p>
     * transient 字段直接反射读写；{@link RIndex} 字段在 set 时同步更新 Redis 索引结构。
     */
    @RuntimeType
    @SuppressWarnings("NestedIfDepth")
    public Object intercept(@Origin Method method,
                            @SuperCall Callable<?> superMethod,
                            @AllArguments Object[] args,
                            @This Object me,
                            @FieldProxy("liveObjectLiveMap") LiveObjectInterceptor.Setter mapSetter,
                            @FieldProxy("liveObjectLiveMap") LiveObjectInterceptor.Getter mapGetter
    ) throws Exception {
        if (isGetter(method, getREntityIdFieldName(me))) {
            return ((RLiveObject) me).getLiveObjectId();
        }
        if (isSetter(method, getREntityIdFieldName(me))) {
            ((RLiveObject) me).setLiveObjectId(args[0]);
            return null;
        }

        Object id = ((RLiveObject) me).getLiveObjectId();
        RMap<String, Object> liveMap = mapResolver.resolve(commandExecutor, entityClass, id, mapSetter, mapGetter);

        String fieldName = getFieldName(me.getClass().getSuperclass(), method);
        Field field = ClassUtils.getDeclaredField(me.getClass().getSuperclass(), fieldName);
        Class<?> fieldType = field.getType();

        boolean isCollectionIndex = field.getAnnotation(RIndex.class) != null
                && (Collection.class.isAssignableFrom(field.getType()) || field.getType().isArray());

        if (isGetter(method, fieldName)) {
            if (Modifier.isTransient(field.getModifiers())) {
                return field.get(me);
            }

            Object result = liveMap.get(fieldName);
            if (result == null) {
                RObject ar = commandExecutor.getObjectBuilder().createObject(((RLiveObject) me).getLiveObjectId(), me.getClass().getSuperclass(), fieldType, fieldName);
                if (ar != null) {
                    commandExecutor.getObjectBuilder().store(ar, fieldName, liveMap);
                    if (isCollectionIndex && ar instanceof Collection) {
                        return wrapForIndexUpdates((Collection<?>) ar, field, (RLiveObject) me);
                    }
                    return ar;
                }
            }

            if (result != null && fieldType.isEnum()) {
                if (result instanceof String) {
                    return Enum.valueOf((Class) fieldType, (String) result);
                }
                return result;
            }
            if (result instanceof RedissonReference) {
                return commandExecutor.getObjectBuilder().fromReference((RedissonReference) result, RedissonObjectBuilder.ReferenceType.DEFAULT);
            }
            if (isCollectionIndex && result instanceof Collection) {
                return wrapForIndexUpdates((Collection<?>) result, field, (RLiveObject) me);
            }
            return result;
        }
        if (isSetter(method, fieldName)) {
            Object arg = args[0];
            if (Modifier.isTransient(field.getModifiers())) {
                field.set(me, arg);
                return me;
            }
            if (arg != null && ClassUtils.isAnnotationPresent(arg.getClass(), REntity.class)) {
                throw new IllegalStateException("REntity object should be attached to Redisson first");
            }

            if (arg instanceof RLiveObject) {
                RLiveObject liveObject = (RLiveObject) arg;

                removeIndex(liveMap, me, field);
                storeIndex(field, me, liveObject.getLiveObjectId());

                if (commandExecutor instanceof CommandBatchService) {
                    liveMap.fastPutAsync(fieldName, liveObject);
                } else {
                    liveMap.fastPut(fieldName, liveObject);
                }

                return me;
            }

            if (!(arg instanceof RObject)
                    && (arg instanceof Collection || arg instanceof Map)
                    && TransformationMode.ANNOTATION_BASED
                    .equals(ClassUtils.getAnnotation(me.getClass().getSuperclass(),
                            REntity.class).fieldTransformation())) {
                Object originalArg = arg;
                RObject rObject = commandExecutor.getObjectBuilder().createObject(((RLiveObject) me).getLiveObjectId(), me.getClass().getSuperclass(), arg.getClass(), fieldName);
                if (arg != null) {
                    if (rObject instanceof Collection) {
                        Collection<?> c = (Collection<?>) rObject;
                        c.clear();
                        c.addAll((Collection) arg);
                    } else {
                        Map<?, ?> m = (Map<?, ?>) rObject;
                        m.clear();
                        m.putAll((Map) arg);
                    }
                }
                if (rObject != null) {
                    arg = rObject;
                }
                if (isCollectionIndex) {
                    removeIndex(liveMap, me, field);
                    if (originalArg != null) {
                        storeIndex(field, me, originalArg);
                    }
                }
            }

            if (arg instanceof RObject) {
                if (commandExecutor instanceof CommandBatchService) {
                    commandExecutor.getObjectBuilder().storeAsync((RObject) arg, fieldName, liveMap);
                } else {
                    commandExecutor.getObjectBuilder().store((RObject) arg, fieldName, liveMap);
                }
                return me;
            }

            removeIndex(liveMap, me, field);
            if (arg != null) {
                storeIndex(field, me, arg);

                if (commandExecutor instanceof CommandBatchService) {
                    liveMap.fastPutAsync(fieldName, arg);
                } else {
                    liveMap.fastPut(fieldName, arg);
                }
            } else {
                if (field.getAnnotation(RIndex.class) == null) {
                    if (commandExecutor instanceof CommandBatchService) {
                        liveMap.removeAsync(fieldName);
                    } else {
                        liveMap.remove(fieldName);
                    }
                }
            }
            return me;
        }
        return superMethod.call();
    }

    /** 数值原始类型集合，索引移除时走 ZSET 分支。 */
    private static final Set<Class<?>> PRIMITIVE_CLASSES = new HashSet<>(Arrays.asList(
            byte.class, short.class, int.class, long.class, float.class, double.class));

    /** 字段值变更或删除前，从 Redis 索引（ZSET 或 SetMultimap）移除旧条目。 */
    private void removeIndex(RMap<String, Object> liveMap, Object me, Field field) {
        if (field.getAnnotation(RIndex.class) == null) {
            return;
        }

        NamingScheme namingScheme = commandExecutor.getObjectBuilder().getNamingScheme(me.getClass().getSuperclass());
        String indexName = namingScheme.getIndexName(me.getClass().getSuperclass(), field.getName());

        CommandBatchService ce;
        if (commandExecutor instanceof CommandBatchService) {
            ce = (CommandBatchService) commandExecutor;
        } else {
            ce = new CommandBatchService(commandExecutor);
        }

        if (Number.class.isAssignableFrom(field.getType()) || PRIMITIVE_CLASSES.contains(field.getType())) {
            RScoredSortedSetAsync<Object> set = new RedissonScoredSortedSet<>(namingScheme.getCodec(), ce, indexName, null);
            set.removeAsync(((RLiveObject) me).getLiveObjectId());
        } else if (Collection.class.isAssignableFrom(field.getType()) || field.getType().isArray()) {
            RMultimapAsync<Object, Object> map = new RedissonSetMultimap<>(namingScheme.getCodec(), ce, indexName);
            map.fastRemoveValueAsync(((RLiveObject) me).getLiveObjectId());
        } else {
            if (ClassUtils.isAnnotationPresent(field.getType(), REntity.class)
                    || commandExecutor.getServiceManager().getCfg().isClusterConfig()) {
                CompletableFuture<Object> f;
                if (commandExecutor instanceof CommandBatchService) {
                    f = liveMap.getAsync(field.getName()).toCompletableFuture();
                } else {
                    Object value = liveMap.get(field.getName());
                    f = CompletableFuture.completedFuture(value);
                }
                f.thenAccept(value -> {
                    if (value != null) {
                        RMultimapAsync<Object, Object> map = new RedissonSetMultimap<>(namingScheme.getCodec(), ce, indexName);
                        Object k = value;
                        if (ClassUtils.isAnnotationPresent(field.getType(), REntity.class)) {
                            k = ((RLiveObject) value).getLiveObjectId();
                        }
                        map.removeAsync(k, ((RLiveObject) me).getLiveObjectId());
                    }
                });
            } else {
                removeAsync(ce, indexName, ((RedissonObject) liveMap).getRawName(),
                        namingScheme.getCodec(), ((RLiveObject) me).getLiveObjectId(), field.getName());
            }
        }

        if (ce != commandExecutor) {
            ce.execute();
        }
    }

    /** 非集群模式下通过 Lua 脚本原子移除 SetMultimap 索引条目。 */
    private void removeAsync(CommandBatchService ce, String name, String mapName, Codec codec, Object value, String fieldName) {
        ByteBuf valueState = ce.encodeMapValue(codec, value);
        ce.evalWriteAsync(name, codec, RedisCommands.EVAL_VOID,
                  "local oldArg = redis.call('hget', KEYS[2], ARGV[2]);" +
                        "if oldArg == false then " +
                            "return; " +
                        "end;" +
                        "local hash = redis.call('hget', KEYS[1], oldArg); " +
                        "local setName = KEYS[1] .. ':' .. hash; " +
                        "local res = redis.call('srem', setName, ARGV[1]); " +
                        "if res == 1 and redis.call('scard', setName) == 0 then " +
                            "redis.call('hdel', KEYS[1], oldArg); " +
                        "end; ",
            Arrays.asList(name, mapName),
                valueState, ce.encodeMapKey(codec, fieldName));
    }

    /** 字段写入后，将新值注册到 Redis 索引（数值→ZSET，其他→SetMultimap）。 */
    private void storeIndex(Field field, Object me, Object arg) {
        if (field.getAnnotation(RIndex.class) == null) {
            return;
        }

        NamingScheme namingScheme = commandExecutor.getObjectBuilder().getNamingScheme(me.getClass().getSuperclass());
        String indexName = namingScheme.getIndexName(me.getClass().getSuperclass(), field.getName());

        boolean skipExecution = false;
        CommandBatchService ce;
        if (commandExecutor instanceof CommandBatchService) {
            ce = (CommandBatchService) commandExecutor;
            skipExecution = true;
        } else {
            ce = new CommandBatchService(commandExecutor);
        }

        if (Collection.class.isAssignableFrom(field.getType()) || field.getType().isArray()) {
            Collection<?> coll;
            if (arg instanceof Collection) {
                coll = (Collection<?>) arg;
            } else {
                int length = Array.getLength(arg);
                List<Object> list = new ArrayList<>(length);
                for (int i = 0; i < length; i++) {
                    list.add(Array.get(arg, i));
                }
                coll = list;
            }
            RMultimapAsync<Object, Object> map = new RedissonSetMultimap<>(namingScheme.getCodec(), ce, indexName);
            for (Object element : coll) {
                if (element == null) {
                    continue;
                }
                Object k = element;
                if (element instanceof RLiveObject) {
                    k = ((RLiveObject) element).getLiveObjectId();
                }
                map.putAsync(k, ((RLiveObject) me).getLiveObjectId());
            }
        } else if (arg instanceof Number) {
            RScoredSortedSetAsync<Object> set = new RedissonScoredSortedSet<>(namingScheme.getCodec(), ce, indexName, null);
            set.addAsync(((Number) arg).doubleValue(), ((RLiveObject) me).getLiveObjectId());
        } else {
            RMultimapAsync<Object, Object> map = new RedissonSetMultimap<>(namingScheme.getCodec(), ce, indexName);
            map.putAsync(arg, ((RLiveObject) me).getLiveObjectId());
        }

        if (!skipExecution) {
            ce.execute();
        }
    }

    /** 从 getter/setter 方法名推导 Java 字段名（支持首字母大小写变体）。 */
    private String getFieldName(Class<?> clazz, Method method) {
        String fieldName = FIELD_PATTERN.matcher(method.getName()).replaceFirst("");
        String propName = fieldName.substring(0, 1).toLowerCase(Locale.ENGLISH) + fieldName.substring(1);
        try {
            ClassUtils.getDeclaredField(clazz, propName);
            return propName;
        } catch (NoSuchFieldException e) {
            return fieldName;
        }
    }

    /** 判断方法是否为指定字段的 getter。 */
    private boolean isGetter(Method method, String fieldName) {
        return GETTER_PATTERN.matcher(method.getName()).replaceFirst("").equalsIgnoreCase(fieldName);
    }

    /** 判断方法是否为指定字段的 setter。 */
    private boolean isSetter(Method method, String fieldName) {
        return SETTER_PATTERN.matcher(method.getName()).replaceFirst("").equalsIgnoreCase(fieldName);
    }

    /** 返回实体 {@link org.redisson.api.annotation.RId} 字段名。 */
    private static String getREntityIdFieldName(Object o) {
        return Introspectior.getREntityIdFieldName(o.getClass().getSuperclass());
    }

    /** 为带 {@link RIndex} 的集合字段包装代理，在 add/remove 时同步索引。 */
    private Object wrapForIndexUpdates(final Collection<?> delegate, final Field field, final RLiveObject liveObj) {
        final NamingScheme ns = commandExecutor.getObjectBuilder().getNamingScheme(liveObj.getClass().getSuperclass());
        final String indexName = ns.getIndexName(liveObj.getClass().getSuperclass(), field.getName());

        return org.redisson.misc.CollectionSyncProxy.wrap(
                delegate,
                element -> syncCollectionIndex(ns, indexName, liveObj, element),
                element -> removeCollectionIndex(ns, indexName, liveObj, element),
                (oldElement, newElement) -> replaceCollectionIndex(ns, indexName, liveObj, oldElement, newElement),
                () -> clearCollectionIndex(ns, indexName, liveObj));
    }

    /** 集合 add 元素时写入 SetMultimap 索引。 */
    private void syncCollectionIndex(NamingScheme ns, String indexName, RLiveObject liveObj, Object element) {
        CommandBatchService ce = new CommandBatchService(commandExecutor);
        new RedissonSetMultimap<>(ns.getCodec(), ce, indexName)
                .putAsync(resolveKey(element), liveObj.getLiveObjectId());
        ce.execute();
    }

    /** 集合 remove 元素时从 SetMultimap 索引删除。 */
    private void removeCollectionIndex(NamingScheme ns, String indexName, RLiveObject liveObj, Object element) {
        CommandBatchService ce = new CommandBatchService(commandExecutor);
        new RedissonSetMultimap<>(ns.getCodec(), ce, indexName)
                .removeAsync(resolveKey(element), liveObj.getLiveObjectId());
        ce.execute();
    }

    /** 集合元素替换时先删旧索引再写新索引。 */
    private void replaceCollectionIndex(NamingScheme ns, String indexName, RLiveObject liveObj,
                                         Object oldElement, Object newElement) {
        CommandBatchService ce = new CommandBatchService(commandExecutor);
        RMultimapAsync<Object, Object> map = new RedissonSetMultimap<>(ns.getCodec(), ce, indexName);
        if (oldElement != null) {
            map.removeAsync(resolveKey(oldElement), liveObj.getLiveObjectId());
        }
        if (newElement != null) {
            map.putAsync(resolveKey(newElement), liveObj.getLiveObjectId());
        }
        ce.execute();
    }

    /** 集合 clear 时批量移除该 Live Object 在索引中的所有条目。 */
    private void clearCollectionIndex(NamingScheme ns, String indexName, RLiveObject liveObj) {
        CommandBatchService ce = new CommandBatchService(commandExecutor);
        new RedissonSetMultimap<>(ns.getCodec(), ce, indexName)
                .fastRemoveValueAsync(liveObj.getLiveObjectId());
        ce.execute();
    }

    /** 索引键：RLiveObject 取 ID，否则取元素本身。 */
    private static Object resolveKey(Object element) {
        if (element instanceof RLiveObject) {
            return ((RLiveObject) element).getLiveObjectId();
        }
        return element;
    }

}
