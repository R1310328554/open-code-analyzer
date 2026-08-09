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
package com.alibaba.csp.sentinel.property;

/**
 * {@link SentinelProperty#updateValue(Object)} 需要通知监听器时的回调接口。
 *
 * @author jialiang.linjl
 */
public interface PropertyListener<T> {

    /**
     * 配置更新时的回调。
     *
     * @param value 更新后的值
     */
    void configUpdate(T value);

    /**
     * 配置值首次加载时的回调。
     *
     * @param value 加载的值
     */
    void configLoad(T value);
}
