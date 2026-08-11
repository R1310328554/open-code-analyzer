/*
 * Copyright (c) 2026 PaddlePaddle Authors. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

// ONNX 推理薄封装：取 session 首个输入/输出名执行单次 run
import type { InferenceSession, Tensor } from "onnxruntime-web";

  // 将 inputTensor 绑定到 session.inputNames[0]，返回第一个输出 Tensor
export async function runInference(
  session: InferenceSession,
  inputTensor: Tensor
): Promise<Tensor> {
  const inputName = session.inputNames[0];
  const outputMap = await session.run({ [inputName]: inputTensor });
  return outputMap[session.outputNames[0]];
}
