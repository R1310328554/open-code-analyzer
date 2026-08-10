/*
 * Copyright 1999-2025 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.api.ai.remote.response;

import com.alibaba.nacos.api.remote.response.Response;

/**
 * 发布 Agent Card 的远程响应。
 *
 * <p>继承 {@link com.alibaba.nacos.api.remote.response.Response}，
 * 成功时通过基类 {@code resultCode} 与 {@code message} 字段返回操作结果；
 * 本类无额外业务字段，表示发布操作已完成。</p>
 *
 * @author xiweng.yy
 */
public class ReleaseAgentCardResponse extends Response {
}
