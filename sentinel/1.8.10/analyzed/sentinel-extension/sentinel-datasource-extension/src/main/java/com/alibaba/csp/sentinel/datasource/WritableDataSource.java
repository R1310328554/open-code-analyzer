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

/**
 * 可写数据源接口：将规则持久化到外部存储（如本地文件）。
 *
 * @author Eric Zhao
 * @since 0.2.0
 */
public interface WritableDataSource<T> {

    /**
     * 将 {@code value} 序列化后写入后端存储。
     *
     * @param value 待写入的配置对象
     * @throws Exception IO 或其他异常
     */
    void write(T value) throws Exception;

    /**
     * 关闭数据源并释放相关资源。
     *
     * @throws Exception IO 或其他异常
     */
    void close() throws Exception;
}
