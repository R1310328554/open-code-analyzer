/*
 * Copyright 1999-2026 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.api.ai.model.importer;

/**
 * 单条 AI 资源导入校验状态枚举。
 *
 * @author xiweng.yy
 * @since 3.2.1
 */
public enum AiResourceImportValidationStatus {
    
    /** 条目可正常导入。 */
    VALID,
    
    /** 条目可导入但存在警告。 */
    WARNING,
    
    /** 条目不可导入。 */
    INVALID,
    
    /** 条目与已有资源冲突。 */
    CONFLICT
}
