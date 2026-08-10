/*
 * Copyright 2022 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.operator;

import java.util.Map;

import io.fabric8.kubernetes.api.model.Quantity;
import io.smallrye.config.ConfigMapping;

/**
 * Keycloak Operator 运行时配置映射，前缀 {@code kc.operator}。
 *
 * <p>通过 SmallRye Config 从环境变量或配置文件注入，控制默认镜像、轮询间隔与资源限制等。
 *
 * @author Vaclav Muzikar <vmuzikar@redhat.com>
 */
@ConfigMapping(prefix = "kc.operator")
public interface Config {

    /** Keycloak 部署相关配置。 */
    Keycloak keycloak();

    /** Keycloak 部署子配置。 */
    interface Keycloak {
        /** 默认 Keycloak 容器镜像。 */
        String image();
        /** 镜像拉取策略（如 Always、IfNotPresent）。 */
        String imagePullPolicy();
        /** 是否以优化模式启动 Keycloak。 */
        boolean startOptimized();
        /** 协调循环轮询间隔（秒）。 */
        int pollIntervalSeconds();
        /** Pod 更新操作的超时上限（秒）。 */
        long updatePodDeadlineSeconds();

        /** Pod 资源请求与限制。 */
        ResourceRequirements resources();
        /** 附加到 Keycloak Pod 的标签。 */
        Map<String, String> podLabels();
    }

    /** Pod 资源需求配置。 */
    interface ResourceRequirements {
        /** 资源请求。 */
        Resources requests();
        /** 资源上限。 */
        Resources limits();

        /** 单项资源量（当前仅内存）。 */
        interface Resources {
            /** 内存用量。 */
            Quantity memory();
        }
    }
}
