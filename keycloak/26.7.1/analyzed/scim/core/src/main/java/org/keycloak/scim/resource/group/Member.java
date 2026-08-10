package org.keycloak.scim.resource.group;

import org.keycloak.scim.resource.common.MultiValuedAttribute;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * SCIM Group 成员引用，继承 {@link MultiValuedAttribute}。
 * <p>{@code value} 通常为成员 User 的 ID，{@code $ref} 指向 User 资源 URI。</p>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Member extends MultiValuedAttribute {
}
