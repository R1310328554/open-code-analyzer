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

package com.alibaba.nacos.plugin.visibility.model;

/**
 * 可见性查询规划的基础谓词枚举。
 *
 * <p>定义列表/范围查询时可用的过滤策略，如全部、公开、所有者等组合。</p>
 *
 * @author xiweng.yy
 */
public enum BaseVisibilityPredicate {
    
    /** 不过滤，返回全部资源。 */
    ALL,
    
    /** 仅返回公开可见的资源。 */
    PUBLIC,
    
    /** 仅返回当前用户拥有的资源。 */
    OWNER,
    
    /** 返回公开资源与当前用户拥有的资源的并集。 */
    PUBLIC_AND_OWNER
}
