package compute

// UTF-8 字符串谓词：正则匹配、大小写敏感/不敏感子串搜索。

import (
	"bytes"
	"errors"
	"fmt"

	"github.com/grafana/regexp"

	"github.com/grafana/loki/v3/pkg/columnar"
	"github.com/grafana/loki/v3/pkg/memory"
)

// RegexpMatch 对 haystack UTF-8 Datum 执行正则匹配，返回布尔 Datum。
// RegexpMatch computes the regular expression match against a datum.
// It returns a boolean datum indicating whether the regular expression matched the haystack datum.
//
// Special cases:
//
//   - If a value in the haystack is null, the result for that value is null.
//   - If the regexp is null, the result is null.
func RegexpMatch(alloc *memory.Allocator, haystack columnar.Datum, regexp *regexp.Regexp, selection memory.Bitmap) (columnar.Datum, error) {
	if haystack.Kind() != columnar.KindUTF8 {
		return nil, fmt.Errorf("haystack must be UTF-8; got %s", haystack.Kind())
	}

	_, haystackArray := haystack.(columnar.Array)

	switch {
	case haystackArray:
		return regexpMatchAS(alloc, haystack.(*columnar.UTF8), regexp, selection)
	case !haystackArray:
		return regexpMatchSS(alloc, haystack.(*columnar.UTF8Scalar), regexp)
	default:
		return nil, errors.New("unsupported haystack and regexp types")
	}
}

// regexpMatchAS 对 UTF-8 数组逐行调用 regexp.Match。
func regexpMatchAS(alloc *memory.Allocator, haystack *columnar.UTF8, regexp *regexp.Regexp, selection memory.Bitmap) (*columnar.Bool, error) {
	if regexp == nil {
		builder := columnar.NewBoolBuilder(alloc)
		builder.AppendNulls(haystack.Len())
		return builder.Build(), nil
	}

	validity, err := computeValidityAA(alloc, haystack.Validity(), selection)
	if err != nil {
		return nil, fmt.Errorf("apply selection to validity: %w", err)
	}

	values := memory.NewBitmap(alloc, haystack.Len())
	values.Resize(haystack.Len())

	for i := range iterTrue(validity, haystack.Len()) {
		values.Set(i, regexp.Match(haystack.Get(i)))
	}

	return columnar.NewBool(values, validity), nil
}

func regexpMatchSS(_ *memory.Allocator, haystack *columnar.UTF8Scalar, regexp *regexp.Regexp) (*columnar.BoolScalar, error) {
	if regexp == nil || haystack.IsNull() {
		return &columnar.BoolScalar{Null: true}, nil
	}

	return &columnar.BoolScalar{Value: regexp.Match(haystack.Value)}, nil
}

// SubstrInsensitive 大小写不敏感子串匹配，needle 仅支持标量。
// SubstrInsensitive computes the case insensitive match of a needle datum against haystack datum
// It returns a boolean datum indicating whether the needle was found in the haystack.
//
// Special cases:
//
//   - If a value in the haystack is null, the result for that value is null.
//   - If the needle is null, the result is null.
func SubstrInsensitive(alloc *memory.Allocator, haystack columnar.Datum, needle columnar.Datum, selection memory.Bitmap) (columnar.Datum, error) {
	if haystack.Kind() != columnar.KindUTF8 || needle.Kind() != columnar.KindUTF8 {
		return nil, fmt.Errorf("haystack and needle must both be UTF-8; got %s and %s", haystack.Kind(), needle.Kind())
	}

	if _, needleArray := needle.(columnar.Array); needleArray {
		return nil, errors.New("needle array unsupported")
	}

	_, haystackArray := haystack.(columnar.Array)

	switch {
	case haystackArray:
		return substrInsensitiveAS(alloc, haystack.(*columnar.UTF8), needle.(*columnar.UTF8Scalar), selection)
	case !haystackArray:
		return substrInsensitiveSS(alloc, haystack.(*columnar.UTF8Scalar), needle.(*columnar.UTF8Scalar))
	default:
		return nil, errors.New("unsupported haystack and needle types")
	}
}

func substrInsensitiveAS(alloc *memory.Allocator, haystack *columnar.UTF8, needle *columnar.UTF8Scalar, selection memory.Bitmap) (*columnar.Bool, error) {
	if needle.IsNull() {
		builder := columnar.NewBoolBuilder(alloc)
		builder.AppendNulls(haystack.Len())
		return builder.Build(), nil
	}

	validity, err := computeValidityAA(alloc, haystack.Validity(), selection)
	if err != nil {
		return nil, fmt.Errorf("apply selection to validity: %w", err)
	}

	needleUpper := bytes.ToUpper(needle.Value)

	values := memory.NewBitmap(alloc, haystack.Len())
	values.Resize(haystack.Len())

	for i := range iterTrue(validity, haystack.Len()) {
		haystackValueUpper := bytes.ToUpper(haystack.Get(i))
		values.Set(i, bytes.Contains(haystackValueUpper, needleUpper))
	}

	return columnar.NewBool(values, validity), nil
}

func substrInsensitiveSS(_ *memory.Allocator, haystack *columnar.UTF8Scalar, needle *columnar.UTF8Scalar) (*columnar.BoolScalar, error) {
	if needle.IsNull() || haystack.IsNull() {
		return &columnar.BoolScalar{Null: true}, nil
	}

	needleUpper := bytes.ToUpper(needle.Value)

	haystackValueUpper := bytes.ToUpper(haystack.Value)
	return &columnar.BoolScalar{Value: bytes.Contains(haystackValueUpper, needleUpper)}, nil
}

// Substr 大小写敏感子串匹配，needle 为 null 时结果全 null。
// Substr computes the case sensitive match of a needle datum against haystack datum
// It returns a boolean datum indicating whether the needle was found in the haystack.
//
// Special cases:
//
//   - If a value in the haystack is null, the result for that value is null.
//   - If the needle is null, the result is null.
func Substr(alloc *memory.Allocator, haystack columnar.Datum, needle columnar.Datum, selection memory.Bitmap) (columnar.Datum, error) {
	if haystack.Kind() != columnar.KindUTF8 || needle.Kind() != columnar.KindUTF8 {
		return nil, fmt.Errorf("haystack and needle must both be UTF-8; got %s and %s", haystack.Kind(), needle.Kind())
	}

	if _, needleArray := needle.(columnar.Array); needleArray {
		return nil, errors.New("needle array unsupported")
	}

	_, haystackArray := haystack.(columnar.Array)

	switch {
	case haystackArray:
		return substrAS(alloc, haystack.(*columnar.UTF8), needle.(*columnar.UTF8Scalar), selection)
	case !haystackArray:
		return substrSS(alloc, haystack.(*columnar.UTF8Scalar), needle.(*columnar.UTF8Scalar))
	default:
		return nil, errors.New("unsupported haystack and needle types")
	}
}

// substrAS 对数组各行执行 bytes.Contains 子串检测。
func substrAS(alloc *memory.Allocator, haystack *columnar.UTF8, needle *columnar.UTF8Scalar, selection memory.Bitmap) (*columnar.Bool, error) {
	if needle.IsNull() {
		builder := columnar.NewBoolBuilder(alloc)
		builder.AppendNulls(haystack.Len())
		return builder.Build(), nil
	}

	validity, err := computeValidityAA(alloc, haystack.Validity(), selection)
	if err != nil {
		return nil, fmt.Errorf("apply selection to validity: %w", err)
	}

	values := memory.NewBitmap(alloc, haystack.Len())
	values.Resize(haystack.Len())

	for i := range iterTrue(validity, haystack.Len()) {
		values.Set(i, bytes.Contains(haystack.Get(i), needle.Value))
	}

	return columnar.NewBool(values, validity), nil
}

func substrSS(_ *memory.Allocator, haystack *columnar.UTF8Scalar, needle *columnar.UTF8Scalar) (*columnar.BoolScalar, error) {
	if needle.IsNull() || haystack.IsNull() {
		return &columnar.BoolScalar{Null: true}, nil
	}

	return &columnar.BoolScalar{Value: bytes.Contains(haystack.Value, needle.Value)}, nil
}
// haystack 为 null 或 regexp/needle 为 null 时对应行结果为 null。
