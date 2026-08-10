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

package org.keycloak.testsuite.forms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.ClientAuthenticationFlowContext;
import org.keycloak.authentication.authenticators.client.AbstractClientAuthenticator;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.provider.ProviderConfigProperty;

/**
 * 测试套件用的客户端直通认证器：自动认证硬编码客户端（如 {@code test-app}）。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class PassThroughClientAuthenticator extends AbstractClientAuthenticator {

    /** 提供者唯一标识。 */
    public static final String PROVIDER_ID = "testsuite-client-passthrough";

    /** 硬编码的客户端 ID，用于查找并绑定客户端。 */
    public static String clientId = "test-app";

    // 若 HTTP 请求中包含此参数，认证过程中将抛出异常
    /** 测试用错误参数名；存在时触发 {@link RuntimeException}。 */
    public static final String TEST_ERROR_PARAM = "test_error_param";

    /** 每客户端配置属性列表（foo/bar 占位项）。 */
    private static final List<ProviderConfigProperty> clientConfigProperties = new ArrayList<ProviderConfigProperty>();

    static {
        ProviderConfigProperty property;
        property = new ProviderConfigProperty();
        property.setName("passthroughauth.foo");
        property.setLabel("Foo Property");
        property.setType(ProviderConfigProperty.STRING_TYPE);
        property.setHelpText("Foo Property of this authenticator, which does nothing");
        clientConfigProperties.add(property);
        property = new ProviderConfigProperty();
        property.setName("passthroughauth.bar");
        property.setLabel("Bar Property");
        property.setType(ProviderConfigProperty.BOOLEAN_TYPE);
        property.setHelpText("Bar Property of this authenticator, which does nothing");
        clientConfigProperties.add(property);

    }

    /**
     * 执行客户端认证：可选触发测试错误，否则绑定硬编码客户端。
     *
     * @param context 客户端认证流程上下文
     */
    @Override
    public void authenticateClient(ClientAuthenticationFlowContext context) {
        String testErrorParamVal = context.getHttpRequest().getDecodedFormParameters().getFirst(TEST_ERROR_PARAM);
        if (testErrorParamVal != null) {
            throw new RuntimeException(testErrorParamVal);
        }

        ClientModel client = context.getRealm().getClientByClientId(clientId);
        if (client == null) {
            context.failure(AuthenticationFlowError.CLIENT_NOT_FOUND, null);
            return;
        }

        context.getEvent().client(client);
        context.setClient(client);
        context.success();
    }

    @Override
    public String getDisplayType() {
        return "Testsuite Dummy Client Validation";
    }

    @Override
    public boolean isConfigurable() {
        return false;
    }

    @Override
    public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
        return REQUIREMENT_CHOICES;
    }

    @Override
    public String getHelpText() {
        return "Testsuite dummy authenticator, which automatically authenticates hardcoded client (like 'test-app' )";
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return new LinkedList<>();
    }

    @Override
    public List<ProviderConfigProperty> getConfigPropertiesPerClient() {
        return clientConfigProperties;
    }

    /** 返回适配器用的虚拟 dummy 配置映射。 */
    @Override
    public Map<String, Object> getAdapterConfiguration(KeycloakSession session, ClientModel client) {
        Map<String, Object> props = new HashMap<>();
        props.put("foo", "some foo value");
        props.put("bar", true);

        Map<String, Object> config = new HashMap<>();
        config.put("dummy", props);
        return config;
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public Set<String> getProtocolAuthenticatorMethods(String loginProtocol) {
        return Collections.emptySet();
    }
}
