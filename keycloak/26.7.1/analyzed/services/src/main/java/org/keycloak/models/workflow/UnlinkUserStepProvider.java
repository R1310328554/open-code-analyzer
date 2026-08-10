package org.keycloak.models.workflow;

import java.util.List;
import java.util.stream.Stream;

import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

import org.jboss.logging.Logger;

/**
 * 工作流步骤：将目标用户与配置的身份提供方（IdP）解除联合绑定。
 * <p>配置项 {@link #CONFIG_ALIAS} 指定 IdP 别名列表；值为 {@code *} 时解除与所有 IdP 的绑定。</p>
 */
public class UnlinkUserStepProvider implements WorkflowStepProvider {

    private final Logger log = Logger.getLogger(UnlinkUserStepProvider.class);
    /** 组件配置中身份提供方别名列表的键名。 */
    public static final String CONFIG_ALIAS = "idp";

    private final KeycloakSession session;
    private final ComponentModel stepModel;

    /** @param session Keycloak 会话 @param model 工作流步骤组件配置 */
    public UnlinkUserStepProvider(KeycloakSession session, ComponentModel model) {
        this.session = session;
        this.stepModel = model;
    }

    @Override
    public void close() {
    }

    /** 按配置的 IdP 别名逐个解除用户联合身份；未配置时记录警告并跳过。 */
    @Override
    public void run(WorkflowExecutionContext context) {
        UserModel user = session.users().getUserById(getRealm(), context.getResourceId());
        getConfiguredProviders().forEach(alias -> UnlinkUserFromIdp(user, alias));
    }

    /** 按别名解除单个 IdP 联合；{@code *} 表示解除全部联合身份。 */
    private void UnlinkUserFromIdp(UserModel user, String alias) {
        RealmModel realm = getRealm();
        // 别名为 "*" 时解除与所有身份提供方的联合绑定
        if ("*".equals(alias)) {
            log.debugv("Unlinking user {0} ({1}) from all Identity Providers.", user.getUsername(), user.getId());
            session.users()
                    .getFederatedIdentitiesStream(realm, user)
                    .forEach(identity -> session.users().removeFederatedIdentity(
                            realm,
                            user,
                            identity.getIdentityProvider()));
        } else {
            log.debugv("Unlinking user {0} ({1}) from Identity Provider with alias {2}.", user.getUsername(),
                    user.getId(), alias);
            session.users().removeFederatedIdentity(realm, user, alias);
        }
    }

    /** 从步骤配置解析并过滤非空的 IdP 别名流。 */
    private Stream<String> getConfiguredProviders() {
        List<String> idpAliases = stepModel.getConfig().getOrDefault(CONFIG_ALIAS, List.of());
        if (idpAliases.isEmpty()) {
            log.warnv("Unlink operation skipped: no Identity Provider alias configured ({0}). " +
                    "Specify one or more Identity Provider aliases or '*' for all.", CONFIG_ALIAS);
            return Stream.of();
        }

        return idpAliases.stream().map(String::trim).filter(s -> !s.isEmpty());
    }

    /** @return 当前会话上下文中的 Realm */
    private RealmModel getRealm() {
        return session.getContext().getRealm();
    }
}
