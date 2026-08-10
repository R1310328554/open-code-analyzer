//go:build windows || darwin

// Package dialog 提供跨平台通用对话框 API（消息框、文件与目录选择）。
// 典型用法：dialog.Message("%s", "继续？").YesNo()
// Package dialog provides// Package dialog provides a simple cross-platform common dialog API.
// Eg. to prompt the user with a yes/no dialog:
//
//	if dialog.MsgDlg("%s", "Do you want to continue?").YesNo() {
//	    // user pressed Yes
//	}
//
// The general usage pattern is to call one of the toplevel *Dlg functions
// which return a *Builder structure. From here you can optionally call
// configuration functions (eg. Title) to customise the dialog, before
// using a launcher function to run the dialog.
package dialog

import (
	"errors"
	"fmt"
)

// ErrCancelled 表示用户取消或关闭对话框。
// ErrCancelled is an error returned when a user cancels/closes a dialog.
var ErrCancelled = errors.New("Cancelled")

// Cancelled 为 ErrCancelled 的别名（已弃用）。
// Cancelled refers to ErrCancelled.
// Deprecated: Use ErrCancelled instead.
var Cancelled = ErrCancelled

// Dlg 为各类对话框共用的标题等基础字段。
// Dlg is the common type for dialogs.
type Dlg struct {
	Title string
}

// MsgBuilder 用于构建消息框。
// MsgBuilder is used for creating message boxes.
type MsgBuilder struct {
	Dlg
	Msg string
}

// Message 创建带格式化正文的消息框构建器。
// Message initialises a MsgBuilder with the provided message.
func Message(format string, args ...interface{}) *MsgBuilder {
	return &MsgBuilder{Msg: fmt.Sprintf(format, args...)}
}

// Title 设置消息框标题。
// Title specifies what the title of the message dialog will be.
func (b *MsgBuilder) Title(title string) *MsgBuilder {
	b.Dlg.Title = title
	return b
}

// YesNo 弹出「是/否」对话框，选「是」返回 true。
// YesNo spawns the message dialog with two buttons, "Yes" and "No".
// Returns true iff the user selected "Yes".
func (b *MsgBuilder) YesNo() bool {
	return b.yesNo()
}

// Info 弹出带信息图标的「确定」对话框。
// Info spawns the message dialog with an information icon and single button, "Ok".
func (b *MsgBuilder) Info() {
	b.info()
}

// Error 弹出带错误图标的「确定」对话框。
// Error spawns the message dialog with an error icon and single button, "Ok".
func (b *MsgBuilder) Error() {
	b.error()
}

// FileFilter 表示一类可选文件（如音频、表格）。
// FileFilter represents a category of files (eg. audio files, spreadsheets).
type FileFilter struct {
	Desc       string
	Extensions []string
}

// FileBuilder 用于配置文件打开/保存对话框。
// FileBuilder is used for creating file browsing dialogs.
type FileBuilder struct {
	Dlg
	StartDir        string
	StartFile       string
	Filters         []FileFilter
	ShowHiddenFiles bool
}

// File 创建默认配置的 FileBuilder。
// File initialises a FileBuilder using the default configuration.
func File() *FileBuilder {
	return &FileBuilder{}
}

// Title 设置文件对话框标题。
// Title specifies the title to be used for the dialog.
func (b *FileBuilder) Title(title string) *FileBuilder {
	b.Dlg.Title = title
	return b
}

// Filter 添加允许的文件类型分类；多次调用累加；"*" 表示全部文件。
// Filter adds a category of files to the types allowed by the dialog. Multiple
// calls to Filter are cumulative - any of the provided categories will be allowed.
// By default all files can be selected.
//
// The special extension '*' allows all files to be selected when the Filter is active.
func (b *FileBuilder) Filter(desc string, extensions ...string) *FileBuilder {
	filt := FileFilter{desc, extensions}
	if len(filt.Extensions) == 0 {
		filt.Extensions = append(filt.Extensions, "*")
	}
	b.Filters = append(b.Filters, filt)
	return b
}

// SetStartDir 设置对话框初始目录。
// SetStartDir specifies the initial directory of the dialog.
func (b *FileBuilder) SetStartDir(startDir string) *FileBuilder {
	b.StartDir = startDir
	return b
}

// SetStartFile 设置对话框初始文件名。
// SetStartFile specifies the initial file name of the dialog.
func (b *FileBuilder) SetStartFile(startFile string) *FileBuilder {
	b.StartFile = startFile
	return b
}

// ShowHidden 设置是否显示隐藏文件。
// ShowHiddenFiles sets whether hidden files should be visible in the dialog.
func (b *FileBuilder) ShowHidden(show bool) *FileBuilder {
	b.ShowHiddenFiles = show
	return b
}

// Load 打开文件选择对话框并返回单个路径；取消返回 ErrCancelled。
// Load spawns the file selection dialog using the configured settings,
// asking the user to select a single file. Returns ErrCancelled as the error
// if the user cancels or closes the dialog.
func (b *FileBuilder) Load() (string, error) {
	return b.load()
}

// LoadMultiple 打开多文件选择对话框。
// LoadMultiple spawns the file selection dialog using the configured settings,
// asking the user to select multiple files. Returns ErrCancelled as the error
// if the user cancels or closes the dialog.
func (b *FileBuilder) LoadMultiple() ([]string, error) {
	return b.loadMultiple()
}

// Save 打开保存对话框；文件已存在时会询问是否覆盖。
// Save spawns the file selection dialog using the configured settings,
// asking the user for a filename to save as. If the chosen file exists, the
// user is prompted whether they want to overwrite the file. Returns
// ErrCancelled as the error if the user cancels/closes the dialog, or selects
// not to overwrite the file.
func (b *FileBuilder) Save() (string, error) {
	return b.save()
}

// DirectoryBuilder 用于配置目录浏览对话框。
// DirectoryBuilder is used for directory browse dialogs.
type DirectoryBuilder struct {
	Dlg
	StartDir        string
	ShowHiddenFiles bool
}

// Directory 创建默认 DirectoryBuilder。
// Directory initialises a DirectoryBuilder using the default configuration.
func Directory() *DirectoryBuilder {
	return &DirectoryBuilder{}
}

// Browse 打开目录选择对话框。
// Browse spawns the directory selection dialog using the configured settings,
// asking the user to select a single folder. Returns ErrCancelled as the error
// if the user cancels or closes the dialog.
func (b *DirectoryBuilder) Browse() (string, error) {
	return b.browse()
}

// Title 设置文件对话框标题。
// Title specifies the title to be used for the dialog.
func (b *DirectoryBuilder) Title(title string) *DirectoryBuilder {
	b.Dlg.Title = title
	return b
}

// SetStartDir 设置目录对话框的初始路径。
// StartDir specifies the initial directory to be used for the dialog.
func (b *DirectoryBuilder) SetStartDir(dir string) *DirectoryBuilder {
	b.StartDir = dir
	return b
}

// ShowHidden 设置是否显示隐藏文件。
// ShowHiddenFiles sets whether hidden files should be visible in the dialog.
func (b *DirectoryBuilder) ShowHidden(show bool) *DirectoryBuilder {
	b.ShowHiddenFiles = show
	return b
}
