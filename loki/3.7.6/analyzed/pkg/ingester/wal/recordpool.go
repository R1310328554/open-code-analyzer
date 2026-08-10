package wal

// ResettingPool 通过 sync.Pool 复用 wal.Record 与编码用 []byte 切片，Get 时 Reset 清空状态，Put 时截断字节缓冲归还池。

import (
	"sync"
)

type ResettingPool struct {
	rPool *sync.Pool // records
	bPool *sync.Pool // bytes
}

func NewRecordPool() *ResettingPool {
	return &ResettingPool{
		rPool: &sync.Pool{
			New: func() interface{} {
				return &Record{}
			},
		},
		bPool: &sync.Pool{
			New: func() interface{} {
				buf := new([]byte)            // Attempt to force allocation on heap.
				*buf = make([]byte, 0, 1<<10) // 1kb
				return buf
			},
		},
	}
}

// GetRecord 从池取出 Record 并调用 Reset 清空 UserID/Series/RefEntries。
func (p *ResettingPool) GetRecord() *Record {
	rec := p.rPool.Get().(*Record)
	rec.Reset()
	return rec
}

func (p *ResettingPool) PutRecord(r *Record) {
	p.rPool.Put(r)
}

func (p *ResettingPool) GetBytes() *[]byte {
	return p.bPool.Get().(*[]byte)
}

// PutBytes 将切片长度归零后归还池，保留底层 capacity 供下次编码。
func (p *ResettingPool) PutBytes(b *[]byte) {
	*b = (*b)[:0]
	p.bPool.Put(b)
}
// 指针包装 []byte 强制堆分配，避免 sync.Pool 存值类型时的逃逸问题。
