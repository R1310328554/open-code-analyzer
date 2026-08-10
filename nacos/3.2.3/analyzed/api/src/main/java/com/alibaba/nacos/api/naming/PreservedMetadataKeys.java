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

package com.alibaba.nacos.api.naming;

/**
 * Nacos 识别的实例/服务元数据保留键名。
 *
 * <p>以 {@code preserved.} 为前缀的键由服务端解释，用于心跳、超时与注册来源等内置行为。</p>
 *
 * @author nkorange
 * @since 1.0.0
 */
public class PreservedMetadataKeys {
    
    /** 标识实例注册来源（如 Dubbo、Spring Cloud）的元数据键。 */
    public static final String REGISTER_SOURCE = "preserved.register.source";
    
    /** 客户端心跳超时（毫秒）元数据键。 */
    public static final String HEART_BEAT_TIMEOUT = "preserved.heart.beat.timeout";
    
    /** IP 被删除前的等待超时（毫秒）元数据键。 */
    public static final String IP_DELETE_TIMEOUT = "preserved.ip.delete.timeout";
    
    /** 客户端心跳间隔（毫秒）元数据键。 */
    public static final String HEART_BEAT_INTERVAL = "preserved.heart.beat.interval";
    
    /** 实例 ID 生成策略元数据键。 */
    public static final String INSTANCE_ID_GENERATOR = "preserved.instance.id.generator";
}
