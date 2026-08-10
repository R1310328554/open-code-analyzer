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
package org.keycloak.client.cli.config;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.keycloak.util.JsonSerialization;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Keycloak 客户端 CLI 配置文件的数据模型。
 * <p>
 * 包含服务器 URL、当前 realm、truststore 及按 endpoint/realm 分层的 {@link RealmConfigData} 映射。
 * 支持合并、深拷贝及 JSON 序列化。
 *
 * @author <a href="mailto:mstrukel@redhat.com">Marko Strukelj</a>
 */
public class ConfigData {

    /** 外部注入的访问令牌（不持久化到配置文件）。 */
    @JsonIgnore
    private String externalToken;

    /** Keycloak 服务器基础 URL。 */
    private String serverUrl;

    /** 当前会话 realm 名称。 */
    private String realm;

    /** truststore 文件路径。 */
    private String truststore;

    /** truststore 密码。 */
    private String trustpass;

    /** 外部编辑器命令（用于 config editor 子命令）。 */
    private String editor;

    /** endpoint → (realm → 领域配置) 的嵌套映射。 */
    private Map<String, Map<String, RealmConfigData>> endpoints = new HashMap<>();


    /** 获取服务器 URL。 */
    public String getServerUrl() {
        return serverUrl;
    }

    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    @JsonIgnore
    public String getExternalToken() {
        return externalToken;
    }

    @JsonIgnore
    public void setExternalToken(String externalToken) {
        this.externalToken = externalToken;
    }

    public String getRealm() {
        return realm;
    }

    public void setRealm(String realm) {
        this.realm = realm;
    }

    public String getTruststore() {
        return truststore;
    }

    public void setTruststore(String truststore) {
        this.truststore = truststore;
    }

    public String getTrustpass() {
        return trustpass;
    }

    public void setTrustpass(String trustpass) {
        this.trustpass = trustpass;
    }

    public String getEditor() {
        return editor;
    }

    public void setEditor(String editor) {
        this.editor = editor;
    }

    public Map<String, Map<String, RealmConfigData>> getEndpoints() {
        return endpoints;
    }

    public void setEndpoints(Map<String, Map<String, RealmConfigData>> endpoints) {
        for (Map.Entry<String, Map<String, RealmConfigData>> entry: endpoints.entrySet()) {
            String endpoint = entry.getKey();
            for (Map.Entry<String, RealmConfigData> sub: entry.getValue().entrySet()) {
                RealmConfigData rdata = sub.getValue();
                rdata.serverUrl(endpoint);
                rdata.realm(sub.getKey());
            }
        }
        this.endpoints = endpoints;
    }

    /** 获取当前会话（{@code serverUrl} + {@code realm}）对应的领域配置。 */
    public RealmConfigData sessionRealmConfigData() {
        if (serverUrl == null)
            throw new RuntimeException("Illegal state - no current endpoint in config data");
        if (realm == null)
            throw new RuntimeException("Illegal state - no current realm in config data");
        return ensureRealmConfigData(serverUrl, realm);
    }

    public RealmConfigData getRealmConfigData(String endpoint, String realm) {
        Map<String, RealmConfigData> realmData = endpoints.get(endpoint);
        if (realmData == null) {
            return null;
        }
        return realmData.get(realm);
    }

    /** 获取当前会话 realm 的配置；不存在时自动创建空条目。 */
    public RealmConfigData ensureRealmConfigData(String endpoint, String realm) {
        RealmConfigData result = getRealmConfigData(endpoint, realm);
        if (result == null) {
            result = new RealmConfigData();
            result.serverUrl(endpoint);
            result.realm(realm);
            setRealmConfigData(result);
        }
        return result;
    }


    public void setRealmConfigData(RealmConfigData data) {
        Map<String, RealmConfigData> realm = endpoints.get(data.serverUrl());
        if (realm == null) {
            realm = new HashMap<>();
            endpoints.put(data.serverUrl(), realm);
        }
        realm.put(data.realm(), data);
    }

    /** 将源配置的全局字段及当前 realm 数据合并到本实例。 */
    public void merge(ConfigData source) {
        serverUrl = source.serverUrl;
        realm = source.realm;
        truststore = source.truststore;
        trustpass = source.trustpass;
        editor = source.editor;

        RealmConfigData current = getRealmConfigData(serverUrl, realm);
        RealmConfigData sourceRealm = source.getRealmConfigData(serverUrl, realm);

        if (current == null) {
            setRealmConfigData(sourceRealm);
        } else {
            current.merge(sourceRealm);
        }
    }

    /** 深拷贝配置，包括所有 endpoint/realm 下的 {@link RealmConfigData}。 */
    public ConfigData deepcopy() {
        ConfigData data = new ConfigData();
        data.serverUrl = serverUrl;
        data.realm = realm;
        data.truststore = truststore;
        data.trustpass = trustpass;
        data.editor = editor;
        data.endpoints = new HashMap<>();

        for (Map.Entry<String, Map<String, RealmConfigData>> item: endpoints.entrySet()) {

            Map<String, RealmConfigData> nuitems = new HashMap<>();
            Map<String, RealmConfigData> curitems = item.getValue();

            if (curitems != null) {
                for (Map.Entry<String, RealmConfigData> ditem : curitems.entrySet()) {
                    RealmConfigData nudata = ditem.getValue();
                    if (nudata != null) {
                        nuitems.put(ditem.getKey(), nudata.deepcopy());
                    }
                }
                data.endpoints.put(item.getKey(), nuitems);
            }
        }
        return data;
    }

    @Override
    public String toString() {
        try {
            return JsonSerialization.writeValueAsPrettyString(this);
        } catch (IOException e) {
            return super.toString() + " - Error: " + e.toString();
        }
    }
}
