package org.keycloak.models.mapper;

import org.keycloak.models.ClientModel;
import org.keycloak.representations.admin.v2.BaseClientRepresentation;

/**
 * @author Vaclav Muzikar <vmuzikar@redhat.com>
 * 客户端 {@link BaseClientRepresentation} 与 {@link ClientModel} 的映射器标记接口。
 */
public interface ClientModelMapper extends RepModelMapper<BaseClientRepresentation, ClientModel> {
}
