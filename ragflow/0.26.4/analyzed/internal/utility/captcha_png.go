//
//  Copyright 2026 The InfiniFlow Authors. All Rights Reserved.
//
//  Licensed under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License.
//  You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.
//

// 纯标准库 PNG 验证码渲染器。
//
// PR #15290：旧 SVG 在响应中嵌入明文，可被脚本正则提取。本实现仅用 image/png
// 与手写 5×7 点阵字体绘制栅格图，响应流中不含可机器读取的明文答案。
package utility

import (
	"bytes"
	"encoding/base64"
	"image"
	"image/color"
	"image/draw"
	"image/png"
	"math/rand"
	"strings"
	"time"
)

// captchaPNGScale 字形像素放大倍数；5×7 点阵 ×4 约 20×28，浏览器下可读。
const (
	captchaPNGScale    = 4
	captchaGlyphW      = 5
	captchaGlyphH      = 7
	captchaCharSpacing = 4 // px between glyphs (after scaling)
	captchaSidePadding = 8
	captchaTopPadding  = 6
	captchaNoiseDots   = 60
	captchaNoiseLines  = 4
)

// font5x7 将字符映射为 7 行 5 列点阵，'#' 表示前景像素；覆盖 A-Z0-9 及 '?' 回退字形。
var font5x7 = map[byte][7]string{
	'A': {".###.", "#...#", "#...#", "#####", "#...#", "#...#", "#...#"},
	'B': {"####.", "#...#", "#...#", "####.", "#...#", "#...#", "####."},
	'C': {".####", "#....", "#....", "#....", "#....", "#....", ".####"},
	'D': {"####.", "#...#", "#...#", "#...#", "#...#", "#...#", "####."},
	'E': {"#####", "#....", "#....", "####.", "#....", "#....", "#####"},
	'F': {"#####", "#....", "#....", "####.", "#....", "#....", "#...."},
	'G': {".####", "#....", "#....", "#..##", "#...#", "#...#", ".####"},
	'H': {"#...#", "#...#", "#...#", "#####", "#...#", "#...#", "#...#"},
	'I': {"#####", "..#..", "..#..", "..#..", "..#..", "..#..", "#####"},
	'J': {"#####", "...#.", "...#.", "...#.", "...#.", "#..#.", ".##.."},
	'K': {"#...#", "#..#.", "#.#..", "##...", "#.#..", "#..#.", "#...#"},
	'L': {"#....", "#....", "#....", "#....", "#....", "#....", "#####"},
	'M': {"#...#", "##.##", "#.#.#", "#...#", "#...#", "#...#", "#...#"},
	'N': {"#...#", "##..#", "#.#.#", "#.#.#", "#..##", "#...#", "#...#"},
	'O': {".###.", "#...#", "#...#", "#...#", "#...#", "#...#", ".###."},
	'P': {"####.", "#...#", "#...#", "####.", "#....", "#....", "#...."},
	'Q': {".###.", "#...#", "#...#", "#...#", "#.#.#", "#..#.", ".##.#"},
	'R': {"####.", "#...#", "#...#", "####.", "#.#..", "#..#.", "#...#"},
	'S': {".####", "#....", "#....", ".###.", "....#", "....#", "####."},
	'T': {"#####", "..#..", "..#..", "..#..", "..#..", "..#..", "..#.."},
	'U': {"#...#", "#...#", "#...#", "#...#", "#...#", "#...#", ".###."},
	'V': {"#...#", "#...#", "#...#", "#...#", "#...#", ".#.#.", "..#.."},
	'W': {"#...#", "#...#", "#...#", "#...#", "#.#.#", "##.##", "#...#"},
	'X': {"#...#", "#...#", ".#.#.", "..#..", ".#.#.", "#...#", "#...#"},
	'Y': {"#...#", "#...#", ".#.#.", "..#..", "..#..", "..#..", "..#.."},
	'Z': {"#####", "....#", "...#.", "..#..", ".#...", "#....", "#####"},
	'0': {".###.", "#...#", "#..##", "#.#.#", "##..#", "#...#", ".###."},
	'1': {"..#..", ".##..", "..#..", "..#..", "..#..", "..#..", ".###."},
	'2': {".###.", "#...#", "....#", "...#.", "..#..", ".#...", "#####"},
	'3': {"####.", "....#", "....#", ".###.", "....#", "....#", "####."},
	'4': {"...#.", "..##.", ".#.#.", "#..#.", "#####", "...#.", "...#."},
	'5': {"#####", "#....", "####.", "....#", "....#", "....#", "####."},
	'6': {".###.", "#....", "#....", "####.", "#...#", "#...#", ".###."},
	'7': {"#####", "....#", "....#", "...#.", "..#..", ".#...", "#...."},
	'8': {".###.", "#...#", "#...#", ".###.", "#...#", "#...#", ".###."},
	'9': {".###.", "#...#", "#...#", ".####", "....#", "....#", ".###."},
	'?': {".###.", "#...#", "....#", "...#.", "..#..", ".....", "..#.."},
}

// RenderCaptchaPNG 渲染 PNG 字节：字符抖动、干扰线与噪点；答案仅以像素形式存在。
func RenderCaptchaPNG(text string) []byte {
	if text == "" {
		text = " "
	}
	upper := strings.ToUpper(text)
	rng := rand.New(rand.NewSource(time.Now().UnixNano()))

	glyphW := captchaGlyphW * captchaPNGScale
	glyphH := captchaGlyphH * captchaPNGScale
	width := captchaSidePadding*2 + len(upper)*glyphW + (len(upper)-1)*captchaCharSpacing
	if width < 40 {
		width = 40
	}
	height := captchaTopPadding*2 + glyphH + 8 // a bit of headroom for jitter

	img := image.NewRGBA(image.Rect(0, 0, width, height))
	// 背景：浅冷灰色
	bg := color.RGBA{R: 0xf5, G: 0xf5, B: 0xf7, A: 0xff}
	draw.Draw(img, img.Bounds(), &image.Uniform{bg}, image.Point{}, draw.Src)

	// 字形下方绘制干扰线
	for i := 0; i < captchaNoiseLines; i++ {
		drawLine(
			img,
			rng.Intn(width), rng.Intn(height),
			rng.Intn(width), rng.Intn(height),
			pickStrokeRGBA(rng),
		)
	}

	// 逐字绘制点阵，含 xy 抖动与随机前景色
	x := captchaSidePadding
	for i := 0; i < len(upper); i++ {
		ch := upper[i]
		bitmap, ok := font5x7[ch]
		if !ok {
			bitmap = font5x7['?']
		}
		dx := rng.Intn(5) - 2
		dy := rng.Intn(7) - 3
		fg := pickFillRGBA(rng)
		drawGlyph(img, x+dx, captchaTopPadding+dy, bitmap, fg)
		x += glyphW + captchaCharSpacing
		_ = i // explicit to silence any future lint pass
	}

	// 前景随机噪点
	for i := 0; i < captchaNoiseDots; i++ {
		img.Set(rng.Intn(width), rng.Intn(height), pickStrokeRGBA(rng))
	}

	var buf bytes.Buffer
	_ = png.Encode(&buf, img)
	return buf.Bytes()
}

// RenderCaptchaPNGDataURL 将 PNG 包装为 data URL 供前端 <img src> 使用。
func RenderCaptchaPNGDataURL(text string) string {
	pngBytes := RenderCaptchaPNG(text)
	return "data:image/png;base64," + base64.StdEncoding.EncodeToString(pngBytes)
}

// drawGlyph 在 (x,y) 按 captchaPNGScale 块大小 blit 5×7 点阵。
func drawGlyph(img *image.RGBA, x, y int, bitmap [7]string, fg color.RGBA) {
	for row := 0; row < captchaGlyphH; row++ {
		line := bitmap[row]
		for col := 0; col < captchaGlyphW && col < len(line); col++ {
			if line[col] != '#' {
				continue
			}
			for dy := 0; dy < captchaPNGScale; dy++ {
				for dx := 0; dx < captchaPNGScale; dx++ {
					img.Set(x+col*captchaPNGScale+dx, y+row*captchaPNGScale+dy, fg)
				}
			}
		}
	}
}

// drawLine Bresenham 算法画 1px 线段；越界像素由 Set 静默裁剪。
func drawLine(img *image.RGBA, x0, y0, x1, y1 int, c color.RGBA) {
	dx := abs(x1 - x0)
	dy := -abs(y1 - y0)
	sx := 1
	if x0 >= x1 {
		sx = -1
	}
	sy := 1
	if y0 >= y1 {
		sy = -1
	}
	err := dx + dy
	for {
		img.Set(x0, y0, c)
		if x0 == x1 && y0 == y1 {
			return
		}
		e2 := 2 * err
		if e2 >= dy {
			err += dy
			x0 += sx
		}
		if e2 <= dx {
			err += dx
			y0 += sy
		}
	}
}

func abs(n int) int {
	if n < 0 {
		return -n
	}
	return n
}

func pickFillRGBA(rng *rand.Rand) color.RGBA {
	palette := []color.RGBA{
		{R: 0x1f, G: 0x29, B: 0x37, A: 0xff},
		{R: 0x1d, G: 0x4e, B: 0xd8, A: 0xff},
		{R: 0x7c, G: 0x2d, B: 0x12, A: 0xff},
		{R: 0x06, G: 0x5f, B: 0x46, A: 0xff},
		{R: 0x7e, G: 0x22, B: 0xce, A: 0xff},
	}
	return palette[rng.Intn(len(palette))]
}

func pickStrokeRGBA(rng *rand.Rand) color.RGBA {
	palette := []color.RGBA{
		{R: 0x9c, G: 0xa3, B: 0xaf, A: 0xff},
		{R: 0x6b, G: 0x72, B: 0x80, A: 0xff},
		{R: 0xa1, G: 0x62, B: 0x07, A: 0xff},
		{R: 0x0e, G: 0x74, B: 0x90, A: 0xff},
		{R: 0xbe, G: 0x18, B: 0x5d, A: 0xff},
	}
	return palette[rng.Intn(len(palette))]
}
// captcha_png.go — 纯标准库 PNG 验证码渲染，避免 SVG 明文泄露答案。
