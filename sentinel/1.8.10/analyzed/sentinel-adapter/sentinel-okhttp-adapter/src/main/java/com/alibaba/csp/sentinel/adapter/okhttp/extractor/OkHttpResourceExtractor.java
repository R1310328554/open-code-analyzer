/*
 * Copyright 1999-2020 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.adapter.okhttp.extractor;

import okhttp3.Connection;
import okhttp3.Request;

/**
 * OkHttp 资源名提取器接口。
 *
 * @author zhaoyuguang
 */
public interface OkHttpResourceExtractor {

    /**
     * 从 HTTP 请求中提取 Sentinel 资源名。
     *
     * @param request    HTTP 请求实体
     * @param connection HTTP 连接
     * @return 当前请求的资源名
     */
    String extract(Request request, Connection connection);
}
