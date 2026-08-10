package util //nolint:revive

// util 包 YoloBuf 以 unsafe 将 string 零拷贝转为 []byte：仅用于已知生命周期内只读场景，禁止让返回切片逃逸到可变引用。

import "unsafe"

func YoloBuf(s string) []byte {
	return *((*[]byte)(unsafe.Pointer(&s))) //#nosec G103 -- This is used correctly; all uses of this function do not allow the mutable reference to escape -- nosemgrep: use-of-unsafe-block
}
// nolint 与 nosemgrep 标注表明团队已审计全部调用点，禁止在新代码中滥用。
