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

/** ACL 配置键与权限类型常量（PUB/SUB/DENY 等）。 */
public class AclConstants {

    /** 配置文件中的 accessKey 键名。 */
    public static final String CONFIG_ACCESS_KEY = "accessKey";

    /** 配置文件中的 secretKey 键名。 */
    public static final String CONFIG_SECRET_KEY = "secretKey";

    /** 仅发布权限。 */
    public static final String PUB = "PUB";

    /** 仅订阅权限。 */
    public static final String SUB = "SUB";

    /** 拒绝访问。 */
    public static final String DENY = "DENY";

    /** 发布与订阅权限（PUB 在前）。 */
    public static final String PUB_SUB = "PUB|SUB";

    /** 发布与订阅权限（SUB 在前）。 */
    public static final String SUB_PUB = "SUB|PUB";
}
