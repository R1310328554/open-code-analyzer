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

import java.util.List;

import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.KeycloakSessionTask;
import org.keycloak.models.utils.KeycloakModelUtils;

import org.jboss.logging.Logger;

/**
 * 测试套件 CLI 命令抽象基类，在 Keycloak 事务中执行命令逻辑。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public abstract class AbstractCommand {

    /** 命令日志记录器。 */
    protected final Logger log = Logger.getLogger(this.getClass().getName());

    /** 命令行参数列表。 */
    protected List<String> args;
    /** Keycloak 会话工厂。 */
    protected KeycloakSessionFactory sessionFactory;

    /** 注入 CLI 参数与会话工厂。 */
    public void injectProperties(List<String> args, TestsuiteCLI cli, KeycloakSessionFactory sessionFactory) {
        this.args = args;
        this.sessionFactory = sessionFactory;
    }

    /** 在事务中执行命令，捕获已处理异常。 */
    public void runCommand() {
        try {
            KeycloakModelUtils.runJobInTransaction(sessionFactory, new KeycloakSessionTask() {

                @Override
                public void run(KeycloakSession session) {
                    doRunCommand(session);
                }

            });
        } catch (HandledException handled) {
            // 已处理异常，可忽略
        } catch (RuntimeException e) {
            log.error("Error occurred during command. ", e);
        }
    }

    /** 返回命令名称。 */
    public abstract String getName();
    /** 在会话上下文中执行命令核心逻辑。 */
    protected abstract void doRunCommand(KeycloakSession session);

    /** 获取指定索引的命令行参数，缺失时打印用法并抛出 {@link HandledException}。 */
    protected String getArg(int index) {
        try {
            return args.get(index);
        } catch (IndexOutOfBoundsException ex) {
            log.errorf("Usage: %s", printUsage());
            throw new HandledException();
        }
    }

    /** 获取整型命令行参数。 */
    protected Integer getIntArg(int index) {
        String str = getArg(index);
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException nex) {
            log.errorf("Usage: %s", printUsage());
            throw new HandledException();
        }
    }

    /** 返回命令用法说明。 */
    public String printUsage() {
        return getName();
    }

    /** 表示命令错误已记录日志、无需再次抛出的异常。 */
    public static class HandledException extends RuntimeException {
    }

}
