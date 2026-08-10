// PyTorch 读取：从 .bin/.pth checkpoint 解析张量（gopickle）。
package convert

import (
	"io"
	"io/fs"
	"strings"

	"github.com/nlpodyssey/gopickle/pytorch"
	"github.com/nlpodyssey/gopickle/types"
)

// parseTorch 加载 PyTorch pickle 权重并构建 torch Tensor 列表。
func parseTorch(fsys fs.FS, replacer *strings.Replacer, ps ...string) ([]Tensor, error) {
	var ts []Tensor
	for _, p := range ps {
		pt, err := pytorch.Load(p)
		if err != nil {
			return nil, err
		}

		for _, k := range pt.(*types.Dict).Keys() {
			t := pt.(*types.Dict).MustGet(k)

			var shape []uint64
			for dim := range t.(*pytorch.Tensor).Size {
				shape = append(shape, uint64(dim))
			}

			ts = append(ts, torch{
				storage: t.(*pytorch.Tensor).Source,
				tensorBase: &tensorBase{
					name:  replacer.Replace(k.(string)),
					shape: shape,
				},
			})
		}
	}

	return ts, nil
}

// torch 实现 Tensor，从 PyTorch storage 懒读取（WriteTo 暂未实现）。
type torch struct {
	storage pytorch.StorageInterface
	*tensorBase
}

// Clone 复制 torch 张量元数据。
func (t torch) Clone() Tensor {
	return torch{
		storage: t.storage,
		tensorBase: &tensorBase{
			name:     t.name,
			shape:    t.shape,
			repacker: t.repacker,
		},
	}
}

// WriteTo 占位实现（PyTorch 路径当前不写入数据）。
func (pt torch) WriteTo(w io.Writer) (int64, error) {
	return 0, nil
}
