/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.services.clientpolicy;

import java.io.IOException;
import java.util.List;

import org.keycloak.common.Profile;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.representations.idm.ClientPoliciesRepresentation;
import org.keycloak.representations.idm.ClientProfilesRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.services.clientpolicy.condition.ClientPolicyConditionProvider;
import org.keycloak.services.clientpolicy.executor.ClientPolicyExecutorProvider;
import org.keycloak.util.JsonSerialization;

import org.jboss.logging.Logger;

/**
 * 默认 {@link ClientPolicyManager} 实现。
 * <p>在 {@link Profile.Feature#CLIENT_POLICIES} 启用时，按 Realm 已启用的 {@link ClientPolicy} 评估条件并执行关联 {@link ClientProfile} 中的 Executor；同时负责客户端策略/配置文件的 CRUD 与 Realm 导入导出同步。</p>
 *
 * @author <a href="mailto:takashi.norimatsu.ws@hitachi.com">Takashi Norimatsu</a>
 */
public class DefaultClientPolicyManager implements ClientPolicyManager {

    /** 策略运行 trace 日志 */
    private static final Logger logger = Logger.getLogger(DefaultClientPolicyManager.class);

    /** 当前 Keycloak 会话 */
    private final KeycloakSession session;

    /** @param session Keycloak 会话 */
    public DefaultClientPolicyManager(KeycloakSession session) {
        this.session = session;
    }

    /** 在客户端策略事件上触发条件评估与 Executor 执行 @param context 事件上下文 */
    @Override
    public void triggerOnEvent(ClientPolicyContext context) throws ClientPolicyException {
        if (!Profile.isFeatureEnabled(Profile.Feature.CLIENT_POLICIES)) {
            return;
        }

        RealmModel realm = session.getContext().getRealm();
        logger.tracev("POLICY OPERATION :: context realm = {0}, event = {1}", realm.getName(), context.getEvent());

        doPolicyOperation(
                (ClientPolicyConditionProvider condition) -> condition.applyPolicy(context),
                (ClientPolicyExecutorProvider executor) -> executor.executeOnEvent(context),
                realm
        );
    }

    /** 遍历 Realm 已启用策略：条件满足则执行关联 Profile 中的 Executor */
    private void doPolicyOperation(ClientConditionOperation condition, ClientExecutorOperation executor, RealmModel realm) throws ClientPolicyException {
        List<ClientPolicy> list = ClientPoliciesUtil.getEnabledClientPolicies(session, realm);

        if (list == null || list.isEmpty()) {
            logger.trace("POLICY OPERATION :: No enabled policy.");
            return;
        }

        for (ClientPolicy policy: list) {
            logger.tracev("POLICY OPERATION :: policy name = {0}", policy.getName());
            if (!isSatisfied(policy, condition)) {
                logger.tracev("POLICY UNSATISFIED :: policy name = {0}", policy.getName());
                continue;
            }

            logger.tracev("POLICY APPLIED :: policy name = {0}", policy.getName());
            execute(policy, executor, realm);
        }
    }

    /** 评估策略全部条件（含否定逻辑与 STRICT/PERMISSIVE 模式） @return 条件组合是否满足 */
    private boolean isSatisfied(
            ClientPolicy policy,
            ClientConditionOperation op) throws ClientPolicyException {

        if (policy.getConditions() == null || policy.getConditions().isEmpty()) {
            logger.tracev("NO CONDITION :: policy name = {0}", policy.getName());
            return false;
        }

        boolean ret = false;
        for (ClientPolicyConditionProvider condition : policy.getConditions()) {
            logger.tracev("CONDITION OPERATION :: policy name = {0}, condition name = {1}, provider id = {2}", policy.getName(), condition.getName(), condition.getProviderId());
            try {
                ClientPolicyVote vote = op.run(condition);
                if (condition.isNegativeLogic()) {
                    if (vote == ClientPolicyVote.YES) {
                        vote = ClientPolicyVote.NO;
                    } else if (vote == ClientPolicyVote.NO) {
                        vote = ClientPolicyVote.YES;
                    }
                }
                if (vote == ClientPolicyVote.ABSTAIN) {
                    if (logger.isTraceEnabled()) {
                        String modeMessage = policy.getMode() == ClientPolicyMode.STRICT ? "(NEGATIVE due the STRICT mode)" : "";
                        logger.tracev("CONDITION SKIP {0}:: policy name = {1}, condition name = {2}, provider id = {3}",
                                modeMessage, policy.getName(), condition.getName(), condition.getProviderId());
                    }
                    if (policy.getMode() == ClientPolicyMode.STRICT) {
                        return false;
                    } else {
                        continue;
                    }
                } else if (vote == ClientPolicyVote.NO) {
                    logger.tracev("CONDITION NEGATIVE :: policy name = {0}, condition name = {1}, provider id = {2}", policy.getName(), condition.getName(), condition.getProviderId());
                    return false;
                }
                ret = true;
            } catch (ClientPolicyException e) {
                logger.tracev("CONDITION EXCEPTION :: policy name = {0}, provider id = {1}, error = {2}, error detail = {3}", condition.getName(), condition.getProviderId(), e.getError(), e.getErrorDetail());
                throw e;
            }
        }

        if (ret == true) {
            logger.tracev("CONDITIONS SATISFIED :: policy name = {0}", policy.getName());
        } else {
            logger.tracev("CONDITIONS UNSATISFIED :: policy name = {0}", policy.getName());
        }

        return ret;
    }

    /** 对策略引用的各 ClientProfile 依次执行 Executor */
    private void execute(
            ClientPolicy policy,
            ClientExecutorOperation op,
            RealmModel realm) throws ClientPolicyException {

        if (policy.getProfiles() == null || policy.getProfiles().isEmpty()) {
            logger.tracev("NO PROFILE :: policy name = {0}", policy.getName());
            return;
        }

        // 从 Realm 加载客户端配置文件表示
        ClientProfilesRepresentation clientProfiles =  ClientPoliciesUtil.getClientProfilesRepresentation(session, realm);

        for (String profileName : policy.getProfiles()) {
            ClientProfile profile = ClientPoliciesUtil.getClientProfileModel(session, realm, clientProfiles,
                    ClientPoliciesUtil.getGlobalClientProfiles(session), profileName);
            if (profile == null) {
                logger.tracev("PROFILE NOT FOUND :: policy name = {0}, profile name = {1}", policy.getName(), profileName);
                continue;
            }

            if (profile.getExecutors() == null || profile.getExecutors().isEmpty()) {
                logger.tracev("PROFILE NO EXECUTOR :: policy name = {0}, profile name = {1}", policy.getName(), profileName);
                continue;
            }

            for (ClientPolicyExecutorProvider executor : profile.getExecutors()) {
                logger.tracev("EXECUTION :: policy name = {0}, profile name = {1}, executor name = {2}, provider id = {3}", policy.getName(), profileName, executor.getName(), executor.getProviderId());
                try {
                    op.run(executor);
                } catch(ClientPolicyException e) {
                    logger.tracev("EXECUTOR EXCEPTION :: executor name = {0}, provider id = {1}, error = {2}, error detail = {3}", executor.getName(), executor.getProviderId(), e.getError(), e.getErrorDetail());
                    throw e;
                }
            }

        }
    }

    /** 条件评估回调 */
    private interface ClientConditionOperation {
        ClientPolicyVote run(ClientPolicyConditionProvider condition) throws ClientPolicyException;
    }

    /** Executor 执行回调 */
    private interface ClientExecutorOperation {
        void run(ClientPolicyExecutorProvider executor) throws ClientPolicyException;
    }

    /** 新建 Realm 时初始化客户端策略（当前不自动创建） @param realm 目标 Realm */
    @Override
    public void setupClientPoliciesOnCreatedRealm(RealmModel realm) {
        // 暂不自动创建策略，由管理员按需配置
    }

    /** 从 Realm 导入表示更新客户端配置文件与策略 @param realm 目标 Realm @param rep 导入表示 */
    @Override
    public void updateRealmModelFromRepresentation(RealmModel realm, RealmRepresentation rep) {
        logger.tracev("LOAD PROFILE POLICIES ON IMPORTED REALM :: realm = {0}", realm.getName());

        if (rep.getParsedClientProfiles() != null) {
            try {
                updateClientProfiles(realm, rep.getParsedClientProfiles());
            } catch (ClientPolicyException e) {
                logger.warnv("VALIDATE SERIALIZE IMPORTED REALM PROFILES FAILED :: error = {0}, error detail = {1}", e.getError(), e.getErrorDetail());
                throw new RuntimeException("Failed to update client profiles", e);
            }
        }

        ClientPoliciesRepresentation clientPolicies = rep.getParsedClientPolicies();
        if (clientPolicies != null) {
            try {
                updateClientPolicies(realm, clientPolicies);
            } catch (ClientPolicyException e) {
                logger.warnv("VALIDATE SERIALIZE IMPORTED REALM POLICIES FAILED :: error = {0}, error detail = {1}", e.getError(), e.getErrorDetail());
                throw new RuntimeException("Failed to update client policies", e);
            }
        } else {
            setupClientPoliciesOnCreatedRealm(realm);
        }
    }

    /** 校验并持久化客户端配置文件 @param realm 目标 Realm @param clientProfiles 待更新表示 */
    @Override
    public void updateClientProfiles(RealmModel realm, ClientProfilesRepresentation clientProfiles) throws ClientPolicyException {
        try {
            if (clientProfiles == null) {
                throw new ClientPolicyException("Passing null clientProfiles not allowed");
            }
            ClientProfilesRepresentation validatedProfilesRep = ClientPoliciesUtil.getValidatedClientProfilesForUpdate(session, realm, clientProfiles,
                    ClientPoliciesUtil.getGlobalClientProfiles(session));
            String validatedJsonString = ClientPoliciesUtil.convertClientProfilesRepresentationToJson(validatedProfilesRep);
            ClientPoliciesUtil.setClientProfilesJsonString(realm, validatedJsonString);
            logger.tracev("UPDATE PROFILES :: realm = {0}, validated and modified PUT = {1}", realm.getName(), validatedJsonString);
        } catch (ClientPolicyException e) {
            logger.warnv("VALIDATE SERIALIZE PROFILES FAILED :: error = {0}, error detail = {1}", e.getError(), e.getErrorDetail());
            throw e;
        }
    }

    /** 读取 Realm 客户端配置文件 @param includeGlobalProfiles 是否合并全局配置 @return 配置文件表示 */
    @Override
    public ClientProfilesRepresentation getClientProfiles(RealmModel realm, boolean includeGlobalProfiles) throws ClientPolicyException {
        try {
            ClientProfilesRepresentation clientProfiles = ClientPoliciesUtil.getClientProfilesRepresentation(session, realm);
            if (includeGlobalProfiles) {
                clientProfiles.setGlobalProfiles(ClientPoliciesUtil.getGlobalClientProfiles(session));
            }

            if (logger.isTraceEnabled()) {
                logger.tracev("GET PROFILES :: realm = {0}, GET = {1}", realm.getName(), JsonSerialization.writeValueAsString(clientProfiles));
            }

            return clientProfiles;
        } catch (ClientPolicyException e) {
            logger.warnv("GET CLIENT PROFILES FAILED :: error = {0}, error detail = {1}", e.getError(), e.getErrorDetail());
            throw e;
        } catch (IOException ioe) {
            throw new RuntimeException("Unexpected exception when converting JSON to String", ioe);
        }
    }

    /** 校验并持久化客户端策略 @param realm 目标 Realm @param clientPolicies 待更新表示 */
    @Override
    public void updateClientPolicies(RealmModel realm, ClientPoliciesRepresentation clientPolicies) throws ClientPolicyException {
        String validatedJsonString = null;
        try {
            if (clientPolicies == null) {
                throw new ClientPolicyException("Passing null clientPolicies not allowed");
            }
            ClientPoliciesRepresentation clientPoliciesRep = ClientPoliciesUtil.getValidatedClientPoliciesForUpdate(session, realm, clientPolicies,
                    ClientPoliciesUtil.getGlobalClientProfiles(session), ClientPoliciesUtil.getGlobalClientPolicies(session));
            validatedJsonString = ClientPoliciesUtil.convertClientPoliciesRepresentationToJson(clientPoliciesRep);
        } catch (ClientPolicyException e) {
            logger.warnv("VALIDATE SERIALIZE POLICIES FAILED :: error = {0}, error detail = {1}", e.getError(), e.getErrorDetail());
            throw e;
        }
        ClientPoliciesUtil.setClientPoliciesJsonString(realm, validatedJsonString);
        logger.tracev("UPDATE POLICIES :: realm = {0}, validated and modified PUT = {1}", realm.getName(), validatedJsonString);
    }

    /** 读取 Realm 客户端策略 @param includeGlobalPolicies 是否合并全局策略 @return 策略表示 */
    @Override
    public ClientPoliciesRepresentation getClientPolicies(RealmModel realm, boolean includeGlobalPolicies) throws ClientPolicyException {
        try {
            ClientPoliciesRepresentation clientPolicies = ClientPoliciesUtil.getClientPoliciesRepresentation(session, realm);
            if (includeGlobalPolicies) {
                clientPolicies.setGlobalPolicies(ClientPoliciesUtil.getGlobalClientPolicies(session));
            }
            if (logger.isTraceEnabled()) {
                logger.tracev("GET POLICIES :: realm = {0}, GET = {1}", realm.getName(), JsonSerialization.writeValueAsString(clientPolicies));
            }
            return clientPolicies;
        } catch (ClientPolicyException e) {
            logger.warnv("GET CLIENT POLICIES FAILED :: error = {0}, error detail = {1}", e.getError(), e.getErrorDetail());
            throw e;
        } catch (IOException ioe) {
            throw new RuntimeException("Unexpected exception when converting JSON to String", ioe);
        }
    }

    /** 导出 Realm 时将客户端配置文件/策略写入表示（不含全局项） @param realm 源 Realm @param rep 导出表示 */
    @Override
    public void updateRealmRepresentationFromModel(RealmModel realm, RealmRepresentation rep) {
        try {
            // 导出时过滤掉全局配置文件
            ClientProfilesRepresentation filteredOutProfiles = getClientProfiles(realm, false);
            rep.setParsedClientProfiles(filteredOutProfiles);

            ClientPoliciesRepresentation filteredOutPolicies = getClientPolicies(realm, false);
            rep.setParsedClientPolicies(filteredOutPolicies);
        } catch (ClientPolicyException cpe) {
            throw new IllegalStateException("Exception during export client profiles or client policies", cpe);
        }
    }

    /** 关闭 Manager（无资源需释放） */
    @Override
    public void close() {
    }
}
