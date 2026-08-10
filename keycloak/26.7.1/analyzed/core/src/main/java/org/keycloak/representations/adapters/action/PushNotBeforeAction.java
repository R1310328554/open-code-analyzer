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

package org.keycloak.representations.adapters.action;

/**
 * 向 adapter 推送 realm {@code notBefore} 时间戳的管理动作，使 adapter 拒绝早于该时刻签发的令牌。
 * <p>
 * 动作类型为 {@link #PUSH_NOT_BEFORE}，由 {@link AdminAction} 基类承载通用字段。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class PushNotBeforeAction extends AdminAction {

    /** 动作类型常量：推送 notBefore。 */
    public static final String PUSH_NOT_BEFORE = "PUSH_NOT_BEFORE";
    /** 新的 notBefore 值（Unix 秒级时间戳）。 */
    protected int notBefore;

    /** 默认无参构造器。 */
    public PushNotBeforeAction() {
    }

    /**
     * 构造推送 notBefore 的管理动作。
     *
     * @param id 动作 ID
     * @param expiration 过期时间（秒）
     * @param resource 目标资源
     * @param notBefore 新的 notBefore 时间戳
     */
    public PushNotBeforeAction(String id, int expiration, String resource, int notBefore) {
        super(id, expiration, resource, PUSH_NOT_BEFORE);
        this.notBefore = notBefore;
    }

    /** 返回 notBefore 时间戳。 */
    public int getNotBefore() {
        return notBefore;
    }

    /** 设置 notBefore 时间戳。 */
    public void setNotBefore(int notBefore) {
        this.notBefore = notBefore;
    }

    /** {@inheritDoc} */
    @Override
    public boolean validate() {
        return PUSH_NOT_BEFORE.equals(action);
    }

}
