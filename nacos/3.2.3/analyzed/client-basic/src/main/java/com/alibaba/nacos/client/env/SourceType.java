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

package com.alibaba.nacos.client.env;

/**
 * properties source type enum.
 * <p>客户端配置属性来源枚举，用于 {@link NacosClientProperties#getPropertyFrom(SourceType, String)} 定向读取及 {@link SearchableProperties} 检索顺序配置。</p>
 * @author onewe
 */
public enum SourceType {
    /** 内存 {@link Properties} 或派生链中的显式设置（优先级通常最高） */
    PROPERTIES,
    /** JVM 启动参数 {@code -Dkey=value}（{@link System#getProperties()}） */
    JVM,
    /** 操作系统环境变量（{@link System#getenv()}） */
    ENV,
    /** 未指定来源时按全局检索顺序依次查找全部属性源 */
    UNKNOWN
}
