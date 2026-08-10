package org.keycloak.models.cache.infinispan.organization;

import java.util.Map;
import java.util.stream.Stream;

import org.keycloak.models.OrganizationInvitationModel;
import org.keycloak.models.OrganizationInvitationModel.Filter;
import org.keycloak.models.OrganizationModel;
import org.keycloak.organization.InvitationManager;

/**
 * Infinispan 组织缓存层使用的邀请管理器装饰器。
 * <p>
 * 当前为纯委托实现，将 {@link InvitationManager} 操作直接转发至底层提供者；
 * 预留扩展点以便后续在邀请增删改时接入缓存失效逻辑。
 *
 * @param delegate 底层邀请管理器实现
 */
record InfinispanInvitationManager(InvitationManager delegate) implements InvitationManager {

    /** 创建组织邀请并委托底层实现。 */
    @Override
    public OrganizationInvitationModel create(OrganizationModel organization, String email, String firstName, String lastName) {
        return delegate().create(organization, email, firstName, lastName);
    }

    /** 按 ID 查询邀请并委托底层实现。 */
    @Override
    public OrganizationInvitationModel getById(String id) {
        return delegate().getById(id);
    }

    /** 分页查询组织邀请流并委托底层实现。 */
    @Override
    public Stream<OrganizationInvitationModel> getAllStream(OrganizationModel organization, Map<Filter, String> attributes, Integer first, Integer max) {
        return delegate().getAllStream(organization, attributes, first, max);
    }

    /** 删除邀请并委托底层实现。 */
    @Override
    public boolean remove(String id) {
        return delegate().remove(id);
    }
}
