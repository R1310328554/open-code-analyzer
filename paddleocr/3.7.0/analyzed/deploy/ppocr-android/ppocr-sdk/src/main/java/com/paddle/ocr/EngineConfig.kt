// Copyright (c) 2026 PaddlePaddle Authors. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.paddle.ocr

// ONNX Runtime 引擎运行时配置
package com.paddle.ocr

/**
 * 推理引擎配置：控制 ONNX Runtime  intra-op 线程数等底层参数。
 */
data class EngineConfig(
    // ONNX 算子内部并行线程数，默认 4
    val numThreads: Int = 4,
)
