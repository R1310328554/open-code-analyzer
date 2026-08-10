/*
 * Copyright 1999-2022 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.nacos.plugin.datasource.proxy;

import com.alibaba.nacos.common.utils.JacksonUtils;
import com.alibaba.nacos.plugin.datasource.mapper.Mapper;
import com.alibaba.nacos.plugin.datasource.model.MapperResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 数据源插件 Mapper SQL 调用代理。
 *
 * <p>通过 JDK 动态代理拦截 {@link Mapper} 方法调用，
 * 在启用日志时将 SQL 与参数序列化输出，便于调试与审计。</p>
 *
 * @author hyx
 **/
public class MapperProxy implements InvocationHandler {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(MapperProxy.class);
    
    /** 被代理的 Mapper 实现实例。 */
    private Mapper mapper;
    
    /** 按类名缓存的单例代理，避免重复创建。 */
    private static final Map<String, Mapper> SINGLE_MAPPER_PROXY_MAP = new ConcurrentHashMap<>(16);
    
    /**
     * 为给定 Mapper 实现创建动态代理，覆盖其所有 Mapper 子接口。
     *
     * @param mapper Mapper 实现
     * @param <R>    Mapper 子类型
     * @return 代理后的 Mapper 实例
     */
    public <R> R createProxy(Mapper mapper) {
        this.mapper = mapper;
        Class<?> clazz = mapper.getClass();
        Set<Class<?>> interfacesSet = new HashSet<>();
        while (!clazz.equals(Object.class)) {
            interfacesSet.addAll(Arrays.stream(clazz.getInterfaces())
                .filter(Mapper.class::isAssignableFrom)
                .collect(Collectors.toSet()));
            clazz = clazz.getSuperclass();
        }
        return (R) Proxy.newProxyInstance(MapperProxy.class.getClassLoader(),
            interfacesSet.toArray(new Class<?>[interfacesSet.size()]), this);
    }
    
    /**
     * 创建并缓存单例代理，替代每次调用 {@link #createProxy(Mapper)}。
     *
     * @param mapper Mapper 实现
     * @param <R>    Mapper 子类型
     * @return 缓存的单例代理实例
     */
    public static <R> R createSingleProxy(Mapper mapper) {
        return (R) SINGLE_MAPPER_PROXY_MAP.computeIfAbsent(mapper.getClass().getSimpleName(),
            key -> new MapperProxy().createProxy(mapper));
    }
    
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object invoke = method.invoke(mapper, args);
        
        String className = mapper.getClass().getSimpleName();
        String methodName = method.getName();
        String sql;
        if (invoke instanceof MapperResult) {
            sql = ((MapperResult) invoke).getSql();
        } else {
            sql = invoke.toString();
        }
        // 记录 Mapper 方法名、SQL 及入参，便于排查方言差异
        LOGGER.info("[{}] METHOD : {}, SQL : {}, ARGS : {}", className, methodName, sql,
            JacksonUtils.toJson(args));
        return invoke;
    }
}
