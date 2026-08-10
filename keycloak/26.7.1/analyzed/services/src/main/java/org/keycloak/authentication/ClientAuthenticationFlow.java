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

package org.keycloak.authentication;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import jakarta.ws.rs.core.Response;

import org.keycloak.events.Details;
import org.keycloak.events.Errors;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.AuthenticationFlowModel;
import org.keycloak.models.ClientModel;
import org.keycloak.models.utils.KeycloakModelUtils;
import org.keycloak.services.ServicesLogger;

import org.jboss.logging.Logger;

/**
 * 客户端认证流程实现，按 REQUIRED/ALTERNATIVE 执行项依次尝试客户端认证器。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class ClientAuthenticationFlow implements AuthenticationFlow {

    private static final Logger logger = Logger.getLogger(ClientAuthenticationFlow.class);

    /** 首个优先级最高的备选挑战响应。 */
    Response alternativeChallenge = null;
    /** 认证处理器。 */
    AuthenticationProcessor processor;
    /** 客户端认证流程模型。 */
    AuthenticationFlowModel flow;

    /** 客户端认证是否已成功。 */
    private boolean success;

    /** @param processor 认证处理器 @param flow 客户端认证流程模型 */
    public ClientAuthenticationFlow(AuthenticationProcessor processor, AuthenticationFlowModel flow) {
        this.processor = processor;
        this.flow = flow;
    }

    @Override
    /** 客户端认证流程不支持 action 处理。 */
    public Response processAction(String actionExecution) {
        throw new IllegalStateException("Not supposed to be invoked");
    }

    @Override
    /** 依次执行客户端认证器并匹配客户端配置的认证方式。 */
    public Response processFlow() {
        List<AuthenticationExecutionModel> executions = findExecutionsToRun();

        for (AuthenticationExecutionModel model : executions) {
            ClientAuthenticatorFactory factory = (ClientAuthenticatorFactory) processor.getSession().getKeycloakSessionFactory().getProviderFactory(ClientAuthenticator.class, model.getAuthenticator());
            if (factory == null) {
                throw new AuthenticationFlowException("Could not find ClientAuthenticatorFactory for: " + model.getAuthenticator(), AuthenticationFlowError.INTERNAL_ERROR);
            }
            ClientAuthenticator authenticator = factory.create();
            logger.debugv("client authenticator: {0}", factory.getId());

            AuthenticationProcessor.Result context = processor.createClientAuthenticatorContext(model, authenticator, executions);
            authenticator.authenticateClient(context);

            ClientModel client = processor.getClient();
            if (client != null) {
                String expectedClientAuthType = client.getClientAuthenticatorType();

                // 向后兼容：未配置时回退到默认 secret 认证方式
                // 公开客户端忽略 clientAuthenticatorType，使用基于 client_id 的默认认证
                // default, which set the client just based on "client_id" parameter
                if (expectedClientAuthType == null || client.isPublicClient()) {
                    if (expectedClientAuthType == null) {
                        ServicesLogger.LOGGER.authMethodFallback(client.getClientId(), expectedClientAuthType);
                    }
                    expectedClientAuthType = KeycloakModelUtils.getDefaultClientAuthenticatorType();
                }

                // 校验当前认证器是否与客户端期望的认证方式一致
                if (factory.getId().equals(expectedClientAuthType)) {
                    Response response = processResult(context);
                    if (response != null) return response;

                    if (!context.getStatus().equals(FlowStatus.SUCCESS)) {
                        throw new AuthenticationFlowException("Expected success, but for an unknown reason the status was " + context.getStatus(), AuthenticationFlowError.INTERNAL_ERROR);
                    } else {
                        success = true;
                    }

                    logger.debugv("Client {0} authenticated by {1}", client.getClientId(), factory.getId());
                    processor.getEvent().detail(Details.CLIENT_AUTH_METHOD, factory.getId());
                    return null;
                }
            }
        }

        // 若存在备选挑战则返回该挑战
        if (alternativeChallenge != null) {
            processor.getEvent().error(Errors.INVALID_CLIENT);
            return alternativeChallenge;
        }
        
        throw new AuthenticationFlowException("Invalid client or Invalid client credentials", AuthenticationFlowError.CLIENT_NOT_FOUND);
    }

    /** 确定待执行的认证执行项（优先 REQUIRED，否则 ALTERNATIVE）。 */
    protected List<AuthenticationExecutionModel> findExecutionsToRun() {
        List<AuthenticationExecutionModel> alternativeExecutions = new LinkedList<>();
        Optional<AuthenticationExecutionModel> requiredExecution = processor.getRealm().getAuthenticationExecutionsStream(flow.getId())
                .filter(e -> {
                    if (e.isRequired()) {
                        return true;
                    } else if (e.isAlternative()){
                        alternativeExecutions.add(e);
                        return false;
                    }
                    return false;
                }).findFirst();

        List<AuthenticationExecutionModel> executionsToRun = requiredExecution.map(Collections::singletonList).orElse(alternativeExecutions);

        if (logger.isTraceEnabled()) {
            List<String> exIds = new ArrayList<>();
            for (AuthenticationExecutionModel execution : executionsToRun) {
                exIds.add(execution.getId());
            }
            logger.tracef("Using executions for client authentication: %s", exIds.toString());
        }

        return executionsToRun;
    }

    /** 根据认证器执行结果决定后续处理或抛出异常。 */
    protected Response processResult(AuthenticationProcessor.Result result) {
        AuthenticationExecutionModel execution = result.getExecution();
        FlowStatus status = result.getStatus();

        logger.debugv("client authenticator {0}: {1}", status, execution.getAuthenticator());

        if (status == FlowStatus.SUCCESS) {
            return null;
        }

        if (status == FlowStatus.FAILED) {
            if (result.getChallenge() != null) {
                return sendChallenge(result, execution);
            } else {
                throw new AuthenticationFlowException(result.getError());
            }
        } else if (status == FlowStatus.FORCE_CHALLENGE) {
            return sendChallenge(result, execution);
        } else if (status == FlowStatus.CHALLENGE) {

            // 仅保留首个优先级最高的备选挑战
            if (alternativeChallenge == null) {
                alternativeChallenge = result.getChallenge();
            }
            return sendChallenge(result, execution);
        } else if (status == FlowStatus.FAILURE_CHALLENGE) {
            return sendChallenge(result, execution);
        } else if (status == FlowStatus.ATTEMPTED) {
            logger.warnv("Client authentication was attempted did not complete for {0}", execution.getAuthenticator());
            throw new AuthenticationFlowException(AuthenticationFlowError.GENERIC_AUTHENTICATION_ERROR);
        } else {
            ServicesLogger.LOGGER.unknownResultStatus();
            throw new AuthenticationFlowException(AuthenticationFlowError.INTERNAL_ERROR);
        }
    }

    /** 记录失败事件并返回挑战响应。 */
    public Response sendChallenge(AuthenticationProcessor.Result result, AuthenticationExecutionModel execution) {
        logger.debugv("client authenticator: sending challenge for authentication execution {0}", execution.getAuthenticator());

        if (result.getError() != null) {
            String errorAsString = result.getError().toString().toLowerCase();
            result.getEvent().error(errorAsString);
        } else {
            if (result.getClient() == null) {
                result.getEvent().error(Errors.INVALID_CLIENT);
            } else {
                result.getEvent().error(Errors.INVALID_CLIENT_CREDENTIALS);
            }
        }

        return result.getChallenge();
    }

    @Override
    /** @return 客户端认证是否成功 */
    public boolean isSuccessful() {
        return success;
    }
}
