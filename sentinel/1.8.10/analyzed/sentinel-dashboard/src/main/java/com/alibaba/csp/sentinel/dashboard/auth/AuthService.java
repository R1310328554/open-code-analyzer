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
package com.alibaba.csp.sentinel.dashboard.auth;

/**
 * 控制台认证与授权服务接口。
 *
 * @author Carpenter Lee
 * @since 1.5.0
 */
public interface AuthService<R> {

    /**
     * 从请求中解析当前认证用户。
     *
     * @param request 携带用户信息的请求对象
     * @return 当前用户；若未认证或非法则返回 null
     */
    AuthUser getAuthUser(R request);

    /** 权限类型枚举。 */

    enum PrivilegeType {
        /** 读取规则。 */

        READ_RULE,
        /** 创建或修改规则。 */

        WRITE_RULE,
        /** 删除规则。 */

        DELETE_RULE,
        /** 读取监控指标。 */

        READ_METRIC,
        /** 添加机器。 */

        ADD_MACHINE,
        /** 授予上述全部权限。 */

        ALL
    }

    /** 当前认证用户抽象。 */

    interface AuthUser {

        /**
         * 判断当前用户对指定目标是否拥有给定权限；目标可为应用名、IP 等。
         * <p>
         * 通常通过返回值表示是否有权限；抛出 {@link RuntimeException} 表示未授权也是可接受的做法。
         * </p>
         *
         * @param target        待校验目标
         * @param privilegeType 权限类型
         * @return 有权限返回 true，否则 false
         */
        boolean authTarget(String target, PrivilegeType privilegeType);

        /**
         * 判断当前用户是否为超级用户。
         *
         * @return 超级用户返回 true，否则 false
         */
        boolean isSuperUser();

        /**
         * 返回当前用户昵称。
         *
         * @return 用户昵称
         */
        String getNickName();

        /**
         * 返回当前用户登录名。
         *
         * @return 登录名
         */
        String getLoginName();

        /**
         * 返回当前用户 ID。
         *
         * @return 用户 ID
         */
        String getId();
    }
}
