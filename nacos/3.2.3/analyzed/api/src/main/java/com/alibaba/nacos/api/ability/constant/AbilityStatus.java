/*
 * Copyright 1999-2022 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.api.ability.constant;

/**
 * 能力支持状态枚举，表示对某项能力是否可用或未知。
 *
 * @author Daydreamer
 * @description It is used to know a certain ability whether supporting.
 * @date 2022/8/31 12:27
 **/
public enum AbilityStatus {
    
    /** 支持该能力。 */
    SUPPORTED,
    
    /** 不支持该能力。 */
    NOT_SUPPORTED,
    
    /** 未找到能力表，状态未知。 */
    UNKNOWN
}
