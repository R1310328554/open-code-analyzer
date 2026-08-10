package org.keycloak.models.workflow;

import org.keycloak.component.ComponentModel;
import org.keycloak.models.GroupModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserModel;

import org.jboss.logging.Logger;

import static org.keycloak.models.utils.ModelToRepresentation.buildGroupPath;

/**
 * 离开组工作流步骤：将用户从配置的一个或多个组中移除。
 * <p>继承 {@link GroupBasedStepProvider}，在 {@link #run(UserModel, GroupModel)} 中调用 {@link UserModel#leaveGroup(GroupModel)}。</p>
 */
public class LeaveGroupStepProvider extends GroupBasedStepProvider {

    private final Logger log = Logger.getLogger(LeaveGroupStepProvider.class);

    /** @param session Keycloak 会话 @param model 步骤组件配置 */
    protected LeaveGroupStepProvider(KeycloakSession session, ComponentModel model) {
        super(session, model);
    }

    /** 将指定用户从目标组移除并记录调试日志。 */
    @Override
    protected void run(UserModel user, GroupModel group) {
        log.debugv("Removing user {0} from group {1}", user.getId(), buildGroupPath(group));
        user.leaveGroup(group);
    }
}
