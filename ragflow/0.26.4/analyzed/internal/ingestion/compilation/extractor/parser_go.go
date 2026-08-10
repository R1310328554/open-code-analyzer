// parser_go.go — C++ ThincParser/ThincTagger cgo 绑定。
//go:build cgo_thincner

package extractor

/*
#include <stdlib.h>
#include "../../../binding/cpp/thinc_parser.h"
*/
import "C"
import (
	"encoding/json"
	"fmt"
	"unsafe"
)

// DepToken 单 token 依存解析信息（与 dep_relation.go 一致）。
type DepTokenC struct {
	Text  string `json:"text"`
	Head  int    `json:"head"`
	Dep   string `json:"dep"`
	Index int    `json:"index"`
}

// RunParser 对分词文本运行 C++ 依存解析器。
// nerDir/parserDir 为模型子目录；tokensJSON 为 token JSON 数组；返回 DepTokenC JSON。
func RunParser(nerDir, parserDir string, tokensJSON string) (string, error) {
	cNer := C.CString(nerDir)
	cParser := C.CString(parserDir)
	cTokens := C.CString(tokensJSON)
	defer C.free(unsafe.Pointer(cNer))
	defer C.free(unsafe.Pointer(cParser))
	defer C.free(unsafe.Pointer(cTokens))

	handle := C.ThincParser_Create(cNer, cParser)
	if handle == nil {
		return "", fmt.Errorf("failed to create ThincParser handle")
	}
	defer C.ThincParser_Destroy(handle)

	cResult := C.ThincParser_Predict(handle, cTokens)
	if cResult == nil {
		return "", fmt.Errorf("parser prediction failed")
	}
	defer C.ThincParser_FreeString(cResult)

	return C.GoString(cResult), nil
}

// RunTagger 运行 C++ 词性标注器；nerDir 提供 tok2vec 权重。
func RunTagger(nerDir, taggerDir string, tokensJSON string) (string, error) {
	cNer := C.CString(nerDir)
	cTagger := C.CString(taggerDir)
	cTokens := C.CString(tokensJSON)
	defer C.free(unsafe.Pointer(cNer))
	defer C.free(unsafe.Pointer(cTagger))
	defer C.free(unsafe.Pointer(cTokens))

	handle := C.ThincTagger_Create(cNer, cTagger)
	if handle == nil {
		return "", fmt.Errorf("failed to create ThincTagger handle")
	}
	defer C.ThincTagger_Destroy(handle)

	cResult := C.ThincTagger_Predict(handle, cTokens)
	if cResult == nil {
		return "", fmt.Errorf("tagger prediction failed")
	}
	defer C.ThincTagger_FreeString(cResult)

	return C.GoString(cResult), nil
}

// ParseTokensWithParser 运行 C++ 解析器并返回 DepToken 切片。
func ParseTokensWithParser(nerDir, parserDir string, tokens []string) ([]DepTokenC, error) {
	tj, _ := json.Marshal(tokens)
	resultJSON, err := RunParser(nerDir, parserDir, string(tj))
	if err != nil {
		return nil, err
	}
	var tokensC []DepTokenC
	if err := json.Unmarshal([]byte(resultJSON), &tokensC); err != nil {
		return nil, fmt.Errorf("parse result: %w", err)
	}
	return tokensC, nil
}
