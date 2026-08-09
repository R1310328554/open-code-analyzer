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
package org.apache.rocketmq.acl.common;

/**
 * ACL 权限位掩码：发布、订阅、拒绝等权限以位运算组合。
 */
public class Permission {

    /** 拒绝访问。 */
    public static final byte DENY = 1;
    /** 任意权限占位（保留位）。 */
    public static final byte ANY = 1 << 1;
    /** 发布（写）权限。 */
    public static final byte PUB = 1 << 2;
    /** 订阅（读）权限。 */
    public static final byte SUB = 1 << 3;

    /** 将 PUB/SUB/PUB|SUB/DENY 等字符串解析为权限位掩码。 */
    public static byte parsePermFromString(String permString) {
        if (permString == null) {
            return Permission.DENY;
        }
        switch (permString.trim()) {
            case AclConstants.PUB:
                return Permission.PUB;
            case AclConstants.SUB:
                return Permission.SUB;
            case AclConstants.PUB_SUB:
            case AclConstants.SUB_PUB:
                return Permission.PUB | Permission.SUB;
            case AclConstants.DENY:
                return Permission.DENY;
            default:
                return Permission.DENY;
        }
    }
}
