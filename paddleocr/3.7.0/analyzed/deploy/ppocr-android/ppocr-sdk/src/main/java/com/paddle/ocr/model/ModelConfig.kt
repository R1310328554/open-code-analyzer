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

package com.paddle.ocr.model

import android.content.Context
// 识别模型 YAML 配置解析：提取 PostProcess.character_dict 字符表
import com.paddle.ocr.util.YamlUtils

/**
 * 识别模型配置：从 inference.yml 解析 CTC 解码用字符列表。
 */
data class ModelConfig(
    val characterList: List<String>,
) {
    // 静态解析入口：读 assets YAML 并构造 ModelConfig
    companion object {
        // 打开 assetPath，解析 character_dict 并在末尾补空格 token
        fun parse(context: Context, assetPath: String): ModelConfig {
            val content = try {
                context.assets.open(assetPath).bufferedReader().use { it.readText() }
            } catch (t: Throwable) {
                throw OCRError.ConfigParseFailed(assetPath, t)
            }
            val characterDict = try {
                extractCharacterDict(content, assetPath)
            } catch (e: OCRError.ConfigParseFailed) {
                throw e
            } catch (t: Throwable) {
                throw OCRError.ConfigParseFailed(assetPath, t)
            }
            val charListWithSpace = characterDict.toMutableList().apply {
                if (lastOrNull() != " ") add(" ")
            }

            return ModelConfig(characterList = charListWithSpace)
        }

        // 定位 PostProcess: 块下的 character_dict: 列表项
        private fun extractCharacterDict(content: String, assetPath: String): List<String> {
            val lines = content.replace("\r\n", "\n").replace('\r', '\n').lines()
            val postProcessLine = lines.indexOfFirst { it.trim() == "PostProcess:" }
            if (postProcessLine < 0) {
                throw OCRError.ConfigParseFailed("Missing PostProcess in $assetPath")
            }

            val postProcessIndent = YamlUtils.leadingSpaces(lines[postProcessLine])
            val characterDictLine = findCharacterDictLine(lines, postProcessLine + 1, postProcessIndent)
            if (characterDictLine < 0) {
                throw OCRError.ConfigParseFailed("Missing character_dict in $assetPath")
            }

            val characterDictIndent = YamlUtils.leadingSpaces(lines[characterDictLine])
            val characters = mutableListOf<String>()
            var lineIndex = characterDictLine + 1
            while (lineIndex < lines.size) {
                val line = lines[lineIndex]
                val indent = YamlUtils.leadingSpaces(line)
                val content = line.substring(indent)
                val keyLikeContent = content.trim()
                if (keyLikeContent.isEmpty() || keyLikeContent.startsWith("#")) {
                    lineIndex++
                    continue
                }

                if (!content.startsWith("-")) {
                    if (indent <= characterDictIndent) break
                    lineIndex++
                    continue
                }

                characters.add(parseYamlListScalar(content.substring(1)))
                lineIndex++
            }

            if (characters.isEmpty()) {
                throw OCRError.ConfigParseFailed("Empty character_dict in $assetPath")
            }
            return characters
        }

        // 在 PostProcess 子树中查找 character_dict: 行号
        private fun findCharacterDictLine(
            lines: List<String>,
            startLine: Int,
            postProcessIndent: Int,
        ): Int {
            var lineIndex = startLine
            while (lineIndex < lines.size) {
                val line = lines[lineIndex]
                val trimmed = line.trim()
                if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                    lineIndex++
                    continue
                }
                val indent = YamlUtils.leadingSpaces(line)
                if (indent <= postProcessIndent) break
                if (trimmed == "character_dict:") return lineIndex
                lineIndex++
            }
            return -1
        }

        // 解析 YAML 列表项标量，支持单引号、双引号与转义
        private fun parseYamlListScalar(rawValue: String): String {
            val value = rawValue.dropWhile { it == ' ' }
            if (value.length >= 2 && value.first() == '\'' && value.last() == '\'') {
                return value.substring(1, value.length - 1).replace("''", "'")
            }
            if (value.length >= 2 && value.first() == '"' && value.last() == '"') {
                return value.substring(1, value.length - 1)
                    .replace("\\\"", "\"")
                    .replace("\\\\", "\\")
                    .replace("\\n", "\n")
                    .replace("\\r", "\r")
                    .replace("\\t", "\t")
            }
            return value
        }
    }
}
