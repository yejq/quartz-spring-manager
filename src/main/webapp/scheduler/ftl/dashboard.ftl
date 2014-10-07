<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="utf-8">
<meta http-equiv="X-UA-Compatible" content="IE=edge">
<meta name="viewport" content="width=device-width, initial-scale=1">
<#import "/lib/main.ftl" as main>
<title>${main.title}</title>
<#include "/common/header.ftl">
</head>
<body>
	<div class="container" style="width:90%;">
		<div class="row page-header fixed-top">
			<a href="/scheduler/"><img class="logo" src="/scheduler/images/logo-quartz-scheduler.png"></a>
			<h1>欢迎，这里是订单中心的定时器管理面板&nbsp;&nbsp;<small>当前时间：</small><small id="time"></small></h1>
		</div>
		<div class="row" style="margin-top: 83px;">
			<#if hasError??>
			<div id="errorMsgDiv" class="alert alert-danger alert-dismissable">
		        <button type="button" class="close" data-dismiss="alert"
		            aria-hidden="true">&times;</button>
				<span id="errorMsgSpan">操作失败！</span>
		    </div>
		    </#if>
			<div class="panel panel-info">
		        <div class="panel-heading">
		            <h3 class="panel-title"><span class="glyphicon glyphicon-refresh"/> 定时器状态</h3>
		        </div>
		        <div class="panel-body">
		        <#if !scheduler.inStandbyMode && scheduler.started>
		        	<span class="running">运行中……</span><button type="button" class="btn btn-primary" onclick="schedulerOperate('stop')">停止</button>
		        </#if>
		        <#if scheduler.inStandbyMode || !scheduler.started>
		        	<span class="stoped">停止中……</span><button type="button" class="btn btn-primary" onclick="schedulerOperate('run')">启动</button>
		        </#if>
		        &nbsp;&nbsp;
		        <button type="button" class="btn btn-success" onclick="location.href='/scheduler/'">刷新</button>
		        </div>
		    </div>
		</div>
		<#if tiggerGroups??>
		<div class="row">
			<#list tiggerGroups as tiggerGroup>
			<div class="panel panel-default">
		        <div class="panel-heading"><span class="glyphicon glyphicon-cog"/> 组名:${tiggerGroup.groupName}</div>
		        <table class="table table-bordered table-striped">
		        	<thead>
		        		<tr>
				            <th style="width:20%;">任务名</th>
				            <th style="width:25%;">描述</th>
				            <th style="width:10%;">周期表达式</th>
				            <th style="width:12%;">上一次运行时间</th>
				            <th style="width:12%;">下一次运行时间</th>
				            <th style="width:5%;">状态</th>
				            <th style="width:15%;">操作</th>
			            </tr>
		            </thead>
		            <tbody>
			            <#list tiggerGroup.triggerModels as triggerModel>
			            <tr>
			                	<td>${triggerModel.trigger.name}</td>
				                <td>${triggerModel.trigger.description}</td>
				                <td>${triggerModel.trigger.cronExpression}</td>
				                <td>
				                	<#if (triggerModel.trigger.previousFireTime)??>
				                		${triggerModel.trigger.previousFireTime?datetime}
				                	</#if>
				                </td>
				                <td>
				                	<#if triggerModel.trigger.nextFireTime??>
				                		${triggerModel.trigger.nextFireTime?datetime}
				                	</#if>
				                </td>
				                <td>
				                	<#if triggerModel.status == 0>
				                		<font color="green">正常</font>
				                	<#elseif triggerModel.status == 1>
				                		暂停
				                	<#elseif triggerModel.status == 2>
				                		完成
				                	<#elseif triggerModel.status == 3>
				                		<font color="red">错误</font>
				                	<#elseif triggerModel.status == 4>
				                		<font color="red">阻塞</font>
				                	<#elseif triggerModel.status == -1>
				                		无
				                	</#if>
				                </td>
				                <td>
				                	<#if triggerModel.status == 1>
				                		<button type="button" class="btn btn-success btn-xs" onclick="triggerOperate('resume', 'name=${triggerModel.trigger.name}&group=${tiggerGroup.groupName}')">启动</button>
				                	<#elseif triggerModel.status != 1>
				                		<button type="button" class="btn btn-danger btn-xs" onclick="triggerOperate('pause', 'name=${triggerModel.trigger.name}&group=${tiggerGroup.groupName}')">暂停</button>
				                	</#if>
				                	<button type="button" class="btn btn-warning btn-xs disabled" onclick="triggerOperate('trigger', 'name=${triggerModel.trigger.name}&group=${tiggerGroup.groupName}')">立即启动</button>
				                	&nbsp;
				                	<a role="button" class="btn btn-primary btn-xs" data-toggle="modal" data-target="#editModal"
				                		onclick="remoteUrl('/scheduler/editTrigger?name=${triggerModel.trigger.name}&group=${tiggerGroup.groupName}')">编辑</a>
				                </td>
			            </tr>
			            </#list>
		            </tbody>
		        </table>
		    </div>
		    </#list>
		</div>

		<div class="modal fade" id="editModal" tabindex="-1" role="dialog"
			aria-labelledby="myModalLabel" aria-hidden="true">
			<div class="modal-dialog">
				<div class="modal-content">
					<div class="modal-header">
						<button type="button" class="close" data-dismiss="modal"
							aria-hidden="true">&times;</button>
						<h4 class="modal-title" id="myModalLabel"><span class="glyphicon glyphicon-floppy-disk"/> 任务编辑：</h4>
					</div>
					<div class="modal-body"></div>
					<div class="modal-footer">
						<button type="button" class="btn btn-default" data-dismiss="modal">关闭</button>
						<button type="button" class="btn btn-primary" id="saveTriggerBtn" name="saveTriggerBtn" onclick="saveTrigger()">保存</button>
					</div>
					<div class="panel panel-success">
				        <div class="panel-heading">
				            <h3 class="panel-title">常用表达式，<small><a href="http://www.hahuachou.com/cron/index.htm" target="_blank">在线cron表达式生成器</a></small></h3>
				        </div>
				        <div class="panel-body">
				        <pre>
0 0 12 * * ? 		每天中午12点触发 
0 15 10 ? * * 		每天上午10:15触发 
0 15 10 * * ?  		每天上午10:15触发 
0 15 10 * * ? * 	每天上午10:15触发 
0 15 10 * * ? 		2005 2005年的每天上午10:15触发 
0 * 14 * * ? 		在每天下午2点到下午2:59期间的每1分钟触发 
0 0/5 14 * * ? 		在每天下午2点到下午2:55期间的每5分钟触发 
0 0/5 14,18 * * ? 	在每天下午2点到2:55期间和下午6点到6:55期间的每5分钟触发 
0 0-5 14 * * ? 		在每天下午2点到下午2:05期间的每1分钟触发 
0 10,44 14 ? 3 WED 	每年三月的星期三的下午2:10和2:44触发 
0 15 10 ? * MON-FRI 	周一至周五的上午10:15触发 
0 15 10 15 * ? 		每月15日上午10:15触发 
0 15 10 L * ? 		每月最后一日的上午10:15触发 
0 15 10 ? * 6L 		每月的最后一个星期五上午10:15触发 
0 15 10 ? * 6L 		2002-2005 2002年至2005年的每月的最后一个星期五上午10:15触发 
0 15 10 ? * 6#3 	每月的第三个星期五上午10:15触发 
						</pre>
				        </div>
				    </div>
				</div>
			</div>
		</div>

		</#if>
		<#include "/common/footer.ftl">
		<script src="/scheduler/js/dashboard.js"></script>
	</div>
</body>
</html>
