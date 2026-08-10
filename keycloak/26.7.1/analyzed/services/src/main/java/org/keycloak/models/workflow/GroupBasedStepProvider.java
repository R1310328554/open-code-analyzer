package org.keycloak.models.workflow;

import java.util.List;
import java.util.stream.Stream;

import org.keycloak.component.ComponentModel;
import org.keycloak.models.GroupModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.KeycloakModelUtils;

import org.jboss.logging.Logger;

/**
 * 基于群组的工作流步骤抽象基类：按配置路径解析 {@link GroupModel} 并对用户执行群组操作。
 * <p>配置项 {@link #CONFIG_GROUP} 列出群组路径；子类实现 {@link #run(UserModel, GroupModel)}。</p>
 */
public abstract class GroupBasedStepProvider implements WorkflowStepProvider {

    private final Logger log = Logger.getLogger(GroupBasedStepProvider.class);
    /** 组件配置中群组路径列表的键名。 */
    public static final String CONFIG_GROUP = "group";

    private final KeycloakSession session;
    private final ComponentModel model;

    /** @param session Keycloak 会话 @param model 含群组路径列表的工作流步骤配置 */
    public GroupBasedStepProvider(KeycloakSession session, ComponentModel model) {
        this.session = session;
        this.model = model;
    }

    /** 解析配置群组并对目标用户逐个调用子类 {@link #run(UserModel, GroupModel)}。 */
    @Override
    public void run(WorkflowExecutionContext context) {
        UserModel user = session.users().getUserById(getRealm(), context.getResourceId());

        if (user != null) {
            try {
                getGroups().forEach(group -> run(user, group));
            } catch (Exception e) {
                log.errorf(e, "Failed to manage group membership for user %s", user.getId());
            }
        }
    }

    /** 子类实现：对单个用户-群组对执行具体操作（加入/离开等）。 */
    protected abstract void run(UserModel user, GroupModel group);

    @Override
    public void close() {
    }

    /** 将配置中的群组路径解析为 {@link GroupModel} 流。 */
    private Stream<GroupModel> getGroups() {
        return model.getConfig().getOrDefault(CONFIG_GROUP, List.of()).stream()
                .map(name -> KeycloakModelUtils.findGroupByPath(session, getRealm(), name));
    }

    /** @return 当前会话上下文中的 Realm */
    private RealmModel getRealm() {
        return session.getContext().getRealm();
    }
}
