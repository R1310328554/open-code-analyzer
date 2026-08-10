package org.keycloak.authentication.authenticators.conditional;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

/**
 * 条件认证器：仅当同级流程中其他认证器已为当前用户配置完成时才执行子流程。
 * 对 REQUIRED 执行项要求全部已配置，对 ALTERNATIVE 执行项要求至少一项已配置。
 */
public class ConditionalUserConfiguredAuthenticator implements ConditionalAuthenticator {
    /** 单例实例。 */
    public static final ConditionalUserConfiguredAuthenticator SINGLETON = new ConditionalUserConfiguredAuthenticator();

    @Override
    /** 从当前执行项的父流程开始评估同级认证器的配置状态。 */
    public boolean matchCondition(AuthenticationFlowContext context) {
        return matchConditionInFlow(context, context.getExecution().getParentFlow());
    }

    /** 递归评估指定流程内 REQUIRED/ALTERNATIVE 认证器的 configuredFor 状态。 */
    private boolean matchConditionInFlow(AuthenticationFlowContext context, String flowId) {
        List<AuthenticationExecutionModel> requiredExecutions = new LinkedList<>();
        List<AuthenticationExecutionModel> alternativeExecutions = new LinkedList<>();
        context.getRealm().getAuthenticationExecutionsStream(flowId)
                // 跳过条件认证器本身，不参与 configuredFor 评估
                .filter(e -> !isConditionalExecution(context, e))
                .filter(e -> !Objects.equals(context.getExecution().getId(), e.getId()) && !e.isAuthenticatorFlow())
                .forEachOrdered(e -> {
                    if (e.isRequired()) {
                        requiredExecutions.add(e);
                    } else if (e.isAlternative()) {
                        alternativeExecutions.add(e);
                    }
                });
        if (!requiredExecutions.isEmpty()) {
            return requiredExecutions.stream().allMatch(e -> isConfiguredFor(e, context));
        } else  if (!alternativeExecutions.isEmpty()) {
            return alternativeExecutions.stream().anyMatch(e -> isConfiguredFor(e, context));
        }
        return true;
    }

    /** 判断执行项是否为条件认证器（需排除）。 */
    private boolean isConditionalExecution(AuthenticationFlowContext context, AuthenticationExecutionModel e) {
        AuthenticatorFactory factory = (AuthenticatorFactory) context.getSession().getKeycloakSessionFactory()
                .getProviderFactory(Authenticator.class, e.getAuthenticator());
        if (factory != null) {
            Authenticator auth = factory.create(context.getSession());
            return (auth instanceof ConditionalAuthenticator);
        }
        return false;
    }

    /** 检查单个执行项（或嵌套子流程）是否已为当前用户配置。 */
    private boolean isConfiguredFor(AuthenticationExecutionModel model, AuthenticationFlowContext context) {
        if (model.isAuthenticatorFlow()) {
            return matchConditionInFlow(context, model.getId());
        }
        AuthenticatorFactory factory = (AuthenticatorFactory) context.getSession().getKeycloakSessionFactory().getProviderFactory(Authenticator.class, model.getAuthenticator());
        Authenticator authenticator = factory.create(context.getSession());
        if (authenticator.requiresUser() && context.getUser() == null) {
            return false;
        }
        return authenticator.configuredFor(context.getSession(), context.getRealm(), context.getUser());
    }

    @Override
    public void action(AuthenticationFlowContext context) {
        // 未使用
    }

    @Override
    /** @return 配置检查需要已识别用户 */
    public boolean requiresUser() {
        return true;
    }

    @Override
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {
        // Not used
    }

    @Override
    public void close() {
        // 无资源需释放
    }
}
