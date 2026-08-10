package org.keycloak.models.workflow;

import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

import org.jboss.logging.Logger;


/**
 * 工作流步骤：向目标用户添加 Realm 已启用的必需操作（Required Action）。
 * <p>配置项 {@link #REQUIRED_ACTION_KEY} 指定操作别名，连字符会转换为枚举名。</p>
 */
public class AddRequiredActionStepProvider implements WorkflowStepProvider {

    /** 组件配置中必需操作别名的键名。 */
    public static String REQUIRED_ACTION_KEY = "action";

    private final KeycloakSession session;
    private final ComponentModel stepModel;
    private final Logger log = Logger.getLogger(AddRequiredActionStepProvider.class);

    /** @param session Keycloak 会话 @param model 工作流步骤组件配置 */
    public AddRequiredActionStepProvider(KeycloakSession session, ComponentModel model) {
        this.session = session;
        this.stepModel = model;
    }

    /** 解析配置的操作并调用 {@link UserModel#addRequiredAction}；无效或未启用时记录警告。 */
    @Override
    public void run(WorkflowExecutionContext context) {
        RealmModel realm = session.getContext().getRealm();
        UserModel user = session.users().getUserById(realm, context.getResourceId());

        if (user != null) {
            String configuredAction = stepModel.getConfig().getFirst(REQUIRED_ACTION_KEY);
            if (configuredAction == null) {
                log.warnv("Missing required configuration option '{0}' in {1}", REQUIRED_ACTION_KEY, AddRequiredActionStepProviderFactory.ID);
                return;
            }
            try {
                // 将连字符转为下划线并大写以匹配 RequiredAction 枚举命名
                configuredAction = configuredAction.replace("-", "_").toUpperCase();
                UserModel.RequiredAction action = UserModel.RequiredAction.valueOf(configuredAction);
                if (!realm.getRequiredActionProviderByAlias(action.name()).isEnabled()) {
                    log.warnv("Required action {0} is not enabled in realm {1}", action, realm.getName());
                    return;
                }
                log.debugv("Adding required action {0} to user {1}", action, user.getId());
                user.addRequiredAction(action);
            } catch (IllegalArgumentException e) {
                log.warnv("Invalid required action {0} configured in {1}", stepModel.getConfig().getFirst(REQUIRED_ACTION_KEY),
                        AddRequiredActionStepProviderFactory.ID);
            }
        }
    }

    @Override
    public void close() {
    }
}
