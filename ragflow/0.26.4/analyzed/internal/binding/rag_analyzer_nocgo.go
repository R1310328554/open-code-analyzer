//go:build !cgo

// rag_analyzer_nocgo.go — 禁用 CGO 时的桩实现：所有分词 API 返回 cgo required 错误，保证纯 Go 构建可通过。

package rag_analyzer

import "fmt"

// Token represents a single token from the analyzer.
type Token struct {
	Text      string
	Offset    uint32
	EndOffset uint32
}

// TokenWithPosition represents a token with position information.
type TokenWithPosition struct {
	Text      string
	Offset    uint32
	EndOffset uint32
}

// Analyzer is the no-CGO stub used for builds that intentionally skip the
// native tokenizer binding.
// Analyzer 为无 CGO 构建占位类型，不持有原生句柄。
type Analyzer struct{}

// NewAnalyzer 桩构造：提示需启用 CGO 才能使用分词器。
func NewAnalyzer(path string) (*Analyzer, error) {
	return nil, fmt.Errorf("rag_analyzer: cgo required (path=%q)", path)
}

func (a *Analyzer) Load() error {
	return fmt.Errorf("rag_analyzer: cgo required")
}

func (a *Analyzer) SetFineGrained(bool) {}

func (a *Analyzer) SetEnablePosition(bool) {}

// Analyze 桩实现，拒绝无 CGO 环境下的分词请求。
func (a *Analyzer) Analyze(text string) ([]Token, error) {
	return nil, fmt.Errorf("rag_analyzer: cgo required for Analyze(%q)", text)
}

// Tokenize 桩实现，返回 cgo required。
func (a *Analyzer) Tokenize(text string) (string, error) {
	return "", fmt.Errorf("rag_analyzer: cgo required for Tokenize(%q)", text)
}

func (a *Analyzer) TokenizeWithPosition(text string) ([]TokenWithPosition, error) {
	return nil, fmt.Errorf("rag_analyzer: cgo required for TokenizeWithPosition(%q)", text)
}

func (a *Analyzer) Close() {}

func (a *Analyzer) FineGrainedTokenize(tokens string) (string, error) {
	return "", fmt.Errorf("rag_analyzer: cgo required for FineGrainedTokenize(%q)", tokens)
}

func (a *Analyzer) GetTermFreq(term string) int32 { return 0 }

func (a *Analyzer) GetTermTag(term string) string { return "" }

func (a *Analyzer) Copy() *Analyzer { return nil }
