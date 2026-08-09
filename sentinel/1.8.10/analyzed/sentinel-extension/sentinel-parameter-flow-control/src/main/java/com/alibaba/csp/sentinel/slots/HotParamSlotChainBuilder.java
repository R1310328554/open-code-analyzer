/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.slots;

import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowSlot;

/**
 * @author Eric Zhao
 * @since 0.2.0
 *
 * @deprecated 自 1.7.2 起已废弃；可通过 {@link ParamFlowSlot} 上的 @Spi(order = -3000) 调整槽位顺序，
 * 本类仅为兼容旧版本保留。
 *
 * @see ParamFlowSlot
 * @see DefaultSlotChainBuilder
 */
@Deprecated
public class HotParamSlotChainBuilder extends DefaultSlotChainBuilder {

}
