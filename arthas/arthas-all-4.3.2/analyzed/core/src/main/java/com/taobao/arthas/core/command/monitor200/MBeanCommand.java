package com.taobao.arthas.core.command.monitor200;

import com.alibaba.arthas.deps.org.slf4j.Logger;
import com.alibaba.arthas.deps.org.slf4j.LoggerFactory;
import com.taobao.arthas.core.command.Constants;
import com.taobao.arthas.core.command.model.MBeanAttributeVO;
import com.taobao.arthas.core.command.model.MBeanModel;
import com.taobao.arthas.core.shell.cli.CliToken;
import com.taobao.arthas.core.shell.cli.Completion;
import com.taobao.arthas.core.shell.cli.CompletionUtils;
import com.taobao.arthas.core.shell.command.AnnotatedCommand;
import com.taobao.arthas.core.shell.command.CommandProcess;
import com.taobao.arthas.core.shell.handlers.Handler;
import com.taobao.arthas.core.shell.handlers.command.CommandInterruptHandler;
import com.taobao.arthas.core.shell.handlers.shell.QExitHandler;
import com.taobao.arthas.core.shell.session.Session;
import com.taobao.arthas.core.util.StringUtils;
import com.taobao.arthas.core.util.TokenUtils;
import com.taobao.arthas.core.util.matcher.Matcher;
import com.taobao.arthas.core.util.matcher.RegexMatcher;
import com.taobao.arthas.core.util.matcher.WildcardMatcher;
import com.taobao.middleware.cli.annotations.Argument;
import com.taobao.middleware.cli.annotations.Description;
import com.taobao.middleware.cli.annotations.Name;
import com.taobao.middleware.cli.annotations.Option;
import com.taobao.middleware.cli.annotations.Summary;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.TabularData;

/**
 * {@code mbean} 命令：查询平台 MBeanServer，支持列举 ObjectName、读取属性值或展示 MBeanInfo 元数据。
 * <p>
 * 无参数时列出全部 MBean 名称；指定 name/attribute 模式可通配或 {@code -E} 正则；
 * {@code -i} 间隔大于 0 时周期性采样属性值，行为类似 dashboard。
 *
 * @author xuzhiyi
 */
@Name("mbean")
@Summary("Display the mbean information")
@Description("\nExamples:\n" +
        "  mbean\n" +
        "  mbean -m java.lang:type=Threading\n" +
        "  mbean java.lang:type=Threading\n" +
        "  mbean java.lang:type=Threading *Count\n" +
        "  mbean java.lang:type=MemoryPool,name=PS\\ Old\\ Gen\n" +
        "  mbean java.lang:type=MemoryPool,name=*\n" +
        "  mbean java.lang:type=MemoryPool,name=* Usage\n" +
        "  mbean -E java.lang:type=Threading PeakThreadCount|ThreadCount|DaemonThreadCount\n" +
        "  mbean -i 1000 java.lang:type=Threading *Count\n" +
        Constants.WIKI + Constants.WIKI_HOME + "mbean")
public class MBeanCommand extends AnnotatedCommand {

    private static final Logger logger = LoggerFactory.getLogger(MBeanCommand.class);

    /** ObjectName 匹配模式（positional 或 -m） */
    private String name;
    /** MBean 属性名匹配模式 */
    private String attribute;
    /** 属性名是否使用正则（-E） */
    private boolean isRegEx = false;
    /** 周期性采样间隔毫秒，0 表示只执行一次 */
    private long interval = 0;
    /** 为 true 时输出 MBeanInfo 元数据而非属性值 */
    private boolean metaData;
    /** 周期性模式下的最大执行次数 */
    private int numOfExecutions = 100;
    /** 驱动周期性属性采样的 Timer */
    private Timer timer;
    /** 已完成采样轮次 */
    private long count = 0;

    @Argument(argName = "name-pattern", index = 0, required = false)
    @Description("ObjectName pattern, see javax.management.ObjectName for more detail. \n" +
            "It looks like this: \n" +
            "  domain: key-property-list\n" +
            "For example: \n" +
            "  java.lang:name=G1 Old Gen,type=MemoryPool\n" +
            "  java.lang:name=*,type=MemoryPool")
    public void setNamePattern(String name) {
        this.name = name;
    }

    @Argument(argName = "attribute-pattern", index = 1, required = false)
    @Description("Attribute name pattern.")
    public void setAttributePattern(String attribute) {
        this.attribute = attribute;
    }

    @Option(shortName = "i", longName = "interval")
    @Description("The interval (in ms) between two executions.")
    public void setInterval(long interval) {
        this.interval = interval;
    }

    @Option(shortName = "E", longName = "regex", flag = true)
    @Description("Enable regular expression to match attribute name (wildcard matching by default).")
    public void setRegEx(boolean regEx) {
        isRegEx = regEx;
    }

    @Option(shortName = "m", longName = "metadata", flag = true)
    @Description("Show metadata of mbean.")
    public void setMetaData(boolean metaData) {
        this.metaData = metaData;
    }

    @Option(shortName = "n", longName = "number-of-execution")
    @Description("The number of times this command will be executed.")
    public void setNumOfExecutions(int numOfExecutions) {
        this.numOfExecutions = numOfExecutions;
    }

    public String getName() {
        return name;
    }

    public boolean isRegEx() {
        return isRegEx;
    }

    public boolean isMetaData() {
        return metaData;
    }

    public long getInterval() {
        return interval;
    }

    public int getNumOfExecutions() {
        return numOfExecutions;
    }

    /** 按是否指定 name / --metadata 分发到列举、元数据或属性采样分支 */
    @Override
    public void process(CommandProcess process) {
        //每个分支调用process.end()结束执行
        if (StringUtils.isEmpty(getName())) {
            listMBean(process);
        } else if (isMetaData()) {
            listMetaData(process);
        } else {
            listAttribute(process);
        }
    }

    /** 无参数：queryNames 默认 *:* 并返回全部 MBean 名称列表 */
    private void listMBean(CommandProcess process) {
        Set<ObjectName> objectNames = queryObjectNames();
        List<String> mbeanNames = new ArrayList<String>(objectNames.size());
        for (ObjectName objectName : objectNames) {
            mbeanNames.add(objectName.toString());
        }
        process.appendResult(new MBeanModel(mbeanNames));
        process.end();
    }

    /** 按 name/attribute 模式周期性或单次读取可读属性，CompositeData 等会递归转换 */
    private void listAttribute(final CommandProcess process) {
        Session session = process.session();
        timer = new Timer("Timer-for-arthas-mbean-" + session.getSessionId(), true);

        // ctrl-C support
        process.interruptHandler(new MBeanInterruptHandler(process, timer));

        // 通过handle回调，在suspend和end时停止timer，resume时重启timer
        Handler<Void> stopHandler = new Handler<Void>() {
            @Override
            public void handle(Void event) {
                stop();
            }
        };

        Handler<Void> restartHandler = new Handler<Void>() {
            @Override
            public void handle(Void event) {
                restart(process);
            }
        };
        process.suspendHandler(stopHandler);
        process.resumeHandler(restartHandler);
        process.endHandler(stopHandler);
        // q exit support
        process.stdinHandler(new QExitHandler(process));

        // start the timer
        if (getInterval() > 0) {
            timer.scheduleAtFixedRate(new MBeanTimerTask(process), 0, getInterval());
        } else {
            timer.schedule(new MBeanTimerTask(process), 0);
        }

        //异步执行，这里不能调用process.end()，在timer task中结束命令执行
    }

    public synchronized void stop() {
        if (timer != null) {
            timer.cancel();
            timer.purge();
            timer = null;
        }
    }

    public synchronized void restart(CommandProcess process) {
        if (timer == null) {
            Session session = process.session();
            timer = new Timer("Timer-for-arthas-mbean-" + session.getSessionId(), true);
            timer.scheduleAtFixedRate(new MBeanTimerTask(process), 0, getInterval());
        }
    }

    /** --metadata：对每个匹配 ObjectName 拉取 MBeanInfo 写入模型 */
    private void listMetaData(CommandProcess process) {
        Set<ObjectName> objectNames = queryObjectNames();
        MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
        try {
            MBeanModel mbeanModel = new MBeanModel();
            Map<String, MBeanInfo> mbeanMetaData = new LinkedHashMap<String, MBeanInfo>();
            mbeanModel.setMbeanMetadata(mbeanMetaData);
            for (ObjectName objectName : objectNames) {
                MBeanInfo mBeanInfo = mBeanServer.getMBeanInfo(objectName);
                mbeanMetaData.put(objectName.toString(), mBeanInfo);
            }
            process.appendResult(mbeanModel);
            process.end();
        } catch (Throwable e) {
            logger.warn("listMetaData error", e);
            process.end(1, "list mbean metadata error");
        }
    }

    @Override
    public void complete(Completion completion) {
        int argumentIndex = CompletionUtils.detectArgumentIndex(completion);
        if (argumentIndex == 1) {
            if (!completeBeanName(completion)) {
                super.complete(completion);
            }
            return;
        } else if (argumentIndex == 2) {
            if (!completeAttributeName(completion)) {
                super.complete(completion);
            }
            return;
        }
        super.complete(completion);
    }

    private boolean completeBeanName(Completion completion) {
        List<CliToken> tokens = completion.lineTokens();
        String lastToken = TokenUtils.getLast(tokens).value();

        if (StringUtils.isBlank(lastToken)) {
            lastToken = "";
        }

        if (lastToken.startsWith("-") || lastToken.startsWith("--")) {
            return false;
        }

        Set<ObjectName> objectNames = queryObjectNames();
        Set<String> names = new HashSet<String>();

        if (objectNames == null) {
            return false;
        }
        for (ObjectName objectName : objectNames) {
            String name = objectName.toString();
            if (name.startsWith(lastToken)) {
                int index = name.indexOf('.', lastToken.length());
                if (index > 0) {
                    names.add(name.substring(0, index + 1));
                    continue;
                }
                index = name.indexOf(':', lastToken.length());
                if (index > 0) {
                    names.add(name.substring(0, index + 1));
                    continue;
                }
                names.add(name);
            }
        }
        String next = names.iterator().next();
        if (names.size() == 1 && (next.endsWith(".") || next.endsWith(":"))) {
            completion.complete(next.substring(lastToken.length()), false);
            return true;
        } else {
            return CompletionUtils.complete(completion, names);
        }
    }

    private boolean completeAttributeName(Completion completion) {
        List<CliToken> tokens = completion.lineTokens();
        String lastToken = TokenUtils.getLast(tokens).value();

        if (StringUtils.isBlank(lastToken)) {
            lastToken = "";
        }

        MBeanServer platformMBeanServer = ManagementFactory.getPlatformMBeanServer();
        String beanName = TokenUtils.retrievePreviousArg(tokens, lastToken);
        Set<ObjectName> objectNames = null;
        try {
            objectNames = platformMBeanServer.queryNames(new ObjectName(beanName), null);
        } catch (MalformedObjectNameException e) {
            logger.warn("queryNames error", e);
        }
        if (objectNames == null || objectNames.size() == 0) {
            return false;
        }
        try {
            MBeanInfo mBeanInfo = platformMBeanServer.getMBeanInfo(objectNames.iterator().next());
            List<String> attributeNames = new ArrayList<String>();
            MBeanAttributeInfo[] attributes = mBeanInfo.getAttributes();
            for (MBeanAttributeInfo attribute : attributes) {
                if (StringUtils.isBlank(lastToken)) {
                    attributeNames.add(attribute.getName());
                } else if (attribute.getName().startsWith(lastToken)) {
                    attributeNames.add(attribute.getName());
                }
            }
            return CompletionUtils.complete(completion, attributeNames);
        } catch (Throwable e) {
            logger.warn("getMBeanInfo error", e);
        }
        return false;
    }

    /** 将 name 模式转为 ObjectName 并 queryNames；空 name 视为 *:* */
    private Set<ObjectName> queryObjectNames() {
        MBeanServer platformMBeanServer = ManagementFactory.getPlatformMBeanServer();
        Set<ObjectName> objectNames = new HashSet<ObjectName>();
        try {
            if (StringUtils.isEmpty(name)) {
                name = "*:*";
            }
            objectNames = platformMBeanServer.queryNames(new ObjectName(name), null);
        } catch (MalformedObjectNameException e) {
            logger.warn("queryObjectNames error", e);
        }
        return objectNames;
    }

    /** 构造属性名通配或正则匹配器；未指定时匹配全部属性 */
    private Matcher<String> getAttributeMatcher() {
        if (StringUtils.isEmpty(attribute)) {
            attribute = isRegEx ? ".*" : "*";
        }
        return isRegEx ? new RegexMatcher(attribute) : new WildcardMatcher(attribute);
    }


    public static class MBeanInterruptHandler extends CommandInterruptHandler {

        private volatile Timer timer;

        public MBeanInterruptHandler(CommandProcess process, Timer timer) {
            super(process);
            this.timer = timer;
        }

        @Override
        public void handle(Void event) {
            timer.cancel();
            super.handle(event);
        }
    }

    /** 每轮读取匹配 MBean 的可读属性，interval<=0 时首轮后结束命令 */
    private class MBeanTimerTask extends TimerTask {

        private CommandProcess process;

        public MBeanTimerTask(CommandProcess process) {
            this.process = process;
        }

        @Override
        public void run() {
            if (count >= getNumOfExecutions()) {
                // stop the timer
                timer.cancel();
                timer.purge();
                process.end(-1, "Process ends after " + getNumOfExecutions() + " time(s).");
                return;
            }

            try {
                //result model
                MBeanModel mBeanModel = new MBeanModel();
                Map<String, List<MBeanAttributeVO>> mbeanAttributeMap = new LinkedHashMap<String, List<MBeanAttributeVO>>();
                mBeanModel.setMbeanAttribute(mbeanAttributeMap);

                MBeanServer platformMBeanServer = ManagementFactory.getPlatformMBeanServer();
                Set<ObjectName> objectNames = queryObjectNames();
                for (ObjectName objectName : objectNames) {
                    List<MBeanAttributeVO> attributeVOs = null;
                    MBeanInfo mBeanInfo = platformMBeanServer.getMBeanInfo(objectName);
                    MBeanAttributeInfo[] attributes = mBeanInfo.getAttributes();
                    for (MBeanAttributeInfo attribute : attributes) {
                        String attributeName = attribute.getName();
                        if (!getAttributeMatcher().matching(attributeName)) {
                            continue;
                        }

                        //create attributeVO list
                        if (attributeVOs == null) {
                            attributeVOs = new ArrayList<MBeanAttributeVO>();
                            mbeanAttributeMap.put(objectName.toString(), attributeVOs);
                        }

                        if (!attribute.isReadable()) {
                            attributeVOs.add(new MBeanAttributeVO(attributeName, null, "Unavailable"));
                        } else {
                            try {
                                Object attributeObj = platformMBeanServer.getAttribute(objectName, attributeName);
                                attributeVOs.add(createMBeanAttributeVO(attributeName, attributeObj));
                            } catch (Throwable e) {
                                logger.error("read mbean attribute failed: objectName={}, attributeName={}", objectName, attributeName, e);
                                String errorStr;
                                Throwable cause = e.getCause();
                                if (cause instanceof UnsupportedOperationException) {
                                    errorStr = "Unsupported";
                                } else {
                                    errorStr = "Failure";
                                }
                                attributeVOs.add(new MBeanAttributeVO(attributeName, null, errorStr));
                            }
                        }
                    }
                }
                process.appendResult(mBeanModel);
            } catch (Throwable e) {
                logger.warn("read mbean error", e);
                stop();
                process.end(1, "read mbean error.");
                return;
            }

            count++;
            process.times().incrementAndGet();
            if (getInterval() <= 0) {
                stop();
                process.end();
            }
        }
    }

    private MBeanAttributeVO createMBeanAttributeVO(String attributeName, Object originAttrValue) {
        Object attrValue = convertAttrValue(attributeName, originAttrValue);

        return new MBeanAttributeVO(attributeName, attrValue);
    }

    /** 将 ObjectName、CompositeData、TabularData 等 JMX 类型转为 JSON 友好结构 */
    private Object convertAttrValue(String attributeName, Object originAttrValue) {
        Object attrValue = originAttrValue;

        try {
            if (originAttrValue instanceof ObjectName) {
                attrValue = String.valueOf(originAttrValue);
            } else if (attrValue instanceof CompositeData) {
                //mbean java.lang:type=MemoryPool,name=*
                CompositeData compositeData = (CompositeData) attrValue;
                attrValue = convertCompositeData(attributeName, compositeData);
            } else if (attrValue instanceof CompositeData[]) {
                //mbean com.sun.management:type=HotSpotDiagnostic
                CompositeData[] compositeDataArray = (CompositeData[]) attrValue;
                List<Map<String, Object>> dataList = new ArrayList<Map<String, Object>>(compositeDataArray.length);
                for (CompositeData compositeData : compositeDataArray) {
                    dataList.add(convertCompositeData(attributeName, compositeData));
                }
                attrValue = dataList;
            } else if (attrValue instanceof TabularData) {
                //mbean java.lang:type=GarbageCollector,name=*
                TabularData tabularData = (TabularData) attrValue;
                Collection<CompositeData> compositeDataList = (Collection<CompositeData>) tabularData.values();
                List<Map<String, Object>> dataList = new ArrayList<Map<String, Object>>(compositeDataList.size());
                for (CompositeData compositeData : compositeDataList) {
                    dataList.add(convertCompositeData(attributeName, compositeData));
                }
                attrValue = dataList;
            }
        } catch (Throwable e) {
            logger.error("convert mbean attribute error, attribute: {}={}", attributeName, originAttrValue, e);
            attrValue = String.valueOf(originAttrValue);
        }
        return attrValue;
    }

    private Map<String, Object> convertCompositeData(String attributeName, CompositeData compositeData) {
        Set<String> keySet = compositeData.getCompositeType().keySet();
        String[] keys = keySet.toArray(new String[0]);
        Object[] values = compositeData.getAll(keys);
        Map<String, Object> data = new LinkedHashMap<String, Object>();
        for (int i = 0; i < keys.length; i++) {
            data.put(keys[i], convertAttrValue(attributeName + "." + keys[i], values[i]));
        }
        return data;
    }
}