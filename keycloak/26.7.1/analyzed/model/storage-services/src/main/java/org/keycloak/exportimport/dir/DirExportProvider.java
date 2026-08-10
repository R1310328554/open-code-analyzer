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

package org.keycloak.exportimport.dir;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.keycloak.exportimport.util.ExportUtils;
import org.keycloak.exportimport.util.MultipleStepsExportProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.services.resources.KeycloakApplication;
import org.keycloak.util.JsonSerialization;

/**
 * 目录导出 Provider：将 realm 与用户分文件写入指定目录（多步导出流程）。
 * <p>
 * 默认导出到平台临时目录下的 {@code keycloak-export}，亦可通过 {@link #withDir(String)} 指定目标路径。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class DirExportProvider extends MultipleStepsExportProvider<DirExportProvider> {
    
    /** 用户配置的导出根目录路径。 */
    private String dir; 
    
    /** 解析后的导出根目录 {@link File}。 */
    private File rootDirectory;

    /** 使用会话工厂初始化多步导出基类。 */
    public DirExportProvider(KeycloakSessionFactory sessionFactory) {
        super(sessionFactory);
    }
    
    /** 懒加载并创建导出根目录。 */
    private File getRootDirectory() {
        if (rootDirectory == null) {
            if (dir == null) {
                rootDirectory = new File(KeycloakApplication.getTmpDirectory(), "keycloak-export");
            } else {
                rootDirectory = new File(dir);
            }
            rootDirectory.mkdirs();
            logger.infof("Exporting into directory %s", rootDirectory.getAbsolutePath());
        }
        return rootDirectory;
    }

    /** 递归删除目录及其内容。 */
    public static boolean recursiveDeleteDir(File dirPath) {
        if (dirPath.exists()) {
            File[] files = dirPath.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    recursiveDeleteDir(files[i]);
                } else {
                    files[i].delete();
                }
            }
        }
        if (dirPath.exists())
            return dirPath.delete();
        else
            return true;
    }

    /** 将 realm JSON 表示写入目录下的指定文件名。 */
    @Override
    public void writeRealm(String fileName, RealmRepresentation rep) throws IOException {
        File file = new File(getRootDirectory(), fileName);
        try (FileOutputStream is = new FileOutputStream(file)) {
            JsonSerialization.prettyMapper.writeValue(is, rep);
        }
    }

    /** 将用户批次导出为单独的 JSON 文件。 */
    @Override
    protected void writeUsers(String fileName, KeycloakSession session, RealmModel realm, List<UserModel> users) throws IOException {
        File file = new File(getRootDirectory(), fileName);
        FileOutputStream os = new FileOutputStream(file);
        ExportUtils.exportUsersToStream(session, realm, users, JsonSerialization.prettyMapper, os);
    }

    /** 将联邦用户 ID 批次导出为单独的 JSON 文件。 */
    @Override
    protected void writeFederatedUsers(String fileName, KeycloakSession session, RealmModel realm, List<String> users) throws IOException {
        File file = new File(getRootDirectory(), fileName);
        FileOutputStream os = new FileOutputStream(file);
        ExportUtils.exportFederatedUsersToStream(session, realm, users, JsonSerialization.prettyMapper, os);
    }

    @Override
    public void close() {
    }

    /** 设置导出目标目录并返回 {@code this} 以支持链式配置。 */
    public DirExportProvider withDir(String dir) {
        this.dir = dir;
        return this;
    }

}
