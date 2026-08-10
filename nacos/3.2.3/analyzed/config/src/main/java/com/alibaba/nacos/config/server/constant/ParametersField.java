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

package com.alibaba.nacos.config.server.constant;

/**
 * Config HTTP 请求参数字段名常量，供参数校验与 OpenAPI 文档统一引用。
 * Parameters Field.
 *
 * @author haiqi.wang
 * @date 2024/08/13
 */
public final class ParametersField {
    
    /**
     * 批量导出/导入时指定配置类型列表的参数字段名。
     * Types.
     */
    public static final String TYPES = "types";
}
