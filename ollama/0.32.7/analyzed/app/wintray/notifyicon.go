//go:build windows

// Package wintray（notifyicon）封装 Shell_NotifyIcon 托盘图标数据结构。
package wintray

import (
	"unsafe"

	"golang.org/x/sys/windows"
)

// notifyIconData 对应 NOTIFYICONDATA，供 Shell_NotifyIcon 增删改托盘图标。
// Contains information that the system needs to display notifications in the notification area.
// Used by Shell_NotifyIcon.
// https://msdn.microsoft.com/en-us/library/windows/desktop/bb773352(v=vs.85).aspx
// https://msdn.microsoft.com/en-us/library/windows/desktop/bb762159
type notifyIconData struct {
	Size                       uint32
	Wnd                        windows.Handle
	ID, Flags, CallbackMessage uint32
	Icon                       windows.Handle
	Tip                        [128]uint16
	State, StateMask           uint32
	Info                       [256]uint16
	// Timeout, Version           uint32
	Timeout uint32

	InfoTitle   [64]uint16
	InfoFlags   uint32
	GuidItem    windows.GUID
	BalloonIcon windows.Handle
}

// add 向通知区域添加托盘图标（NIM_ADD）。
func (nid *notifyIconData) add() error {
	const NIM_ADD = 0x00000000
	res, _, err := pShellNotifyIcon.Call(
		uintptr(NIM_ADD),
		uintptr(unsafe.Pointer(nid)),
	)
	if res == 0 {
		return err
	}
	return nil
}

// modify 更新已有托盘图标或气球提示（NIM_MODIFY）。
func (nid *notifyIconData) modify() error {
	const NIM_MODIFY = 0x00000001
	res, _, err := pShellNotifyIcon.Call(
		uintptr(NIM_MODIFY),
		uintptr(unsafe.Pointer(nid)),
	)
	if res == 0 {
		return err
	}
	return nil
}

// delete 从通知区域移除托盘图标（NIM_DELETE）。
func (nid *notifyIconData) delete() error {
	const NIM_DELETE = 0x00000002
	res, _, err := pShellNotifyIcon.Call(
		uintptr(NIM_DELETE),
		uintptr(unsafe.Pointer(nid)),
	)
	if res == 0 {
		return err
	}
	return nil
}
