//
// Copyright 2026 The InfiniFlow Authors. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//

package chunk

// helpers.go — 分块辅助函数：CJK 语言检测与 rune 计数。


import "unicode"

// DetectLanguage 按 CJK 字符占字母比例启发式返回 zh 或 en。
// 阈值 0.3 以上判定为中文，写入 chunk metadata.language。
func DetectLanguage(text string) string {
	cjk := 0
	total := 0
	for _, r := range text {
		if unicode.Is(unicode.Han, r) {
			cjk++
		}
		if unicode.IsLetter(r) {
			total++
		}
	}
	if total > 0 && float64(cjk)/float64(total) > 0.3 {
		return "zh"
	}
	return "en"
}

// RuneCount 返回文本 rune 数（非字节长度），供长度策略与过滤使用。
func RuneCount(text string) int {
	return len([]rune(text))
}

// 语言检测为 best-effort 启发式，不替代正式 NLP 语种识别。
