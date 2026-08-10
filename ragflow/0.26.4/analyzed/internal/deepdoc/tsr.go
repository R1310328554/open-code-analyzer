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

package deepdoc

import "context"

// TSR 为占位实现：Python deepdoc 无远程 TSR 端点，表格结构识别为本地 ONNX 管道。需 TSR 的调用方应继续用 Python 服务；本 Go 客户端仅支持 DLA。无条件返回 ErrNoRemoteEndpoint，避免静默失败。
func (c *Client) TSR(_ context.Context, _ [][]byte) ([][]byte, error) {
	return nil, ErrNoRemoteEndpoint
}
