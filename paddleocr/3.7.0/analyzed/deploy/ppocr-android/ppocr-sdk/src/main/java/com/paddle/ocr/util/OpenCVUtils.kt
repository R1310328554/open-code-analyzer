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

// OpenCVUtils.kt — OpenCV 原生库加载与一次性初始化。

package com.paddle.ocr.utilpackage com.paddle.ocr.util

import android.content.Context
import android.util.Log

/**
 * OpenCVUtils 负责在 Android 端加载 opencv_java4 动态库。
 * init 幂等，失败时记录日志并返回 false。
 */
object OpenCVUtils {

    private var initialized = false

    // init 加载 OpenCV JNI 库；context 预留供未来扩展，当前未使用。
    fun init(context: Context): Boolean {
        if (initialized) return true
        try {
            System.loadLibrary("opencv_java4")
            initialized = true
        } catch (e: UnsatisfiedLinkError) {
            Log.e("OpenCVUtils", "Failed to initialize OpenCV: ${e.message}")
        }
        return initialized
    }
}
