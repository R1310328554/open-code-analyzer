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

package org.keycloak.exportimport;

import java.io.IOException;

import org.keycloak.provider.Provider;

/**
 * 领域导入提供者 SPI：从导出文件恢复 Keycloak 模型数据。
 * <p>启动时或管理 API 触发导入流程时由 {@link ImportProviderFactory} 实例化。</p>
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public interface ImportProvider extends Provider {

    /** 执行完整导入：读取导出目录并写入领域/用户等模型。 */
    void importModel() throws IOException;

    /**
     * 待导入数据中是否包含先前导出的 master 领域。
     * @return true, if master realm was previously exported and is available in the data to be imported
     */
    boolean isMasterRealmExported() throws IOException;
}
