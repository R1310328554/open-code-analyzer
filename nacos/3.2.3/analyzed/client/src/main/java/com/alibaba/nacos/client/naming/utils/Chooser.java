/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.nacos.client.naming.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 带权随机与轮询选择器。
 *
 * <p>维护 {@link Pair} 权重列表，支持均匀随机、按权重随机及 {@link Poller} 轮询；refresh 时重建累积概率数组与轮询器。</p>
 *
 * @author alibaba
 */
public class Chooser<K, T> {
    
    /** 选择器唯一标识（如 serviceKey），用于 equals/hashCode。 */
    private final K uniqueKey;
    
    /** 当前权重与轮询状态引用（volatile 保证 refresh 可见性）。 */
    private volatile Ref<T> ref;
    
    /** 创建空项列表的选择器。 */
    public Chooser(K uniqueKey) {
        this(uniqueKey, new ArrayList<>());
    }
    
    /** 使用初始权重对创建选择器并计算累积权重。 */
    public Chooser(K uniqueKey, List<Pair<T>> pairs) {
        Ref<T> ref = new Ref<>(pairs);
        ref.refresh();
        this.uniqueKey = uniqueKey;
        this.ref = ref;
    }
    
    /**
     * 均匀随机选取一项。
     *
     * @return 选中项，列表为空时返回 null
     */
    public T random() {
        List<T> items = ref.items;
        if (items.size() == 0) {
            return null;
        }
        if (items.size() == 1) {
            return items.get(0);
        }
        return items.get(ThreadLocalRandom.current().nextInt(items.size()));
    }
    
    /**
     * 按权重随机选取一项。
     *
     * <p>使用预计算的累积概率数组与二分查找定位区间。</p>
     *
     * @return 选中项
     */
    public T randomWithWeight() {
        Ref<T> ref = this.ref;
        double random = ThreadLocalRandom.current().nextDouble(0, 1);
        int index = Arrays.binarySearch(ref.weights, random);
        if (index < 0) {
            index = -index - 1;
        } else {
            return ref.items.get(index);
        }
        
        if (index < ref.weights.length) {
            if (random < ref.weights[index]) {
                return ref.items.get(index);
            }
        }
        
        if (ref.weights.length == 0) {
            throw new IllegalStateException(
                "Cumulative Weight wrong , the array length is equal to 0.");
        }
        
        /* 浮点精度导致未命中区间时的兜底：返回最后一项（参见 ChooserTest）。 */
        return ref.items.get(ref.items.size() - 1);
    }
    
    /** 返回选择器唯一键。 */
    public K getUniqueKey() {
        return uniqueKey;
    }
    
    /** 返回当前权重引用（测试或诊断用）。 */
    public Ref<T> getRef() {
        return ref;
    }
    
    /**
     * 刷新候选项与权重，保留原轮询进度。
     *
     * @param itemsWithWeight 带权重的候选项列表
     */
    public void refresh(List<Pair<T>> itemsWithWeight) {
        Ref<T> newRef = new Ref<>(itemsWithWeight);
        newRef.refresh();
        newRef.poller = this.ref.poller.refresh(newRef.items);
        this.ref = newRef;
    }
    
    /** 权重快照与轮询器状态容器。 */
    public class Ref<T> {
        
        /** 原始带权列表。 */
        private List<Pair<T>> itemsWithWeight = new ArrayList<>();
        
        /** 有效候选项（权重 &gt; 0）。 */
        private final List<T> items = new ArrayList<>();
        
        /** 轮询器，refresh 时继承进度。 */
        private Poller<T> poller = new GenericPoller<>(items);
        
        /** 累积权重上界数组，供按权随机二分查找。 */
        private double[] weights;
        
        public Ref(List<Pair<T>> itemsWithWeight) {
            if (itemsWithWeight != null) {
                this.itemsWithWeight = itemsWithWeight;
            }
        }
        
        /** 根据 itemsWithWeight 重建 items 与累积权重数组。 */
        public void refresh() {
            double originWeightSum = 0;
            int size = 0;
            for (Pair<T> item : itemsWithWeight) {
                
                double weight = item.weight();
                // 忽略权重为 0 的项，参见 ChooserTest#test_randomWithWeight_weight0
                if (weight <= 0) {
                    continue;
                }
                
                items.add(item.item());
                if (Double.isInfinite(weight)) {
                    weight = 10000.0D;
                }
                if (Double.isNaN(weight)) {
                    weight = 1.0D;
                }
                originWeightSum += weight;
                size++;
            }
            
            weights = new double[size];
            double exactWeight;
            double randomRange = 0D;
            int index = 0;
            for (Pair<T> item : itemsWithWeight) {
                double singleWeight = item.weight();
                //ignore item which weight is zero.see test_randomWithWeight_weight0 in ChooserTest
                if (singleWeight <= 0) {
                    continue;
                }
                
                exactWeight = singleWeight / originWeightSum;
                weights[index] = randomRange + exactWeight;
                randomRange = weights[index++];
            }
            
            double doublePrecisionDelta = 0.0001;
            
            if (index == 0 || (Math.abs(weights[index - 1] - 1) < doublePrecisionDelta)) {
                return;
            }
            throw new IllegalStateException(
                "Cumulative Weight calculate wrong , the sum of probabilities does not equals 1.");
        }
        
        @Override
        public int hashCode() {
            return itemsWithWeight.hashCode();
        }
        
        @SuppressWarnings("unchecked")
        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (other == null) {
                return false;
            }
            if (getClass() != other.getClass()) {
                return false;
            }
            Ref<T> otherRef = (Ref<T>) other;
            return this.itemsWithWeight.equals(otherRef.itemsWithWeight);
        }
    }
    
    @Override
    public int hashCode() {
        return uniqueKey.hashCode();
    }
    
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null) {
            return false;
        }
        if (getClass() != other.getClass()) {
            return false;
        }
        
        Chooser otherChooser = (Chooser) other;
        if (this.uniqueKey == null) {
            if (otherChooser.getUniqueKey() != null) {
                return false;
            }
        } else {
            if (otherChooser.getUniqueKey() == null) {
                return false;
            } else if (!this.uniqueKey.equals(otherChooser.getUniqueKey())) {
                return false;
            }
            
        }
        return this.ref.equals(otherChooser.getRef());
    }
}
