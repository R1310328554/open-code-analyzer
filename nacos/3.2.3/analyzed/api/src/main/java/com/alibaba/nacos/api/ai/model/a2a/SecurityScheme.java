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
 *
 */

package com.alibaba.nacos.api.ai.model.a2a;

import java.util.HashMap;

/**
 * A2A 安全方案定义，以键值 Map 形式承载 OpenAPI 风格的安全方案属性。
 *
 * <p>出现在 {@link AgentCard#getSecuritySchemes()} 映射中，
 * 键为方案名称，值为类型、位置、格式等字段集合。</p>
 *
 * @author KiteSoar
 */
public class SecurityScheme extends HashMap<String, Object> {
    
    private static final long serialVersionUID = -708604225878249736L;
    
    /** 构造空安全方案，初始容量为 4。 */
    public SecurityScheme() {
        super(4);
    }
}
