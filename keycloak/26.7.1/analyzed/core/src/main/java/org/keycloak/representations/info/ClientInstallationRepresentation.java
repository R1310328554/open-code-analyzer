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

package org.keycloak.representations.info;

/**
 * 客户端安装/适配器配置的 REST 表示，描述可下载或展示的客户端安装选项。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class ClientInstallationRepresentation {
    /** 安装选项 ID。 */
    protected String id;
    /** 关联协议（如 openid-connect、saml）。 */
    protected String protocol;
    /** 是否仅提供下载（不可在线展示）。 */
    protected boolean downloadOnly;
    /** 安装选项显示类型名称。 */
    protected String displayType;
    /** 帮助说明文本。 */
    protected String helpText;
    /** 下载文件名。 */
    protected String filename;
    /** 内容 MIME 类型。 */
    protected String mediaType;

    /** @return 安装选项 ID */
    public String getId() {
        return id;
    }

    /** @param id 安装选项 ID */
    public void setId(String id) {
        this.id = id;
    }

    /** @return 协议名称 */
    public String getProtocol() {
        return protocol;
    }

    /** @param protocol 协议名称 */
    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    /** @return 是否仅下载 */
    public boolean isDownloadOnly() {
        return downloadOnly;
    }

    /** @param downloadOnly 是否仅下载 */
    public void setDownloadOnly(boolean downloadOnly) {
        this.downloadOnly = downloadOnly;
    }

    /** @return 显示类型名称 */
    public String getDisplayType() {
        return displayType;
    }

    /** @param displayType 显示类型名称 */
    public void setDisplayType(String displayType) {
        this.displayType = displayType;
    }

    /** @return 帮助说明文本 */
    public String getHelpText() {
        return helpText;
    }

    /** @param helpText 帮助说明文本 */
    public void setHelpText(String helpText) {
        this.helpText = helpText;
    }

    /** @return 下载文件名 */
    public String getFilename() {
        return filename;
    }

    /** @param filename 下载文件名 */
    public void setFilename(String filename) {
        this.filename = filename;
    }

    /** @return 内容 MIME 类型 */
    public String getMediaType() {
        return mediaType;
    }

    /** @param mediaType 内容 MIME 类型 */
    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }
}
