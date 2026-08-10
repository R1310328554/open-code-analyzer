package org.keycloak.models.workflow;

import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.RealmModel;
import org.keycloak.provider.Provider;

/**
 * 工作流相关提供者的静态工厂工具类。
 * <p>按 ID 解析条件、事件与步骤提供者及其工厂。</p>
 */
public final class Workflows {

    /** 创建指定名称的条件提供者实例。 */
    public static WorkflowConditionProvider getConditionProvider(KeycloakSession session, String name, String configParameter) {
        return getConditionProviderFactory(session, name).create(session, configParameter);
    }

    public static WorkflowConditionProviderFactory<WorkflowConditionProvider> getConditionProviderFactory(KeycloakSession session, String providerId) {
        return getProviderFactory(session, WorkflowConditionProvider.class, providerId);
    }

    /** 创建指定名称的事件提供者实例。 */
    public static WorkflowEventProvider getEventProvider(KeycloakSession session, String name, String configParameter) {
        return getEventProviderFactory(session, name).create(session, configParameter);
    }

    public static WorkflowEventProviderFactory<WorkflowEventProvider> getEventProviderFactory(KeycloakSession session, String providerId) {
        return getProviderFactory(session, WorkflowEventProvider.class, providerId);
    }

    /** 根据步骤组件创建步骤提供者实例。 */
    public static WorkflowStepProvider getStepProvider(KeycloakSession session, WorkflowStep step) {
        RealmModel realm = session.getContext().getRealm();
        return getStepProviderFactory(session, step).create(session, realm.getComponent(step.getId()));
    }

    public static WorkflowStepProviderFactory<WorkflowStepProvider> getStepProviderFactory(KeycloakSession session, WorkflowStep step) {
        return getProviderFactory(session, WorkflowStepProvider.class, step.getProviderId());
    }

    /** 从 SessionFactory 查找提供者工厂，未找到时抛出 {@link WorkflowInvalidStateException}。 */
    private static <P extends Provider, F> F getProviderFactory(KeycloakSession session, Class<P> providerClass, String providerId) {
        KeycloakSessionFactory sessionFactory = session.getKeycloakSessionFactory();
        @SuppressWarnings("unchecked")
        F providerFactory = (F) sessionFactory.getProviderFactory(providerClass, providerId);

        if (providerFactory == null) {
            throw new WorkflowInvalidStateException("Could not find provider factory with id: " + providerId);
        }
        return providerFactory;
    }
}
