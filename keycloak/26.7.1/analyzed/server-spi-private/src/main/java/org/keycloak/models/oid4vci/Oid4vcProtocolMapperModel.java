/*
 * Copyright 2025 Red Hat, Inc. and/or its affiliates
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
 *
 */

package org.keycloak.models.oid4vci;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.keycloak.models.ProtocolMapperModel;

/**
 * {@link ProtocolMapperModel} 的 OID4VCI 协议映射器委托。
 * <p>扩展 claim 路径、是否必填及元数据显示等 VC 专用配置。</p>
 *
 * @author Pascal Knüppel
 */
public class Oid4vcProtocolMapperModel extends ProtocolMapperModel {

    /** claim 在凭证中的点分路径配置键。 */
    public static final String PATH = "claim.name"; // TODO 讨论是否可重命名（会破坏现有安装）
    /** 该 claim 是否为 VC 必填项。 */
    public static final String MANDATORY = "vc.mandatory";
    /** 元数据端点中的 claim 显示文本。 */
    public static final String DISPLAY = "vc.display";

    private final ProtocolMapperModel protocolMapper;

    public Oid4vcProtocolMapperModel(ProtocolMapperModel protocolMapper) {
        this.protocolMapper = protocolMapper;
    }

    /**
     * 返回 claim 在凭证 JSON 中的点分路径段列表。
     * @return the path of the attribute where it can be extracted
     */
    public List<String> getPath()
    {
        return Optional.ofNullable(protocolMapper.getConfig().get(PATH))
                       .map(s -> s.split("\\."))
                       .map(Arrays::asList)
                       .orElse(Collections.emptyList());
    }

    public void setPath(List<String> path) {
        protocolMapper.getConfig().put(PATH, Optional.ofNullable(path)
                                                     .map(l -> String.join(".", l))
                                                     .orElse(null));
    }

    /** 该映射 claim 是否标记为必填。 */
    public boolean isMandatory()
    {
        return Optional.ofNullable(protocolMapper.getConfig().get(MANDATORY)).map(Boolean::valueOf).orElse(false);
    }

    public void setMandatory(Boolean mandatory)
    {
        if (mandatory == null) {
            protocolMapper.getConfig().remove(MANDATORY);
        }else {
            protocolMapper.getConfig().put(MANDATORY, String.valueOf(mandatory));
        }
    }

    /** 获取元数据展示用显示名。 */
    public String getDisplay()
    {
        return protocolMapper.getConfig().get(DISPLAY);
    }

    public void setDisplay(String display)
    {
        protocolMapper.getConfig().put(DISPLAY, display);
    }

    @Override
    public String getId() {
        return protocolMapper.getId();
    }

    @Override
    public void setId(String id) {
        protocolMapper.setId(id);
    }

    @Override
    public String getName() {
        return protocolMapper.getName();
    }

    @Override
    public void setName(String name) {
        protocolMapper.setName(name);
    }

    @Override
    public String getProtocol() {
        return protocolMapper.getProtocol();
    }

    @Override
    public void setProtocol(String protocol) {
        protocolMapper.setProtocol(protocol);
    }

    @Override
    public String getProtocolMapper() {
        return protocolMapper.getProtocolMapper();
    }

    @Override
    public void setProtocolMapper(String protocolMapper) {
        this.protocolMapper.setProtocolMapper(protocolMapper);
    }

    @Override
    public Map<String, String> getConfig() {
        return protocolMapper.getConfig();
    }

    @Override
    public void setConfig(Map<String, String> config) {
        protocolMapper.setConfig(config);
    }

    @Override
    public boolean equals(Object obj) {
        return protocolMapper.equals(obj);
    }

    @Override
    public int hashCode() {
        return protocolMapper.hashCode();
    }
}
