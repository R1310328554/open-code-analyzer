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

package com.alibaba.nacos.naming.cluster;

/**
 * 命名服务端运行状态枚举：标识节点是否可对外提供服务。
 *
 * @author nkorange
 * @since 1.0.0
 */
public enum ServerStatus {
    /** 节点正常运行，可处理读写请求。 */
    UP,
    /** 节点异常下线，不可对外服务。 */
    DOWN,
    /** 节点启动中，通常随后变为 UP。 */
    STARTING,
    /** 节点被手动暂停服务。 */
    PAUSED,
    /** 仅允许写操作（读不可用）。 */
    WRITE_ONLY,
    /** 仅允许读操作（写不可用）。 */
    READ_ONLY
}
