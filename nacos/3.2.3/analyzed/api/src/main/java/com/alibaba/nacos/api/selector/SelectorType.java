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

package com.alibaba.nacos.api.selector;

/**
 * Nacos 支持的服务实例选择器类型枚举。
 *
 * <p>与服务/订阅配置中的 {@code selector.type} 字段对应。</p>
 *
 * @author nkorange
 * @since 0.7.0
 */
public enum SelectorType {
    /** 未知或未匹配的类型。 */
    unknown,
    /** 不做过滤，返回全部实例。 */
    none,
    /** 按标签表达式筛选。 */
    label,
    /** 按集群名筛选。 */
    cluster,
    /** 按健康状态筛选。 */
    health,
    /** 按启用/禁用状态筛选。 */
    enable
}
