
//
//  MAIN PAGE
//

var QUEUE_PREFIX = "queue://";

function mainMonitorizationInit() 
{
  // queues
  var f_queues = function() {
   new Ajax.Request('/dataquery/last?predicate=queue-size&minvalue=0&count=10',
   {
    method:'get',
    onSuccess: function(transport){
      var panel = s$('queue_size');
      var data = transport.responseJSON;      
      setQueueInfo(data, panel);
    },
    onFailure: function(){ alert('Something went wrong while trying to get queue info...'); }
   });  
  }
 
  // pending ack
   var f_pendingAck = function() {
   new Ajax.Request('/dataquery/last?subject=system-message&predicate=ack-pending&minvalue=-1&count=10',
   {
    method:'get',
    onSuccess: function(transport){
      var panel = s$('pending_ack');
      var data = transport.responseJSON;      
      setSysMsgInfo(data, panel);
    },
    onFailure: function(){ alert('Something went wrong while trying to get pending ack info...'); }
   });  
  }
 
  // dropbox
  var f_dropboxes = function() {
   new Ajax.Request('/dataquery/last?predicate=count&subject=dropbox&count=10&minvalue=-1&orderby=object_value',
   {
    method:'get',
    onSuccess: function(transport){
      var panel = s$('dropbox_files');
      var data = transport.responseJSON;      
      setDropboxInfo(data, panel);
    },
    onFailure: function(){ alert('Something went wrong while trying to get dropbox info...'); }
   });  
  }
  // errors
  var f_errors = function() {
   new Ajax.Request('dataquery/groupfault?groupby=shortmessage',
   {
    method:'get',
    onSuccess: function(transport){
      var panel = s$('errors');
      var data = transport.responseJSON;      
      setErrorInfo(data, panel);
    },
    onFailure: function(){ alert('Something went wrong while trying to get faults info...'); }
   });  
  }

  // agents
  var f_agents = function() {
   new Ajax.Request('/dataquery/last?predicate=status',
   {
    method:'get',
    onSuccess: function(transport){
      var panel = s$('agentsDownInformationPanel');
      var data = transport.responseJSON;      
      setAgentsInfo(data, panel);
    },
    onFailure: function(){ alert('Something went wrong while trying to get agent status info...'); }
   });  
  }

  // rate
  var f_rates = function() {
	processGraph("/dataquery/static?queuecount", "img_queue_size_rate", "queue_size_rate");
	processGraph("/dataquery/static?faultrate", "img_error_rate", "count_error_rate", "m/s");
	processGraph("/dataquery/static?inputrate", "img_input_rate", "count_input_rate", "m/s");
	processGraph("/dataquery/static?outputrate", "img_output_rate", "count_output_rate", "m/s");
  }

  f_queues();
  setInterval(f_queues, 5000);
  f_pendingAck();
  setInterval(f_pendingAck, 5100);
  f_agents();
  setInterval(f_agents, 5300);
  f_errors();
  setInterval(f_errors, 5400);
  f_dropboxes();
  setInterval(f_dropboxes, 5500);
  f_rates();
  setInterval(f_rates, 5600);
}


function processGraph(queryStr, imgId, legendId, unit)
{
  new Ajax.Request(queryStr,
   {
    method:'get',
    onSuccess: function(transport){
	var img = s$(imgId);
	var data = transport.responseJSON;
	var min = 0;
	var max = 0;
	var dif = 0;
	
	if(data.length == 0)
		return;

	// determine max ans min	
	// first sample
	var min = parseFloat(data[0].value);
	var max = parseFloat(data[0].value);
	
	for(var i = 1; i < data.length;i++)
	{
		var curValue = parseFloat(data[i].value);
		if(curValue < min) min = curValue;
		if(curValue > max) max = curValue;
	}
	dif = max-min;
	var normalizedValues = new Array(data.length);
	var originalData = new Array(data.length);
	var url="http://chart.apis.google.com/chart?cht=ls&chs=200x90&chd=t:"
	
	// process first sample
	var sample = (parseFloat(data[0].value));
	originalData[0] = sample;
	var bottom = sample - min;	
	var curValue = (bottom != 0) ? ((bottom / dif) * 100) : bottom;
	curValue = round(curValue);	
	normalizedValues[0] = curValue;
	url = url + curValue;
	

	// ((originalData[0]-min) != 0) ? (((originalData[0]-min) / dif) * 100) : originalData[0]

	// process remaining samples
	for(var i = 1; i < data.length;i++)
	{
		var sample = (parseFloat(data[i].value));
		originalData[i] = sample;		
		var bottom = sample - min;	
		var curValue = (bottom != 0) ? ((bottom / dif) * 100) : bottom;		
		//var curValue = (sample != 0) ? (((sample - min) / dif) * 100) : sample;
		curValue = round(curValue);
		normalizedValues[i] = curValue;
		url = url + ","+curValue;
	}
	url = url + "&chco=336699&chls=3,1,0&chm=o,990000,0," + (data.length-1) + ",4&chxt=r,x,y&chxs=0,990000,40,0,_|1,990000,1,0,_|2,990000,1,0,_&chxl=0:|"
	//url = url + Math.round(parseFloat(data[data.length-1].value));
	url = url + "|1:||2:||&chxp=0,42.3&chf=bg,s,cecece";

	min = round(min);
	max = round(max);
	var latest = round(parseFloat(data[data.length-1].value));

	var s_unit = "";
	if(typeof(unit)!=undefined && unit!=null)
	{
		s_unit="&nbsp;" + unit;
	}
	

	var legend = s$(legendId);
	legend.innerHTML = "<p><span class='mvalue-latest'>" + latest + s_unit+ "</span></p><p><span class='mlabel'>Min: </span><span class='mvalue'>" + min + "</span>;<span class='mlabel'> Max: </span><span class='mvalue'>" + max + "</span></p>";
	
	img.src = url;
    },
    onFailure: function(){ alert('Something went wrong while trying to get queue info...'); }
   });
}

var previousQueueInfo = new Object();

// general queue info
function setQueueInfo(queueInfo, panel)
{
	var newContent = "";

	if (queueInfo.length == 0)
	{
        	newContent = "<td class=\"oddrow\" colspan=\"3\">No information available.</td>";
  	}
	else
	{
		var queues = new Object();
		for(var i = 0; i != queueInfo.length; ++i)
		{
			var queueName = removePrefix(queueInfo[i].subject, QUEUE_PREFIX);
			var strCount = queueInfo[i].value;
			var count = parseFloat(strCount);
			var curDate = parseISO8601(queueInfo[i].time);
			//queues[queueName] += count;
			if( queues[queueName] === undefined)
			{	
				queues[queueName] = new Object();
				queues[queueName].count = count;
				queues[queueName].time = curDate;
			}
			else
			{
				queues[queueName].count += count;
			}
			if (curDate > queues[queueName].time)
			{
				queues[queueName].time = curDate;
			}
		}
		var content = "";
		var count = 0;
		for(var queueName in queues)
		{
			var count = queues[queueName].count;
			var previousValue = previousQueueInfo[queueName];
			var pic = getLocalPic(previousValue, count);
			var curDate = queues[queueName].time;
			previousQueueInfo[queueName] = count;
			var rowClass =  ( ((count++)%2) == 0) ? "evenrow" : "oddrow";
			content = content + "<tr class=\"" + rowClass +"\"><td><a href='./queue.html?queuename="+queueName+"'>"+ queueName+ "</a></td><td class=\"countrow\">" +  count + "</td><td><img src='"+ pic + "' /></td></tr>";
		}
		newContent = content;
	}
	panel.innerHTML = newContent;
}

var previousSysMsgInfo = new Object();
// pending sys messages 
function setSysMsgInfo(sysMsgInfo, panel)
{
	var newContent = "";

	if (sysMsgInfo.length == 0)
	{
        	newContent = "<td class=\"oddrow\" colspan=\"3\">No information available.</td>";
  	}
	else
	{
		var content = "";
		var count = 0;
		for(var i = 0; i != sysMsgInfo.length; ++i)
		{
			var sysInfo = sysMsgInfo[i];
			var agentname = sysInfo.agentName;
			var agentHostname = sysInfo.agentHostname;
			
			var count = parseFloat(sysInfo.value);

			var previousValue = previousSysMsgInfo[agentname];
			var pic = getLocalPic(previousValue, count);
			previousSysMsgInfo[agentname] = count;
			var rowClass =  ( ((i+1)%2) == 0) ? "evenrow" : "oddrow";
			content = content + "<tr class=\"" + rowClass +"\"><td><a href='./agent.html?agentname="+agentname+"'>" + agentHostname + "</a></td><td class=\"countrow\">" +  count + "</td><td><img src='"+ pic + "' /></td></tr>";
		}
		newContent = content;
	}
	panel.innerHTML = newContent;
}

var previousDropboxInfo = new Object();

// general dropbox info
function setDropboxInfo(dropboxInfo, panel)
{
	var newContent = "";

	if (dropboxInfo.length == 0)
	{
        	newContent = "<td class=\"oddrow\" colspan=\"3\">No information available.</td>";
  	}
	else
	{
		for(var i = 0; i != dropboxInfo.length; ++i)
		{
			var agentname = dropboxInfo[i].agentName;
			var agentHostname = dropboxInfo[i].agentHostname;
			var count = parseFloat(dropboxInfo[i].value);

			var previousValue = previousDropboxInfo[agentname];
			var pic = getLocalPic(previousValue, count);
			previousDropboxInfo[agentname] = count;

			var rowClass =  ( ((i+1)%2) == 0) ? "evenrow" : "oddrow";
			newContent = newContent + "<tr class=\"" + rowClass +"\"><td><a href='./agent.html?agentname="+agentname+"'>"+ agentHostname+ "</a></td><td class=\"countrow\">" +  count + "</td><td><img src='"+ pic + "' /></td></tr>";
		}
	}
	panel.innerHTML = newContent;
}

var previousFaultInfo = new Object();
// general fault info
function setErrorInfo(errorInfo, panel)
{
	var newContent = "";

	if (errorInfo.length == 0)
	{
        	newContent = "<td class=\"oddrow\" colspan=\"3\">No information available.</td>";
  	}
	else
	{
		for(var i = 0; i != errorInfo.length; ++i)
		{
			var shortMessage = errorInfo[i].shortMessage;
			var count = errorInfo[i].count;
			var previousValue = previousSysMsgInfo[shortMessage];
			var pic = getLocalPic(previousValue, count);
			previousSysMsgInfo[shortMessage] = count;
			var rowClass =  ( ((i+1)%2) == 0) ? "evenrow" : "oddrow";

			newContent = newContent + "<tr class=\"" + rowClass +"\"><td><a href='./faults.html?shortmessage="+shortMessage+"'>"+ shortMessage+ "</a></td><td class=\"countrow\">" +  count + "</td><td><img src='"+ pic + "' /></td></tr>";
		}
	}
	panel.innerHTML = newContent;
}

// general agents info
function setAgentsInfo(agentInfo, panel)
{
	var newContent = "";

	if (agentInfo.length == 0)
	{
        	newContent = "<p>All agents are online</P>";
  	}
	else
	{
		for(var i = 0; i != agentInfo.length; ++i)
		{
			var agentname = agentInfo[i].agentName;
			var result = parseFloat(agentInfo[i].value);
			if( result == 0)
			{
				newContent = newContent + "<p><a href='./agent.html?agentname="+ agentname+ "'>" + agentname + "</a> : Down : " + agentInfo[i].time +"</p>";
			}
		}
	}
	panel.innerHTML = newContent;
}


//
// QUEUE PAGE
//
function queueMonitorizationInit() 
{
  var params = SAPO.Utility.Url.getQueryString();
  var queueName = params.queuename;
  var qnPanel =  s$('queue_name'); 
  
  var countPanel = s$('queue_msg_count');

  if (queueName == null)
  {
        qnPanel.innerHTML = "<b>Queue name not specified</b>";
	return;
  }
 qnPanel.innerHTML = queueName;

  var f_rates = function() {
	processGraph("/dataquery/queue?rate=count&queuename=" + queueName, "img_queue_size", "count_queue_size");
	processGraph("/dataquery/queue?rate=input&queuename=" + queueName, "img_input_rate", "count_input_rate", "m/s");
	processGraph("/dataquery/queue?rate=output&queuename=" + queueName, "img_output_rate", "count_output_rate", "m/s");
	processGraph("/dataquery/queue?rate=failed&queuename=" + queueName, "img_failed_rate", "count_failed_rate", "m/s");
  }

  var f_generalInfo = function() {
   new Ajax.Request("/dataquery/queue?queuename=" + queueName,
   {
    method:'get',
    onSuccess: function(transport){
      var panel = s$('general_queue_information');
      var data = transport.responseJSON;      
      setGeneralQueueInfo(data, panel);
    },
    onFailure: function(){ alert('Something went wrong...') }
   });  
  }

  f_rates();
  setInterval(f_rates, 5200);
  f_generalInfo();
  setInterval(f_generalInfo, 5000);
}

var previousGeneralQueueInfo = new Object();
// queue agent info
function setGeneralQueueInfo(queueGeneralInfo,  panel)
{
	var count = 0;
	var newContent = "";
	if (queueGeneralInfo.length == 0)
	{
        	newContent = "<p>No information available.</P>";
  	}
	else
	{
		for(var i = 0; i != queueGeneralInfo.length; ++i)
		{
			var agentname = queueGeneralInfo[i].agentName;		

			var prevQueueInfo = previousGeneralQueueInfo[agentname];

			if( prevQueueInfo === undefined)
			{	
				prevQueueInfo = new Object();
				previousGeneralQueueInfo[agentname] = prevQueueInfo;
			}
		
			var agentCount = parseFloat(queueGeneralInfo[i].queueSize);
			var pic = getLocalPic(prevQueueInfo.queueSize, agentCount);
			prevQueueInfo.queueSize = agentCount;
			
			var rowClass =  ( ((i+1)%2) == 0) ? "evenrow" : "oddrow";

			newContent = newContent + "<tr class=\"" + rowClass +"\"><td><a href='./agent.html?agentname="+ agentname+ "'>" + queueGeneralInfo[i].agentHostname + "</a></td><td>" + agentCount + "</td><td><img src='" + pic + "' /></td>";


			var inputRate = round(parseFloat(queueGeneralInfo[i].inputRate));
			pic = getLocalPic(prevQueueInfo.inputRate, inputRate);
			prevQueueInfo.inputRate = inputRate;
			newContent = newContent + "<td>" + inputRate + "</td><td><img src='" + pic + "' /></td>";
	
			var outputRate = round(parseFloat(queueGeneralInfo[i].outputRate));
			pic = getLocalPic(prevQueueInfo.outputRate, outputRate);
			prevQueueInfo.outputRate = outputRate;
			newContent = newContent + "<td>" + outputRate + "</td><td><img src='" + pic + "' /></td>";

			var failedRate = round(parseFloat(queueGeneralInfo[i].failedRate));
			pic = getLocalPic(prevQueueInfo.failedRate, failedRate);
			prevQueueInfo.failedRate = failedRate;
			newContent = newContent + "<td>" + failedRate + "</td><td><img src='" + pic + "' /></td>";

			var expiredRate = round(parseFloat(queueGeneralInfo[i].expiredRate));
			pic = getLocalPic(prevQueueInfo.expiredRate, expiredRate);
			prevQueueInfo.expiredRate = expiredRate;
			newContent = newContent + "<td>" + expiredRate + "</td><td><img src='" + pic + "' /></td>";

			var redeliveredRate = round(parseFloat(queueGeneralInfo[i].redeliveredRate));
			pic = getLocalPic(prevQueueInfo.redeliveredRate, redeliveredRate);
			prevQueueInfo.redeliveredRate = redeliveredRate;
			newContent = newContent + "<td>" + redeliveredRate + "</td><td><img src='" + pic + "' /></td>";

			newContent = newContent + "<td>" + parseFloat(queueGeneralInfo[i].subscriptions) + "</td><td><img src='" + pic + "' /></td></tr>";

		}
	}

	panel.innerHTML = newContent;
}
// Delete queue confirmation
function confirmDelete()
{
	var qnPanel =  s$('queue_name'); 
	var queueName = qnPanel.innerHTML;
	var res = confirm('Are you sure you want to delete queue: ' + queueName);
	if(res)
	{
		window.location = 'deletequeue.html?queuename='+queueName;
	}		
	
	
	return false;
}

//
// DELETE QUEUE PAGE
//
function queueDeleteInit()
{
	var params = SAPO.Utility.Url.getQueryString();
	var qnPanel =  s$('queue_name'); 
	var queueName = params.queuename;

	if (queueName == null)
	{
		qnPanel.innerHTML = "<b>Queue name not specified</b>";
		return;
	}
	qnPanel.innerHTML = queueName;
	var msgPanel =  s$('msg_pannel'); 
	msgPanel.innerHTML = "Deleting queue. This may take some time...";
	new Ajax.Request('/action/deletequeue?queuename='+queueName,
	{
	    method:'get',
	    onSuccess: function(transport){
	      var data = transport.responseJSON;
	      var newContent = "";

	      if (data.length == 0)
	      {
	      	newContent = "There is no queue info";
	      }
	      else
	      {	      var fail = false;
		      for(var i = 0; i != data.length; ++i)
		      {
				if(data[i].sucess == "true")
				{
					newContent = newContent + "<a href='./agent.html?agentname="+data[i].agentName+"'>"+ data[i].agentName+ "</a> : OK" +"\n";
				}
				else
				{
					newContent = newContent + "<a href='./agent.html?agentname="+data[i].agentName+"'>"+ data[i].agentName+ "</a> : Failed : " + data[i].reason +"\n";
					fail = true;
				}
		      }
		      if(fail)
		      {
				newContent = newContent + "\n\n Message delete failures caused by connection failure or the existence of active subscribers will be retried later."
		      }
			
	      }
	      
	      msgPanel.innerHTML = newContent;
	    },
	    onFailure: function(){ alert('Something went wrong...') }
	 });
}

//
// FAULT PAGE
//
function faultInformationInit()
{
  var params = SAPO.Utility.Url.getQueryString();
  var params = SAPO.Utility.Url.getQueryString();
  var idPanel = s$('fault_id');
  var faultId = params.faultid;
  if (faultId == null)
  {
        idPanel.innerHTML = "Fault id not specified";
	return;
  }
  idPanel.innerHTML = faultId;
  new Ajax.Request('/dataquery/fault?id='+faultId,
   {
    method:'get',
    onSuccess: function(transport){
      var data = transport.responseJSON; 

      var shortMsgPanel = s$('fault_shortmsg');
      shortMsgPanel.innerHTML = data[0].shortMessage;

      var datePanel = s$('fault_date');
      datePanel.innerHTML = data[0].time;

      var agentPanel = s$('agent_name');
      agentPanel.innerHTML = data[0].agentName;

      var msgPanel = s$('fault_msg');
      msgPanel.innerHTML = data[0].message;
    },
    onFailure: function(){ alert('Something went wrong...') }
   });
}
//
// ALL AGENT PAGE
//

function allAgentInit()
{
   new Ajax.Request('/dataquery/last?predicate=status&order=agent_name',
   {
    method:'get',
    onSuccess: function(transport){
	var panel = s$('agents');
	var data = transport.responseJSON;
	
	var newContent = "";

	if (data.length == 0)
	{
        	newContent = "<td class=\"oddrow\" colspan=\"3\">No information available.</td>";
  	}
	else
	{
		var count = 1;
		var content = "";
		for(var i = 0; i != data.length; ++i)
		{
			var status = ( data[i].value == "1.0") ? "Ok" : "Down";
			var rowClass =  ( ((count++)%2) == 0) ? "evenrow" : "oddrow";
			var agentName = data[i].agentName;
			//content = content + "<tr class=\"" + rowClass +"\"><td><a href='./agent.html?agentname="+agentName+"'>"+ agentName+ "</a></td><td class=\"countrow\">" +  data[i].agentHostname + "</td><td>"+ status + "</td></tr>";
			content = content + "<tr class=\"" + rowClass +"\"><td><a href='./agent.html?agentname="+agentName+"'>"+ data[i].agentHostname+ "</a></td><td>"+ status + "</td></tr>";
		}
		newContent = content;
	}
	panel.innerHTML = newContent;
    },
    onFailure: function(){ alert('Something went wrong while trying to get all agent\'s info...') }
   });  
	
}

//
// AGENT PAGE
//
function agentMonitorizationInit() 
{
  var params = SAPO.Utility.Url.getQueryString();
  var idPanel = s$('agent_name');
  var agentname = params.agentname;
  if (agentname == null)
  {
        idPanel.innerHTML = "<b>Agent name not specified</b>";
	return;
  }
  idPanel.innerHTML = agentname;
  // queues
  var f_queues = function() {
   new Ajax.Request('/dataquery/last?predicate=queue-size&agent='+agentname,
   {
    method:'get',
    onSuccess: function(transport){
      var panel = s$('queuesInformationPanel');
      var data = transport.responseJSON;      
      setAgentQueueInfo(data, panel);
    },
    onFailure: function(){ alert('Something went wrong while trying to get agent\'s queue info...') }
   });  
  }
  // faults
  var f_faults = function() {
   new Ajax.Request('/dataquery/fault?count=10&agent='+agentname,
   {
    method:'get',
    onSuccess: function(transport){
      var panel = s$('faultsInformationPanel');
      var data = transport.responseJSON;      
      setAgentFaultInfo(data, panel);
    },
    onFailure: function(){ alert('Something went wrong while trying to get agent\'s faults info...') }
   });  
  }
  // subscriptions
  var f_subscriptions = function() {
   new Ajax.Request('/dataquery/last?predicate=subscriptions&agent='+agentname,
   {
    method:'get',
    onSuccess: function(transport){
      var panel = s$('subscriptionInformationPanel');
      var data = transport.responseJSON;      
      setAgentSubscriptionInfo(data, panel);
    },
    onFailure: function(){ alert('Something went wrong while trying to get agent\'s subscriptions info...') }
   });  
  }

  // state
  var f_state = function() {
   new Ajax.Request('/dataquery/last?predicate=status&agent='+agentname,
   {
    method:'get',
    onSuccess: function(transport){
      var panel = s$('agent_state');
      var data = transport.responseJSON;      
      var content = "<p>Agent status not available.</p>";
      if(data.length != 0)
	content = ( ( parseFloat(data[0].value) == 0) ? "Down" : "Ok") +" : " + data[0].time;
      panel.innerHTML = content;     
    },
    onFailure: function(){ alert('Something went wrong while trying to get agent\'s status info...') }
   });  
  }
  // dropbox
  var f_dropbox = function() {
   new Ajax.Request('/data/dropbox/agent?agentname='+agentname,
   {
    method:'get',
    onSuccess: function(transport){
      var panel = s$('agent_dropbox');
      var data = transport.responseJSON;      
      var content = "Agent dropbox information not available.";
      if(data.length != 0)
	content = data[0].dropboxLocation +" : " + data[0].messages +" : " + data[0].goodMessages;
      panel.innerHTML = content;     
    },
    onFailure: function(){ alert('Something went wrong while trying to get agent\'s dropbox info...') }
   });
  }

  // hostname
  var f_hostname = function() {
   new Ajax.Request('/hostname?name='+agentname,
   {
    method:'get',
    onSuccess: function(transport){
      var response = transport.responseText;
      var panel = s$('host_name');
      var data = response.evalJSON();
      var content = "";
      content = data.hostname;
      panel.innerHTML = content;     
    },
    onFailure: function(){ var panel = s$('host_name'); panel.innerHTML = "";}
   });  
  }

  f_hostname();

  f_queues();
  setInterval(f_queues, 5000);
  f_subscriptions();
  setInterval(f_subscriptions, 5000);
  f_faults();
  setInterval(f_faults, 5000);
  f_state();
  setInterval(f_state, 5000);
//  f_dropbox();
//  setInterval(f_dropbox, 50000);
}
// agent queue info
function setAgentQueueInfo(queueInfo, panel)
{
	var newContent = "";

	if (queueInfo.length == 0)
	{
        	newContent = "<p>There is no queue info</P>";
  	}
	else
	{
		for(var i = 0; i != queueInfo.length; ++i)
		{
			var queueName = removePrefix(queueInfo[i].subject, QUEUE_PREFIX);
			newContent = newContent + "<p><a href='./queue.html?queuename="+queueName+"'>"+ queueName+ "</a> : " + parseFloat(queueInfo[i].value) +"</p>";
		}
	}
	panel.innerHTML = newContent;
}

// agent subscription info
function setAgentSubscriptionInfo(subscriptionsInfo, panel)
{
	var newContent = "";

	if (subscriptionsInfo.length == 0)
	{
        	newContent = "<p>There are no subscriptions</P>";
  	}
	else
	{
		for(var i = 0; i != subscriptionsInfo.length; ++i)
		{
			var destinationName = subscriptionsInfo[i].subject;
			var isTopic = isPrefix(destinationName, "topic://");
			var imageLoc;
			if(isTopic)
			{
				destinationName = removePrefix(destinationName, "topic://");
				imageLoc = "images/topic.gif";
			} else {
				destinationName = removePrefix(destinationName, "queue://");
				imageLoc = "images/queue.gif";
			}
			newContent =  newContent + "<p><img src=\"images/clock.gif\" title=\"" + subscriptionsInfo[i].time + "\"/><img src=\"" + imageLoc + "\"/>" + destinationName + " : "  + parseFloat( subscriptionsInfo[i].value ) + "</p>";
		}
	}
	panel.innerHTML = newContent;
}

// agent's fault info
function setAgentFaultInfo(faultInfo, panel)
{
	var newContent = "";

	if (faultInfo.length == 0)
	{
        	newContent = "<p>There is no faults info</P>";
  	}
	else
	{
		for(var i = 0; i != faultInfo.length; ++i)
		{
			newContent = newContent + "<p><a href='./fault.html?faultid="+faultInfo[i].id+"'>"  + faultInfo[i].shortMessage + "</a> : " + faultInfo[i].time +"</p>";
		}
	}
	panel.innerHTML = newContent;
}
// go to agent's subscription page

function subscriptionsPage()
{
	return goToAgentPage("/broker/subscriptions");
}

// go to agent's misc information page

function miscInfoPage()
{
	return goToAgentPage("/broker/miscinfo");
}

function goToAgentPage(page)
{
	var anPanel =  s$('agent_name'); 
	var agentName = anPanel.innerHTML;
	var agentIp = agentName.split(":")[0];

	window.location = "http://"+ agentIp + ":3380" + page; 
	
	return false;
}

//
// ALL QUEUES
//
function allQueuesInformationInit()
{
  var infoPanel = s$('queue_list');
  infoPanel.innerHTML = "Information not available";
  var f_allQueues = function(){
	  new Ajax.Request('/dataquery/last?predicate=queue-size',
	   {
	    method:'get',
	    onSuccess: function(transport){
	      var infoPanel = s$('queue_list');
	var response = transport.responseText;
	      var data = transport.responseJSON;
	      setQueueInfo(data, infoPanel);
	    },
	    onFailure: function(){ alert('Something went wrong...') }
	   });
	}
  f_allQueues();
  setInterval(f_allQueues, 50000);
}


//
// UTILS
//

function getLocalPic(oldValue, newValue)
{
	var tendencyPic = "images/trend_flat.gif";
	if( oldValue !== undefined)
	{
		tendencyPic = (oldValue == newValue)? "images/trend_flat.gif" : (newValue > oldValue)? "images/trend_up_bad.gif" : "images/trend_down_good.gif";
	}
	return tendencyPic;
}

function removePrefix(string, prefix)
{
	if(isPrefix(string, prefix))
	{	
		return string.substring(prefix.length);
	}
	return string;
}

function isPrefix(string, prefix)
{
	return string.match("^"+prefix)==prefix;
}

function round(value)
{
	if(value == 0) return 0;
	return Math.round(value * 10) / 10;
}

function parseISO8601(str) {
 // we assume str is a UTC date ending in 'Z'

 var parts = str.split('T'),
 dateParts = parts[0].split('-'),
 timeParts = parts[1].split('Z'),
 timeSubParts = timeParts[0].split(':'),
 timeSecParts = timeSubParts[2].split('.'),
 timeHours = Number(timeSubParts[0]),
 _date = new Date;

 _date.setUTCFullYear(Number(dateParts[0]));
 _date.setUTCMonth(Number(dateParts[1])-1);
 _date.setUTCDate(Number(dateParts[2]));
 _date.setUTCHours(Number(timeHours));
 _date.setUTCMinutes(Number(timeSubParts[1]));
 _date.setUTCSeconds(Number(timeSecParts[0]));
 if (timeSecParts[1]) _date.setUTCMilliseconds(Number(timeSecParts[1]));

 // by using setUTC methods the date has already been converted to local time(?)
 return _date;
}
function getHumanTextDiff(date)
{
	var dDif = new Date( new Date() - date);
	var str = "  ";
	if( dDif.getMinutes() != 0 )
	{
		str += " " + dDif.getMinutes() +  " minutes";
	}
	
	if( dDif.getSeconds() != 0 )
	{
		str = (str == "") ? str : str + " and "; 
		str += " " + dDif.getSeconds() + " seconds";
	}
	
	return str;
	//return dDif.toTimeString();
}

