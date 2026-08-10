/*
 * Copyright 1999-2026 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.api.ai.listener;

/**
 * Nacos AI 模块 Skill 变更事件监听器抽象基类。
 *
 * <p>继承此类即可接收 {@link NacosSkillEvent} 推送，
 * 事件载荷含 Skill ZIP 字节与内容 MD5 指纹，便于本地缓存与增量对比。</p>
 *
 * @author nacos
 * @since 3.2.0
 */
public abstract class AbstractNacosSkillListener implements NacosAiListener<NacosSkillEvent> {
}
