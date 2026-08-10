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

// Package wordnet 提供 NLTK WordNet 同义词集能力的 Go 实现。
// 读取 WordNet 3.0 数据库文件并支持按词形与词性检索 synset。
package nlp

import (
	"bufio"
	"fmt"
	"os"
	"path/filepath"
	"strconv"
	"strings"
	"sync"
)

// WordNet 词性常量：名词/动词/形容词/副词
const (
	NOUN = "n"
	VERB = "v"
	ADJ  = "a"
	ADV  = "r"
)

// 各词性的形态还原替换规则（Morphy 算法）
var morphologicalSubstitutions = map[string][][2]string{
	NOUN: {
		{"s", ""},
		{"ses", "s"},
		{"ves", "f"},
		{"xes", "x"},
		{"zes", "z"},
		{"ches", "ch"},
		{"shes", "sh"},
		{"men", "man"},
		{"ies", "y"},
	},
	VERB: {
		{"s", ""},
		{"ies", "y"},
		{"es", "e"},
		{"es", ""},
		{"ed", "e"},
		{"ed", ""},
		{"ing", "e"},
		{"ing", ""},
	},
	ADJ: {
		{"er", ""},
		{"est", ""},
		{"er", "e"},
		{"est", "e"},
	},
	ADV: {},
}

// 词性到索引/数据文件后缀的映射
var fileMap = map[string]string{
	NOUN: "noun",
	VERB: "verb",
	ADJ:  "adj",
	ADV:  "adv",
}

// Synset 表示 WordNet 同义词集（含义项、释义与例句）
type Synset struct {
	Name       string
	POS        string
	Offset     int
	Lemmas     []string
	Definition string
	Examples   []string
}

// WordNet 封装词库目录、索引映射与数据文件缓存
type WordNet struct {
	wordNetDir          string
	lemmaPosOffsetMap   map[string]map[string][]int
	exceptionMap        map[string]map[string][]string
	dataFileCache       map[string]*os.File
	dataFileCacheOffset map[string]int64
	fileMutexes         map[string]*sync.Mutex // Mutex for each POS to ensure concurrency safety
}

// NewWordNet 加载异常表与 lemma→offset 索引，初始化 WordNet 实例
func NewWordNet(wordNetDir string) (*WordNet, error) {
	wn := &WordNet{
		wordNetDir:          wordNetDir,
		lemmaPosOffsetMap:   make(map[string]map[string][]int),
		exceptionMap:        make(map[string]map[string][]string),
		dataFileCache:       make(map[string]*os.File),
		dataFileCacheOffset: make(map[string]int64),
		fileMutexes:         make(map[string]*sync.Mutex),
	}

	// Initialize exception maps for all POS
	for pos := range fileMap {
		wn.exceptionMap[pos] = make(map[string][]string)
	}

	// Load exception files
	if err := wn.loadExceptionMaps(); err != nil {
		return nil, fmt.Errorf("failed to load exception maps: %w", err)
	}

	// Load lemma pos offset map
	if err := wn.loadLemmaPosOffsetMap(); err != nil {
		return nil, fmt.Errorf("failed to load lemma pos offset map: %w", err)
	}

	return wn, nil
}

// Close 关闭已缓存的数据文件句柄
func (wn *WordNet) Close() {
	for pos, f := range wn.dataFileCache {
		if mutex, ok := wn.fileMutexes[pos]; ok {
			mutex.Lock()
			f.Close()
			mutex.Unlock()
		} else {
			f.Close()
		}
	}
}

// loadExceptionMaps 读取各词性 .exc 不规则变形表
func (wn *WordNet) loadExceptionMaps() error {
	for pos, suffix := range fileMap {
		filename := filepath.Join(wn.wordNetDir, suffix+".exc")
		file, err := os.Open(filename)
		if err != nil {
			// It's okay if the file doesn't exist for some POS
			continue
		}
		defer file.Close()

		scanner := bufio.NewScanner(file)
		for scanner.Scan() {
			line := scanner.Text()
			fields := strings.Fields(line)
			if len(fields) >= 2 {
				// First field is the inflected form, rest are base forms
				wn.exceptionMap[pos][fields[0]] = fields[1:]
			}
		}
		if err := scanner.Err(); err != nil {
			return fmt.Errorf("error reading %s: %w", filename, err)
		}
	}
	return nil
}

// loadLemmaPosOffsetMap 解析 index.* 文件构建 lemma 到 synset 偏移索引
func (wn *WordNet) loadLemmaPosOffsetMap() error {
	for _, suffix := range fileMap {
		filename := filepath.Join(wn.wordNetDir, "index."+suffix)
		file, err := os.Open(filename)
		if err != nil {
			return fmt.Errorf("failed to open %s: %w", filename, err)
		}
		defer file.Close()

		scanner := bufio.NewScanner(file)
		for scanner.Scan() {
			line := scanner.Text()

			// Skip license header lines (lines starting with space)
			if len(line) == 0 || line[0] == ' ' {
				continue
			}

			fields := strings.Fields(line)
			if len(fields) < 6 {
				continue
			}

			// Parse index file format:
			// lemma pos n_synsets n_pointers [pointers] n_senses n_ranked_synsets [synset_offsets...]
			lemma := strings.ToLower(fields[0])
			filePos := fields[1]
			nSynsets, err := strconv.Atoi(fields[2])
			if err != nil {
				continue
			}
			nPointers, err := strconv.Atoi(fields[3])
			if err != nil {
				continue
			}

			// Calculate field positions
			fieldIdx := 4

			// Skip pointer symbols
			for i := 0; i < nPointers && fieldIdx < len(fields); i++ {
				fieldIdx++
			}

			// Read n_senses and n_ranked_synsets
			if fieldIdx >= len(fields) {
				continue
			}
			_, err = strconv.Atoi(fields[fieldIdx]) // n_senses
			if err != nil {
				continue
			}
			fieldIdx++

			if fieldIdx >= len(fields) {
				continue
			}
			_, err = strconv.Atoi(fields[fieldIdx]) // n_ranked_synsets
			if err != nil {
				continue
			}
			fieldIdx++

			// Read synset offsets
			var offsets []int
			for i := 0; i < nSynsets && fieldIdx < len(fields); i++ {
				offset, err := strconv.Atoi(fields[fieldIdx])
				if err != nil {
					continue
				}
				offsets = append(offsets, offset)
				fieldIdx++
			}

			// Store in map
			if wn.lemmaPosOffsetMap[lemma] == nil {
				wn.lemmaPosOffsetMap[lemma] = make(map[string][]int)
			}
			wn.lemmaPosOffsetMap[lemma][filePos] = offsets
		}
		if err := scanner.Err(); err != nil {
			return fmt.Errorf("error reading %s: %w", filename, err)
		}
	}
	return nil
}

// morphy 对词形做形态分析，返回 WordNet 中存在的词根候选
func (wn *WordNet) morphy(form string, pos string, checkExceptions bool) []string {
	form = strings.ToLower(form)
	exceptions := wn.exceptionMap[pos]
	substitutions := morphologicalSubstitutions[pos]

	// Helper function to apply substitution rules
	applyRules := func(forms []string) []string {
		var results []string
		for _, f := range forms {
			for _, sub := range substitutions {
				old, new := sub[0], sub[1]
				if strings.HasSuffix(f, old) {
					base := f[:len(f)-len(old)] + new
					results = append(results, base)
				}
			}
		}
		return results
	}

	// Helper function to filter forms that exist in WordNet
	filterForms := func(forms []string) []string {
		var results []string
		seen := make(map[string]bool)
		for _, f := range forms {
			if posMap, ok := wn.lemmaPosOffsetMap[f]; ok {
				if _, hasPos := posMap[pos]; hasPos {
					if !seen[f] {
						results = append(results, f)
						seen[f] = true
					}
				}
			}
		}
		return results
	}

	var forms []string
	if checkExceptions {
		if baseForms, ok := exceptions[form]; ok {
			forms = baseForms
		}
	}

	// If no exception found, apply rules
	if len(forms) == 0 {
		forms = applyRules([]string{form})
	}

	// Filter to keep only valid forms, also check original form
	return filterForms(append([]string{form}, forms...))
}

// getDataFile 按词性返回 data.* 文件（带 per-POS 互斥与缓存）
func (wn *WordNet) getDataFile(pos string) (*os.File, *sync.Mutex, error) {
	if pos == "s" { // Adjective satellite uses the same file as adjective
		pos = ADJ
	}

	// Get or create mutex for this POS
	mutex, exists := wn.fileMutexes[pos]
	if !exists {
		mutex = &sync.Mutex{}
		wn.fileMutexes[pos] = mutex
	}

	if file, ok := wn.dataFileCache[pos]; ok {
		return file, mutex, nil
	}

	suffix, ok := fileMap[pos]
	if !ok {
		return nil, nil, fmt.Errorf("unknown POS: %s", pos)
	}

	filename := filepath.Join(wn.wordNetDir, "data."+suffix)
	file, err := os.Open(filename)
	if err != nil {
		return nil, nil, fmt.Errorf("failed to open %s: %w", filename, err)
	}

	wn.dataFileCache[pos] = file
	return file, mutex, nil
}

// parseDataLine 解析 data 文件单行，提取 offset、lemmas 与 gloss
func parseDataLine(line string, pos string) (*Synset, error) {
	// Data file format:
	// synset_offset lex_filenum ss_type w_cnt word lex_id [word lex_id...] p_cnt [ptr_symbol synset_offset pos src_trgt...] [frames...] | gloss

	parts := strings.SplitN(line, "|", 2)
	if len(parts) != 2 {
		return nil, fmt.Errorf("invalid line format: no gloss separator")
	}

	dataPart := strings.TrimSpace(parts[0])
	glossPart := strings.TrimSpace(parts[1])

	// Parse gloss to get definition and examples
	var definition string
	var examples []string

	// Remove quotes from examples
	gloss := glossPart
	for {
		start := strings.Index(gloss, "\"")
		if start == -1 {
			break
		}
		end := strings.Index(gloss[start+1:], "\"")
		if end == -1 {
			break
		}
		end += start + 1

		example := gloss[start+1 : end]
		if len(examples) == 0 && start > 0 {
			definition = strings.TrimSpace(gloss[:start])
		}
		examples = append(examples, example)
		gloss = gloss[end+1:]
	}

	if definition == "" {
		definition = strings.Trim(glossPart, "; ")
		// Remove quoted examples from definition
		definition = regexpRemoveQuotes(definition)
	}

	// Final cleanup: trim trailing semicolon and whitespace to match Python NLTK
	definition = strings.TrimRight(definition, "; ")

	// Parse data part
	fields := strings.Fields(dataPart)
	if len(fields) < 4 {
		return nil, fmt.Errorf("invalid data line: too few fields")
	}

	offset, err := strconv.Atoi(fields[0])
	if err != nil {
		return nil, fmt.Errorf("invalid offset: %w", err)
	}

	// lexFilenum := fields[1]  // Not used currently
	ssType := fields[2]

	wCnt, err := strconv.ParseInt(fields[3], 16, 32)
	if err != nil {
		return nil, fmt.Errorf("invalid word count: %w", err)
	}

	// Parse lemmas
	var lemmas []string
	fieldIdx := 4
	for i := 0; i < int(wCnt) && fieldIdx+1 < len(fields); i++ {
		lemma := fields[fieldIdx]
		// Remove syntactic marker if present (e.g., "(a)" or "(p)")
		if idx := strings.Index(lemma, "("); idx != -1 {
			lemma = lemma[:idx]
		}
		// Keep original case for lemmas (Python NLTK preserves case)
		lemmas = append(lemmas, lemma)
		fieldIdx += 2 // skip lex_id
	}

	if len(lemmas) == 0 {
		return nil, fmt.Errorf("no lemmas found")
	}

	// Build synset name from first lemma (Python uses lowercase in synset name)
	senseIndex := 1 // Default to 1, would need to look up in index for actual sense number
	name := fmt.Sprintf("%s.%s.%02d", strings.ToLower(lemmas[0]), ssType, senseIndex)

	return &Synset{
		Name:       name,
		POS:        ssType,
		Offset:     offset,
		Lemmas:     lemmas,
		Definition: definition,
		Examples:   examples,
	}, nil
}

// regexpRemoveQuotes removes quoted strings from text (simplified version)
func regexpRemoveQuotes(s string) string {
	var result strings.Builder
	inQuote := false
	for _, ch := range s {
		if ch == '"' {
			inQuote = !inQuote
			continue
		}
		if !inQuote {
			result.WriteRune(ch)
		}
	}
	return strings.TrimSpace(strings.Trim(result.String(), "; "))
}

// synsetFromPosAndOffset 按词性与字节偏移定位并解析 synset
func (wn *WordNet) synsetFromPosAndOffset(pos string, offset int) (*Synset, error) {
	file, mutex, err := wn.getDataFile(pos)
	if err != nil {
		return nil, err
	}

	// Lock only for Seek and Read operations to minimize critical section
	mutex.Lock()

	// Seek to the offset
	_, err = file.Seek(int64(offset), 0)
	if err != nil {
		mutex.Unlock()
		return nil, fmt.Errorf("failed to seek to offset %d: %w", offset, err)
	}

	reader := bufio.NewReader(file)
	line, err := reader.ReadString('\n')
	mutex.Unlock() // Release lock immediately after reading

	if err != nil {
		return nil, fmt.Errorf("failed to read line at offset %d: %w", offset, err)
	}

	//if len(line) < 8 {
	//	fmt.Println(line)
	//}

	// Verify the offset matches
	lineOffset := strings.TrimSpace(line[:8])
	expectedOffset := fmt.Sprintf("%08d", offset)
	if lineOffset != expectedOffset {
		return nil, fmt.Errorf("offset mismatch: expected %s, got %s", expectedOffset, lineOffset)
	}

	synset, err := parseDataLine(line, pos)
	if err != nil {
		return nil, err
	}

	// Calculate the correct sense number by looking up the offset in the index
	// This operation only accesses memory map, no need for file lock
	senseNum := wn.findSenseNumber(synset.Lemmas[0], pos, offset)
	if senseNum > 0 {
		synset.Name = fmt.Sprintf("%s.%s.%02d", synset.Lemmas[0], synset.POS, senseNum)
	}

	return synset, nil
}

// findSenseNumber 在索引中查找义项序号以生成标准 synset 名称
func (wn *WordNet) findSenseNumber(lemma string, pos string, offset int) int {
	lemma = strings.ToLower(lemma)
	if posMap, ok := wn.lemmaPosOffsetMap[lemma]; ok {
		if offsets, hasPos := posMap[pos]; hasPos {
			for i, off := range offsets {
				if off == offset {
					return i + 1 // sense numbers are 1-indexed
				}
			}
		}
	}
	return 1 // Default to 1 if not found
}

// Synsets 查询指定 lemma（可选词性）的全部 synset，等价 NLTK synsets()
// If pos is empty, all parts of speech are searched.
// This is the main function equivalent to NLTK's wordnet.synsets()
func (wn *WordNet) Synsets(lemma string, pos string) []*Synset {
	lemma = strings.ToLower(lemma)

	var poses []string
	if pos == "" {
		poses = []string{NOUN, VERB, ADJ, ADV}
	} else {
		poses = []string{pos}
	}

	var results []*Synset
	seen := make(map[string]bool)

	for _, p := range poses {
		// Get morphological forms
		forms := wn.morphy(lemma, p, true)

		for _, form := range forms {
			if posMap, ok := wn.lemmaPosOffsetMap[form]; ok {
				if offsets, hasPos := posMap[p]; hasPos {
					for _, offset := range offsets {
						// Create unique key to avoid duplicates
						key := fmt.Sprintf("%s-%d", p, offset)
						if !seen[key] {
							seen[key] = true
							synset, err := wn.synsetFromPosAndOffset(p, offset)
							if err == nil {
								results = append(results, synset)
							}
						}
					}
				}
			}
		}
	}

	return results
}

// Name returns the synset name (e.g., "dog.n.01")
func (s *Synset) NameStr() string {
	return s.Name
}

// String returns a string representation of the synset
func (s *Synset) String() string {
	return fmt.Sprintf("Synset('%s')", s.Name)
}
// wordnet.go — 读取 WordNet 3.0 词库，提供同义词集查询与形态还原（对齐 NLTK）。
