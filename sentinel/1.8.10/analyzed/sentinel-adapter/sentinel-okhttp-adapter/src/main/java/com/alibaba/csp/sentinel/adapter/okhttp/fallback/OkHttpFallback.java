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
package com.alibaba.csp.sentinel.adapter.okhttp.fallback;

import com.alibaba.csp.sentinel.slots.block.BlockException;
import okhttp3.Connection;
import okhttp3.Request;
import okhttp3.Response;

/**
 * OkHttp 适配器降级处理器接口。
 *
 * @author zhaoyuguang
 */
public interface OkHttpFallback {

    /**
     * 流控触发时的降级处理。
     *
     * @param request    HTTP 请求
     * @param connection HTTP 连接
     * @param e          流控异常
     * @return 降级响应
     */
    Response handle(Request request, Connection connection, BlockException e);
}
