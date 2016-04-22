package com.yejq.spring.quartz.controller;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;
import org.quartz.CronTrigger;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.yejq.spring.quartz.model.TiggerGroup;
import com.yejq.spring.quartz.model.TriggerModel;
import com.yejq.spring.quartz.util.SchedulerUtil;

@Controller
@RequestMapping("/scheduler")
public class SchedulerManagerController {

	// private Logger logger = LoggerFactory.getLogger(getClass());

	private String defaultView = "dashboard";

	@Resource
	private Scheduler scheduler;

	@RequestMapping({ "/", "/index" })
	public ModelAndView dashboard(String hasError) {
		ModelAndView modelAndView = getBaseMv();
		modelAndView.addObject("hasError", hasError);
		return modelAndView;
	}

	@RequestMapping(value = "/run", method = RequestMethod.GET)
	public String run(Model model) {
		try {
			SchedulerUtil.startupScheduler(scheduler);
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
		return "redirect:/scheduler/";
	}

	@RequestMapping(value = "/stop", method = RequestMethod.GET)
	public String stop() {
		try {
			scheduler.standby();
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
		return "redirect:/scheduler/";
	}

	@RequestMapping(value = "/pauseTrigger", method = RequestMethod.GET)
	public String pauseTrigger(String name, String group) {
		try {
			scheduler.pauseTrigger(name, group);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "redirect:/scheduler/";
	}

	@RequestMapping(value = "/resumeTrigger", method = RequestMethod.GET)
	public String resumeTrigger(String name, String group) {
		try {
			scheduler.resumeTrigger(name, group);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "redirect:/scheduler/";
	}
	
	@RequestMapping(value = "/triggerTrigger", method = RequestMethod.GET)
	public String triggerTrigger(String name, String group, Model model) {
		try {
			scheduler.triggerJob(name, group);
		} catch (Exception e) {
			model.addAttribute("hasError", "true");
			e.printStackTrace();
		}
		return "redirect:/scheduler/";
	}

	@RequestMapping(value = "/editTrigger", method = RequestMethod.GET)
	public String editTrigger(String name, String group, Model model) {
		Trigger trigger = null;
		try {
			trigger = scheduler.getTrigger(name, group);
		} catch (SchedulerException e) {
			e.printStackTrace();
		}

		String des = trigger.getDescription();
		while (des.endsWith(SchedulerUtil.PAUSED_FLAG)) {
			des = des.substring(0, des.indexOf(SchedulerUtil.PAUSED_FLAG));
		}
		trigger.setDescription(des);
		model.addAttribute("trigger", trigger);
		return "editTrigger";
	}

	@RequestMapping(value = "/saveTrigger", method = RequestMethod.POST)
	public void saveTrigger(String name, String group, String cronExpression, Model model, HttpServletResponse response)
			throws Exception {
		JSONObject jo = new JSONObject();
		try {
			CronTrigger cronTrigger = (CronTrigger) scheduler.getTrigger(name, group);
			cronTrigger.setCronExpression(cronExpression);
			
			if(reSchedulerJob(cronTrigger))
			{
				jo.put("flag", "1");
				jo.put("msg", "操作成功!");
			}else {
				jo.put("flag", "-1");
				jo.put("msg", "提示信息：任务无法重启");
			}
		} catch (SchedulerException e) {
			e.printStackTrace();
			jo.put("flag", "-1");
			jo.put("msg", "提示信息：" + e.getMessage());
		}

		PrintWriter outs = response.getWriter();
		outs.print(jo);
		outs.close();
	}
	
	private boolean reSchedulerJob(Trigger trigger)
	{
		boolean rs = true;
		try
		{	
			int status = scheduler.getTriggerState(trigger.getName(), trigger.getGroup());
			scheduler.unscheduleJob(trigger.getName(), trigger.getGroup());
			scheduler.scheduleJob(trigger);
			if (status == 0)
			{
				scheduler.resumeTrigger(trigger.getName(), trigger.getGroup());
			}
		}
		catch(SchedulerException e)
		{
			rs = false;
			e.printStackTrace();
		}
		return rs;
	}

	private ModelAndView getBaseMv() {
		ModelAndView mv = new ModelAndView(defaultView);
		mv.addObject("scheduler", scheduler);

		String[] triggerGroups;
		String[] triggersInGroup;
		try {
			if (scheduler.isInStandbyMode()) {
				return mv;
			}
			triggerGroups = scheduler.getTriggerGroupNames();

			List<TiggerGroup> tiggerGroups = new ArrayList<TiggerGroup>();
			for (int i = 0; i < triggerGroups.length; i++) {
				TiggerGroup tiggerGroup = new TiggerGroup();
				tiggerGroup.setGroupName(triggerGroups[i]);

				triggersInGroup = scheduler.getTriggerNames(triggerGroups[i]);
				List<TriggerModel> triggerModels = new ArrayList<TriggerModel>();
				for (int j = 0; j < triggersInGroup.length; j++) {
					TriggerModel tmodel = new TriggerModel();
					Trigger trigger = scheduler.getTrigger(triggersInGroup[j], triggerGroups[i]);
					if (trigger instanceof CronTrigger) {
						// 添加描述，去掉原来在trigger中的描述的 pasused_flag
						String des = trigger.getDescription();
						if (des != null) {
							while (des.endsWith(SchedulerUtil.PAUSED_FLAG)) {
								des = des.substring(0, des.indexOf(SchedulerUtil.PAUSED_FLAG));
							}
						}
						trigger.setDescription(des);
						tmodel.setTrigger(trigger);
						int status = scheduler.getTriggerState(triggersInGroup[j], triggerGroups[i]);
						// 如果发现执行job有错误,而且不是暂停状态,则设置为ERROR状态显示.
						if (SchedulerUtil.hasErrorTrigger(triggersInGroup[j]) && status != Trigger.STATE_PAUSED) {
							tmodel.setStatus(Trigger.STATE_ERROR);
						} else {
							tmodel.setStatus(status);
						}
						triggerModels.add(tmodel);
					}
				}
				tiggerGroup.setTriggerModels(triggerModels);
				tiggerGroups.add(tiggerGroup);
			}
			mv.addObject("tiggerGroups", tiggerGroups);
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
		return mv;
	}

}
