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
package com.alibaba.csp.sentinel.cluster.request;

/**
 * 集群传输请求接口。
 *
 * @author Eric Zhao
 * @since 1.4.0
 */
public interface Request {

    /**
     * 获取请求类型。
     *
     * @return 请求类型
     */
    int getType();

    /**
     * 获取请求 ID。
     *
     * @return 唯一请求 ID
     */
    int getId();
}
