/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.testsuite.federation.sync;

import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.KeycloakSessionTask;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.KeycloakModelUtils;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;
import org.keycloak.storage.UserStoragePrivateUtil;
import org.keycloak.storage.UserStorageProviderModel;
import org.keycloak.storage.user.SynchronizationResult;
import org.keycloak.testsuite.federation.DummyUserFederationProviderFactory;

import org.jboss.logging.Logger;

/**
 * 用于 {@code SyncFederationTest} 的虚拟用户联邦同步工厂。
 * <p>
 * 通过 {@link CountDownLatch} 协调测试与同步事务的时序，模拟用户变更同步场景。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class SyncDummyUserFederationProviderFactory extends DummyUserFederationProviderFactory {

    // 在 SyncFederationTest 中使用
    /** 同步任务已启动时递减，通知测试继续执行。 */
    public static volatile CountDownLatch latchStarted = new CountDownLatch(1);
    /** 同步事务内等待测试完成。 */
    public static volatile CountDownLatch latchWait = new CountDownLatch(1);
    /** 同步整体完成时递减，允许测试结束。 */
    public static volatile CountDownLatch latchFinished = new CountDownLatch(1);

    /** 重置所有门闩，供测试用例多次运行。 */
    public static void restartLatches() {
        latchStarted = new CountDownLatch(1);
        latchWait = new CountDownLatch(1);
        latchFinished = new CountDownLatch(1);
    }



    private static final Logger logger = Logger.getLogger(SyncDummyUserFederationProviderFactory.class);

    /** 同步提供者标识符。 */
    public static final String SYNC_PROVIDER_ID = "sync-dummy";
    /** 配置项：事务提交前的等待时间（秒）。 */
    public static final String WAIT_TIME = "wait-time"; // 事务提交前的 waitTime

    /** {@inheritDoc} 返回同步虚拟联邦提供者 ID。 */
    @Override
    public String getId() {
        return SYNC_PROVIDER_ID;
    }


    /** 返回包含重要配置与等待时间的提供者配置属性列表。 */
    public List<ProviderConfigProperty> getConfigProperties() {
        return ProviderConfigurationBuilder.create()
                .property().name("important.config")
                .type(ProviderConfigProperty.STRING_TYPE)
                .add()
                .property().name(WAIT_TIME)
                .type(ProviderConfigProperty.STRING_TYPE)
                .add()
                .build();
    }


    /**
     * 执行增量用户同步：删除并重建测试用户，并在事务内等待指定时间。
     *
     * @param lastSync 上次同步时间（本实现未使用）
     * @param sessionFactory Keycloak 会话工厂
     * @param realmId 领域 ID
     * @param model 用户存储提供者模型
     * @return 空的同步结果
     */
    @Override
    public SynchronizationResult syncSince(Date lastSync, KeycloakSessionFactory sessionFactory, String realmId, UserStorageProviderModel model) {
        if (latchStarted.getCount() <= 0) {
            logger.info("Already executed, returning");
            return SynchronizationResult.empty();
        }
        // 同步开始 => 允许测试继续
        latchStarted.countDown();

        KeycloakModelUtils.runJobInTransaction(sessionFactory, new KeycloakSessionTask() {

            @Override
            public void run(KeycloakSession session) {
                int waitTime = Integer.parseInt(model.getConfig().getFirst(WAIT_TIME));

                logger.infof("Starting sync of changed users. Wait time is: %s", waitTime);

                RealmModel realm = session.realms().getRealm(realmId);

                // KEYCLOAK-2412：为测试目的删除并重新添加若干用户
                for (int i = 0; i < 10; i++) {
                    String username = "dummyuser-" + i;
                    UserModel user = UserStoragePrivateUtil.userLocalStorage(session).getUserByUsername(realm, username);

                    if (user != null) {
                        UserStoragePrivateUtil.userLocalStorage(session).removeUser(realm, user);
                    }

                    user = UserStoragePrivateUtil.userLocalStorage(session).addUser(realm, username);
                }

                logger.infof("Finished sync of changed users. Waiting now for %d seconds", waitTime);


                try {
                    // 等待测试完成
                    latchWait.await(waitTime * 1000, TimeUnit.MILLISECONDS);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupted!", ie);
                }

                logger.infof("Finished waiting");
            }

        });

        // 递减，以便 SyncFederationTest 可以结束
        latchFinished.countDown();

        return SynchronizationResult.empty();
    }

}
