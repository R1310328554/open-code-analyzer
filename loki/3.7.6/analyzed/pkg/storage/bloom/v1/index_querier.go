package v1

// index_querier 提供 Bloom 块索引的惰性 series 页迭代：按需解码 SeriesPageDecoder，Seek 用二分定位含目标 fingerprint 的页。

import (
	"sort"

	"github.com/efficientgo/core/errors"
	"github.com/prometheus/common/model"
)

type LazySeriesIter struct {
	b *Block

	// state
	initialized  bool
	err          error
	curPageIndex int
	curPage      *SeriesPageDecoder
}

// NewLazySeriesIter 创建惰性迭代器，首次访问时才 LoadHeaders 并解码页。
// Decodes series pages one at a time and iterates through them
func NewLazySeriesIter(b *Block) *LazySeriesIter {
	return &LazySeriesIter{
		b: b,
	}
}

func (it *LazySeriesIter) ensureInit() {
	// TODO(owen-d): better control over when to decode
	if !it.initialized {
		if err := it.b.LoadHeaders(); err != nil {
			it.err = err
		}
		it.initialized = true
	}
}

// Seek 用 sort.Search 定位 pageHeaders 中 Bounds.Max >= fp 的首页，必要时切换 curPage。
// Seek returns an iterator over the pages where the first fingerprint is >= fp
func (it *LazySeriesIter) Seek(fp model.Fingerprint) error {
	it.ensureInit()
	if it.err != nil {
		return it.err
	}

	// first potentially relevant page
	desiredPage := sort.Search(len(it.b.index.pageHeaders), func(i int) bool {
		header := it.b.index.pageHeaders[i]
		return header.Bounds.Max >= fp
	})

	switch {
	case desiredPage == len(it.b.index.pageHeaders):
		// no overlap exists, either because no page was found with a throughFP >= fp
		// or because the first page that was found has a fromFP > fp,
		// meaning successive pages would also have a fromFP > fp
		// since they're sorted in ascending fp order
		it.curPageIndex = len(it.b.index.pageHeaders)
		it.curPage = nil
		return nil

	case desiredPage == it.curPageIndex && it.curPage != nil:
		// on the right page, no action needed
	default:
		// need to load a new page
		page := it.b.index.pageHeaders[desiredPage]
		r, err := it.b.reader.Index()
		if err != nil {
			return errors.Wrap(err, "getting index reader")
		}
		it.curPage, err = it.b.index.NewSeriesPageDecoder(
			r,
			page,
			it.b.metrics,
		)
		if err != nil {
			return err
		}
		it.curPageIndex = desiredPage
	}

	it.curPage.Seek(fp)
	return nil
}

func (it *LazySeriesIter) Next() bool {
	it.ensureInit()
	if it.err != nil {
		return false
	}

	return it.next()
}

func (it *LazySeriesIter) next() bool {
	for it.curPageIndex < len(it.b.index.pageHeaders) {
		// first access of next page
		if it.curPage == nil {
			curHeader := it.b.index.pageHeaders[it.curPageIndex]
			r, err := it.b.reader.Index()
			if err != nil {
				it.err = errors.Wrap(err, "getting index reader")
				return false
			}
			it.curPage, err = it.b.index.NewSeriesPageDecoder(
				r,
				curHeader,
				it.b.metrics,
			)
			if err != nil {
				it.err = err
				return false
			}
			continue
		}

		if !it.curPage.Next() {
			// there was an error
			if it.curPage.Err() != nil {
				return false
			}

			// we've exhausted the current page, progress to next
			it.curPageIndex++
			it.curPage = nil
			continue
		}

		return true
	}

	// finished last page
	return false
}

// At 返回当前页迭代器指向的 SeriesWithMeta，调用前须 Next 返回 true。
func (it *LazySeriesIter) At() *SeriesWithMeta {
	return it.curPage.At()
}

func (it *LazySeriesIter) Err() error {
	if it.err != nil {
		return it.err
	}
	if it.curPage != nil {
		return it.curPage.Err()
	}
	return nil
}
// next 在页耗尽时递增 curPageIndex 并置 nil，错误经 Err 向上传递。
