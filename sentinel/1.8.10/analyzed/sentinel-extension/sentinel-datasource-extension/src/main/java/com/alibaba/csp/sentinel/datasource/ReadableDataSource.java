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
package com.alibaba.csp.sentinel.datasource;

import com.alibaba.csp.sentinel.property.SentinelProperty;

/**
 * 只读数据源接口：从后端拉取配置并暴露为 {@link SentinelProperty}。
 *
 * @param <S> 原始数据类型
 * @param <T> 解析后的目标类型
 * @author leyou
 * @author Eric Zhao
 */
public interface ReadableDataSource<S, T> {

    /**
     * 读取原始数据并解析为目标类型。
     *
     * @return 解析后的配置对象
     * @throws Exception IO 或其他异常
     */
    T loadConfig() throws Exception;

    /**
     * 从后端读取原始配置字符串或二进制数据。
     *
     * @return 原始配置
     * @throws Exception IO 或其他异常
     */
    S readSource() throws Exception;

    /**
     * 获取与此数据源绑定的 {@link SentinelProperty}，规则变更时通过其推送。
     *
     * @return 动态属性对象
     */
    SentinelProperty<T> getProperty();

    /**
     * 关闭数据源并释放后台连接或监听资源。
     *
     * @throws Exception IO 或其他异常
     */
    void close() throws Exception;
}
