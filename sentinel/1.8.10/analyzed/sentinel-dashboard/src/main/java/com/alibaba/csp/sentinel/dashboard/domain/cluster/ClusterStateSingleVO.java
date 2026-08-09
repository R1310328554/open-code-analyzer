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

/**
 * 单台机器的集群状态视图，含地址、运行模式与关联目标。
 *
 * @author Eric Zhao
 * @since 1.4.1
 */
public class ClusterStateSingleVO {

    /** 机器地址，通常为 {@code ip:port}。 */
    private String address;
    /** 集群运行模式（客户端/服务端/未启用等）。 */
    private Integer mode;
    /** 客户端模式下所连服务端地址，或服务端模式下的监听信息。 */
    private String target;

    public String getAddress() {
        return address;
    }

    public ClusterStateSingleVO setAddress(String address) {
        this.address = address;
        return this;
    }

    public Integer getMode() {
        return mode;
    }

    public ClusterStateSingleVO setMode(Integer mode) {
        this.mode = mode;
        return this;
    }

    public String getTarget() {
        return target;
    }

    public ClusterStateSingleVO setTarget(String target) {
        this.target = target;
        return this;
    }

    @Override
    public String toString() {
        return "ClusterStateSingleVO{" +
            "address='" + address + '\'' +
            ", mode=" + mode +
            ", target='" + target + '\'' +
            '}';
    }
}
