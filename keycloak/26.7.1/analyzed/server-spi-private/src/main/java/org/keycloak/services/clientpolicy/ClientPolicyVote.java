/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.services.clientpolicy;

/**
 * 客户端策略投票枚举：表示条件评估器对策略条件的表决结果。
 * <p>{@link #YES} 满足条件，{@link #NO} 不满足，{@link #ABSTAIN} 弃权。</p>
 *
 * @author <a href="mailto:takashi.norimatsu.ws@hitachi.com">Takashi Norimatsu</a>
 */
public enum ClientPolicyVote {
    /** 条件满足 */
    YES,
    /** 条件不满足 */
    NO,
    /** 弃权（不参与表决） */
    ABSTAIN
}
