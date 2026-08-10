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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.representations.idm.PartialImportRepresentation;
import org.keycloak.services.ErrorResponse;
import org.keycloak.services.ErrorResponseException;
import org.keycloak.services.ServicesLogger;

/**
 * 部分导入抽象基类：按 SKIP/OVERWRITE 策略处理资源存在性，统一 prepare/removeOverwrites/doImport 流程。
 * <p>子类实现各资源类型的表示解析、存在性检查与创建/删除逻辑。</p>
 *
 * @author Stan Silvert ssilvert@redhat.com (C) 2016 Red Hat Inc.
 */
public abstract class AbstractPartialImport<T> implements PartialImport<T> {

    /** 策略为 OVERWRITE 时待覆盖重建的资源集合。 */
    protected final Set<T> toOverwrite = new HashSet<>();
    /** 策略为 SKIP 时跳过的已存在资源集合。 */
    protected final Set<T> toSkip = new HashSet<>();

    public abstract List<T> getRepList(PartialImportRepresentation partialImportRep);
    public abstract String getName(T resourceRep);
    public abstract String getModelId(RealmModel realm, KeycloakSession session, T resourceRep);
    public abstract boolean exists(RealmModel realm, KeycloakSession session, T resourceRep);
    public abstract String existsMessage(RealmModel realm, T resourceRep);
    public abstract ResourceType getResourceType();
    public abstract void remove(RealmModel realm, KeycloakSession session, T resourceRep);
    public abstract void create(RealmModel realm, KeycloakSession session, T resourceRep);

    /**
     * 预处理：扫描资源列表，按策略归类为跳过或覆盖；冲突且策略为 FAIL 时抛错。
     * @param partialImportRep 部分导入请求体
     * @param realm 目标 Realm
     * @param session Keycloak 会话
     */
    @Override
    public void prepare(PartialImportRepresentation partialImportRep,
                         RealmModel realm,
                         KeycloakSession session) {
        List<T> repList = getRepList(partialImportRep);
        if ((repList == null) || repList.isEmpty()) return;

        for (T resourceRep : getRepList(partialImportRep)) {
            if (exists(realm, session, resourceRep)) {
                switch (partialImportRep.getPolicy()) {
                    case SKIP: toSkip.add(resourceRep); break;
                    case OVERWRITE: toOverwrite.add(resourceRep); break;
                    default: throw existsError(existsMessage(realm, resourceRep));
                }
            }
        }
    }

    /** 资源已存在且策略不允许覆盖时抛出 exists 错误。 */
    protected ErrorResponseException existsError(String message) {
        throw ErrorResponse.exists(message);
    }

    /** 构造“已覆盖”导入结果条目。 */
    protected PartialImportResult overwritten(String modelId, T resourceRep){
        return PartialImportResult.overwritten(getResourceType(), getName(resourceRep), modelId, resourceRep);
    }

    /** 构造“已跳过”导入结果条目。 */
    protected PartialImportResult skipped(String modelId, T resourceRep) {
        return PartialImportResult.skipped(getResourceType(), getName(resourceRep), modelId, resourceRep);
    }

    /** 构造“已新增”导入结果条目。 */
    protected PartialImportResult added(String modelId, T resourceRep) {
        return PartialImportResult.added(getResourceType(), getName(resourceRep), modelId, resourceRep);
    }

    /** 删除所有标记为覆盖的既有资源（在 doImport 重建之前统一执行）。 */
    @Override
    public void removeOverwrites(RealmModel realm, KeycloakSession session) {
        for (T resourceRep : toOverwrite) {
            remove(realm, session, resourceRep);
        }
    }

    /**
     * 执行导入：先重建覆盖项、记录跳过项，再创建其余新资源。
     * @return 汇总的部分导入结果
     */
    @Override
    public PartialImportResults doImport(PartialImportRepresentation partialImportRep, RealmModel realm, KeycloakSession session) {
        PartialImportResults results = new PartialImportResults();
        List<T> repList = getRepList(partialImportRep);
        if ((repList == null) || repList.isEmpty()) return results;

        for (T resourceRep : toOverwrite) {
            try {
                create(realm, session, resourceRep);
            } catch (Exception e) {
                ServicesLogger.LOGGER.overwriteError(e, getName(resourceRep));
                throw e;
            }

            String modelId = getModelId(realm, session, resourceRep);
            results.addResult(overwritten(modelId, resourceRep));
        }

        for (T resourceRep : toSkip) {
            String modelId = getModelId(realm, session, resourceRep);
            results.addResult(skipped(modelId, resourceRep));
        }

        for (T resourceRep : repList) {
            if (toOverwrite.contains(resourceRep)) continue;
            if (toSkip.contains(resourceRep)) continue;

            try {
                create(realm, session, resourceRep);
                String modelId = getModelId(realm, session, resourceRep);
                results.addResult(added(modelId, resourceRep));
            } catch (Exception e) {
                ServicesLogger.LOGGER.creationError(e, getName(resourceRep));
                throw e;
            }
        }

        return results;
    }

}
