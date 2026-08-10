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

import org.keycloak.component.ComponentModel;

/**
 * 客户端注册策略拒绝请求时抛出的异常。
 * <p>可关联触发拒绝的 {@link ComponentModel} 策略组件以生成详细错误消息。</p>
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class ClientRegistrationPolicyException extends RuntimeException {

    /** 触发拒绝的策略组件模型 */
    private ComponentModel policyModel;

    /** @param message 拒绝原因描述 */
    public ClientRegistrationPolicyException(String message) {
        super(message);
    }

    /** @param message 拒绝原因描述 @param throwable 根因异常 */
    public ClientRegistrationPolicyException(String message, Throwable throwable) {
        super(message, throwable);
    }

    /** @return 关联的策略组件模型 */
    public ComponentModel getPolicyModel() {
        return policyModel;
    }

    /** @param policyModel 关联的策略组件模型 */
    public void setPolicyModel(ComponentModel policyModel) {
        this.policyModel = policyModel;
    }

    /** 若已设置策略模型，则返回含策略名称的格式化消息 */
    @Override
    public String getMessage() {
        return policyModel==null ? super.getMessage() : String.format("Policy '%s' rejected request to client-registration service. Details: %s", policyModel.getName(), super.getMessage());
    }
}
