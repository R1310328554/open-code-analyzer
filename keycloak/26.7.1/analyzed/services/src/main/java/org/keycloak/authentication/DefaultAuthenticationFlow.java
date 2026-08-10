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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;

import org.keycloak.authentication.authenticators.browser.AbstractUsernameFormAuthenticator;
import org.keycloak.authentication.authenticators.conditional.ConditionalAuthenticator;
import org.keycloak.authentication.authenticators.util.AuthenticatorUtils;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.AuthenticationFlowModel;
import org.keycloak.models.Constants;
import org.keycloak.models.OrganizationModel;
import org.keycloak.models.UserModel;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;
import org.keycloak.services.ServicesLogger;
import org.keycloak.sessions.AuthenticationSessionModel;
import org.keycloak.sessions.CommonClientSessionModel;
import org.keycloak.utils.StringUtil;

import org.jboss.logging.Logger;

/**
 * 默认浏览器认证流程实现，处理 REQUIRED/ALTERNATIVE/CONDITIONAL 执行项及子流程递归。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class DefaultAuthenticationFlow implements AuthenticationFlow {
    private static final Logger logger = Logger.getLogger(DefaultAuthenticationFlow.class);
    /** 当前流程下的全部执行项。 */
    private final List<AuthenticationExecutionModel> executions;
    /** 认证处理器。 */
    private final AuthenticationProcessor processor;
    /** 认证流程模型。 */
    private final AuthenticationFlowModel flow;
    /** 当前流程是否已成功完成。 */
    private boolean successful = false;
    /** 备选执行项失败时收集的异常列表。 */
    private List<AuthenticationFlowException> afeList = new ArrayList<>();

    public DefaultAuthenticationFlow(AuthenticationProcessor processor, AuthenticationFlowModel flow) {
        this.processor = processor;
        this.flow = flow;
        this.executions = processor.getRealm().getAuthenticationExecutionsStream(flow.getId()).collect(Collectors.toList());
    }

    /** 判断执行项是否已处理完毕。 */
    protected boolean isProcessed(AuthenticationExecutionModel model) {
        return isProcessed(processor, model);
    }

    protected static boolean isProcessed(AuthenticationProcessor processor, AuthenticationExecutionModel model) {
        if (model.isDisabled()) return true;
        AuthenticationSessionModel.ExecutionStatus status = processor.getAuthenticationSession().getExecutionStatus().get(model.getId());
        if (status == null) return false;
        return status == AuthenticationSessionModel.ExecutionStatus.SUCCESS || status == AuthenticationSessionModel.ExecutionStatus.SKIPPED
                || status == AuthenticationSessionModel.ExecutionStatus.ATTEMPTED
                || status == AuthenticationSessionModel.ExecutionStatus.SETUP_REQUIRED;
    }

    /** 通过工厂创建认证器实例。 */
    protected Authenticator createAuthenticator(AuthenticatorFactory factory) {
        return factory.create(processor.getSession());
    }

    @Override
    /** 处理表单提交等用户 action，含切换组织、尝试其他方式等分支。 */
    public Response processAction(String actionExecution) {
        logger.debugv("processAction: {0}", actionExecution);

        if (actionExecution == null || actionExecution.isEmpty()) {
            throw new AuthenticationFlowException("action is not in current execution", AuthenticationFlowError.INTERNAL_ERROR);
        }
        AuthenticationExecutionModel model = processor.getRealm().getAuthenticationExecutionById(actionExecution);
        if (model == null) {
            throw new AuthenticationFlowException("Execution not found", AuthenticationFlowError.INTERNAL_ERROR);
        }

        if (HttpMethod.POST.equals(processor.getRequest().getHttpMethod())) {
            MultivaluedMap<String, String> inputData = processor.getRequest().getDecodedFormParameters();
            String authExecId = inputData.getFirst(Constants.AUTHENTICATION_EXECUTION);

            // 用户点击「切换组织」链接
            if (inputData.containsKey("switchOrganization")) {
                logger.trace("User clicked on link 'Switch Organization'");
                AuthenticationSessionModel authSession = processor.getAuthenticationSession();
                // 保留用户名以便流程重置后无需重新输入
                String attemptedUsername = authSession.getAuthNote(AbstractUsernameFormAuthenticator.ATTEMPTED_USERNAME);
                if (attemptedUsername != null) {
                    authSession.setClientNote(OIDCLoginProtocol.LOGIN_HINT_PARAM, attemptedUsername);
                }
                authSession.removeClientNote(OrganizationModel.ORGANIZATION_ATTRIBUTE);
                // 清除会话上下文中缓存的组织以便重新评估
                processor.getSession().getContext().setOrganization(null);
                processor.resetFlow();
                return processor.authenticate();
            }

            // 用户点击「尝试其他方式」链接
            if (inputData.containsKey("tryAnotherWay")) {
                logger.trace("User clicked on link 'Try Another Way'");

                processor.getAuthenticationSession().setAuthNote(AuthenticationProcessor.AUTHENTICATION_SELECTOR_SCREEN_DISPLAYED, "true");
                return createSelectAuthenticatorsScreen(model);
            }

            // 检查用户是否切换到新的认证执行项
            if (authExecId != null && !authExecId.isEmpty()) {

                processor.getAuthenticationSession().removeAuthNote(AuthenticationProcessor.AUTHENTICATION_SELECTOR_SCREEN_DISPLAYED);
                List<AuthenticationSelectionOption> selectionOptions = createAuthenticationSelectionList(model);

                // 校验请求的认证执行项切换是否被允许
                selectionOptions.stream()
                        .filter(authSelectionOption -> authExecId.equals(authSelectionOption.getAuthExecId()))
                        .findFirst()
                        .orElseThrow(() -> new AuthenticationFlowException("Requested authentication execution is not allowed",
                                AuthenticationFlowError.INTERNAL_ERROR)
                        );

                model = processor.getRealm().getAuthenticationExecutionById(authExecId);

                Response response = processSingleFlowExecutionModel(model, false);
                if (response == null) {
                    return continueAuthenticationAfterSuccessfulAction(model);
                } else
                    return response;
            }
        }

        // 执行项为子流程时（例如用户注册场景）
        if (model.isAuthenticatorFlow()) {
            logger.debug("execution is flow");
            AuthenticationFlow authenticationFlow = processor.createFlowExecution(model.getFlowId(), model);
            Response flowChallenge = authenticationFlow.processAction(actionExecution);
            if (flowChallenge == null) {
                checkAndValidateParentFlow(model);
                return processFlow();
            } else {
                setExecutionStatus(model, AuthenticationSessionModel.ExecutionStatus.CHALLENGED);
                return flowChallenge;
            }
        }

        // 普通认证器执行项
        AuthenticatorFactory factory = getAuthenticatorFactory(model);
        Authenticator authenticator = createAuthenticator(factory);
        AuthenticationProcessor.Result result = processor.createAuthenticatorContext(model, authenticator, executions);
        result.setAuthenticationSelections(createAuthenticationSelectionList(model));

        if (factory instanceof AuthenticationFlowCallbackFactory) {
            AuthenticatorUtil.setAuthCallbacksFactoryIds(processor.getAuthenticationSession(), factory.getId());
        }

        logger.debugv("action: {0}", model.getAuthenticator());
        authenticator.action(result);
        Response response = processResult(result, true);
        if (response == null) {
            return continueAuthenticationAfterSuccessfulAction(model);
        } else return response;
    }


    /**
     * action 执行完成后查找下一待处理流程并继续认证（递归向上处理父流程）。
     * Called after "actionExecutionModel" execution is finished (Either successful or attempted). Find the next appropriate authentication
     * flow where the authentication should continue and continue with authentication process.
     * The method recursively continues with the parent flow
     * until finally the top flow is processed.
     *
     * @param actionExecutionModel
     * @return Response if some more forms should be displayed during authentication. Null otherwise.
     */
    private Response continueAuthenticationAfterSuccessfulAction(AuthenticationExecutionModel actionExecutionModel) {
        processor.getAuthenticationSession().removeAuthNote(AuthenticationProcessor.CURRENT_AUTHENTICATION_EXECUTION);

        String firstUnfinishedParentFlowId = checkAndValidateParentFlow(actionExecutionModel);
        AuthenticationExecutionModel parentFlowExecution = processor.getRealm().getAuthenticationExecutionByFlowId(firstUnfinishedParentFlowId);

        if (parentFlowExecution == null) {
            // This means that 1st unfinished ancestor flow is the top flow. We can just process it from the start
            return processFlow();
        } else {
            Response response = processSingleFlowExecutionModel(parentFlowExecution, false);
            if (response == null) {
                // the parent flow is now the last action that has been executed, continue with that until the top flow is reached
                return continueAuthenticationAfterSuccessfulAction(parentFlowExecution);
            } else {
                return response;
            }
        }
    }


    /**
     * 当子执行项成功后，递归标记父流程执行项为成功并返回首个未完成的祖先流程 ID。
     * This method makes sure that the parent flow's corresponding execution is considered successful if its contained
     * executions are successful.
     * The purpose is for when an execution is validated through an action, to make sure its parent flow can be successful
     * when re-evaluation the flow tree. If the flow is successful, we will recursively check it's parent flow as well
     *
     * @param model An execution model.
     * @return flowId of the 1st ancestor flow, which is not yet successfully finished and may require some further processing
     */
    private String checkAndValidateParentFlow(AuthenticationExecutionModel model) {
        while (true) {
            AuthenticationExecutionModel parentFlowExecutionModel = processor.getRealm().getAuthenticationExecutionByFlowId(model.getParentFlow());

            if (parentFlowExecutionModel != null) {
                List<AuthenticationExecutionModel> requiredExecutions = new LinkedList<>();
                List<AuthenticationExecutionModel> alternativeExecutions = new LinkedList<>();
                fillListsOfExecutions(processor.getRealm().getAuthenticationExecutionsStream(model.getParentFlow()),
                        requiredExecutions, alternativeExecutions);

                // Note: If we evaluate alternative execution, we will also doublecheck that there are not required elements in same subflow
                if (((model.isRequired() || model.isConditional()) && requiredExecutions.stream().allMatch(processor::isSuccessful)) ||
                        (model.isAlternative() && alternativeExecutions.stream().anyMatch(processor::isSuccessful) && requiredExecutions.isEmpty())) {
                    logger.debugf("Flow '%s' successfully finished after children executions success", logExecutionAlias(parentFlowExecutionModel));
                    setExecutionStatus(parentFlowExecutionModel, AuthenticationSessionModel.ExecutionStatus.SUCCESS);

                    // Flow is successfully finished. Recursively check whether it's parent flow is now successful as well
                    model = parentFlowExecutionModel;
                } else {
                    return model.getParentFlow();
                }
            } else {
                return model.getParentFlow();
            }
        }
    }

    /**
     * 创建认证方式选择界面（用户点击「尝试其他方式」时展示）。
     *
     * @param executionModel Last execution (should be typically available in the methods)
     * @return response with the screen to be displayed to the user
     */
    private Response createSelectAuthenticatorsScreen(AuthenticationExecutionModel executionModel) {
        List<AuthenticationSelectionOption> selectionOptions = createAuthenticationSelectionList(executionModel);

        AuthenticationProcessor.Result result = processor.createAuthenticatorContext(executionModel, null, null);
        result.setAuthenticationSelections(selectionOptions);
        return result.form().createSelectAuthenticator();
    }

    @Override
    /** 按 REQUIRED/ALTERNATIVE 规则遍历并执行认证流程。 */
    public Response processFlow() {
        logger.debugf("processFlow: %s", flow.getAlias());

        if (Boolean.parseBoolean(processor.getAuthenticationSession().getAuthNote(AuthenticationProcessor.AUTHENTICATION_SELECTOR_SCREEN_DISPLAYED))) {
            logger.tracef("Refreshed page on authentication selector screen");
            String lastExecutionId = processor.getAuthenticationSession().getAuthNote(AuthenticationProcessor.CURRENT_AUTHENTICATION_EXECUTION);
            if (lastExecutionId != null) {
                AuthenticationExecutionModel executionModel = processor.getRealm().getAuthenticationExecutionById(lastExecutionId);
                if (executionModel != null) {
                    return createSelectAuthenticatorsScreen(executionModel);
                }
            }
        }

        // 将执行项分为 REQUIRED 与 ALTERNATIVE 两组
        List<AuthenticationExecutionModel> requiredList = new ArrayList<>();
        List<AuthenticationExecutionModel> alternativeList = new ArrayList<>();

        fillListsOfExecutions(executions.stream(), requiredList, alternativeList);

        // 处理 REQUIRED 执行项：全部需成功
        boolean requiredElementsSuccessful = true;
        Iterator<AuthenticationExecutionModel> requiredIListIterator = requiredList.listIterator();
        while (requiredIListIterator.hasNext()) {
            AuthenticationExecutionModel required = requiredIListIterator.next();
            // 条件子流程条件为 false 时视为禁用
            //If the flow has been processed before it will not be removed to consider its execution status.
            if (required.isConditional() && !isProcessed(required) && isConditionalSubflowDisabled(required)) {
                requiredIListIterator.remove();
                continue;
            }
            Response response = processSingleFlowExecutionModel(required, true);
            requiredElementsSuccessful &= processor.isSuccessful(required) || isSetupRequired(required);
            if (response != null) {
                return response;
            }
            // 部分 REQUIRED 执行项未成功且无响应时可提前终止
            // We can break as we know that the whole subflow would be considered unsuccessful as well
            if (!requiredElementsSuccessful) {
                break;
            }
        }

        // 无 REQUIRED 执行项时才评估 ALTERNATIVE
        if (requiredList.isEmpty()) {
            // action 返回后检查是否已有 ALTERNATIVE 成功
            if (alternativeList.stream().anyMatch(alternative -> processor.isSuccessful(alternative) || isSetupRequired(alternative))) {
                return onFlowExecutionsSuccessful();
            }

            // ALTERNATIVE：首个满足即可
            for (AuthenticationExecutionModel alternative : alternativeList) {
                try {
                    Response response = processSingleFlowExecutionModel(alternative, true);
                    if (response != null) {
                        return response;
                    }
                    if (processor.isSuccessful(alternative) || isSetupRequired(alternative)) {
                        return onFlowExecutionsSuccessful();
                    }
                } catch (AuthenticationFlowException afe) {
                    // 管理视角不理想，但用户可尝试其他 ALTERNATIVE 继续
                    afeList.add(afe);
                    setExecutionStatus(alternative, AuthenticationSessionModel.ExecutionStatus.ATTEMPTED);
                }
            }
        } else {
            if (requiredElementsSuccessful) {
                return onFlowExecutionsSuccessful();
            }
        }
        return null;
    }


    /**
     * 遍历执行项并按类型填入 requiredList 与 alternativeList。
     */
    void fillListsOfExecutions(Stream<AuthenticationExecutionModel> executionsToProcess, List<AuthenticationExecutionModel> requiredList, List<AuthenticationExecutionModel> alternativeList) {
        executionsToProcess
                .filter(((Predicate<AuthenticationExecutionModel>) this::isConditionalAuthenticator).negate())
                .forEachOrdered(execution -> {
                    if (execution.isRequired() || execution.isConditional()) {
                        requiredList.add(execution);
                    } else if (execution.isAlternative()) {
                        alternativeList.add(execution);
                    }
                });

        if (!requiredList.isEmpty() && !alternativeList.isEmpty()) {
            List<String> alternativeIds = alternativeList.stream()
                    .map(AuthenticationExecutionModel::getAuthenticator)
                    .collect(Collectors.toList());

            logger.warnf("REQUIRED and ALTERNATIVE elements at same level! Those alternative executions will be ignored: %s", alternativeIds);
            alternativeList.clear();
        }
    }


    /**
     * 判断条件子流程是否因条件不满足而被禁用。
     * @param model
     * @return
     */
    boolean isConditionalSubflowDisabled(AuthenticationExecutionModel model) {
        if (model == null || !model.isAuthenticatorFlow() || !model.isConditional()) {
            return false;
        };
        List<AuthenticationExecutionModel> modelList = processor.getRealm()
                .getAuthenticationExecutionsStream(model.getFlowId()).collect(Collectors.toList());
        List<AuthenticationExecutionModel> conditionalAuthenticatorList = modelList.stream()
                .filter(this::isConditionalAuthenticator)
                .filter(s -> s.isEnabled())
                .collect(Collectors.toList());
        boolean conditionalSubflowDisabled = conditionalAuthenticatorList.isEmpty() || conditionalAuthenticatorList.stream()
                .anyMatch(m -> conditionalNotMatched(m, modelList));
        logger.tracef("Conditional subflow '%s' is %s", logExecutionAlias(model), conditionalSubflowDisabled ? "disabled" : "enabled");
        return conditionalSubflowDisabled;
    }

    private boolean isConditionalAuthenticator(AuthenticationExecutionModel model) {
        return !model.isAuthenticatorFlow() && model.getAuthenticator() != null && model.isEnabled()
                && createAuthenticator(getAuthenticatorFactory(model)) instanceof ConditionalAuthenticator;
    }

    private AuthenticatorFactory getAuthenticatorFactory(AuthenticationExecutionModel model) {
        AuthenticatorFactory factory = (AuthenticatorFactory) processor.getSession().getKeycloakSessionFactory().getProviderFactory(Authenticator.class, model.getAuthenticator());
        if (factory == null) {
            throw new RuntimeException("Unable to find factory for AuthenticatorFactory: " + model.getAuthenticator() + " did you forget to declare it in a META-INF/services file?");
        }
        return factory;
    }

    private boolean conditionalNotMatched(AuthenticationExecutionModel model, List<AuthenticationExecutionModel> executionList) {
        AuthenticatorFactory factory = getAuthenticatorFactory(model);
        ConditionalAuthenticator authenticator = (ConditionalAuthenticator) createAuthenticator(factory);
        AuthenticationProcessor.Result context = processor.createAuthenticatorContext(model, authenticator, executionList);

        // 存储条件评估结果供后续复用（流程中可能多次评估）
        // but this is expected as "conditions of condition" can be changed during the flow (EG. when acr level is reached or when user is added to the context)
        boolean matchCondition = authenticator.matchCondition(context);
        setExecutionStatus(model,
                matchCondition ? AuthenticationSessionModel.ExecutionStatus.EVALUATED_TRUE : AuthenticationSessionModel.ExecutionStatus.EVALUATED_FALSE);

        return !matchCondition;
    }

    private boolean isSetupRequired(AuthenticationExecutionModel model) {
        return AuthenticationSessionModel.ExecutionStatus.SETUP_REQUIRED.equals(processor.getAuthenticationSession().getExecutionStatus().get(model.getId()));
    }


    private Response processSingleFlowExecutionModel(AuthenticationExecutionModel model, boolean calledFromFlow) {
        logger.debugf("check execution: '%s', requirement: '%s'", logExecutionAlias(model), model.getRequirement());

        if (isProcessed(model)) {
            logger.debugf("execution '%s' is processed", logExecutionAlias(model));
            return null;
        }
        //handle case where execution is a flow
        if (model.isAuthenticatorFlow()) {
            AuthenticationFlow authenticationFlow = processor.createFlowExecution(model.getFlowId(), model);
            Response flowChallenge = authenticationFlow.processFlow();
            if (flowChallenge == null) {
                if (authenticationFlow.isSuccessful()) {
                    logger.debugf("Flow '%s' successfully finished", logExecutionAlias(model));
                    setExecutionStatus(model, AuthenticationSessionModel.ExecutionStatus.SUCCESS);
                } else {
                    logger.debugf("Flow '%s' failed", logExecutionAlias(model));
                    setExecutionStatus(model, AuthenticationSessionModel.ExecutionStatus.FAILED);
                }
                return null;
            } else {
                setExecutionStatus(model, AuthenticationSessionModel.ExecutionStatus.CHALLENGED);
                return flowChallenge;
            }
        }

        //handle normal execution case
        AuthenticatorFactory factory = getAuthenticatorFactory(model);
        Authenticator authenticator = createAuthenticator(factory);
        logger.debugv("authenticator: {0}", factory.getId());
        UserModel authUser = processor.getAuthenticationSession().getAuthenticatedUser();

        // ALTERNATIVE 执行项时按用户偏好选择实际展示的执行项
        List<AuthenticationSelectionOption> selectionOptions = createAuthenticationSelectionList(model);
        if (!selectionOptions.isEmpty() && calledFromFlow) {
            List<AuthenticationSelectionOption> finalSelectionOptions = selectionOptions.stream().filter(aso -> !aso.getAuthenticationExecution().isAuthenticatorFlow() && !isProcessed(aso.getAuthenticationExecution())).collect(Collectors.toList());
            if (finalSelectionOptions.isEmpty()) {
                //move to next
                return null;
            }
            model = finalSelectionOptions.get(0).getAuthenticationExecution();
            factory = (AuthenticatorFactory) processor.getSession().getKeycloakSessionFactory().getProviderFactory(Authenticator.class, model.getAuthenticator());
            if (factory == null) {
                throw new RuntimeException("Unable to find factory for AuthenticatorFactory: " + model.getAuthenticator() + " did you forget to declare it in a META-INF/services file?");
            }
            authenticator = createAuthenticator(factory);
        }
        AuthenticationProcessor.Result context = processor.createAuthenticatorContext(model, authenticator, executions);
        context.setAuthenticationSelections(selectionOptions);

        if (authenticator.requiresUser()) {
            if (authUser == null) {
                throw new AuthenticationFlowException("authenticator '" + factory.getId() + "' requires user to be set in the authentication context by previous authenticators, but user is not set yet", AuthenticationFlowError.UNKNOWN_USER);
            }
            if (!authenticator.configuredFor(processor.getSession(), processor.getRealm(), authUser)) {
                if (factory.isUserSetupAllowed() && model.isRequired() && authenticator.areRequiredActionsEnabled(processor.getSession(), processor.getRealm())) {
                    // 用户尚未完成凭证配置但允许设置 REQUIRED ACTION
                    logger.debugv("authenticator SETUP_REQUIRED: {0}", factory.getId());
                    setExecutionStatus(model, AuthenticationSessionModel.ExecutionStatus.SETUP_REQUIRED);
                    authenticator.setRequiredActions(processor.getSession(), processor.getRealm(), processor.getAuthenticationSession().getAuthenticatedUser());
                    return null;
                } else {
                    throw new AuthenticationFlowException("authenticator: " + factory.getId(), AuthenticationFlowError.CREDENTIAL_SETUP_REQUIRED);
                }
            }
        }
        else {
            if ((authUser != null) &&
                    !authenticator.configuredFor(processor.getSession(), processor.getRealm(), authUser) &&
                    !factory.isUserSetupAllowed() &&
                    (authenticator instanceof CredentialValidator)) {
                throw new AuthenticationFlowException("authenticator: " + factory.getId(), AuthenticationFlowError.CREDENTIAL_SETUP_REQUIRED);
            }
        }
        logger.debugv("invoke authenticator.authenticate: {0}", factory.getId());
        authenticator.authenticate(context);

        return processResult(context, false);
    }

    // 调试用：记录认证器或子流程别名
    private String logExecutionAlias(AuthenticationExecutionModel executionModel) {
        if (executionModel.isAuthenticatorFlow()) {
            // Resolve authenticationFlow model in case of debug logging. Otherwise don't lookup flowModel just because of logging and return only flowId
            if (logger.isDebugEnabled()) {
                AuthenticationFlowModel flowModel = processor.getRealm().getAuthenticationFlowById(executionModel.getFlowId());
                if (flowModel != null) {
                    return flowModel.getAlias() + " flow";
                }
            }
            return executionModel.getFlowId() + " flow";
        } else {
            return executionModel.getAuthenticator();
        }
    }

    /**
     * 构建向用户展示的认证方式选择列表（含凭证优先级排序）。
     * only the credentials associated to the authenticator, and for an alternative execution, this is all other alternative
     * executions in the flow, including the credentials.
     * <p>
     * In both cases, the credentials take precedence, with the order selected by the user (or his administrator).
     *
     * @param model The current execution model
     * @return an ordered list of the authentication selection options to present the user.
     */
    private List<AuthenticationSelectionOption> createAuthenticationSelectionList(AuthenticationExecutionModel model) {
        return AuthenticationSelectionResolver.createAuthenticationSelectionList(processor, model);
    }


    /** 根据认证器返回状态更新执行项状态并决定下一步响应。 */
    public Response processResult(AuthenticationProcessor.Result result, boolean isAction) {
        AuthenticationExecutionModel execution = result.getExecution();
        FlowStatus status = result.getStatus();
        switch (status) {
            case SUCCESS:
                logger.debugv("authenticator SUCCESS: {0}", execution.getAuthenticator());
                setExecutionStatus(execution, AuthenticationSessionModel.ExecutionStatus.SUCCESS);
                AuthenticatorUtils.updateCompletedExecutions(processor.getAuthenticationSession(), processor.getUserSession(), execution.getId());
                return null;
            case FAILED:
                logger.debugv("authenticator FAILED: {0}", execution.getAuthenticator());
                processor.logFailure(execution.getAuthenticator());
                setExecutionStatus(execution, AuthenticationSessionModel.ExecutionStatus.FAILED);
                if (result.getChallenge() != null) {
                    return sendChallenge(result, execution);
                }
                throw new AuthenticationFlowException(result.getError(), result.getEventDetails(), result.getUserErrorMessage());
            case FORK:
                logger.debugv("reset browser login from authenticator: {0}", execution.getAuthenticator());
                processor.getAuthenticationSession().setAuthNote(AuthenticationProcessor.CURRENT_AUTHENTICATION_EXECUTION, execution.getId());
                throw new ForkFlowException(result.getSuccessMessage(), result.getErrorMessage());
            case FORCE_CHALLENGE:
            case CHALLENGE:
                setExecutionStatus(execution, AuthenticationSessionModel.ExecutionStatus.CHALLENGED);
                return sendChallenge(result, execution);
            case FAILURE_CHALLENGE:
                logger.debugv("authenticator FAILURE_CHALLENGE: {0}", execution.getAuthenticator());
                processor.logFailure(execution.getAuthenticator());
                setExecutionStatus(execution, AuthenticationSessionModel.ExecutionStatus.CHALLENGED);
                return sendChallenge(result, execution);
            case ATTEMPTED:
                logger.debugv("authenticator ATTEMPTED: {0}", execution.getAuthenticator());
                setExecutionStatus(execution, AuthenticationSessionModel.ExecutionStatus.ATTEMPTED);
                return null;
            case FLOW_RESET:
                processor.resetFlow();
                return processor.authenticate();
            default:
                logger.debugv("authenticator INTERNAL_ERROR: {0}", execution.getAuthenticator());
                ServicesLogger.LOGGER.unknownResultStatus();
                throw new AuthenticationFlowException(AuthenticationFlowError.INTERNAL_ERROR);
        }
    }

    /** 记录当前执行项并返回挑战响应。 */
    public Response sendChallenge(AuthenticationProcessor.Result result, AuthenticationExecutionModel execution) {
        processor.getAuthenticationSession().setAuthNote(AuthenticationProcessor.CURRENT_AUTHENTICATION_EXECUTION, execution.getId());
        return result.getChallenge();
    }

    @Override
    /** @return 当前流程是否成功 */
    public boolean isSuccessful() {
        return successful;
    }

    @Override
    /** @return 备选执行项收集的异常列表 */
    public List<AuthenticationFlowException> getFlowExceptions(){
        return afeList;
    }

    private void setExecutionStatus(AuthenticationExecutionModel authExecutionModel, CommonClientSessionModel.ExecutionStatus status) {
        this.processor.getAuthenticationSession().setExecutionStatus(authExecutionModel.getId(), status);

        logger.tracef("Set execution status: Execution: %s, status: %s", logExecutionAlias(authExecutionModel), status);

        if (authExecutionModel.isAuthenticatorFlow() && status == CommonClientSessionModel.ExecutionStatus.SUCCESS) {
            // 子流程成功后触发回调检查
            processor.getRealm().getAuthenticationExecutionsStream(authExecutionModel.getFlowId()).forEach(this::checkAuthCallback);
        }
    }

    private void checkAuthCallback(AuthenticationExecutionModel execution) {
        // 仅当对应认证器 SUCCESS 或条件为 true 时触发回调
        CommonClientSessionModel.ExecutionStatus executionStatus = processor.getAuthenticationSession().getExecutionStatus().get(execution.getId());
        if (executionStatus == CommonClientSessionModel.ExecutionStatus.SUCCESS || executionStatus == CommonClientSessionModel.ExecutionStatus.EVALUATED_TRUE) {
            if (!execution.isAuthenticatorFlow()) {
                AuthenticatorFactory authFactory = getAuthenticatorFactory(execution);
                if (authFactory instanceof AuthenticationFlowCallbackFactory) {
                    AuthenticationFlowCallback authCallback = (AuthenticationFlowCallback) createAuthenticator(authFactory);
                    logger.tracef("Will trigger callback '%s' after successful finish of the flow '%s'", authFactory.getId(), execution.getParentFlow());
                    authCallback.onParentFlowSuccess(processor.createAuthenticatorContext(execution, authCallback, null)); // no need to have executions filled
                    AuthenticatorUtil.setAuthCallbacksFactoryIds(processor.getAuthenticationSession(), authFactory.getId());
                }
            }
        }
    }

    // 当前流程所有执行项通过后触发，可执行顶层成功回调
    // It is opportunity to do some last "generic" checks before considering whole authentication as successful
    private Response onFlowExecutionsSuccessful() {
        if (flow.isTopLevel()) {
            logger.debugf("Authentication successful of the top flow '%s'", flow.getAlias());
            executeTopFlowSuccessCallbacks();
        }

        successful = true;
        return null;
    }

    /**
     * 顶层认证流程成功后执行各 {@link AuthenticationFlowCallbackFactory} 注册的成功回调。
     */
    private void executeTopFlowSuccessCallbacks() {
        final AuthenticationSessionModel authSession = processor.getAuthenticationSession();
        final Set<String> factoryProviderIDs = AuthenticatorUtil.getAuthCallbacksFactoryIds(authSession);

        factoryProviderIDs.stream()
                .filter(StringUtil::isNotBlank)
                .map(id -> processor.getSession().getProvider(Authenticator.class, id))
                .filter(Objects::nonNull)
                .filter(AuthenticationFlowCallback.class::isInstance)
                .map(AuthenticationFlowCallback.class::cast)
                .forEach(callback -> callback.onTopFlowSuccess(flow));
    }
}
