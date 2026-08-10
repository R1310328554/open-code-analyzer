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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RoleModel;
import org.keycloak.models.UserCredentialModel;
import org.keycloak.models.UserModel;

/**
 * 测试套件 CLI 中的用户管理命令集合（创建、删除、计数、查询）。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class UserCommands {

    /** 批量创建用户并可选分配领域/客户端角色的命令。 */
    public static class Create extends AbstractCommand {

        private String usernamePrefix;
        private String password;
        private String realmName;
        private String roleNames;

        @Override
        public String getName() {
            return "createUsers";
        }

        /**
         * 按批次创建带前缀的用户，设置密码、邮箱并授予指定角色。
         * 参数：用户名前缀、密码、领域、起始偏移、总数、批次大小、逗号分隔角色列表。
         */
        @Override
        protected void doRunCommand(KeycloakSession session) {
            usernamePrefix = getArg(0);
            password = getArg(1);
            realmName = getArg(2);
            int first = getIntArg(3);
            int count = getIntArg(4);
            int batchCount = getIntArg(5);
            roleNames = getArg(6);

            BatchTaskRunner.runInBatches(first, count, batchCount, session.getKeycloakSessionFactory(), this::createUsersInBatch);

            log.infof("Command finished. All users from %s to %s created", usernamePrefix + first, usernamePrefix + (first + count - 1));
        }

        /** 在单个批次事务中创建连续编号的用户。 */
        private void createUsersInBatch(KeycloakSession session, int first, int count) {
            RealmModel realm = session.realms().getRealmByName(realmName);
            if (realm == null) {
                log.errorf("Unknown realm: %s", realmName);
                throw new HandledException();
            }

            Set<RoleModel> roles = findRoles(realm, roleNames);

            int last = first + count;
            for (int counter = first; counter < last; counter++) {
                String username = usernamePrefix + counter;
                UserModel user = session.users().addUser(realm, username);
                user.setEnabled(true);
                user.setEmail(username + "@keycloak.org");
                UserCredentialModel passwordCred = UserCredentialModel.password(password);
                user.credentialManager().updateCredential(passwordCred);

                for (RoleModel role : roles) {
                    user.grantRole(role);
                }
            }
            log.infof("Users from %s to %s created", usernamePrefix + first, usernamePrefix + (last - 1));
        }

        @Override
        public String printUsage() {
            return super.printUsage() + " <username-prefix> <password> <realm-name> <starting-user-offset> <total-count> <batch-size> <realm-roles-list>. " +
                    "\n'total-count' refers to total count of newly created users. 'batch-size' refers to number of created users in each transaction. 'starting-user-offset' refers to starting username offset." +
                    "\nFor example if 'starting-user-offset' is 15 and total-count is 10 and username-prefix is 'test', it will create users test15, test16, test17, ... , test24" +
                    "\nRoles list is divided by comma (client roles are referenced with format <client-id>/<role-name> )>\n" +
                    "Example usage: " + super.printUsage() + " test test demo 0 500 100 user,admin";
        }

        /** 解析逗号分隔的角色列表，支持 {@code clientId/roleName} 格式的客户端角色。 */
        private Set<RoleModel> findRoles(RealmModel realm, String rolesList) {
            Set<RoleModel> result = new HashSet<>();

            String[] roles = rolesList.split(",");
            for (String roleName : roles) {
                roleName = roleName.trim();
                RoleModel role;
                if (roleName.contains("/")) {
                    String[] spl = roleName.split("/");
                    ClientModel client = realm.getClientByClientId(spl[0]);
                    if (client == null) {
                        log.errorf("Client not found: %s", spl[0]);
                        throw new HandledException();
                    }
                    role = client.getRole(spl[1]);
                } else {
                    role = realm.getRole(roleName);
                }

                if (role == null) {
                    log.errorf("Role not found: %s", roleName);
                    throw new HandledException();
                }

                result.add(role);
            }

            return result;
        }

    }


    /** 按前缀与偏移批量删除用户的命令。 */
    public static class Remove extends AbstractCommand {

        @Override
        public String getName() {
            return "removeUsers";
        }

        /** 删除指定范围内带前缀的用户，不存在时记录错误。 */
        @Override
        protected void doRunCommand(KeycloakSession session) {
            String usernamePrefix = getArg(0);
            String realmName = getArg(1);
            int first = getIntArg(2);
            int count = getIntArg(3);

            RealmModel realm = session.realms().getRealmByName(realmName);
            if (realm == null) {
                log.errorf("Unknown realm: %s", realmName);
                return;
            }

            int last = first + count;
            for (int counter = first; counter < last; counter++) {
                String username = usernamePrefix + counter;
                UserModel user = session.users().getUserByUsername(realm, username);
                if (user == null) {
                    log.errorf("User '%s' not found", username);
                } else {
                    session.users().removeUser(realm, user);
                }
            }
            log.infof("Users from %s to %s removed", usernamePrefix + first, usernamePrefix + (last - 1));
        }

        @Override
        public String printUsage() {
            return super.printUsage() + " <username-prefix> <realm-name> <starting-user-offset> <count> \n" +
                    "Example usage: " + super.printUsage() + " test demo 0 20";
        }
    }


    /** 统计领域中用户总数的命令。 */
    public static class Count extends AbstractCommand {

        @Override
        public String getName() {
            return "getUsersCount";
        }

        /** 输出指定领域的 {@code getUsersCount} 结果。 */
        @Override
        protected void doRunCommand(KeycloakSession session) {
            String realmName = getArg(0);
            RealmModel realm = session.realms().getRealmByName(realmName);
            if (realm == null) {
                log.errorf("Unknown realm: %s", realmName);
                return;
            }

            int usersCount = session.users().getUsersCount(realm);
            log.infof("Users count in realm %s: %d", realmName, usersCount);
        }

        @Override
        public String printUsage() {
            return super.printUsage() + " <realm-name>";
        }
    }


    /** 按用户名查询用户详情及角色映射的命令。 */
    public static class GetUser extends AbstractCommand {

        @Override
        public String getName() {
            return "getUser";
        }

        /** 查询指定用户并输出 ID、邮箱及角色映射列表。 */
        @Override
        protected void doRunCommand(KeycloakSession session) {
            String realmName = getArg(0);
            String username = getArg(1);
            RealmModel realm = session.realms().getRealmByName(realmName);
            if (realm == null) {
                log.errorf("Unknown realm: %s", realmName);
                return;
            }

            UserModel user = session.users().getUserByUsername(realm, username);
            if (user == null) {
                log.infof("User '%s' doesn't exist in realm '%s'", username, realmName);
            } else {
                List<String> roleMappings = getRoleMappings(user);
                log.infof("User: ID: '%s', username: '%s', mail: '%s', roles: '%s'", user.getId(), user.getUsername(), user.getEmail(), roleMappings.toString());
            }
        }

        /** 收集用户的领域角色与 {@code clientId/role} 格式的客户端角色名称。 */
        private List<String> getRoleMappings(UserModel user) {
            return user.getRoleMappingsStream()
                    .map(role -> {
                        if (role.getContainer() instanceof RealmModel)
                            return role.getName();
                        else {
                            ClientModel client = (ClientModel) role.getContainer();
                            return  client.getClientId() + "/" + role.getName();
                        }
                    })
                    .collect(Collectors.toList());
        }

        @Override
        public String printUsage() {
            return super.printUsage() + " <realm-name> <username>";
        }
    }
}
