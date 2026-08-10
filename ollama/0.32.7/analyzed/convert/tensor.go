// 张量拆分与合并：模型转换时切分、合并 HF 权重并追踪 FP8 源量化。
package convert

import (
	"cmp"
	"errors"
	"io"
	"iter"
	"maps"
	"path"
	"slices"
	"strconv"
	"strings"

	"github.com/pdevine/tensor"
	"github.com/pdevine/tensor/native"

	"github.com/ollama/ollama/fs/ggml"
)

// split 描述沿某一维切分张量时的替换规则与可选后处理。
type split struct {
	*strings.Replacer
	dim    int
	slices []tensor.Slice

	// afterFunc 在切片完成后可选地对张量做额外变换。
	// afterFunc is an optional function to apply to the tensor after slicing
	afterFunc func(tensor.Tensor) (tensor.Tensor, error)
}

// splitDim 沿指定维度将张量均分为多块，每块可重命名并可选后处理。
// splitDim splits a tensor along a specified dimension into multiple tensors. The dimension
// is split evenly based on the number of replacers provided unless a specific count is given.
func splitDim(t Tensor, dim int, splits ...split) iter.Seq[*ggml.Tensor] {
	return func(yield func(*ggml.Tensor) bool) {
		var offset int
		for _, split := range splits {
			t := t.Clone()
			shape := slices.Clone(t.Shape())
			shape[dim] = cmp.Or(uint64(split.dim), shape[dim]/uint64(len(splits)))

			slice := split.slices
			if len(slice) == 0 {
				slice = slices.Repeat([]tensor.Slice{nil}, len(shape))
				slice[dim] = tensor.S(offset, offset+int(shape[dim]))
				offset += int(shape[dim])
			}

			t.SetRepacker(func(_ string, data []float32, shape []uint64) ([]float32, error) {
				dims := make([]int, len(shape))
				for i := range shape {
					dims[i] = int(shape[i])
				}

				var tt tensor.Tensor = tensor.New(tensor.WithShape(dims...), tensor.WithBacking(data))
				tt, err := tt.Slice(slice...)
				if err != nil {
					return nil, err
				}

				tt = tensor.Materialize(tt)

				if split.afterFunc != nil {
					tt, err = split.afterFunc(tt)
					if err != nil {
						return nil, err
					}
				}

				// 展平为一维向量以便写入 GGUF。
			// flatten tensor so it can be written as a vector
				if err := tt.Reshape(tt.Shape().TotalSize()); err != nil {
					return nil, err
				}

				return native.VectorF32(tt.(*tensor.Dense))
			})

			if !yield(&ggml.Tensor{
				Name:     split.Replace(t.Name()),
				Kind:     t.Kind(),
				Shape:    shape,
				WriterTo: t,
			}) {
				break
			}
		}
	}
}

// merge 将 glob 模式匹配的多个张量合并为一个。
type merge struct {
	pattern, name string
}

// mergeTensors 按模式匹配并排序后，将同名前缀张量堆叠合并。
// mergeTensors merges tensors that match a given pattern into a single tensor.
func mergeTensors(unmatched []Tensor, merges ...merge) (out []*ggml.Tensor, _ []Tensor) {
	var matched []Tensor
	for i := range merges {
		matched, unmatched = slicesSplitFunc(unmatched, func(t Tensor) bool {
			matched, _ := path.Match(merges[i].pattern, t.Name())
			return matched
		})

		slices.SortStableFunc(matched, func(a, b Tensor) int {
			x := strings.Split(a.Name(), ".")
			y := strings.Split(b.Name(), ".")
			if len(x) != len(y) {
				return cmp.Compare(len(x), len(y))
			}

			vals := make([]int, len(x))
			for i := range x {
				vals[i] = strings.Compare(x[i], y[i])
				m, err := strconv.ParseInt(x[i], 0, 0)
				n, err2 := strconv.ParseInt(y[i], 0, 0)
				if errors.Join(err, err2) == nil {
					vals[i] = cmp.Compare(m, n)
				}
			}

			return cmp.Or(vals...)
		})

		if len(matched) > 0 {
			out = append(out, &ggml.Tensor{
				Name:     merges[i].name,
				Kind:     matched[0].Kind(),
				Shape:    append([]uint64{uint64(len(matched))}, matched[0].Shape()...),
				WriterTo: mergeGroup(matched),
			})
		}
	}

	return out, unmatched
}

// slicesSplitFunc 按谓词将切片分为匹配与未匹配两部分。
// slicesSplitFunc splits a slice into two slices based on a predicate function.
func slicesSplitFunc[S ~[]E, E comparable](s S, fn func(e E) bool) (matched, unmatched S) {
	for _, e := range s {
		if fn(e) {
			matched = append(matched, e)
		} else {
			unmatched = append(unmatched, e)
		}
	}

	return matched, unmatched
}

// mergeGroup 将多个 Tensor 顺序写入同一 io.Writer。
type mergeGroup []Tensor

func (g mergeGroup) WriteTo(w io.Writer) (int64, error) {
	for _, t := range g {
		if _, err := t.WriteTo(w); err != nil {
			return 0, err
		}
	}

	return 0, nil
}

// sourceTensorKV 若存在 F8_E4M3 源张量则写入 HF FP8 量化元数据。
func sourceTensorKV(ts []*ggml.Tensor) KV {
	sourceFP8 := make(map[string]struct{})
	for _, t := range ts {
		if writerSourceDType(t.WriterTo) == "F8_E4M3" {
			sourceFP8[t.Name] = struct{}{}
		}
	}
	if len(sourceFP8) == 0 {
		return nil
	}

	return KV{
		"source_quantization": "hf_fp8",
		"source_fp8_tensors":  slices.Sorted(maps.Keys(sourceFP8)),
	}
}

// sourceDTypeTensor 暴露原始 HuggingFace 数据类型。
type sourceDTypeTensor interface {
	SourceDType() string
}

// writerSourceDType 从 WriterTo 链推断统一的源 dtype。
func writerSourceDType(w io.WriterTo) string {
	switch w := w.(type) {
	case sourceDTypeTensor:
		return w.SourceDType()
	case mergeGroup:
		if len(w) == 0 {
			return ""
		}
		dtype := sourceDType(w[0])
		if dtype == "" {
			return ""
		}
		for _, t := range w[1:] {
			if sourceDType(t) != dtype {
				return ""
			}
		}
		return dtype
	default:
		return ""
	}
}

// sourceDType 读取单张 Tensor 的源 dtype。
func sourceDType(t Tensor) string {
	if t, ok := t.(sourceDTypeTensor); ok {
		return t.SourceDType()
	}
	return ""
}
