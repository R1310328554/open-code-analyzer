package computetest

// computetest 为 compute 包提供测试用例解析：
// 从文本读取函数名、参数 Datum、selection 位图与期望结果。

import (
	"io"

	"github.com/grafana/loki/v3/pkg/columnar"
	"github.com/grafana/loki/v3/pkg/memory"
)

// Case 表示一条解析后的 compute 测试用例，含源行号与期望 Datum。
// Case is a parsed test case.
type Case struct {
	Line int // Source line of the test case.

	Function  string
	Arguments []columnar.Datum
	Selection memory.Bitmap
	Expect    columnar.Datum
}

// ParseCases 从 io.Reader 读取并解析全部测试用例，内部使用 scanner 与 parser。
// ParseCases parses all test cases from the given reader.
func ParseCases(r io.Reader) ([]Case, error) {
	p := &parser{
		scanner: newScanner(r),
		alloc:   memory.NewAllocator(nil),
	}

	return p.Parse()
}
