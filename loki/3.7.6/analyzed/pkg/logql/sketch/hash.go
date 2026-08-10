package sketch

// hashn 为 Count-Min Sketch 提供双哈希：FNv-1a 与 Jenkins one-at-a-time，满足 Kirsch-Mitzenmacher 从单一哈希派生多行索引的构造。

import "hash/fnv"

// Copyright (c) 2012-2017  Dustin Sallings <dustin@spy.net>
// Copyright (c) 2012-2017  Damian Gryski <damian@gryski.com>
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in
// all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// SOFTWARE.
//
// <http://www.opensource.org/licenses/mit-license.php>
// hashn 对字节切片计算 h1/h2，供 CMS 各行独立桶定位与 sketch-bf 布隆过滤。
func hashn(s []byte) (h1, h2 uint32) {
	// This construction comes from
	// http://www.eecs.harvard.edu/~michaelm/postscripts/tr-02-05.pdf
	// "Building a Better Bloom Filter", by Kirsch and Mitzenmacher. Their
	// proof that this is allowed for count-min requires the h functions to
	// be from the 2-universal hash family, w be a prime and d be larger
	// than the traditional CM-sketch requirements.
	// Empirically, though, this seems to work "just fine".

	fnv1a := fnv.New32a()
	fnv1a.Write(s)
	h1 = fnv1a.Sum32()

	// inlined jenkins one-at-a-time hash
	h2 = uint32(0)
	for _, c := range s {
		h2 += uint32(c)
		h2 += h2 << 10
		h2 ^= h2 >> 6
	}
	h2 += (h2 << 3)
	h2 ^= (h2 >> 11)
	h2 += (h2 << 15)

	return h1, h2
}
// 该构造在 Count-Min 理论中需 2-通用哈希族；实践中对 Loki topk 场景表现稳定。
