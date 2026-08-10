/*
 * Copyright 1999-2023 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.nacos.plugin.control.rule.storage;

/**
 * 外部规则存储标记接口，扩展 {@link RuleStorage} 以对接远端或集群配置中心。
 *
 * <p>由 SPI 构建并在 {@link RuleStorageProxy} 中与本地磁盘存储协同使用。</p>
 *
 * @author shiyiyue
 */
public interface ExternalRuleStorage extends RuleStorage {
    
}
