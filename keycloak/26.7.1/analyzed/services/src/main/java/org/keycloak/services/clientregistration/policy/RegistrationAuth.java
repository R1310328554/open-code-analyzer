/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.keycloak.services.clientregistration.policy;

/**
 * 客户端动态注册时的认证类型。
 * <p>区分匿名注册（无 initialAccessToken/Bearer Token）与已认证注册，并用于选择对应的 {@link ClientRegistrationPolicy} 组件 subType。</p>
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public enum RegistrationAuth {

    /**
     * 匿名注册：未携带 initialAccessToken 或 Bearer Token。
     * <p>通过匿名注册获得的 registrationToken 进行的更新/查看/删除也属于此类型。</p>
     */
    ANONYMOUS,

    /**
     * 已认证注册：携带 initialAccessToken 或 Bearer Token。
     * <p>通过已认证注册或管理控制台创建的 registrationToken 进行的后续操作也属于此类型。</p>
     */
    AUTHENTICATED;

    /** 从字符串解析枚举值（大小写不敏感）。
     * @param regAuth 认证类型名称
     * @return 对应的 {@link RegistrationAuth} 常量
     */
    public static RegistrationAuth fromString(String regAuth) {
        return Enum.valueOf(RegistrationAuth.class, regAuth.toUpperCase());
    }

}
