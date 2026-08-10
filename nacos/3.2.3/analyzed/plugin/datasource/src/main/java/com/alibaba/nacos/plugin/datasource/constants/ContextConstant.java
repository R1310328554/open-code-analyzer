/*
 * Copyright 1999-2023 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.plugin.datasource.constants;

/**
 * 数据源插件 Mapper 上下文键名常量。
 *
 * <p>定义 {@link com.alibaba.nacos.plugin.datasource.model.MapperContext} 查询/更新参数在上下文中的键名。</p>
 *
 * @author zunfei.lzf
 */
public class ContextConstant {
    
    /** 分页导出时是否携带配置内容（{@code content}）字段。 */
    public static final String NEED_CONTENT = "needContent";
    
}
