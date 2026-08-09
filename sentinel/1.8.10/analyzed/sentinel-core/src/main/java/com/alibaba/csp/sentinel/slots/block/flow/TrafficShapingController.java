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
package com.alibaba.csp.sentinel.slots.block.flow;

import com.alibaba.csp.sentinel.node.Node;

/**
 * 流量整形控制器的通用接口。
 *
 * @author jialiang.linjl
 */
public interface TrafficShapingController {

    /**
     * 校验给定资源入口在指定数量下是否可通过。
     *
     * @param node 资源节点
     * @param acquireCount 要获取的配额数
     * @param prioritized 是否为优先级请求
     * @return 可通过返回 true；应被阻断返回 false
     */
    boolean canPass(Node node, int acquireCount, boolean prioritized);

    /**
     * 校验给定资源入口在指定数量下是否可通过。
     *
     * @param node 资源节点
     * @param acquireCount 要获取的配额数
     * @return 可通过返回 true；应被阻断返回 false
     */
    boolean canPass(Node node, int acquireCount);
}
