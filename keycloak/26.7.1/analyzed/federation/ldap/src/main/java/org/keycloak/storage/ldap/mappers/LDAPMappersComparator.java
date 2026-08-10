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

package org.keycloak.storage.ldap.mappers;

import java.util.Comparator;

import org.keycloak.component.ComponentModel;
import org.keycloak.models.UserModel;
import org.keycloak.storage.ldap.LDAPConfig;

/**
 * LDAP 映射器排序比较器，确保用户属性映射器（尤其是 username）按稳定、可预期的顺序执行。
 *
 * TODO: 可考虑改为可配置的 priority 而非硬编码规则
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class LDAPMappersComparator {

    private LDAPConfig ldapConfig;

    public LDAPMappersComparator(LDAPConfig ldapConfig) {
        this.ldapConfig = ldapConfig;
    }

    /** 返回升序比较器：重要映射器优先。 */
    public Comparator<ComponentModel> sortAsc() {
        return new ImportantFirstComparator(ldapConfig);
    }

    /** 返回降序比较器。 */
    public Comparator<ComponentModel> sortDesc() {
        return new ImportantFirstComparator(ldapConfig).reversed();
    }


    /** 按映射器类型与 username 相关配置决定执行优先级的内部比较器。 */
    private static class ImportantFirstComparator implements Comparator<ComponentModel> {

        private final LDAPConfig ldapConfig;

        public ImportantFirstComparator(LDAPConfig ldapConfig) {
            this.ldapConfig = ldapConfig;
        }

        @Override
        public int compare(ComponentModel o1, ComponentModel o2) {
            // UserAttributeLDAPFederationMapper 优先
            boolean isO1AttrMapper = o1.getProviderId().equals(UserAttributeLDAPStorageMapperFactory.PROVIDER_ID);
            boolean isO2AttrMapper = o2.getProviderId().equals(UserAttributeLDAPStorageMapperFactory.PROVIDER_ID);
            if (!isO1AttrMapper) {
                if (isO2AttrMapper) {
                    return 1;
                } else {
                    return compareWithStableOrdering(o1, o2);
                }
            } else if (!isO2AttrMapper) {
                return -1;
            }

            // 映射 username 属性的映射器优先
            String model1 = o1.getConfig().getFirst(UserAttributeLDAPStorageMapper.USER_MODEL_ATTRIBUTE);
            String model2 = o2.getConfig().getFirst(UserAttributeLDAPStorageMapper.USER_MODEL_ATTRIBUTE);
            boolean isO1UsernameMapper = model1 != null && model1.equalsIgnoreCase(UserModel.USERNAME);
            boolean isO2UsernameMapper = model2 != null && model2.equalsIgnoreCase(UserModel.USERNAME);
            if (!isO1UsernameMapper) {
                if (isO2UsernameMapper) {
                    return 1;
                } else {
                    return compareWithStableOrdering(o1, o2);
                }
            } else if (!isO2UsernameMapper) {
                return -1;
            }

            // 与联邦提供者 username LDAP 属性一致的 username 映射器最优先
            String o1LdapAttr = o1.getConfig().getFirst(UserAttributeLDAPStorageMapper.LDAP_ATTRIBUTE);
            String o2LdapAttr = o2.getConfig().getFirst(UserAttributeLDAPStorageMapper.LDAP_ATTRIBUTE);
            boolean isO1LdapAttr = o1LdapAttr != null && ldapConfig.getUsernameLdapAttribute().equalsIgnoreCase(o1LdapAttr);
            boolean isO2LdapAttr = o2LdapAttr != null && ldapConfig.getUsernameLdapAttribute().equalsIgnoreCase(o2LdapAttr);

            if (!isO1LdapAttr) {
                if (isO2LdapAttr) {
                    return 1;
                } else {
                    return compareWithStableOrdering(o1, o2);
                }
            } else if (!isO2LdapAttr) {
                return -1;
            }

            return compareWithStableOrdering(o1, o2);
        }

        /**
         * 保证稳定排序，使映射器始终以相同顺序执行，
         * 避免因并发修改属性顺序不同而触发数据库死锁。
         */
        private static int compareWithStableOrdering(ComponentModel o1, ComponentModel o2) {
            return o1.getId().compareTo(o2.getId());
        }

    }


}
