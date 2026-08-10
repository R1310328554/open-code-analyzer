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

package com.alibaba.nacos.sys.file;

import java.io.Serializable;

/**
 * 文件变更事件载体。
 *
 * <p>由 {@link WatchFileCenter} 在目录监听触发时分发给 {@link FileWatcher}，携带监听路径与变更上下文（通常为文件名）。</p>
 *
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
public class FileChangeEvent implements Serializable {
    
    private static final long serialVersionUID = -4255584033113954765L;
    
    private String paths;
    
    private Object context;
    
    /** 创建 {@link FileChangeEventBuilder} 构建变更事件。 */
    public static FileChangeEventBuilder builder() {
        return new FileChangeEventBuilder();
    }
    
    public String getPaths() {
        return paths;
    }
    
    public void setPaths(String paths) {
        this.paths = paths;
    }
    
    public Object getContext() {
        return context;
    }
    
    public void setContext(Object context) {
        this.context = context;
    }
    
    @Override
    public String toString() {
        return "FileChangeEvent{" + "paths='" + paths + '\'' + ", context=" + context + '}';
    }
    
    public static final class FileChangeEventBuilder {
        
        private String paths;
        
        private Object context;
        
        private FileChangeEventBuilder() {
        }
        
        public FileChangeEventBuilder paths(String paths) {
            this.paths = paths;
            return this;
        }
        
        public FileChangeEventBuilder context(Object context) {
            this.context = context;
            return this;
        }
        
        /**
         * 组装不可变 {@link FileChangeEvent} 实例。
         *
         * @return {@link FileChangeEvent}
         */
        public FileChangeEvent build() {
            FileChangeEvent fileChangeEvent = new FileChangeEvent();
            fileChangeEvent.setPaths(paths);
            fileChangeEvent.setContext(context);
            return fileChangeEvent;
        }
    }
}
