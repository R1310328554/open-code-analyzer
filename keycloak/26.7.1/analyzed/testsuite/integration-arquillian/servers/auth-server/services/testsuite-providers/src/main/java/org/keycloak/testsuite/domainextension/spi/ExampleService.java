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

package org.keycloak.testsuite.domainextension.spi;

import java.util.List;

import org.keycloak.provider.Provider;
import org.keycloak.testsuite.domainextension.CompanyRepresentation;

/**
 * 域扩展示例服务接口，定义公司实体的持久化与查询操作。
 */
public interface ExampleService extends Provider {

    /** @return 当前 Realm 下的全部公司列表 */
    List<CompanyRepresentation> listCompanies();

    /**
     * 按标识查找公司。
     *
     * @param id 公司标识
     * @return 匹配的公司表示对象，不存在时可为 null
     */
    CompanyRepresentation findCompany(String id);

    /**
     * 新增或更新公司记录。
     *
     * @param company 公司表示对象
     * @return 持久化后的公司表示对象
     */
    CompanyRepresentation addCompany(CompanyRepresentation company);

    /** 删除当前 Realm 下的全部公司记录。 */
    void deleteAllCompanies();

}
