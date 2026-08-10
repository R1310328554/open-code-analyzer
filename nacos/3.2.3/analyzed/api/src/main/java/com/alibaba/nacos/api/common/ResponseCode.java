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

package com.alibaba.nacos.api.common;

/**
 * API 响应码定义。
 *
 * <p>与 HTTP 状态码分离，提供更细粒度的业务结果信息。推荐编码规则：</p>
 * <ul>
 * <li>全局/通用码以 10001 起</li>
 * <li>命名模块以 20001 起</li>
 * <li>配置模块以 30001 起</li>
 * <li>核心模块以 40001 起</li>
 * </ul>
 *
 * @author nkorange
 * @since 1.2.0
 */
public class ResponseCode {
    
    /** 一切正常。 */
    public static final int OK = 10200;
}
