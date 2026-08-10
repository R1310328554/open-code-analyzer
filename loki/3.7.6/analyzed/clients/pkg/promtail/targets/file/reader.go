package file

// Reader 接口：tailer 与 decompressor 统一生命周期，供 FileTarget 管理与 positions 同步。

// Stop/IsRunning/Path/MarkPositionAndSize 为 filetarget sync 与 reportSize 所需最小集合。
// Reader contains the set of expected calls the file target manager relies on.
type Reader interface {
	Stop()
	IsRunning() bool
	Path() string
	MarkPositionAndSize() error
}
