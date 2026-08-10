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

package com.alibaba.nacos.api.config.model;

/**
 * 导入配置时遇到同名配置的处理策略。
 *
 * <p>用于批量导入或迁移场景，决定冲突时中止、跳过或覆盖。</p>
 *
 * @author klw
 */
public enum SameConfigPolicy {
    
    /** 发现重复配置时中止整个导入。 */
    ABORT,
    
    /** 发现重复配置时跳过该项。 */
    SKIP,
    
    /** 发现重复配置时覆盖已有配置。 */
    OVERWRITE
    
}
