//
//  Copyright 2026 The InfiniFlow Authors. All Rights Reserved.
//
//  Licensed under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License.
//  You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.
//

package component

// idempotency.go — 组件执行幂等键：task/pipeline/组件版本与输入指纹。


// IdempotencyKey 标识一次组件执行，用于去重与缓存键构造。
type IdempotencyKey struct {
	TaskID           string
	PipelineVersion  string
	ComponentName    string
	ComponentVersion string
	InputFingerprint string
}

func (k IdempotencyKey) String() string {
	return k.TaskID + "|" + k.PipelineVersion + "|" + k.ComponentName + "|" + k.ComponentVersion + "|" + k.InputFingerprint
}

// String() 用竖线拼接各字段，形成稳定的幂等标识符。
