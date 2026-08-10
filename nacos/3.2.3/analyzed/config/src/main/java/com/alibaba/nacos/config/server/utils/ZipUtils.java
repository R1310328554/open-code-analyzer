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

package com.alibaba.nacos.config.server.utils;

import com.alibaba.nacos.config.server.constant.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * 配置导入导出 ZIP 压缩/解压工具：支持多文件打包及元数据条目单独识别。
 * ZipUtils for import and export.
 *
 * @author klw
 * @date 2019/5/14 16:59
 */
public class ZipUtils {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ZipUtils.class);
    
    /** ZIP 内单个条目：文件名 + UTF-8 文本内容 */
    public static class ZipItem {
        
        /** 条目在 ZIP 中的路径/文件名 */
        private String itemName;
        
        /** 条目文本内容（UTF-8） */
        private String itemData;
        
        public ZipItem(String itemName, String itemData) {
            this.itemName = itemName;
            this.itemData = itemData;
        }
        
        public String getItemName() {
            return itemName;
        }
        
        public void setItemName(String itemName) {
            this.itemName = itemName;
        }
        
        public String getItemData() {
            return itemData;
        }
        
        public void setItemData(String itemData) {
            this.itemData = itemData;
        }
    }
    
    /** ZIP 解压结果：配置条目列表 + 元数据条目（若有） */
    public static class UnZipResult {
        
        /** 除元数据外的配置 ZIP 条目列表 */
        private List<ZipItem> zipItemList;
        
        /** 导出元数据条目（旧版或新版 metadata 文件名） */
        private ZipItem metaDataItem;
        
        public UnZipResult(List<ZipItem> zipItemList, ZipItem metaDataItem) {
            this.zipItemList = zipItemList;
            this.metaDataItem = metaDataItem;
        }
        
        public List<ZipItem> getZipItemList() {
            return zipItemList;
        }
        
        public void setZipItemList(List<ZipItem> zipItemList) {
            this.zipItemList = zipItemList;
        }
        
        public ZipItem getMetaDataItem() {
            return metaDataItem;
        }
        
        public void setMetaDataItem(ZipItem metaDataItem) {
            this.metaDataItem = metaDataItem;
        }
    }
    
    /**
     * 将多个 {@link ZipItem} 压缩为 ZIP 字节数组。
     * zip method.
     *
     * @param source 待压缩条目列表
     * @return ZIP 字节数组，失败时返回 null
     */
    public static byte[] zip(List<ZipItem> source) {
        byte[] result = null;
        try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
            ZipOutputStream zipOut = new ZipOutputStream(
                byteOut)) {
            for (ZipItem item : source) {
                zipOut.putNextEntry(new ZipEntry(item.getItemName()));
                zipOut.write(item.getItemData().getBytes(StandardCharsets.UTF_8));
            }
            zipOut.flush();
            zipOut.finish();
            result = byteOut.toByteArray();
        } catch (IOException e) {
            LOGGER.error("an error occurred while compressing data.", e);
        }
        return result;
    }
    
    /**
     * 解压 ZIP 字节数组，单独识别 {@link Constants#CONFIG_EXPORT_METADATA} 与新版 metadata 条目。
     * unzip method.
     *
     * @param source ZIP 字节数组
     * @return 解压结果（配置列表 + 元数据）
     */
    public static UnZipResult unzip(byte[] source) {
        List<ZipItem> itemList = new ArrayList<>();
        ZipItem metaDataItem = null;
        try (ZipInputStream zipIn = new ZipInputStream(new ByteArrayInputStream(source))) {
            ZipEntry entry;
            while ((entry = zipIn.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    continue;
                }
                try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                    byte[] buffer = new byte[1024];
                    int offset;
                    while ((offset = zipIn.read(buffer)) != -1) {
                        out.write(buffer, 0, offset);
                    }
                    String entryName = entry.getName();
                    if (metaDataItem == null
                        && Constants.CONFIG_EXPORT_METADATA.equals(entryName)) {
                        metaDataItem = new ZipItem(entryName, out.toString("UTF-8"));
                        continue;
                    }
                    if (metaDataItem == null
                        && Constants.CONFIG_EXPORT_METADATA_NEW.equals(entryName)) {
                        metaDataItem = new ZipItem(entryName, out.toString("UTF-8"));
                        continue;
                    }
                    itemList.add(new ZipItem(entryName, out.toString("UTF-8")));
                } catch (IOException e) {
                    LOGGER.error("unzip error", e);
                }
            }
        } catch (IOException e) {
            LOGGER.error("unzip error", e);
        }
        return new UnZipResult(itemList, metaDataItem);
    }
    
}
