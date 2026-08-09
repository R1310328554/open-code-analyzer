// Copyright 2013 Julien Schmidt. All rights reserved.
// Based on the path package, Copyright 2009 The Go Authors.
// Use of this source code is governed by a BSD-style license that can be found
// 位于 https://github.com/julienschmidt/httprouter/blob/master/LICENSE。

package gin

const stackBufSize = 128

// cleanPath 是 path.Clean 的 URL 版本，它返回规范的 URL 路径
//  对于 p，消除 .和..元素。
//
//  迭代应用以下规则，直到无法进行进一步处理为止
//  完成：
//  1. 将多个斜杠替换为单个斜杠。
//  2. 消除每个 。路径名元素（当前目录）。
//  3.消除每个内部..路径名元素（父目录）
//      along with the non-.. element that precedes it.
//  4. 消除开始根路径的 .. 元素：
//      that is, replace "/.." by "/" at the beginning of a path.
//
//  如果此过程的结果是空字符串，则返回“/”。
func cleanPath(p string) string {
	// 将空字符串转为“/”
	if p == "" {
		return "/"
	}

	// 堆栈上合理大小的缓冲区以避免常见情况下的分配。
	//  如果需要更大的缓冲区，则会动态分配它。
	buf := make([]byte, 0, stackBufSize)

	n := len(p)

	// 不变量：
	//       reading from path; r is index of next byte to process.
	//       writing to buf; w is index of next byte to write.

	// 路径必须以“/”开头
	r := 1
	w := 1

	if p[0] != '/' {
		r = 0

		if n+1 > stackBufSize {
			buf = make([]byte, n+1)
		} else {
			buf = buf[:n+1]
		}
		buf[0] = '/'
	}

	trailing := n > 1 && p[n-1] == '/'

	// 没有像路径包那样的“lazybuf”会有点笨重，但是循环
	//  完全内联（bufApp 调用）。
	//  循环没有昂贵的函数调用（除了 1x make） // 因此，与路径包相比，此循环没有昂贵的函数
	//  调用（除非需要，否则 make）。

	for r < n {
		switch {
		case p[r] == '/':
			// 空路径元素，末尾添加斜杠
			r++

		case p[r] == '.' && r+1 == n:
			trailing = true
			r++

		case p[r] == '.' && p[r+1] == '/':
			// 。元素
			r += 2

		case p[r] == '.' && p[r+1] == '.' && (r+2 == n || p[r+2] == '/'):
			// ..元素：删除到最后/
			r += 3

			if w > 1 {
				// 可以原路返回
				w--

				if len(buf) == 0 {
					for w > 1 && p[w] != '/' {
						w--
					}
				} else {
					for w > 1 && buf[w] != '/' {
						w--
					}
				}
			}

		default:
			// 真实路径元素。
			//  如果需要的话添加斜杠
			if w > 1 {
				bufApp(&buf, p, w, '/')
				w++
			}

			// 复制元素
			for r < n && p[r] != '/' {
				bufApp(&buf, p, w, p[r])
				w++
				r++
			}
		}
	}

	// 重新附加尾部斜杠
	if trailing && w > 1 {
		bufApp(&buf, p, w, '/')
		w++
	}

	// 如果原始字符串没有被修改（或者只是在末尾被缩短），
	//  返回原始字符串的相应子字符串。
	//  否则从缓冲区返回一个新字符串。
	if len(buf) == 0 {
		return p[:w]
	}
	return string(buf[:w])
}

// 如有必要，内部助手会延迟创建缓冲区。
//  对此函数的调用将被内联。
func bufApp(buf *[]byte, s string, w int, c byte) {
	b := *buf
	if len(b) == 0 {
		// 到目前为止，没有对原始字符串进行任何修改。
		//  如果下一个字符与原始字符串中的相同，我们会这样做
		//  还不必分配缓冲区。
		if s[w] == c {
			return
		}

		// 否则使用堆栈缓冲区（如果它足够大），或者
		//  在堆上分配一个新的缓冲区，并复制所有以前的字符。
		length := len(s)
		if length > cap(b) {
			*buf = make([]byte, length)
		} else {
			*buf = (*buf)[:length]
		}
		b = *buf

		copy(b, s[:w])
	}
	b[w] = c
}

// removeRepeatedChar 从字符串中删除多个连续的“char”。
//  如果 s == "/a//b///c////" && char == '/'，则返回 "/a/b/c/"
func removeRepeatedChar(s string, char byte) string {
	// 检查是否有连续字符
	hasRepeatedChar := false
	for i := 1; i < len(s); i++ {
		if s[i] == char && s[i-1] == char {
			hasRepeatedChar = true
			break
		}
	}
	if !hasRepeatedChar {
		return s
	}

	// 堆栈上合理大小的缓冲区以避免常见情况下的分配。
	buf := make([]byte, 0, stackBufSize)

	// 不变量：
	//       reading from s; r is index of next byte to process.
	//       writing to buf; w is index of next byte to write.
	r := 0
	w := 0

	for n := len(s); r < n; {
		if s[r] == char {
			// 写入第一个字符
			bufApp(&buf, s, w, char)
			w++
			r++

			// 跳过所有连续字符
			for r < n && s[r] == char {
				r++
			}
		} else {
			// 复制非 char 字符
			bufApp(&buf, s, w, s[r])
			w++
			r++
		}
	}

	// 如果原始字符串未修改（或仅在末尾缩短），则返回原始字符串的相应子字符串。否则，从缓冲区返回一个新字符串。
	//  return the respective substring of the original string.
	//  Otherwise, return a new string from the buffer.
	if len(buf) == 0 {
		return s[:w]
	}
	return string(buf[:w])
}
