/*
 * Copyright 1999-2021 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.auth.parser.http;

import com.alibaba.nacos.auth.parser.AbstractResourceParser;

import jakarta.servlet.http.HttpServletRequest;

/**
 * HTTP 资源解析器抽象基类。
 *
 * <p>将 {@link AbstractResourceParser} 的泛型参数固定为 {@link HttpServletRequest}，
 * 各模块 HTTP 解析器（配置、命名、AI 等）继承此类并实现具体的字段提取逻辑。</p>
 *
 * @author xiweng.yy
 */
public abstract class AbstractHttpResourceParser
    extends AbstractResourceParser<HttpServletRequest> {
    
}
