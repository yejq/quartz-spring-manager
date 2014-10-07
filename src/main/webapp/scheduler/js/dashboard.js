function schedulerOperate(operate){
	if(operate == "run"){
		location.href = "/scheduler/run";
	}else if(operate == "stop"){
		location.href = "/scheduler/stop";
	}
}

function triggerOperate(operate, param){
	if(operate == "pause"){
		location.href = "/scheduler/pauseTrigger?" + param;
	}else if(operate == "resume"){
		location.href = "/scheduler/resumeTrigger?" + param;
	}else if(operate == "trigger"){
		location.href = "/scheduler/triggerTrigger?" + param;
	}
}

function remoteUrl(u){
    u += '&t=' + new Date().getTime();
    $.get(u, function(data){
        $('#editModal .modal-body').html(data);
        $('#editModal').modal({
        	show:true,
        	backdrop:false
        });
    });
}

function saveTrigger(){
	$("#saveTriggerBtn").attr("disabled", "true");
	$("#editTriggerForm").form("submit",{
		url: "/scheduler/saveTrigger",
		success:function(json){
	  		try{
	  			eval("var data="+json);
            	alert(data.msg);
	    		if(data.flag == "1"){ 
	    	    	window.location.href = "/scheduler/";
	    		}else{
	    			$("#saveTriggerBtn").attr("disabled", "false");
	    		}
	  		}catch(e){
	    		alert("操作失败："+e);
	  		}
		}
	});
}

//对Date的扩展，将 Date 转化为指定格式的String 
//月(M)、日(d)、小时(h)、分(m)、秒(s)、季度(q) 可以用 1-2 个占位符， 
//年(y)可以用 1-4 个占位符，毫秒(S)只能用 1 个占位符(是 1-3 位的数字) 
//例子： 
//(new Date()).Format("yyyy-MM-dd hh:mm:ss.S") ==> 2006-07-02 08:09:04.423 
//(new Date()).Format("yyyy-M-d h:m:s.S")      ==> 2006-7-2 8:9:4.18 
Date.prototype.Format = function(fmt) 
{ //author: meizz 
	var o = { 
	 "M+" : this.getMonth()+1,                 //月份 
	 "d+" : this.getDate(),                    //日 
	 "h+" : this.getHours(),                   //小时 
	 "m+" : this.getMinutes(),                 //分 
	 "s+" : this.getSeconds(),                 //秒 
	 "q+" : Math.floor((this.getMonth()+3)/3), //季度 
	 "S"  : this.getMilliseconds()             //毫秒 
	}; 
	if(/(y+)/.test(fmt)) 
	 fmt=fmt.replace(RegExp.$1, (this.getFullYear()+"").substr(4 - RegExp.$1.length)); 
	for(var k in o) 
	 if(new RegExp("("+ k +")").test(fmt)) 
	fmt = fmt.replace(RegExp.$1, (RegExp.$1.length==1) ? (o[k]) : (("00"+ o[k]).substr((""+ o[k]).length))); 
	return fmt; 
}

$(function(){
	$("#editModal").on("hidden.bs.modal", function() {
	    //$(this).removeData("bs.modal");
		$(this).removeData();
	});
	
	function displayTime() {  
	    $("#time").html(new Date().Format("yyyy-MM-dd hh:mm:ss"));  
	}   
	            
	setInterval(displayTime,1000);
});