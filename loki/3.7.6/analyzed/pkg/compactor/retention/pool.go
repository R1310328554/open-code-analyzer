package retention

// 保留标记处理对象池：复用 keyPair 缓冲以减少 markerProcessor
// 遍历 BoltDB 键值对时的内存分配开销。

import (
	"bytes"
	"sync"
)

var (
	keyPool = sync.Pool{
		New: func() interface{} {
			return &keyPair{
				key:   bytes.NewBuffer(make([]byte, 0, 8)),
				value: bytes.NewBuffer(make([]byte, 0, 512)),
			}
		},
	}
)

// keyPair 持有标记文件 cursor 键值对的 bytes.Buffer 复用缓冲。
type keyPair struct {
	key   *bytes.Buffer
	value *bytes.Buffer
}

func getKeyPairBuffer(key, value []byte) (*keyPair, error) {
	keyBuf := keyPool.Get().(*keyPair)
	if _, err := keyBuf.key.Write(key); err != nil {
		putKeyBuffer(keyBuf)
		return nil, err
	}
	if _, err := keyBuf.value.Write(value); err != nil {
		putKeyBuffer(keyBuf)
		return nil, err
	}
	return keyBuf, nil
}

// putKeyBuffer 重置缓冲后将 keyPair 归还对象池。
func putKeyBuffer(pair *keyPair) {
	pair.key.Reset()
	pair.value.Reset()
	keyPool.Put(pair)
}
