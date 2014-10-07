package com.yejq.spring.quartz.util;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.Node;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;

public class SchedulerUtil {
	/**
	 * 用于记录有问题的任务
	 */
	@SuppressWarnings("rawtypes")
	private static Map errorJobMap = new HashMap();
	private final static Log logger = LogFactory.getLog(SchedulerUtil.class);
	public final static String PAUSED_FLAG = "[P]";
	private static String xmlFile = "scheduling.xml";

	/**
	 * 记录出错的Trigger
	 * 
	 * @param triggerName
	 * @param exception
	 */
	@SuppressWarnings("unchecked")
	public static void addErrorJob(String triggerName, JobExecutionException exception) {
		if (errorJobMap.containsKey(triggerName)) {
			int count = (Integer) errorJobMap.get(triggerName);
			count++;
			errorJobMap.put(triggerName, count);
		} else {
			errorJobMap.put(triggerName, 0);
		}
	}

	/*
	 * 打开XML配置文件
	 */
	private static Document getConfigDocument() throws Exception {
		SAXReader reader = new SAXReader();
		reader.setEncoding("UTF-8");
		reader.setValidation(false);
		reader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
		Document document = reader.read(SchedulerUtil.class.getResourceAsStream(xmlFile));
		return document;
	}

	public static boolean hasErrorTrigger(String triggerName) {
		return errorJobMap.containsKey(triggerName);
	}

	public static void pausedTriggerInConfig(String triggerName) throws Exception {
		Document document = getConfigDocument();
		Node node_desc = document.selectSingleNode("/beans/bean[@id='" + triggerName
				+ "']/property[@name='description']/@value");
		String content = node_desc.getText();
		if (content == null) {
			content = "";
		}
		node_desc.setText(content + PAUSED_FLAG);
		saveConfig(document);
	}

	public static void removeErrorJob(String triggerName) {
		errorJobMap.remove(triggerName);
	}

	/*
	 * 保存XML配置文件
	 */
	private static void saveConfig(Document document) throws Exception {
		OutputFormat outformat = OutputFormat.createPrettyPrint();
		DefaultResourceLoader defaultResourceLoader = new DefaultResourceLoader();
		outformat.setEncoding("UTF-8");
		Resource resource = defaultResourceLoader.getResource(xmlFile);
		String xmlPath = "";
		try {
			xmlPath = resource.getFile().getPath();
		} catch (IOException e) {
			e.printStackTrace();
		}
		XMLWriter writer = new XMLWriter(new FileOutputStream(xmlPath), outformat);
		writer.write(document);
		writer.flush();
	}

	/**
	 * 启动定时器 如果xml配置文件中的description中,是以$paused_flag
	 * 结尾的,表示这个trigger是暂停状态,不需要resume. 其它的trigger全部resume.
	 */
	public static void startupScheduler(Scheduler scheduler) throws SchedulerException {

		scheduler.pauseAll();
		scheduler.start();
		String[] triggerGroups = scheduler.getTriggerGroupNames();

		for (int i = 0; i < triggerGroups.length; i++) {
			String[] triggersInGroup = scheduler.getTriggerNames(triggerGroups[i]);
			for (int j = 0; j < triggersInGroup.length; j++) {
				Trigger t = scheduler.getTrigger(triggersInGroup[j], triggerGroups[i]);
				if (t.getDescription() == null || !t.getDescription().endsWith(PAUSED_FLAG)) {
					scheduler.resumeTrigger(triggersInGroup[j], triggerGroups[i]);
				}
			}
		}
	}

	public static void unpausedTriggerInConfig(String triggerName) throws Exception {
		Document document = getConfigDocument();
		Node node_desc = document.selectSingleNode("/beans/bean[@id='" + triggerName
				+ "']/property[@name='description']/@value");
		if (node_desc == null) {
			return;
		}
		String content = node_desc.getText();
		if (content == null || !content.endsWith(PAUSED_FLAG)) {
			logger.warn("Description without paused flag. content[" + content + "]");
			return;
		}
		while (content.endsWith(PAUSED_FLAG)) {
			content = content.substring(0, content.indexOf(PAUSED_FLAG));
		}
		node_desc.setText(content);
		saveConfig(document);
	}

	/**
	 * 更新配置文件 for CronTrigger
	 * 
	 * @param triggerName
	 * @param cronExpression
	 * @throws Exception
	 */
	public static void updateCronTriggerConfig(String triggerName, String cronExpression) throws Exception {
		Document document = getConfigDocument();
		Node node = document.selectSingleNode("/beans/bean[@id='" + triggerName + "']/property/value");
		node.setText(cronExpression);
		saveConfig(document);
	}

	/**
	 * 更新配置文件 for SimpleTrigger
	 * 
	 * @param triggerName
	 * @param repeatInterval
	 * @throws Exception
	 */
	public static void updateSimpleTriggerConfig(String triggerName, long repeatInterval) throws Exception {
		Document document = getConfigDocument();
		Node node_repeatInterval = document.selectSingleNode("/beans/bean[@id='" + triggerName
				+ "']/property[@name='repeatInterval']/@value");
		node_repeatInterval.setText("" + repeatInterval);
		saveConfig(document);
	}
}
