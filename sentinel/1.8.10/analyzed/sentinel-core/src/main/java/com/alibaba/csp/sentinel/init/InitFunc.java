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
package com.alibaba.csp.sentinel.init;

/**
 * Sentinel 初始化 SPI 接口，实现类通过 {@code META-INF/services} 注册。
 *
 * @author Eric Zhao
 */
public interface InitFunc {

    /**
     * 执行初始化逻辑。
     *
     * @throws Exception 初始化失败时抛出
     */
    void init() throws Exception;
}
