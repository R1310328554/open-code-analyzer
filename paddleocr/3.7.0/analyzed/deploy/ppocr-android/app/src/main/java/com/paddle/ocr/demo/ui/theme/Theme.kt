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

package com.paddle.ocr.demo.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
// PPOCR Demo Material3 主题：定义浅色配色方案
import androidx.compose.ui.graphics.Color

// 浅色 ColorScheme：主色 Paddle 蓝、辅色青绿与错误红
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF1565C0),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD1E4FF),
    onPrimaryContainer = Color(0xFF001D36),
    secondary = Color(0xFF006874),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF9EEFF7),
    onSecondaryContainer = Color(0xFF001F24),
    surface = Color(0xFFFDFBFF),
    onSurface = Color(0xFF1A1C1E),
    background = Color(0xFFFDFBFF),
    onBackground = Color(0xFF1A1C1E),
    error = Color(0xFFBA1A1A),
    onError = Color.White,
)

/**
 * 应用根主题 Composable，统一 Material3 颜色与排版。
 */
@Composable
fun PPOCRTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        content = content,
    )
}
