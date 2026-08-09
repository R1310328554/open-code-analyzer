/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.rocketmq.proxy;

/**
 * Proxy 部署模式：本地内嵌 Broker 或集群独立 Proxy。
 */
public enum ProxyMode {
    /** 本地模式：Proxy 与 Broker 同进程。 */
    LOCAL("LOCAL"),
    /** 集群模式：Proxy 独立部署，经 NameServer 路由。 */
    CLUSTER("CLUSTER");

    /** 模式字符串标识。 */
    private final String mode;

    ProxyMode(String mode) {
        this.mode = mode;
    }

    /** 判断字符串是否为 CLUSTER 模式（忽略大小写）。 */
    public static boolean isClusterMode(String mode) {
        if (mode == null) {
            return false;
        }
        return CLUSTER.mode.equals(mode.toUpperCase());
    }

    /** 判断枚举值是否为 CLUSTER。 */
    public static boolean isClusterMode(ProxyMode mode) {
        if (mode == null) {
            return false;
        }
        return CLUSTER.equals(mode);
    }

    /** 判断字符串是否为 LOCAL 模式。 */
    public static boolean isLocalMode(String mode) {
        if (mode == null) {
            return false;
        }
        return LOCAL.mode.equals(mode.toUpperCase());
    }

    /** 判断枚举值是否为 LOCAL。 */
    public static boolean isLocalMode(ProxyMode mode) {
        if (mode == null) {
            return false;
        }
        return LOCAL.equals(mode);
    }
}
