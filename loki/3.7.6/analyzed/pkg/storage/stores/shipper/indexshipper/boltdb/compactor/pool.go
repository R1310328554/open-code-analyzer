package compactor

// componentRef 对象池：复用 [][]byte 切片缓冲，供索引键解析时临时存放分隔组件。

import (
	"sync"
)

type componentRef struct {
	components [][]byte
}

var (
	componentPools = sync.Pool{
		New: func() interface{} {
			return &componentRef{
				components: make([][]byte, 0, 5),
			}
		},
	}
)

// getComponents 从 sync.Pool 取出 componentRef 并清空已有组件。
func getComponents() *componentRef {
	ref := componentPools.Get().(*componentRef)
	ref.components = ref.components[:0]
	return ref
}

func putComponents(ref *componentRef) {
	componentPools.Put(ref)
}
// 索引键解析路径应成对调用 getComponents/putComponents 以防切片泄漏到堆上。
