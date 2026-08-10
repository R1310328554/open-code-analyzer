package util //nolint:revive

// util 包 ExtendedMock 扩展 testify/mock.Mock，按方法名过滤已记录调用，便于断言特定 RPC 或存储接口交互次数。

import (
	"github.com/stretchr/testify/mock"
)

type ExtendedMock struct {
	mock.Mock
}

func (m *ExtendedMock) GetMockedCallsByMethod(method string) []mock.Call {
	calls := make([]mock.Call, 0)

	for _, call := range m.Calls {
		if call.Method == method {
			calls = append(calls, call)
		}
	}

	return calls
}
// 典型用于单测中验证 Delete/Get 等方法的调用参数与次数，无需手动遍历 Calls。
