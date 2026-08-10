package org.keycloak.models.workflow;

import java.util.List;

import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

import org.jboss.logging.Logger;


/**
 * 移除用户属性工作流步骤：按配置批量删除用户自定义属性。
 * <p>从步骤配置 {@link #CONFIG_ATTRIBUTE} 读取属性名列表，逐项调用 {@link UserModel#removeAttribute(String)}。</p>
 */
public class RemoveUserAttributeStepProvider implements WorkflowStepProvider {

    private final KeycloakSession session;
    private final ComponentModel stepModel;
    private final Logger log = Logger.getLogger(RemoveUserAttributeStepProvider.class);

    /** 步骤配置键：待移除的属性名称列表。 */
    public static final String CONFIG_ATTRIBUTE = "attribute";

    /** @param session Keycloak 会话 @param model 步骤组件配置 */
    public RemoveUserAttributeStepProvider(KeycloakSession session, ComponentModel model) {
        this.session = session;
        this.stepModel = model;
    }

    /** 无资源需释放。 */
    @Override
    public void close() {
    }

    /** 遍历配置属性名并逐个从目标用户移除。 */
    @Override
    public void run(WorkflowExecutionContext context) {
        RealmModel realm = session.getContext().getRealm();
        UserModel user = session.users().getUserById(realm, context.getResourceId());

        if (user != null) {
            try {
                List<String> attrs = stepModel.getConfig().getOrDefault(CONFIG_ATTRIBUTE, List.of());
                for (String attr : attrs) {
                    log.debugv("Removing attribute {0} from user {1}", attr, user.getId());
                    user.removeAttribute(attr);
                }
            } catch (Exception e) {
                log.errorf(e, "Failed to remove attributes from user %s", user.getId());
            }
        }
    }
}
