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
 * 测试 adapter 可用性的管理动作，用于管理端探测受管客户端是否在线。
 * <p>
 * 动作类型为 {@link #TEST_AVAILABILITY}，继承 {@link AdminAction} 的通用令牌字段。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class TestAvailabilityAction extends AdminAction {

    /** 动作类型常量：测试可用性。 */
    public static final String TEST_AVAILABILITY = "TEST_AVAILABILITY";

    /** 默认无参构造器。 */
    public TestAvailabilityAction() {
    }

    /**
     * 构造测试可用性的管理动作。
     *
     * @param id 动作 ID
     * @param expiration 过期时间（秒）
     * @param resource 目标资源
     */
    public TestAvailabilityAction(String id, int expiration, String resource) {
        super(id, expiration, resource, TEST_AVAILABILITY);
    }

    /** {@inheritDoc} */
    @Override
    public boolean validate() {
        return TEST_AVAILABILITY.equals(action);
    }

}
