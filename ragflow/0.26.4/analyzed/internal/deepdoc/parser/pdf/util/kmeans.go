// kmeans.go — 一维 KMeans 聚类与轮廓系数：用于表格列/行坐标分组；初始化与 sklearn 固定种子行为等价。

package util

import (
	"math"
	"sort"
)

// KMeans1D 一维 KMeans：返回各点标签与最终质心；质心沿 min–max 均匀初始化，确定性等价 sklearn 固定种子。
func KMeans1D(data []float64, k int) (labels []int, centroids []float64) {
	n := len(data)
	labels = make([]int, n)

	if k <= 1 {
		var sum float64
		for _, v := range data {
			sum += v
		}
		return labels, []float64{sum / float64(n)}
	}
	if n <= k {
		// n≤k 时每点自成一簇，簇数不超过点数。
		centroids = make([]float64, n)
		for i, v := range data {
			centroids[i] = v
			labels[i] = i
		}
		return labels, centroids
	}

	// 线性扫描 min/max，O(n) 优于排序。
	minV, maxV := data[0], data[0]
	for _, v := range data {
		if v < minV {
			minV = v
		}
		if v > maxV {
			maxV = v
		}
	}

	centroids = make([]float64, k)
	for c := 0; c < k; c++ {
		// 在 min 与 max 间均匀布质心
		if k == 1 {
			centroids[c] = minV
		} else {
			centroids[c] = minV + float64(c)*(maxV-minV)/float64(k-1)
		}
	}

	// Lloyd 迭代：分配→更新质心，最多 100 轮
	for iter := 0; iter < 100; iter++ {
		changed := false
		// 每点分配到最近质心
		for i, v := range data {
			bestC, bestD := 0, math.Abs(v-centroids[0])
			for c := 1; c < k; c++ {
				d := math.Abs(v - centroids[c])
				if d < bestD {
					bestC, bestD = c, d
				}
			}
			if labels[i] != bestC {
				changed = true
			}
			labels[i] = bestC
		}
		if !changed {
			break
		}
		// 按簇内均值更新质心
		counts := make([]int, k)
		sums := make([]float64, k)
		for i, v := range data {
			counts[labels[i]]++
			sums[labels[i]] += v
		}
		for c := 0; c < k; c++ {
			if counts[c] > 0 {
				centroids[c] = sums[c] / float64(counts[c])
			}
		}
	}

	return
}

// Silhouette1D 计算一维轮廓系数 [-1,1]，越高越好；少于 2 个簇返回 -1；单点簇贡献 0，对齐 sklearn。
func Silhouette1D(data []float64, labels []int) float64 {
	n := len(data)
	if n <= 1 {
		return 0
	}

	clusterCounts := make(map[int]int)
	for _, l := range labels {
		clusterCounts[l]++
	}

	uniqueClusters := make([]int, 0, len(clusterCounts))
	for cl := range clusterCounts {
		uniqueClusters = append(uniqueClusters, cl)
	}

	// 轮廓系数至少需要 2 个不同标签。
	if len(uniqueClusters) < 2 {
		return -1
	}
	sort.Ints(uniqueClusters)

	var totalScore float64
	for i := 0; i < n; i++ {
		// sklearn：单点簇样本轮廓为 0。
		if clusterCounts[labels[i]] <= 1 {
			continue
		}

		// a_i：同簇内到其他点的平均距离
		var aSum float64
		aCount := 0
		for j := 0; j < n; j++ {
			if i != j && labels[j] == labels[i] {
				aSum += math.Abs(data[i] - data[j])
				aCount++
			}
		}
		a := 0.0
		if aCount > 0 {
			a = aSum / float64(aCount)
		}

		// b_i：到其他簇的最小平均距离
		b := math.MaxFloat64
		for _, cl := range uniqueClusters {
			if cl == labels[i] {
				continue
			}
			var bSum float64
			bCount := 0
			for j := 0; j < n; j++ {
				if labels[j] == cl {
					bSum += math.Abs(data[i] - data[j])
					bCount++
				}
			}
			if bCount > 0 {
				meanDist := bSum / float64(bCount)
				if meanDist < b {
					b = meanDist
				}
			}
		}
		if b == math.MaxFloat64 {
			b = 0
		}

		maxAB := math.Max(a, b)
		if maxAB > 0 {
			totalScore += (b - a) / maxAB
		}
	}

	return totalScore / float64(n)
}
