package bufpool

// unsized 定义无分桶 bytes.Buffer 池，供 protocodec 等无法预估大小的解码路径使用。

import (
	"bytes"
	"sync"
)

var unsizedPool = sync.Pool{
	New: func() any {
		return new(bytes.Buffer)
	},
}
// 与分桶池互补：不绑定容量档位，避免错误分桶导致内存膨胀。
