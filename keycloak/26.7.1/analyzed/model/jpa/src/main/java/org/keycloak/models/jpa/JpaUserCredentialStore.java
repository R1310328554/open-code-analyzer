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
package org.keycloak.models.jpa;

import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.TypedQuery;

import org.keycloak.credential.CredentialModel;
import org.keycloak.credential.UserCredentialStore;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ModelDuplicateException;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.jpa.entities.CredentialEntity;
import org.keycloak.models.jpa.entities.UserEntity;
import org.keycloak.models.utils.KeycloakModelUtils;

import org.jboss.logging.Logger;

import static org.keycloak.utils.StreamsUtil.closing;

/**
 * JPA 用户凭据存储：管理密码、OTP 等 Credential 的持久化、排序与旧版 salt 迁移。
 * 凭据以 priority 链表顺序存储，支持 moveCredentialTo 重排。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class JpaUserCredentialStore implements UserCredentialStore {

    /** 相邻凭据 priority 间隔，便于插入时留空位。 */
    public static final int PRIORITY_DIFFERENCE = 10;

    protected static final Logger logger = Logger.getLogger(JpaUserCredentialStore.class);

    /** 当前 Keycloak 会话。 */
    private final KeycloakSession session;
    /** JPA EntityManager。 */
    protected final EntityManager em;

    public JpaUserCredentialStore(KeycloakSession session, EntityManager em) {
        this.session = session;
        this.em = em;
    }

    @Override
    public void updateCredential(RealmModel realm, UserModel user, CredentialModel cred) {
        CredentialEntity entity = em.find(CredentialEntity.class, cred.getId());
        if (!checkCredentialEntity(entity, user)) return;
        if (!Objects.equals(cred.getUserLabel(), entity.getUserLabel())) {
            // 历史数据可能存在重复 userLabel，更新时重新校验唯一性
            // Ignore them when the credential is updated, which might happen when credentials are verified.
            validateDuplicateCredential(realm, user, cred.getType(), cred.getUserLabel(), cred.getId());
        }
        entity.setCreatedDate(cred.getCreatedDate());
        entity.setUserLabel(cred.getUserLabel());
        entity.setType(cred.getType());
        entity.setSecretData(cred.getSecretData());
        entity.setCredentialData(cred.getCredentialData());
    }

    @Override
    public CredentialModel createCredential(RealmModel realm, UserModel user, CredentialModel cred) {
        CredentialEntity entity = createCredentialEntity(realm, user, cred);
        return toModel(entity);
    }

    /** 移除 StoredCredential。 */
    @Override
    public boolean removeStoredCredential(RealmModel realm, UserModel user, String id) {
        CredentialEntity entity = removeCredentialEntity(realm, user, id);
        return entity != null;
    }

    @Override
    public CredentialModel getStoredCredentialById(RealmModel realm, UserModel user, String id) {
        CredentialEntity entity = em.find(CredentialEntity.class, id);
        if (!checkCredentialEntity(entity, user)) return null;
        CredentialModel model = toModel(entity);
        return model;
    }

    CredentialModel toModel(CredentialEntity entity) {
        CredentialModel model = new CredentialModel();
        model.setId(entity.getId());
        model.setType(entity.getType());
        model.setCreatedDate(entity.getCreatedDate());
        model.setUserLabel(entity.getUserLabel());

        // 向后兼容：旧版 salt 列在读取时迁移到 secretData JSON
        // We migrate it to new secretData format on-the-fly
        if (entity.getSalt() != null) {
            String newSecretData = entity.getSecretData().replace("__SALT__", Base64.getEncoder().encodeToString(entity.getSalt()));
            entity.setSecretData(newSecretData);
            entity.setSalt(null);
        }

        model.setSecretData(entity.getSecretData());
        model.setCredentialData(entity.getCredentialData());
        return model;
    }

    @Override
    public Stream<CredentialModel> getStoredCredentialsStream(RealmModel realm, UserModel user) {
        return this.getStoredCredentialEntities(realm, user).map(this::toModel);
    }

    private Stream<CredentialEntity> getStoredCredentialEntities(RealmModel realm, UserModel user) {
        UserEntity userEntity = em.getReference(UserEntity.class, user.getId());
        TypedQuery<CredentialEntity> query = em.createNamedQuery("credentialByUser", CredentialEntity.class)
                .setParameter("user", userEntity);
        return closing(query.getResultStream());
    }

    @Override
    public Stream<CredentialModel> getStoredCredentialsByTypeStream(RealmModel realm, UserModel user, String type) {
        return getStoredCredentialsStream(realm, user).filter(credential -> Objects.equals(type, credential.getType()));
    }

    /** 获取 StoredCredentialByNameAndType。 */
    @Override
    public CredentialModel getStoredCredentialByNameAndType(RealmModel realm, UserModel user, String name, String type) {
        return getStoredCredentialsStream(realm, user).filter(credential ->
                        Objects.equals(type, credential.getType()) && Objects.equals(name, credential.getUserLabel()))
                .findFirst().orElse(null);
    }

    @Override
    public void close() {

    }

    private void validateDuplicateCredential(RealmModel realm, UserModel user, String credType, String userLabel, String credentialId) {
        if (userLabel != null) {
            boolean exists = getStoredCredentialEntities(realm, user)
                    .anyMatch(existing -> existing.getUserLabel() != null
                            && existing.getUserLabel().equalsIgnoreCase(userLabel.trim())
                            && existing.getType().equals(credType)
                            && !existing.getId().equals(credentialId)); // Exclude self in update

            if (exists) {
                throw new ModelDuplicateException("Device already exists with the same name", CredentialModel.USER_LABEL);
            }
        }
    }

    CredentialEntity createCredentialEntity(RealmModel realm, UserModel user, CredentialModel cred) {
        validateDuplicateCredential(realm, user, cred.getType(), cred.getUserLabel(), null);
        CredentialEntity entity = new CredentialEntity();
        String id = cred.getId() == null ? KeycloakModelUtils.generateId() : cred.getId();
        entity.setId(id);
        entity.setCreatedDate(cred.getCreatedDate());
        entity.setUserLabel(cred.getUserLabel());
        entity.setType(cred.getType());
        entity.setSecretData(cred.getSecretData());
        entity.setCredentialData(cred.getCredentialData());
        UserEntity userRef = em.getReference(UserEntity.class, user.getId());
        entity.setUser(userRef);

        // 新凭据追加到链表末尾，priority 递增
        List<CredentialEntity> credentials = getStoredCredentialEntities(realm, user).collect(Collectors.toList());
        int priority = credentials.isEmpty() ? PRIORITY_DIFFERENCE : credentials.get(credentials.size() - 1).getPriority() + PRIORITY_DIFFERENCE;
        entity.setPriority(priority);

        em.persist(entity);
        return entity;
    }

    CredentialEntity removeCredentialEntity(RealmModel realm, UserModel user, String id) {
        CredentialEntity entity = em.find(CredentialEntity.class, id, LockModeType.PESSIMISTIC_WRITE);
        if (!checkCredentialEntity(entity, user)) return null;

        int currentPriority = entity.getPriority();

        this.getStoredCredentialEntities(realm, user).forEach(cred -> {
            if (cred.getPriority() > currentPriority) {
                cred.setPriority(cred.getPriority() - PRIORITY_DIFFERENCE);
            }
        });

        em.remove(entity);
        em.flush();
        return entity;
    }

    // ---- 凭据 priority 链表重排操作 ----
    @Override
    public boolean moveCredentialTo(RealmModel realm, UserModel user, String id, String newPreviousCredentialId) {

        // 1. 加载全部凭据到内存列表
        List<CredentialEntity> newList = this.getStoredCredentialEntities(realm, user).collect(Collectors.toList());

        // 2. 定位目标凭据与 newPrevious 索引
        int ourCredentialIndex = -1;
        int newPreviousCredentialIndex = -1;
        CredentialEntity ourCredential = null;
        int i = 0;
        for (CredentialEntity credential : newList) {
            if (id.equals(credential.getId())) {
                ourCredentialIndex = i;
                ourCredential = credential;
            } else if(newPreviousCredentialId != null && newPreviousCredentialId.equals(credential.getId())) {
                newPreviousCredentialIndex = i;
            }
            i++;
        }

        if (ourCredentialIndex == -1) {
            logger.warnf("Not found credential with id [%s] of user [%s]", id, user.getUsername());
            return false;
        }

        if (newPreviousCredentialId != null && newPreviousCredentialIndex == -1) {
            logger.warnf("Can't move up credential with id [%s] of user [%s]", id, user.getUsername());
            return false;
        }

        // 3. 计算插入位置
        int toMoveIndex = newPreviousCredentialId==null ? 0 : newPreviousCredentialIndex + 1;

        // 4. 插入新位置并移除旧位置
        newList.add(toMoveIndex, ourCredential);
        int indexToRemove = toMoveIndex < ourCredentialIndex ? ourCredentialIndex + 1 : ourCredentialIndex;
        newList.remove(indexToRemove);

        // 5. 按新顺序重写全部 priority
        int expectedPriority = 0;
        for (CredentialEntity credential : newList) {
            expectedPriority += PRIORITY_DIFFERENCE;
            if (credential.getPriority() != expectedPriority) {
                credential.setPriority(expectedPriority);

                logger.tracef("Priority of credential [%s] of user [%s] changed to [%d]", credential.getId(), user.getUsername(), expectedPriority);
            }
        }
        return true;
    }

    private boolean checkCredentialEntity(CredentialEntity entity, UserModel user) {
        return entity != null && entity.getUser() != null && entity.getUser().getId().equals(user.getId());
    }

}
