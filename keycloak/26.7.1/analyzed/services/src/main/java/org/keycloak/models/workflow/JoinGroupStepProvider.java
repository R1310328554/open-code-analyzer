package org.keycloak.models.workflow;

import org.keycloak.component.ComponentModel;
import org.keycloak.models.GroupModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserModel;

import org.jboss.logging.Logger;

import static org.keycloak.models.utils.ModelToRepresentation.buildGroupPath;

/**
 * 加入组工作流步骤：将用户加入配置的一个或多个组。
 * <p>继承 {@link GroupBasedStepProvider}，在 {@link #run(UserModel, GroupModel)} 中调用 {@link UserModel#joinGroup(GroupModel)}。</p>
 */
public class JoinGroupStepProvider extends GroupBasedStepProvider {

    private final Logger log = Logger.getLogger(JoinGroupStepProvider.class);

    /** @param session Keycloak 会话 @param model 步骤组件配置 */
    protected JoinGroupStepProvider(KeycloakSession session, ComponentModel model) {
        super(session, model);
    }

    /** 将指定用户加入目标组并记录调试日志。 */
    @Override
    protected void run(UserModel user, GroupModel group) {
        log.debugv("Adding user {0} to group {1}", user.getId(), buildGroupPath(group));
        user.joinGroup(group);
    }
}
