package log

// label_filter 实现 LogQL 标签谓词 Stage：字符串、数值、字节量、时长与 IP 等类型的比较与 and/or 组合。

import (
	"fmt"
	"strconv"
	"strings"
	"time"
	"unicode"

	"github.com/dustin/go-humanize"
	"github.com/prometheus/prometheus/model/labels"

	"github.com/grafana/loki/v3/pkg/logqlmodel"
)

var (
	_ LabelFilterer = &BinaryLabelFilter{}
	_ LabelFilterer = &BytesLabelFilter{}
	_ LabelFilterer = &DurationLabelFilter{}
	_ LabelFilterer = &NumericLabelFilter{}
	_ LabelFilterer = &StringLabelFilter{}
)

// LabelFilterType 对应标签过滤器中的 ==、!=、>、>=、<、<= 比较运算符。
// LabelFilterType is an enum for label filtering types.
type LabelFilterType int

// Possible LabelFilterType.
const (
	LabelFilterEqual LabelFilterType = iota
	LabelFilterNotEqual
	LabelFilterGreaterThan
	LabelFilterGreaterThanOrEqual
	LabelFilterLesserThan
	LabelFilterLesserThanOrEqual
)

func (f LabelFilterType) String() string {
	switch f {
	case LabelFilterEqual:
		return "=="
	case LabelFilterNotEqual:
		return "!="
	case LabelFilterGreaterThan:
		return ">"
	case LabelFilterGreaterThanOrEqual:
		return ">="
	case LabelFilterLesserThan:
		return "<"
	case LabelFilterLesserThanOrEqual:
		return "<="
	default:
		return ""
	}
}

// LabelFilterer 嵌入 Stage 接口，Process 返回 (line, ok) 表示当前行是否通过标签条件。
// LabelFilterer can filter extracted labels.
//
//sumtype:decl
type LabelFilterer interface {
	Stage
	fmt.Stringer

	// Seal trait
	isLabelFilterer()
}

type BinaryLabelFilter struct {
	Left  LabelFilterer
	Right LabelFilterer
	And   bool
}

// NewAndLabelFilter 构造二元 and 过滤器，Short-circuit 在 or 分支上提前返回。
// NewAndLabelFilter creates a new LabelFilterer from a and binary operation of two LabelFilterer.
func NewAndLabelFilter(left LabelFilterer, right LabelFilterer) *BinaryLabelFilter {
	return &BinaryLabelFilter{
		Left:  left,
		Right: right,
		And:   true,
	}
}

// NewOrLabelFilter creates a new LabelFilterer from a or binary operation of two LabelFilterer.
func NewOrLabelFilter(left LabelFilterer, right LabelFilterer) *BinaryLabelFilter {
	return &BinaryLabelFilter{
		Left:  left,
		Right: right,
	}
}

func (b *BinaryLabelFilter) Process(ts int64, line []byte, lbs *LabelsBuilder) ([]byte, bool) {
	line, lok := b.Left.Process(ts, line, lbs)
	if !b.And && lok {
		return line, true
	}
	line, rok := b.Right.Process(ts, line, lbs)
	if !b.And {
		return line, lok || rok
	}
	return line, lok && rok
}

func (b *BinaryLabelFilter) isLabelFilterer() {}

func (b *BinaryLabelFilter) RequiredLabelNames() []string {
	var names []string
	names = append(names, b.Left.RequiredLabelNames()...)
	names = append(names, b.Right.RequiredLabelNames()...)
	return uniqueString(names)
}

func (b *BinaryLabelFilter) String() string {
	var sb strings.Builder
	sb.WriteString("( ")
	sb.WriteString(b.Left.String())
	if b.And {
		sb.WriteString(" , ")
	} else {
		sb.WriteString(" or ")
	}
	sb.WriteString(b.Right.String())
	sb.WriteString(" )")
	return sb.String()
}

type NoopLabelFilter struct {
	*labels.Matcher
}

func (NoopLabelFilter) Process(_ int64, line []byte, _ *LabelsBuilder) ([]byte, bool) {
	return line, true
}

func (NoopLabelFilter) isLabelFilterer() {}

func (NoopLabelFilter) RequiredLabelNames() []string { return []string{} }

func (f NoopLabelFilter) String() string {
	if f.Matcher != nil {
		return f.Matcher.String()
	}
	return ""
}

// ReduceAndLabelFilter 将过滤器切片折叠为二叉 and 树，空切片返回 NoopLabelFilter。
// ReduceAndLabelFilter Reduces multiple label filterer into one using binary and operation.
func ReduceAndLabelFilter(filters []LabelFilterer) LabelFilterer {
	if len(filters) == 0 {
		return &NoopLabelFilter{}
	}
	if len(filters) == 1 {
		return filters[0]
	}
	result := filters[0]
	for _, f := range filters[1:] {
		result = NewAndLabelFilter(result, f)
	}
	return result
}

type BytesLabelFilter struct {
	Name  string
	Value uint64
	Type  LabelFilterType
}

// NewBytesLabelFilter 用 humanize.ParseBytes 解析标签值后与 uint64 阈值比较。
// NewBytesLabelFilter creates a new label filterer which parses bytes string representation (1KB) from the value of the named label
// and compares it with the given b value.
func NewBytesLabelFilter(t LabelFilterType, name string, b uint64) *BytesLabelFilter {
	return &BytesLabelFilter{
		Name:  name,
		Type:  t,
		Value: b,
	}
}

func (d *BytesLabelFilter) Process(_ int64, line []byte, lbs *LabelsBuilder) ([]byte, bool) {
	v, ok := lbs.Get(d.Name)
	if !ok {
		// we have not found this label.
		return line, false
	}
	value, err := humanize.ParseBytes(v)
	if err != nil {
		// Don't overwrite what might be a more useful error
		if !lbs.HasErr() {
			lbs.SetErr(errLabelFilter)
			lbs.SetErrorDetails(err.Error())
		}
		return line, true
	}
	switch d.Type {
	case LabelFilterEqual:
		return line, value == d.Value
	case LabelFilterNotEqual:
		return line, value != d.Value
	case LabelFilterGreaterThan:
		return line, value > d.Value
	case LabelFilterGreaterThanOrEqual:
		return line, value >= d.Value
	case LabelFilterLesserThan:
		return line, value < d.Value
	case LabelFilterLesserThanOrEqual:
		return line, value <= d.Value
	default:
		if !lbs.HasErr() {
			lbs.SetErr(errLabelFilter)
		}
		return line, true
	}
}

func (d *BytesLabelFilter) isLabelFilterer() {}

func (d *BytesLabelFilter) RequiredLabelNames() []string {
	return []string{d.Name}
}

func (d *BytesLabelFilter) String() string {
	b := strings.Map(func(r rune) rune {
		if unicode.IsSpace(r) {
			return -1
		}
		return r
	}, humanize.Bytes(d.Value)) // TODO: discuss whether this should just be bytes, B, to be more accurate.
	return fmt.Sprintf("%s%s%s", d.Name, d.Type, b)
}

type DurationLabelFilter struct {
	Name  string
	Value time.Duration
	Type  LabelFilterType
}

// NewDurationLabelFilter 解析 Go duration 字符串并与配置时长做有序比较。
// NewDurationLabelFilter creates a new label filterer which parses duration string representation (5s)
// from the value of the named label and compares it with the given d value.
func NewDurationLabelFilter(t LabelFilterType, name string, d time.Duration) *DurationLabelFilter {
	return &DurationLabelFilter{
		Name:  name,
		Type:  t,
		Value: d,
	}
}

func (d *DurationLabelFilter) Process(_ int64, line []byte, lbs *LabelsBuilder) ([]byte, bool) {
	v, ok := lbs.Get(d.Name)
	if !ok {
		// we have not found this label.
		return line, false
	}
	value, err := time.ParseDuration(v)
	if err != nil {
		// Don't overwrite what might be a more useful error
		if !lbs.HasErr() {
			lbs.SetErr(errLabelFilter)
			lbs.SetErrorDetails(err.Error())
		}
		return line, true
	}
	switch d.Type {
	case LabelFilterEqual:
		return line, value == d.Value
	case LabelFilterNotEqual:
		return line, value != d.Value
	case LabelFilterGreaterThan:
		return line, value > d.Value
	case LabelFilterGreaterThanOrEqual:
		return line, value >= d.Value
	case LabelFilterLesserThan:
		return line, value < d.Value
	case LabelFilterLesserThanOrEqual:
		return line, value <= d.Value
	default:
		if !lbs.HasErr() {
			lbs.SetErr(errLabelFilter)
		}
		return line, true
	}
}

func (d *DurationLabelFilter) isLabelFilterer() {}

func (d *DurationLabelFilter) RequiredLabelNames() []string {
	return []string{d.Name}
}

func (d *DurationLabelFilter) String() string {
	return fmt.Sprintf("%s%s%s", d.Name, d.Type, d.Value)
}

type NumericLabelFilter struct {
	Name  string
	Value float64
	Type  LabelFilterType
	err   error
}

// NewNumericLabelFilter 将标签值 ParseFloat 后与浮点阈值比较，解析失败写入 errLabelFilter。
// NewNumericLabelFilter creates a new label filterer which parses float64 string representation (5.2)
// from the value of the named label and compares it with the given f value.
func NewNumericLabelFilter(t LabelFilterType, name string, v float64) *NumericLabelFilter {
	return &NumericLabelFilter{
		Name:  name,
		Type:  t,
		Value: v,
	}
}

func (n *NumericLabelFilter) Process(_ int64, line []byte, lbs *LabelsBuilder) ([]byte, bool) {
	v, ok := lbs.Get(n.Name)
	if !ok {
		// we have not found this label.
		return line, false
	}
	value, err := strconv.ParseFloat(v, 64)
	if err != nil {
		// Don't overwrite what might be a more useful error
		if !lbs.HasErr() {
			lbs.SetErr(errLabelFilter)
			lbs.SetErrorDetails(err.Error())
		}
		return line, true
	}
	switch n.Type {
	case LabelFilterEqual:
		return line, value == n.Value
	case LabelFilterNotEqual:
		return line, value != n.Value
	case LabelFilterGreaterThan:
		return line, value > n.Value
	case LabelFilterGreaterThanOrEqual:
		return line, value >= n.Value
	case LabelFilterLesserThan:
		return line, value < n.Value
	case LabelFilterLesserThanOrEqual:
		return line, value <= n.Value
	default:
		if !lbs.HasErr() {
			lbs.SetErr(errLabelFilter)
		}
		return line, true
	}

}

func (n *NumericLabelFilter) isLabelFilterer() {}

func (n *NumericLabelFilter) RequiredLabelNames() []string {
	return []string{n.Name}
}

func (n *NumericLabelFilter) String() string {
	return fmt.Sprintf("%s%s%s", n.Name, n.Type, strconv.FormatFloat(n.Value, 'f', -1, 64))
}

type StringLabelFilter struct {
	*labels.Matcher
}

// NewStringLabelFilter 可过滤 __error__ 标签；正则模式可降级为 LineFilterLabelFilter 优化路径。
// NewStringLabelFilter creates a new label filterer which compares string label.
// This is the only LabelFilterer that can filter out the __error__ label.
// Unlike other LabelFilterer which apply conversion, if the label name doesn't exist it is compared with an empty value.
func NewStringLabelFilter(m *labels.Matcher) LabelFilterer {
	f, err := NewLabelFilter(m.Value, m.Type)
	if err != nil {
		return &StringLabelFilter{Matcher: m}
	}

	if f == TrueFilter {
		return &NoopLabelFilter{m}
	}

	return &LineFilterLabelFilter{
		Matcher: m,
		Filter:  f,
	}
}

func (s *StringLabelFilter) Process(_ int64, line []byte, lbs *LabelsBuilder) ([]byte, bool) {
	return line, s.Matches(labelValue(s.Name, lbs))
}

func (s *StringLabelFilter) isLabelFilterer() {}

func (s *StringLabelFilter) RequiredLabelNames() []string {
	return []string{s.Name}
}

// LineFilterLabelFilter filters the desired label using an optimized line filter
type LineFilterLabelFilter struct {
	*labels.Matcher
	Filter Filterer
}

// overrides the matcher.String() function in case there is a regexpFilter
func (s *LineFilterLabelFilter) String() string {
	if unwrappedFilter, ok := s.Filter.(regexpFilter); ok {
		rStr := unwrappedFilter.String()
		if strings.Contains(rStr, "`") {
			rStr = strconv.Quote(rStr)
		} else {
			rStr = fmt.Sprintf("`%s`", rStr)
		}
		str := fmt.Sprintf("%s%s%s", s.Name, s.Type, rStr)
		return str
	}
	return s.Matcher.String()
}

func (s *LineFilterLabelFilter) Process(_ int64, line []byte, lbs *LabelsBuilder) ([]byte, bool) {
	v := labelValue(s.Name, lbs)
	return line, s.Filter.Filter(unsafeGetBytes(v))
}

func (s *LineFilterLabelFilter) isLabelFilterer() {}

func (s *LineFilterLabelFilter) RequiredLabelNames() []string {
	return []string{s.Name}
}

// labelValue 读取普通标签或 __error__ 专用值，供字符串与行过滤器求值。
func labelValue(name string, lbs *LabelsBuilder) string {
	if name == logqlmodel.ErrorLabel {
		return lbs.GetErr()
	}
	v, _ := lbs.Get(name)
	return v
}
// BinaryLabelFilter.Process 对 and/or 分别要求两侧均真或任一侧为真，并合并 RequiredLabelNames。
