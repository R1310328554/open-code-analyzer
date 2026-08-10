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
// ocr.go — OCR 远程桩：Python deepdoc 无远程 OCR 端点，本方法始终返回 ErrNoRemoteEndpoint。

//

package deepdoc

import "context"

// OCR 桩实现：Python OCR 为本地 ONNX，Go 客户端仅服务 DLA；无条件返回 ErrNoRemoteEndpoint，避免静默失败。
func (c *Client) OCR(_ context.Context, _ [][]byte) ([][]byte, error) {
	return nil, ErrNoRemoteEndpoint
}
