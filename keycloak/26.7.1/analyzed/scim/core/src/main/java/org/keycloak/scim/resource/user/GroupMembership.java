package org.keycloak.scim.resource.user;

import org.keycloak.scim.resource.common.MultiValuedAttribute;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * SCIM 用户组成员关系多值属性。
 * <p>继承 {@link MultiValuedAttribute}，{@code value} 通常为组 ID 或组名。</p>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GroupMembership extends MultiValuedAttribute {

}
