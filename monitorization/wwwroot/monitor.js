
//
//  MAIN PAGE
//

function mainMonitorizationInit() 
{
  // queues
  var f_queues = function() {
   new Ajax.Request('/data/queues',
   {
    method:'get',
    onSuccess: function(transport){
      var response = transport.responseText;
      var panel = s$('queuesInformationPanel');
      var data = response.evalJSON();
      setQueueInfo(data, panel);
    },
    onFailure: function(){ alert('Something went wrong...') }
   });  
  }
  // dropbox
  var f_dropboxes = function() {
   new Ajax.Request('/data/dropbox',
   {
    method:'get',
    onSuccess: function(transport){
      var response = transport.responseText;
      var panel = s$('dropboxInformationPanel');
      var data = response.evalJSON();
      setDropboxInfo(data, panel);
    },
    onFailure: function(){ alert('Something went wrong...') }
   });  
  }
  // faults
  var f_faults = function() {
   new Ajax.Request('/data/faults',
   {
    method:'get',
    onSuccess: function(transport){
      var response = transport.responseText;
      var panel = s$('faultsInformationPanel');
      var data = response.evalJSON();
      setFaultInfo(data, panel);
    },
    onFailure: function(){ alert('Something went wrong...') }
   });  
  }

  f_queues();
  setInterval(f_queues, 3000);
  f_dropboxes();
  setInterval(f_dropboxes, 3000);
  f_faults();
  setInterval(f_faults, 3000);
}
// general queue info
function setQueueInfo(queueInfo, panel)
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
			newContent = newContent + "<p><a href='./queue.html?queuename="+queueInfo[i].name+"'>"+ queueInfo[i].name+ "</a> - " + queueInfo[i].count +"</p>";
		}
	}
	panel.innerHTML = newContent;
}

// general dropbox info
function setDropboxInfo(dropboxInfo, panel)
{
	var newContent = "";

	if (dropboxInfo.length == 0)
	{
        	newContent = "<p>There is no dropbox info</P>";
  	}
	else
	{
		for(var i = 0; i != dropboxInfo.length; ++i)
		{
			newContent = newContent + "<p>"+ dropboxInfo[i].agentName+ " : " + dropboxInfo[i].dropboxLocation + " : " + dropboxInfo[i].messages + " : " + dropboxInfo[i].goodMessages +"</p>";
		}
	}
	panel.innerHTML = newContent;
}
// general fault info
function setFaultInfo(faultInfo, panel)
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
			newContent = newContent + "<p>"+ faultInfo[i].agentName+ " : " + faultInfo[i].date +"</p>";
			//newContent = newContent + "<p>"+ faultInfo[i].agentName+ " : " + faultInfo[i].message + " : " + faultInfo[i].date +"</p>";
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
  var qnPanel =  s$('queue_name'); 
  var queueName = params.queuename;
  if (queueName == null)
  {
        qnPanel.innerHTML = "<b>Queue name not specified</b>";
	return;
  }
  qnPanel.innerHTML = queueName;

  var f_agents = function() {
   new Ajax.Request('/data/queues/agents?queuename='+queueName,
   {
    method:'get',
    onSuccess: function(transport){
      var response = transport.responseText;
      var panel = s$('queue_agents');
      var data = response.evalJSON();
      setQueueAgentInfo(data,panel);
    },
    onFailure: function(){ alert('Something went wrong...') }
   });  
  }
  var f_subscriptions = function() {
   new Ajax.Request('/data/queues/subscriptions?queuename='+queueName,
   {
    method:'get',
    onSuccess: function(transport){
      var response = transport.responseText;
      var panel = s$('queue_subscriptions');
      var data = response.evalJSON();
      setQueueSubscriptionsInfo(data, panel);
    },
    onFailure: function(){ alert('Something went wrong...') }
   });  
  }
  f_agents();
  setInterval(f_agents, 3000);
  
  f_subscriptions();
  setInterval(f_subscriptions, 3000);
}
// queue agent info
function setQueueAgentInfo(agentQueueInfo, panel)
{
	var newContent = "";
	if (agentQueueInfo.length == 0)
	{
        	newContent = "<p>There are no agents that contain the specified queue</P>";
  	}
	else
	{
		for(var i = 0; i != agentQueueInfo.length; ++i)
		{
			newContent = newContent + "<p>"+ agentQueueInfo[i].agentName+ " : " + agentQueueInfo[i].count + " : " + agentQueueInfo[i].date +"</p>";
		}
	}
	panel.innerHTML = newContent;
}
// queue subscription info
function setQueueSubscriptionsInfo(subscriptionsQueueInfo, panel)
{
	var newContent = "";

	if (subscriptionsQueueInfo.length == 0)
	{
        	newContent = "<p>There are no subscriptions</P>";
  	}
	else
	{
		for(var i = 0; i != subscriptionsQueueInfo.length; ++i)
		{
			newContent = newContent + "<p>"+ subscriptionsQueueInfo[i].agentName+ " : " + subscriptionsQueueInfo[i].subscriptionType + " : " + subscriptionsQueueInfo[i].count + " : " + subscriptionsQueueInfo[i].date +"</p>";
		}
	}
	panel.innerHTML = newContent;
}

