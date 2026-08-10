//
//  Copyright 2025 The InfiniFlow Authors. All Rights Reserved.
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

package extractor

import (
	"regexp"
	"strings"
)

// 多语言关系正则模式，与 Python MULTILANG_RELATION_PATTERNS 一致。
// 实体组大小写敏感；关系关键词内联 (?i)。
// _entWord 支持 Inc./Corp. 及 U.S./J.K. 等缩写。
const _entWord = `[A-Za-z][\w']*(?:\.[A-Za-z][\w']*)*\.?`
const _relEntity = `(` + _entWord + `(?:\s+` + _entWord + `)*?)`
const _relEntity2 = `(` + _entWord + `(?:\s+` + _entWord + `){0,1})`

var relationPatterns = map[string][]relPatternEntry{
	"en": {
		{regexp.MustCompile(_relEntity + `\s+(?i:was)\s+(?i:founded)\s+(?i:by)\s+` + _relEntity2), "founded_by"},
		{regexp.MustCompile(_relEntity + `\s+(?i:is)\s+(?i:an?\s+)?(?i:co-)?(?i:founder)\s+(?i:of)\s+` + _relEntity2), "founded_by"},
		{regexp.MustCompile(_relEntity + `\s+(?i:works)\s+(?i:for)\s+` + _relEntity2), "works_for"},
		{regexp.MustCompile(_relEntity + `\s+(?i:is)\s+(?i:an?\s+)?(?i:employee)\s+(?i:of)\s+` + _relEntity2), "works_for"},
		{regexp.MustCompile(_relEntity + `\s+(?i:joined)\s+` + _relEntity2), "works_for"},
		{regexp.MustCompile(_relEntity + `\s+(?i:is)\s+(?i:the\s+)?(?:CEO|CTO|CFO|VP|(?i:director|manager|engineer))\s+(?i:of|at)\s+` + _relEntity2), "works_for"},
		{regexp.MustCompile(_relEntity + `\s+(?i:is)\s+(?i:located|based|headquartered|situated)\s+(?i:in)\s+` + _relEntity2), "located_in"},
		{regexp.MustCompile(_relEntity + `\s+(?i:was)\s+(?i:born)\s+(?i:in|on)\s+` + _relEntity2), "born_in"},
		{regexp.MustCompile(_relEntity + `\s+(?i:born)\s+(?i:in|on)\s+` + _relEntity2), "born_in"},
		{regexp.MustCompile(_relEntity + `\s+(?i:was)\s+(?i:acquired)\s+(?i:by)\s+` + _relEntity2), "acquired"},
		{regexp.MustCompile(_relEntity + `\s+(?i:acquired)\s+` + _relEntity2), "acquired"},
		{regexp.MustCompile(_relEntity + `\s+(?i:is)\s+(?i:the\s+)?(?i:CEO)\s+(?i:of)\s+` + _relEntity2), "ceo_of"},
	},
	"zh": {
		{regexp.MustCompile(`([\p{Han}\w]{2,6})\s*由\s*([\p{Han}\w]{2,4})\s*(?:创立|创建|成立|创办)`), "founded_by"},
		{regexp.MustCompile(`([\p{Han}\w]{2,4})\s*(?:创立|创建|成立|创办)(?:\s*了\s*)?([\p{Han}\w]{2,10})`), "founded_by"},
		{regexp.MustCompile(`([\p{Han}\w]{2,4})\s*(?:是\s*)?([\p{Han}\w]{2,10})\s*(?:创始人|联合创始人)`), "founded_by"},
		{regexp.MustCompile(`([\p{Han}\w]{2,4})\s*(?:任职于|供职于|工作于|就职于)\s*([\p{Han}\w]{2,10})`), "works_for"},
		{regexp.MustCompile(`([\p{Han}\w]{2,4})\s*(?:是\s*)?([\p{Han}\w]{2,10})\s*(?:的员工|的雇员)`), "works_for"},
		{regexp.MustCompile(`([\p{Han}\w]{2,10})\s*(?:位于|坐落于|总部设在|总部位于)\s*([\p{Han}\w]{2,6})`), "located_in"},
		{regexp.MustCompile(`([\p{Han}\w]{2,10})\s*在\s*([\p{Han}\w]{2,6})`), "located_in"},
		{regexp.MustCompile(`([\p{Han}\w]{2,4})\s*(?:出生于|生于)\s*([\p{Han}\w]{2,6})`), "born_in"},
		{regexp.MustCompile(`([\p{Han}\w]{2,10})\s*(?:收购|并购)\s*([\p{Han}\w]{2,10})`), "acquired"},
		{regexp.MustCompile(`([\p{Han}\w]{2,10})\s*被\s*([\p{Han}\w]{2,10})\s*(?:收购|并购)`), "acquired"},
	},
}

type relPatternEntry struct {
	pattern   *regexp.Regexp
	predicate string
}

// ExtractRelations 抽取实体间类型化关系，
// 匹配 Python RelationExtractor 模式法，含跨句边界过滤。
func ExtractRelations(text string, entities []Entity, lang string) []Relation {
	return extractRelationsWithOpts(text, entities, lang, 100)
}

// extractRelationsWithOpts 可配置最大距离的内部实现。
func extractRelationsWithOpts(text string, entities []Entity, lang string, maxDistance int) []Relation {
	patterns, ok := relationPatterns[lang]
	if !ok {
		patterns = relationPatterns["en"]
	}

	// 构建 entity text → 全部出现位置的多值映射
	entityMultiMap := make(map[string][]Entity, len(entities))
	for _, e := range entities {
		key := strings.ToLower(e.Text)
		entityMultiMap[key] = append(entityMultiMap[key], e)
		// 同时加入去标点版本
		cleaned := strings.TrimRight(e.Text, ".,;:!?")
		cleaned = strings.TrimSpace(cleaned)
		if cleaned != e.Text {
			ckey := strings.ToLower(cleaned)
			entityMultiMap[ckey] = append(entityMultiMap[ckey], e)
		}
	}

	// 构建句子 span（匹配 Python 分句正则）
	hasOffsets := false
	for _, e := range entities {
		if e.StartChar != 0 || e.EndChar != 0 {
			hasOffsets = true
			break
		}
	}
	var sentenceSpans [][2]int
	if hasOffsets {
		sentenceSpans = splitSentences(text)
	}

	seen := make(map[string]bool)
	var relations []Relation

	// 阶段 1：基于模式的类型化关系
	// Process each sentence separately to prevent cross-sentence regex matches.
	// When entities have no offsets, fall back to full-text matching.
	if hasOffsets && len(sentenceSpans) > 0 {
		for _, entry := range patterns {
			for _, sp := range sentenceSpans {
				sentText := text[sp[0]:sp[1]]
				matches := entry.pattern.FindAllStringSubmatchIndex(sentText, -1)
				for _, m := range matches {
					if len(m) < 6 {
						continue
					}
					subjStart, subjEnd := m[2], m[3]
					objStart, objEnd := m[4], m[5]
					if subjStart < 0 || objStart < 0 {
						continue
					}
					// 调整为绝对字符位置
					absSubjStart := subjStart + sp[0]
					absSubjEnd := subjEnd + sp[0]
					absObjStart := objStart + sp[0]
					absObjEnd := objEnd + sp[0]
					subjText := strings.TrimSpace(text[absSubjStart:absSubjEnd])
					objText := strings.TrimSpace(text[absObjStart:absObjEnd])
					subj := findEntityByText(subjText, absSubjStart, entityMultiMap)
					obj := findEntityByText(objText, absObjStart, entityMultiMap)
					if subj.Text == "" || obj.Text == "" {
						continue
					}
					key := subj.Text + "|" + entry.predicate + "|" + obj.Text
					if seen[key] {
						continue
					}
					seen[key] = true

					var ctx string
					absMatchStart := m[0] + sp[0]
					absMatchEnd := m[1] + sp[0]
					ctx = extractContext(text, text[absMatchStart:absMatchEnd])
					relations = append(relations, Relation{
						Subject:    subj,
						Predicate:  entry.predicate,
						Object:     obj,
						Confidence: 0.8,
						Context:    ctx,
					})
				}
			}
		}
	} else {
		// 无 offset：处理全文
		for _, entry := range patterns {
			matches := entry.pattern.FindAllStringSubmatchIndex(text, -1)
			for _, m := range matches {
				if len(m) < 6 {
					continue
				}
				subjStart, subjEnd := m[2], m[3]
				objStart, objEnd := m[4], m[5]
				if subjStart < 0 || objStart < 0 {
					continue
				}
				subjText := strings.TrimSpace(text[subjStart:subjEnd])
				objText := strings.TrimSpace(text[objStart:objEnd])
				subj := findEntityByText(subjText, subjStart, entityMultiMap)
				obj := findEntityByText(objText, objStart, entityMultiMap)
				if subj.Text == "" || obj.Text == "" {
					continue
				}
				key := subj.Text + "|" + entry.predicate + "|" + obj.Text
				if seen[key] {
					continue
				}
				seen[key] = true

				var ctx string
				if len(m) >= 2 && m[0] >= 0 {
					ctx = extractContext(text, text[m[0]:m[1]])
				}
				relations = append(relations, Relation{
					Subject:    subj,
					Predicate:  entry.predicate,
					Object:     obj,
					Confidence: 0.8,
					Context:    ctx,
				})
			}
		}
	}

	// 阶段 2：共现关系（含句子边界检查）
	for _, r := range extractCooccurrence(text, entities, maxDistance) {
		key := r.Subject.Text + "|related_to|" + r.Object.Text
		if !seen[key] {
			seen[key] = true
			relations = append(relations, r)
		}
	}

	// 多跳推理 + 去重（与 Python 一致始终应用）
	relations = inferMultiHop(relations)
	relations = dedupRelations(relations)

	return relations
}

// extractCooccurrence 为同句内 maxDistance 字符内的实体对生成 related_to 关系。
func extractCooccurrence(text string, entities []Entity, maxDistance int) []Relation {
	if len(entities) < 2 {
		return nil
	}
	hasOffsets := false
	for _, e := range entities {
		if e.StartChar != 0 || e.EndChar != 0 {
			hasOffsets = true
			break
		}
	}
	var sentenceSpans [][2]int
	if hasOffsets {
		sentenceSpans = splitSentences(text)
	}
	sameSentence := func(c1, c2 int) bool {
		if !hasOffsets || len(sentenceSpans) == 0 {
			return true
		}
		for _, sp := range sentenceSpans {
			if sp[0] <= c1 && c1 < sp[1] && sp[0] <= c2 && c2 < sp[1] {
				return true
			}
		}
		return false
	}
	var relations []Relation
	for i := 0; i < len(entities); i++ {
		for j := i + 1; j < len(entities); j++ {
			e1, e2 := entities[i], entities[j]
			if !sameSentence(e1.StartChar, e2.StartChar) {
				continue
			}
			dist := abs(e2.StartChar - e1.EndChar)
			if dist > maxDistance {
				continue
			}
			relations = append(relations, Relation{
				Subject:    e1,
				Predicate:  "related_to",
				Object:     e2,
				Confidence: 0.4,
				Context:    extractContextSimple(text, e1, e2),
				Metadata:   map[string]interface{}{"method": "cooccurrence"},
			})
		}
	}
	return relations
}

// findEntityByText 查找距 matchPos 最近的实体出现，支持重名。
func findEntityByText(raw string, matchPos int, entityMultiMap map[string][]Entity) Entity {
	text := strings.TrimSpace(raw)
	// 去除尾部标点
	for len(text) > 0 && strings.ContainsAny(text[len(text)-1:], ".,;:!?") {
		text = strings.TrimSpace(text[:len(text)-1])
	}
	ent := findClosest(text, matchPos, entityMultiMap)
	if ent.Text != "" {
		return ent
	}
	// 尝试截断 and/or/逗号 后的尾部
	key := strings.ToLower(text)
	for _, sep := range []string{" and ", " or ", ", "} {
		if idx := strings.Index(key, sep); idx > 0 {
			if e := findClosest(key[:idx], matchPos, entityMultiMap); e.Text != "" {
				return e
			}
		}
	}
	// 从右向左逐词缩短尝试匹配
	// 处理如 Google in → Google 等截断场景
	words := strings.Fields(key)
	for i := len(words) - 1; i > 0; i-- {
		candidate := strings.Join(words[:i], " ")
		// 查找前去除候选尾部标点
		candidate = strings.TrimRight(candidate, ".,;:!?")
		candidate = strings.TrimSpace(candidate)
		if e := findClosest(candidate, matchPos, entityMultiMap); e.Text != "" {
			return e
		}
	}
	return Entity{}
}

// findClosest 从 multimap 返回距 matchPos 最近的实体，精确匹配失败时去标点重试。
func findClosest(name string, matchPos int, multiMap map[string][]Entity) Entity {
	entries := multiMap[strings.ToLower(name)]
	if len(entries) == 0 {
		// 精确匹配失败时去尾部标点重试
		cleaned := strings.TrimRight(name, ".,;:!?")
		cleaned = strings.TrimSpace(cleaned)
		if cleaned != name {
			entries = multiMap[strings.ToLower(cleaned)]
		}
	}
	if len(entries) == 0 {
		return Entity{}
	}
	if len(entries) == 1 {
		return entries[0]
	}
	// 多次出现：选 span 中心距 matchPos 最近者
	best := entries[0]
	bestDist := abs(best.StartChar + best.EndChar - 2*matchPos)
	for i := 1; i < len(entries); i++ {
		d := abs(entries[i].StartChar + entries[i].EndChar - 2*matchPos)
		if d < bestDist {
			best = entries[i]
			bestDist = d
		}
	}
	return best
}

func extractContext(text string, matchStr string) string {
	idx := strings.Index(text, matchStr)
	if idx < 0 {
		return ""
	}
	start := idx - 30
	if start < 0 {
		start = 0
	}
	end := idx + len(matchStr) + 30
	if end > len(text) {
		end = len(text)
	}
	return text[start:end]
}

func extractContextSimple(text string, e1, e2 Entity) string {
	start := min(e1.StartChar, e2.StartChar) - 20
	if start < 0 {
		start = 0
	}
	end := max(e1.EndChar, e2.EndChar) + 20
	if end > len(text) {
		end = len(text)
	}
	return text[start:end]
}

func abs(x int) int {
	if x < 0 {
		return -x
	}
	return x
}

func min(a, b int) int {
	if a < b {
		return a
	}
	return b
}

func max(a, b int) int {
	if a > b {
		return a
	}
	return b
}

// splitSentences 将文本切分为句子 span [start, end)。
// Go RE2 无前瞻，手动识别边界：大写/句末句号分句，小写前句号视为缩写，! ? 始终分句。
func splitSentences(text string) [][2]int {
	var spans [][2]int
	start := 0
	for i := 0; i < len(text); {
		ch := text[i]
		if ch == '!' || ch == '?' {
			end := i + 1
			spans = append(spans, [2]int{start, end})
			start = end
			i = end
			continue
		}
		if ch == '.' {
			// 判断句号是句末还是缩写
			// 句末：句号 + 空格 + 大写或文本结尾
			// 缩写：句号 + 空格 + 小写
			end := i + 1
			next := end
			for next < len(text) && text[next] == ' ' {
				next++
			}
			if next >= len(text) {
				// 文本末尾句号 = 句末
				spans = append(spans, [2]int{start, end})
				start = end
				i = end
				continue
			}
			if text[next] >= 'A' && text[next] <= 'Z' {
				// 句号 + 空格 + 大写 = 句末
				spans = append(spans, [2]int{start, end})
				start = end
				i = end
				continue
			}
			// 句号后小写 = 缩写，非句末
			i = end
			continue
		}
		i++
	}
	// 最后句界之后的剩余文本
	if start < len(text) {
		spans = append(spans, [2]int{start, len(text)})
	}
	if len(spans) == 0 && len(text) > 0 {
		spans = append(spans, [2]int{0, len(text)})
	}
	return spans
}
