package org.keycloak.models.workflow;

import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

import org.jboss.logging.Logger;


/**
 * 移除必需操作工作流步骤：从用户账户清除指定的 {@link UserModel.RequiredAction}。
 * <p>通过步骤配置 {@link #REQUIRED_ACTION_KEY} 指定要移除的操作枚举名；无效配置时记录警告。</p>
 */
public class RemoveRequiredActionStepProvider implements WorkflowStepProvider {

    /** 步骤配置键：要移除的必需操作枚举名称。 */
    public static String REQUIRED_ACTION_KEY = "action";

    private final KeycloakSession session;
    private final ComponentModel stepModel;
    private final Logger log = Logger.getLogger(RemoveRequiredActionStepProvider.class);

    /** @param session Keycloak 会话 @param model 步骤组件配置 */
    public RemoveRequiredActionStepProvider(KeycloakSession session, ComponentModel model) {
        this.session = session;
        this.stepModel = model;
    }

    /** 解析配置中的必需操作并调用 {@link UserModel#removeRequiredAction} 移除。 */
    @Override
    public void run(WorkflowExecutionContext context) {
        RealmModel realm = session.getContext().getRealm();
        UserModel user = session.users().getUserById(realm, context.getResourceId());

        if (user != null) {
            try {
                UserModel.RequiredAction action = UserModel.RequiredAction.valueOf(stepModel.getConfig().getFirst(REQUIRED_ACTION_KEY));
                log.debugv("Removing required action {0} from user {1}", action, user.getId());
                user.removeRequiredAction(action);
            } catch (IllegalArgumentException e) {
                log.warnv("Invalid required action {0} configured in RemoveRequiredActionStepProvider", stepModel.getConfig().getFirst(REQUIRED_ACTION_KEY));
            }
        }
    }

    /** 无资源需释放。 */
    @Override
    public void close() {
    }
}
