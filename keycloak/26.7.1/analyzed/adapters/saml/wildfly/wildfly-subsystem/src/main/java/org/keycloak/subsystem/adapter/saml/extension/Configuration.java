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
package org.keycloak.subsystem.adapter.saml.extension;

import java.util.List;

import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.web.common.WarMetaData;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.Property;
import org.jboss.metadata.web.jboss.JBossWebMetaData;

/**
 * Keycloak SAML 适配器子系统的内存配置树单例。
 *
 * <p>管理 CLI/XML 操作写入的 {@link ModelNode} 配置，并在部署阶段按 WAR 名称
 * 查找 {@code secure-deployment} 节点。</p>
 *
 * @author <a href="mailto:mstrukel@redhat.com">Marko Strukelj</a>
 */
public class Configuration {

    /** 全局配置单例。 */
    static Configuration INSTANCE = new Configuration();

    /** 子系统配置树根节点。 */
    private ModelNode config = new ModelNode();

    private Configuration() {
    }

    /** 将操作模型合并到配置树（不检查单例冲突）。 */
    void updateModel(ModelNode operation, ModelNode model) throws OperationFailedException {
        this.updateModel(operation, model, false);
    }

    /**
     * 沿操作地址路径将模型写入配置树。
     *
     * @param operation       管理操作
     * @param model           待写入的模型节点
     * @param checkSingleton  为 true 时，地址末级资源已存在则抛出重复资源异常
     * @throws OperationFailedException 重复资源或写入失败
     */
    void updateModel(final ModelNode operation, final ModelNode model, final boolean checkSingleton) throws OperationFailedException {
        ModelNode node = config;

        final List<Property> addressNodes = operation.get("address").asPropertyList();
        final int lastIndex = addressNodes.size() - 1;
        for (int i = 0; i < addressNodes.size(); i++) {
            Property addressNode = addressNodes.get(i);
            // checkSingleton 为 true 时，校验地址末级（如 SP 或 IDP）是否已定义
            if (i == lastIndex && checkSingleton) {
                if (node.get(addressNode.getName()).isDefined()) {
                    // 发现已存在资源，抛出异常
                    throw new OperationFailedException("Duplicate resource: " + addressNode.getName());
                }
            }
            node = node.get(addressNode.getName()).get(addressNode.getValue().asString());
        }
        node.set(model);
    }

    /**
     * 获取指定部署单元的 secure-deployment 配置节点。
     *
     * @param deploymentUnit WildFly 部署单元
     * @return 配置节点，未找到时返回 null
     */
    public ModelNode getSecureDeployment(DeploymentUnit deploymentUnit) {
        String name = preferredDeploymentName(deploymentUnit);
        ModelNode secureDeployment = config.get("subsystem").get("keycloak-saml").get(Constants.Model.SECURE_DEPLOYMENT);
        if (secureDeployment.hasDefined(name)) {
            return secureDeployment.get(name);
        }
        return null;
    }
    
    // KEYCLOAK-3273：优先使用 web 模块名作为部署标识
    /** 解析部署在 secure-deployment 中的键名（优先 moduleName.war）。 */
    private String preferredDeploymentName(DeploymentUnit deploymentUnit) {
        String deploymentName = deploymentUnit.getName();
        WarMetaData warMetaData = deploymentUnit.getAttachment(WarMetaData.ATTACHMENT_KEY);
        if (warMetaData == null) {
            return deploymentName;
        }
        
        JBossWebMetaData webMetaData = warMetaData.getMergedJBossWebMetaData();
        if (webMetaData == null) {
            return deploymentName;
        }
        
        String moduleName = webMetaData.getModuleName();
        if (moduleName != null) return moduleName + ".war";
        
        return deploymentName;
    }
}
