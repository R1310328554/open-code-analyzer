/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.api.naming.pojo.healthcheck;

import com.alibaba.nacos.api.naming.pojo.healthcheck.impl.Http;
import com.alibaba.nacos.api.naming.pojo.healthcheck.impl.Mysql;
import com.alibaba.nacos.api.naming.pojo.healthcheck.impl.Tcp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 健康检查类型枚举，映射类型名与 {@link AbstractHealthChecker} 实现类。
 *
 * <p>内置 TCP、HTTP、MySQL 与 NONE；支持通过 {@link #registerHealthChecker} 注册 SPI 扩展类型。</p>
 *
 * @author nkorange
 */
public enum HealthCheckType {
    
    /**
     * TCP 端口探测。
     */
    TCP(Tcp.class),
    /**
     * HTTP 路径探测。
     */
    HTTP(Http.class),
    /**
     * MySQL 命令探测。
     */
    MYSQL(Mysql.class),
    /**
     * 不进行健康检查。
     */
    NONE(AbstractHealthChecker.None.class);
    
    /** 对应的检查器实现类。 */
    private final Class<? extends AbstractHealthChecker> healthCheckerClass;
    
    /**
     * JDK 1.6 环境下 Map 泛型需完整类名，故忽略行宽检查。
     */
    @SuppressWarnings("checkstyle:linelength")
    private static final Map<String, Class<? extends AbstractHealthChecker>> EXTEND =
        new ConcurrentHashMap<>();
    
    /**
     * 枚举构造，绑定实现类。
     *
     * @param healthCheckerClass 检查器实现类
     */
    HealthCheckType(Class<? extends AbstractHealthChecker> healthCheckerClass) {
        this.healthCheckerClass = healthCheckerClass;
    }
    
    /**
     * 注册扩展健康检查器类型。
     *
     * @param type               扩展检查器类型名
     * @param healthCheckerClass 扩展检查器实现类
     */
    public static void registerHealthChecker(String type,
        Class<? extends AbstractHealthChecker> healthCheckerClass) {
        if (!EXTEND.containsKey(type)) {
            EXTEND.put(type, healthCheckerClass);
            HealthCheckerFactory.registerSubType(healthCheckerClass, type);
        }
    }
    
    /**
     * 根据类型名解析对应的检查器实现类。
     *
     * @param type 检查器类型名（内置枚举名或扩展注册名）
     * @return 已注册的实现类；扩展类型未命中时返回 {@code null}
     */
    public static Class<? extends AbstractHealthChecker> ofHealthCheckerClass(String type) {
        HealthCheckType enumType;
        try {
            enumType = valueOf(type);
        } catch (Exception e) {
            return EXTEND.get(type);
        }
        return enumType.healthCheckerClass;
    }
    
    /** 返回所有已加载的内置与扩展检查器实现类列表。 */
    public static List<Class<? extends AbstractHealthChecker>> getLoadedHealthCheckerClasses() {
        List<Class<? extends AbstractHealthChecker>> all = new ArrayList<>();
        for (HealthCheckType type : values()) {
            all.add(type.healthCheckerClass);
        }
        for (Map.Entry<String, Class<? extends AbstractHealthChecker>> entry : EXTEND.entrySet()) {
            all.add(entry.getValue());
        }
        return all;
    }
}
