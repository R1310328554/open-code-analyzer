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
 * 单条 AI 资源导入执行结果状态枚举。
 *
 * @author xiweng.yy
 * @since 3.2.1
 */
public enum AiResourceImportResultStatus {
    
    /** 条目已成功导入。 */
    SUCCESS,
    
    /** 条目导入失败。 */
    FAILED,
    
    /** 条目因策略被跳过。 */
    SKIPPED
}
