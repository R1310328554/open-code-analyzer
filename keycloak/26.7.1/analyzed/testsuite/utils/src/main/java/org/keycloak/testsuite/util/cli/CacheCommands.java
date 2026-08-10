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

package org.keycloak.testsuite.util.cli;

import java.util.Map;
import java.util.Set;

import org.keycloak.connections.infinispan.InfinispanConnectionProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;

import org.infinispan.Cache;

/**
 * Infinispan 缓存相关的测试套件 CLI 命令集合。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class CacheCommands {

    /** 列出所有可用 Infinispan 缓存名称。 */
    public static class ListCachesCommand extends AbstractCommand {

        @Override
        public String getName() {
            return "listCaches";
        }

        @Override
        protected void doRunCommand(KeycloakSession session) {
            InfinispanConnectionProvider ispnProvider = session.getProvider(InfinispanConnectionProvider.class);
            Set<String> cacheNames = ispnProvider.getCache("realms").getCacheManager().getCacheNames();
            log.infof("Available caches: %s", cacheNames);
        }

    }


    /** 打印指定缓存的内容摘要。 */
    public static class GetCacheCommand extends AbstractCommand {

        @Override
        public String getName() {
            return "getCache";
        }

        @Override
        protected void doRunCommand(KeycloakSession session) {
            String cacheName = getArg(0);
            InfinispanConnectionProvider ispnProvider = session.getProvider(InfinispanConnectionProvider.class);
            Cache<Object, Object> cache = ispnProvider.getCache(cacheName);
            if (cache == null) {
                log.errorf("Cache '%s' doesn't exist", cacheName);
                throw new HandledException();
            }

            printCache(cache);
        }

        /** 输出缓存名称、大小及条目（大缓存跳过明细）。 */
        private void printCache(Cache<Object, Object> cache) {
            int size = cache.size();
            log.infof("Cache %s, size: %d", cache.getName(), size);

            if (size > 50) {
                // 缓存过大时跳过逐条打印
                log.info("Skip printing cache records due to big size");
            } else {
                for (Map.Entry<Object, Object> entry : cache.entrySet()) {
                    log.infof("%s=%s", entry.getKey(), entry.getValue());
                }
            }
        }

        @Override
        public String printUsage() {
            return super.printUsage() + " <cache-name> . cache-name is name of the infinispan cache provided by InfinispanConnectionProvider";
        }

    }


    /** 将指定 realm 的对象预热到缓存。 */
    public static class CacheRealmObjectsCommand extends AbstractCommand {

        @Override
        public String getName() {
            return "cacheRealmObjects";
        }

        @Override
        protected void doRunCommand(KeycloakSession session) {
            String realmName = getArg(0);
            RealmModel realm = session.realms().getRealmByName(realmName);
            if (realm == null) {
                log.errorf("Realm not found: %s", realmName);
                throw new HandledException();
            }

            TestCacheUtils.cacheRealmWithEverything(session, realmName);
        }

        @Override
        public String printUsage() {
            return super.printUsage() + " <realm-name>";
        }
    }
}
