/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.rocketmq.remoting.protocol;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.apache.rocketmq.common.action.RocketMQAction;
import org.apache.rocketmq.remoting.CommandCustomHeader;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

/**
 * 请求头类型注册表：扫描 {@link CommandCustomHeader} 子类并按 {@link RequestCode} 建立映射。
 */
public class RequestHeaderRegistry {

    /** 请求头类所在包，供 Reflections 扫描。 */
    private static final String PACKAGE_NAME = "org.apache.rocketmq.remoting.protocol.header";

    /** RequestCode → 请求头 Class 映射。 */
    private final Map<Integer, Class<? extends CommandCustomHeader>> requestHeaderMap = new HashMap<>();

    /** 返回单例注册表。 */
    public static RequestHeaderRegistry getInstance() {
        return RequestHeaderRegistryHolder.INSTANCE;
    }

    /** 扫描 header 包并注册带 {@link RocketMQAction} 注解的请求头类。 */
    public void initialize() {
        Reflections reflections = new Reflections(new ConfigurationBuilder()
            .setUrls(ClasspathHelper.forPackage(PACKAGE_NAME))
            .setScanners(new SubTypesScanner(false)));

        Set<Class<? extends CommandCustomHeader>> classes = reflections.getSubTypesOf(CommandCustomHeader.class);

        classes.forEach(this::registerHeader);
    }

    /** 按 RequestCode 查找对应请求头类型，未注册返回 null。 */
    public Class<? extends CommandCustomHeader> getRequestHeader(int requestCode) {
        return this.requestHeaderMap.get(requestCode);
    }

    /** 读取 {@link RocketMQAction#value()} 并写入映射表。 */
    private void registerHeader(Class<? extends CommandCustomHeader> clazz) {
        if (!clazz.isAnnotationPresent(RocketMQAction.class)) {
            return;
        }
        RocketMQAction action = clazz.getAnnotation(RocketMQAction.class);
        this.requestHeaderMap.putIfAbsent(action.value(), clazz);
    }

    /** 静态内部类持有单例，延迟加载。 */
    private static class RequestHeaderRegistryHolder {
        private static final RequestHeaderRegistry INSTANCE = new RequestHeaderRegistry();
    }
}
