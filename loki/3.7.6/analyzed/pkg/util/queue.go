package util //nolint:revive

// util 包 Queue 接口定义通用 FIFO 队列：Append 入队、Entries 快照、Length 计数与 Clear 清空，供测试桩与内存缓冲复用。

type Queue interface {
	Append(entry interface{})
	Entries() []interface{}
	Length() int
	Clear()
}
// Entries 返回底层切片引用，Clear 后 Length 归零但不保证释放容量。
