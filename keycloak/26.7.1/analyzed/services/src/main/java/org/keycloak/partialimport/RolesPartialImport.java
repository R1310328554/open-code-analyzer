/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.keycloak.partialimport;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import jakarta.ws.rs.core.Response;

import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RoleModel;
import org.keycloak.models.utils.KeycloakModelUtils;
import org.keycloak.models.utils.RepresentationToModel;
import org.keycloak.representations.idm.PartialImportRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.RolesRepresentation;
import org.keycloak.services.ErrorResponse;
import org.keycloak.services.ServicesLogger;

/**
 * 角色部分导入处理器：统一处理 Realm 角色与客户端角色。
 * <p>委托 {@link RealmRolesPartialImport} 与 {@link ClientRolesPartialImport} 做 prepare，最终通过 {@link RepresentationToModel#importRoles} 批量创建；跳过项从请求中移除，覆盖项在 importRoles 前删除。</p>
 *
 * @author Stan Silvert ssilvert@redhat.com (C) 2016 Red Hat Inc.
 */
public class RolesPartialImport implements PartialImport<RolesRepresentation> {

    private Set<RoleRepresentation> realmRolesToOverwrite;
    private Set<RoleRepresentation> realmRolesToSkip;

    private Map<String, Set<RoleRepresentation>> clientRolesToOverwrite;
    private Map<String, Set<RoleRepresentation>> clientRolesToSkip;

    private final RealmRolesPartialImport realmRolesPI = new RealmRolesPartialImport();
    private final ClientRolesPartialImport clientRolesPI = new ClientRolesPartialImport();
    /** 覆盖导入时若包含当前默认角色，需单独重建并设回 Realm 默认角色。 */
    private RoleRepresentation newDefaultRole;

    @Override
    public void prepare(PartialImportRepresentation rep, RealmModel realm, KeycloakSession session) {
        prepareRealmRoles(rep, realm, session);
        prepareClientRoles(rep, realm, session);
    }

    private void prepareRealmRoles(PartialImportRepresentation rep, RealmModel realm, KeycloakSession session) {
        if (!rep.hasRealmRoles()) return;

        realmRolesPI.prepare(rep, realm, session);
        this.realmRolesToOverwrite = realmRolesPI.getToOverwrite();
        if (realmRolesToOverwrite.size() > 0) {
            String defaultRoleName = realm.getDefaultRole().getName();
            for (RoleRepresentation representation : realmRolesToOverwrite) {
                if (Objects.equals(defaultRoleName, representation.getName())) {
                    this.newDefaultRole = representation;
                    break;
                }
            }
        }

        this.realmRolesToSkip = realmRolesPI.getToSkip();
    }

    private void prepareClientRoles(PartialImportRepresentation rep, RealmModel realm, KeycloakSession session) {
        if (!rep.hasClientRoles()) return;

        clientRolesPI.prepare(rep, realm, session);
        this.clientRolesToOverwrite = clientRolesPI.getToOverwrite();
        this.clientRolesToSkip = clientRolesPI.getToSkip();
    }

    @Override
    public void removeOverwrites(RealmModel realm, KeycloakSession session) {
        deleteClientRoleOverwrites(realm);
        deleteRealmRoleOverwrites(realm, session);
    }

    @Override
    public PartialImportResults doImport(PartialImportRepresentation rep, RealmModel realm, KeycloakSession session) {
        PartialImportResults results = new PartialImportResults();
        if (!rep.hasRealmRoles() && !rep.hasClientRoles()) return results;

        // 完成准备阶段：从请求移除跳过项并记录结果
        removeRealmRoleSkips(results, rep, realm, session);
        removeClientRoleSkips(results, rep, realm);
        if (rep.hasRealmRoles()) setUniqueIds(rep.getRoles().getRealm());
        if (rep.hasClientRoles()) setUniqueIds(rep.getRoles().getClient());

        if (newDefaultRole != null) {
            RoleModel defaultRole = RepresentationToModel.createRole(realm, newDefaultRole);
            realm.setDefaultRole(defaultRole);
        }

        try {
            RepresentationToModel.importRoles(rep.getRoles(), realm);
        } catch (Exception e) {
            ServicesLogger.LOGGER.roleImportError(e);
            throw ErrorResponse.error(e.getMessage(), Response.Status.INTERNAL_SERVER_ERROR);
        }

        // 为新建角色追加“已新增”结果
        realmRoleAdds(results, rep, realm, session);
        clientRoleAdds(results, rep, realm);

        // 为覆盖角色追加“已覆盖”结果
        addResultsForOverwrittenRealmRoles(results, realm, session);
        addResultsForOverwrittenClientRoles(results, realm);

        return results;
    }

    private void setUniqueIds(List<RoleRepresentation> realmRoles) {
        for (RoleRepresentation realmRole : realmRoles) {
            realmRole.setId(KeycloakModelUtils.generateId());
        }
    }

    private void setUniqueIds(Map<String, List<RoleRepresentation>> clientRoles) {
        for (List<RoleRepresentation> roleRepresentations : clientRoles.values()) {
            for (RoleRepresentation clientRole : roleRepresentations) {
                clientRole.setId(KeycloakModelUtils.generateId());
            }
        }
    }

    private void removeRealmRoleSkips(PartialImportResults results,
                                      PartialImportRepresentation rep,
                                      RealmModel realm,
                                      KeycloakSession session) {
        if (isEmpty(realmRolesToSkip)) return;

        for (RoleRepresentation roleRep : realmRolesToSkip) {
            rep.getRoles().getRealm().remove(roleRep);
            String modelId = realmRolesPI.getModelId(realm, session, roleRep);
            results.addResult(realmRolesPI.skipped(modelId, roleRep));
        }
    }

    private void removeClientRoleSkips(PartialImportResults results,
                                       PartialImportRepresentation rep,
                                       RealmModel realm) {
        if (isEmpty(clientRolesToSkip)) return;

        for (var entry : clientRolesToSkip.entrySet()) {
            String clientId = entry.getKey();
            for (RoleRepresentation roleRep : entry.getValue()) {
                rep.getRoles().getClient().get(clientId).remove(roleRep);
                String modelId = clientRolesPI.getModelId(realm, clientId);
                results.addResult(clientRolesPI.skipped(clientId, modelId, roleRep));
            }
        }
    }

    private void deleteRealmRoleOverwrites(RealmModel realm, KeycloakSession session) {
        if (isEmpty(realmRolesToOverwrite)) return;

        for (RoleRepresentation roleRep : realmRolesToOverwrite) {
            realmRolesPI.remove(realm, session, roleRep);
        }
    }

    private void addResultsForOverwrittenRealmRoles(PartialImportResults results, RealmModel realm, KeycloakSession session) {
        if (isEmpty(realmRolesToOverwrite)) return;

        for (RoleRepresentation roleRep : realmRolesToOverwrite) {
            String modelId = realmRolesPI.getModelId(realm, session, roleRep);
            results.addResult(realmRolesPI.overwritten(modelId, roleRep));
        }
    }

    private void deleteClientRoleOverwrites(RealmModel realm) {
        if (isEmpty(clientRolesToOverwrite)) return;

        for (var entry : clientRolesToOverwrite.entrySet()) {
            for (RoleRepresentation roleRep : entry.getValue()) {
                clientRolesPI.deleteRole(realm, entry.getKey(), roleRep);
            }
        }
    }

    private void addResultsForOverwrittenClientRoles(PartialImportResults results, RealmModel realm) {
        if (isEmpty(clientRolesToOverwrite)) return;

        for (var entry : clientRolesToOverwrite.entrySet()) {
            String clientId = entry.getKey();
            for (RoleRepresentation roleRep : entry.getValue()) {
                String modelId = clientRolesPI.getModelId(realm, clientId);
                results.addResult(clientRolesPI.overwritten(clientId, modelId, roleRep));
            }
        }
    }

    private boolean isEmpty(Set set) {
        return (set == null) || (set.isEmpty());
    }

    private boolean isEmpty(Map map) {
        return (map == null) || (map.isEmpty());
    }

    private void realmRoleAdds(PartialImportResults results,
                               PartialImportRepresentation rep,
                               RealmModel realm,
                               KeycloakSession session) {
        if (!rep.hasRealmRoles()) return;

        for (RoleRepresentation roleRep : rep.getRoles().getRealm()) {
            if (realmRolesToOverwrite.contains(roleRep)) continue;
            if (realmRolesToSkip.contains(roleRep)) continue;

            String modelId = realmRolesPI.getModelId(realm, session, roleRep);
            results.addResult(realmRolesPI.added(modelId, roleRep));
        }
    }

    private void clientRoleAdds(PartialImportResults results,
                                PartialImportRepresentation rep,
                                RealmModel realm) {
        if (!rep.hasClientRoles()) return;

        Map<String, List<RoleRepresentation>> repList = clientRolesPI.getRepList(rep);
        for (var entry : repList.entrySet()) {
            String clientId = entry.getKey();
            for (RoleRepresentation roleRep : entry.getValue()) {
                if (clientRolesToOverwrite.get(clientId).contains(roleRep)) continue;
                if (clientRolesToSkip.get(clientId).contains(roleRep)) continue;

                String modelId = clientRolesPI.getModelId(realm, clientId);
                results.addResult(clientRolesPI.added(clientId, modelId, roleRep));
            }
        }
    }
}
