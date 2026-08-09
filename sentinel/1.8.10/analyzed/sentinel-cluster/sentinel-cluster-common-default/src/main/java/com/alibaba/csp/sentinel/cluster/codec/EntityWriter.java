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
package com.alibaba.csp.sentinel.cluster.codec;

/**
 * 将实体写入目标流的通用接口。
 *
 * @param <E> 实体类型
 * @param <T> 目标流类型
 * @author Eric Zhao
 * @since 1.4.0
 */
public interface EntityWriter<E, T> {

    /**
     * 将给定实体写入目标流。
     *
     * @param entity 待发布的实体
     * @param target 目标流
     */
    void writeTo(E entity, T target);
}
