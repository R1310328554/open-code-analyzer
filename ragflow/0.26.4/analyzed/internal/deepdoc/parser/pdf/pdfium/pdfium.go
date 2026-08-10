//go:build cgo

// Package pdfium 使用静态链接的 libpdfium 渲染 PDF 页。专用于替代 pdf_oxide 的 RenderPageRaw，在 OCR/DLA 需要高质量光栅化时使用；文本/字符/表格提取仍由 pdf_oxide 负责。
package pdfium

/*
#cgo LDFLAGS: -lm -lpthread -ldl

#include <stdint.h>
#include <stdlib.h>

typedef struct FPDF_DOCUMENT__  { int unused; } *FPDF_DOCUMENT;
typedef struct FPDF_PAGE__     { int unused; } *FPDF_PAGE;
typedef struct FPDF_BITMAP__   { int unused; } *FPDF_BITMAP;
typedef struct FPDF_BOOKMARK__ { int unused; } *FPDF_BOOKMARK;
typedef struct FPDF_DEST__     { int unused; } *FPDF_DEST;

extern void          FPDF_InitLibrary(void);
extern FPDF_DOCUMENT FPDF_LoadMemDocument(const void* data_buf, int size, const char* password);
extern void          FPDF_CloseDocument(FPDF_DOCUMENT document);
extern int           FPDF_GetPageCount(FPDF_DOCUMENT document);
extern FPDF_PAGE     FPDF_LoadPage(FPDF_DOCUMENT document, int page_index);
extern void          FPDF_ClosePage(FPDF_PAGE page);
extern double        FPDF_GetPageWidth(FPDF_PAGE page);
extern double        FPDF_GetPageHeight(FPDF_PAGE page);
extern FPDF_BITMAP   FPDFBitmap_Create(int width, int height, int alpha);
extern void          FPDFBitmap_Destroy(FPDF_BITMAP bitmap);
extern void          FPDF_RenderPageBitmap(FPDF_BITMAP bitmap, FPDF_PAGE page,
                       int start_x, int start_y, int size_x, int size_y,
                       int rotate, int flags);
extern void*         FPDFBitmap_GetBuffer(FPDF_BITMAP bitmap);
extern int           FPDFBitmap_GetWidth(FPDF_BITMAP bitmap);
extern int           FPDFBitmap_GetHeight(FPDF_BITMAP bitmap);
extern int           FPDFBitmap_GetStride(FPDF_BITMAP bitmap);

// Outline / bookmark API
extern FPDF_BOOKMARK FPDFBookmark_GetFirstChild(FPDF_DOCUMENT document, FPDF_BOOKMARK bookmark);
extern FPDF_BOOKMARK FPDFBookmark_GetNextSibling(FPDF_DOCUMENT document, FPDF_BOOKMARK bookmark);
extern unsigned long  FPDFBookmark_GetTitle(FPDF_BOOKMARK bookmark, void* buffer, unsigned long buflen);
extern FPDF_DEST      FPDFBookmark_GetDest(FPDF_DOCUMENT document, FPDF_BOOKMARK bookmark);
extern int            FPDFDest_GetDestPageIndex(FPDF_DOCUMENT document, FPDF_DEST dest);
*/
import "C"
import (
	"fmt"
	"image"
	"image/color"
	"math"
	"sync"
	"unicode/utf16"
	"unsafe"
)

// Outline 表示 PDF 大纲（书签/目录）中的一条记录。
type Outline struct {
	// Title 书签标题
	Title      string
	// Level 嵌套层级（0 为顶层）
	Level      int
	// PageNumber 目标页码（1 基，对齐 Python）
	PageNumber int // 1-indexed, matching Python
}

var initOnce sync.Once

// pdfiumMu 串行化所有 pdfium C API 调用；pdfium 非线程安全，并发调用会导致堆损坏 SIGSEGV（见 TestPdfiumConcurrentSafety）。
var pdfiumMu sync.Mutex

// Init 初始化 PDFium 库，sync.Once 保证只执行一次，可重复调用。
func Init() { initOnce.Do(func() { C.FPDF_InitLibrary() }) }

// PageSize 返回应用 /Rotate 后的页宽高（PDF 点，1/72 英寸）；如 A4 旋转 90° 时宽高互换；开销小，开页读后即关。
func PageSize(pdfData []byte, pageIdx int) (width, height float64, err error) {
	Init()
	pdfiumMu.Lock()
	defer pdfiumMu.Unlock()
	_, _, pw, ph, closeAll, err := openPage(pdfData, pageIdx)
	if err != nil {
		return 0, 0, err
	}
	closeAll()
	return pw, ph, nil
}

// RenderPage 将单页渲染为 *image.RGBA；pdfData 为原始字节，pageIdx 0 基，dpi 控制分辨率。
func RenderPage(pdfData []byte, pageIdx int, dpi float64) (*image.RGBA, error) {
	Init()
	pdfiumMu.Lock()
	defer pdfiumMu.Unlock()
	_, page, pw, ph, closeAll, err := openPage(pdfData, pageIdx)
	if err != nil {
		return nil, err
	}
	defer closeAll()

	scale := dpi / 72.0
	pxW := int(math.Round(pw * scale))
	pxH := int(math.Round(ph * scale))

	bitmap := C.FPDFBitmap_Create(C.int(pxW), C.int(pxH), 1) // 1 = RGBA
	if bitmap == nil {
		return nil, fmt.Errorf("pdfium: FPDFBitmap_Create(%d,%d) returned nil", pxW, pxH)
	}
	defer C.FPDFBitmap_Destroy(bitmap)

	// Fill with opaque white before rendering, so transparent areas
	// (e.g. outside crop box) are white rather than undefined.
	stride := int(C.FPDFBitmap_GetStride(bitmap))
	buf := C.FPDFBitmap_GetBuffer(bitmap)
	pixels := (*[1 << 30]byte)(unsafe.Pointer(buf))[: pxH*stride : pxH*stride]
	for i := range pixels {
		pixels[i] = 255
	}

	// FPDF_ANNOT (0x01) — render annotations.
	// LCD text AA (0x02) is left off; default text smoothing is sufficient.
	C.FPDF_RenderPageBitmap(bitmap, page, 0, 0, C.int(pxW), C.int(pxH), 0, 0x01)

	// pdfium 输出 BGRA，逐像素转换为 RGBA。
	img := image.NewRGBA(image.Rect(0, 0, pxW, pxH))
	for y := 0; y < pxH; y++ {
		for x := 0; x < pxW; x++ {
			off := y*stride + x*4
			img.SetRGBA(x, y, color.RGBA{
				R: pixels[off+2], // B
				G: pixels[off+1], // G
				B: pixels[off],   // R
				A: 255,
			})
		}
	}
	return img, nil
}

// openPage 打开文档与页，返回旋转后尺寸及 closeAll 清理函数；调用方必须释放资源。
func openPage(pdfData []byte, pageIdx int) (
	doc C.FPDF_DOCUMENT,
	page C.FPDF_PAGE,
	pw, ph float64,
	closeAll func(),
	err error,
) {
	cData := C.CBytes(pdfData)

	doc = C.FPDF_LoadMemDocument(unsafe.Pointer(cData), C.int(len(pdfData)), nil)
	if doc == nil {
		C.free(cData)
		err = fmt.Errorf("pdfium: FPDF_LoadMemDocument returned nil")
		return
	}

	page = C.FPDF_LoadPage(doc, C.int(pageIdx))
	if page == nil {
		C.FPDF_CloseDocument(doc)
		C.free(cData)
		err = fmt.Errorf("pdfium: FPDF_LoadPage(%d) returned nil", pageIdx)
		return
	}

	pw = float64(C.FPDF_GetPageWidth(page))
	ph = float64(C.FPDF_GetPageHeight(page))
	if pw <= 0 || ph <= 0 {
		C.FPDF_ClosePage(page)
		C.FPDF_CloseDocument(doc)
		C.free(cData)
		err = fmt.Errorf("pdfium: invalid page dimensions %.1fx%.1f", pw, ph)
		return
	}

	closeAll = func() {
		C.FPDF_ClosePage(page)
		C.FPDF_CloseDocument(doc)
		C.free(cData)
	}
	return
}

// ExtractOutlines 提取 PDF 书签大纲；空/损坏 PDF 返回 nil；标题 UTF-16LE 解码；显式栈迭代遍历防深树栈溢出；须持有 pdfiumMu。
func ExtractOutlines(pdfData []byte) []Outline {
	if len(pdfData) == 0 {
		return nil
	}
	Init()
	pdfiumMu.Lock()
	defer pdfiumMu.Unlock()

	cData := C.CBytes(pdfData)
	defer C.free(cData)

	doc := C.FPDF_LoadMemDocument(unsafe.Pointer(cData), C.int(len(pdfData)), nil)
	if doc == nil {
		return nil
	}
	defer C.FPDF_CloseDocument(doc)

	type frame struct {
		bm    C.FPDF_BOOKMARK
		level int
	}

	var result []Outline
	stack := []frame{{bm: C.FPDFBookmark_GetFirstChild(doc, nil), level: 0}}

	for len(stack) > 0 {
		top := stack[len(stack)-1]
		stack = stack[:len(stack)-1]
		if top.bm == nil {
			continue
		}

		// 读取书签标题（UTF-16LE 编码）。
		title := bookmarkTitle(top.bm)

		// 解析书签目标页码。
		// 无目标时默认第 1 页
		pageNum := 1 // default to page 1 if dest is unavailable
		if dest := C.FPDFBookmark_GetDest(doc, top.bm); dest != nil {
			pn := C.FPDFDest_GetDestPageIndex(doc, dest)
			if pn >= 0 {
				pageNum = int(pn) + 1 // pdfium returns 0-based
			}
		}

		result = append(result, Outline{Title: title, Level: top.level, PageNumber: pageNum})

		// 子节点先入栈实现前序遍历（先处理子书签再兄弟）。
		if sibling := C.FPDFBookmark_GetNextSibling(doc, top.bm); sibling != nil {
			stack = append(stack, frame{bm: sibling, level: top.level})
		}
		if child := C.FPDFBookmark_GetFirstChild(doc, top.bm); child != nil {
			stack = append(stack, frame{bm: child, level: top.level + 1})
		}
	}
	return result
}

// bookmarkTitle 读取书签 UTF-16LE 标题并转为 Go 字符串。
func bookmarkTitle(bm C.FPDF_BOOKMARK) string {
	// 首次调用获取标题缓冲区字节长度。
	buflen := C.FPDFBookmark_GetTitle(bm, nil, 0)
	if buflen <= 0 {
		return ""
	}
	buf := make([]byte, buflen)
	C.FPDFBookmark_GetTitle(bm, unsafe.Pointer(&buf[0]), buflen)

	// 标题为 UTF-16LE，转 []uint16 后 utf16.Decode。
	n := int(buflen) / 2
	u16 := unsafe.Slice((*uint16)(unsafe.Pointer(&buf[0])), n)

	// 去除末尾空终止符。
	if n > 0 && u16[n-1] == 0 {
		u16 = u16[:n-1]
	}
	return string(utf16.Decode(u16))
}
