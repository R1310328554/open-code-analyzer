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
package com.alibaba.csp.sentinel.dashboard.domain.cluster;

import java.util.Set;

/**
 * 集群 Token Server 分配结果视图对象。
 * <p>记录分配失败的服务端/客户端机器集合及参与分配的总数。
 *
 * @author Eric Zhao
 * @since 1.4.1
 */
public class ClusterAppAssignResultVO {

    /** 分配失败的 Token Server 机器标识集合。 */
    private Set<String> failedServerSet;
    /** 分配失败的客户端机器标识集合。 */
    private Set<String> failedClientSet;

    /** 参与本次分配的机器总数。 */
    private Integer totalCount;

    public Set<String> getFailedServerSet() {
        return failedServerSet;
    }

    public ClusterAppAssignResultVO setFailedServerSet(Set<String> failedServerSet) {
        this.failedServerSet = failedServerSet;
        return this;
    }

    public Set<String> getFailedClientSet() {
        return failedClientSet;
    }

    public ClusterAppAssignResultVO setFailedClientSet(Set<String> failedClientSet) {
        this.failedClientSet = failedClientSet;
        return this;
    }

    public Integer getTotalCount() {
        return totalCount;
    }

    public ClusterAppAssignResultVO setTotalCount(Integer totalCount) {
        this.totalCount = totalCount;
        return this;
    }

    @Override
    public String toString() {
        return "ClusterAppAssignResultVO{" +
            "failedServerSet=" + failedServerSet +
            ", failedClientSet=" + failedClientSet +
            ", totalCount=" + totalCount +
            '}';
    }
}
