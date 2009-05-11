package pt.com.types;

import pt.com.types.NetAction.ActionType;

public final class NetFault
{
	private String actionId;
	private String code;
	private String message;
	private String detail;

	public static final NetMessage InvalidMessageSizeErrorMessage;
	public static final NetMessage UnknownEncodingProtocolMessage; //Not sent
	public static final NetMessage UnknownEncodingVersionMessage; //Not sent
	public static final NetMessage InvalidMessageFormatErrorMessage;
	public static final NetMessage UnexpectedMessageTypeErrorMessage;
	public static final NetMessage InvalidDestinationNameErrorMessage;
	public static final NetMessage InvalidMessageDestinationTypeErrorMessage;
	public static final NetMessage AccessDeniedErrorMessage;
	public static final NetMessage AuthenticationFailedErrorMessage;

	static
	{
		InvalidMessageSizeErrorMessage = buildNetFaultMessage("1101", "Invalid message size");
		UnknownEncodingProtocolMessage = buildNetFaultMessage("1102", "Unknown encoding protocol");
		UnknownEncodingVersionMessage = buildNetFaultMessage("1103", "Unknown encoding version");
		InvalidMessageFormatErrorMessage = buildNetFaultMessage("1201", "Invalid message format");
		UnexpectedMessageTypeErrorMessage = buildNetFaultMessage("1202", "Unexpected message type");
		InvalidDestinationNameErrorMessage = buildNetFaultMessage("2001", "Invalid destination name");
		InvalidMessageDestinationTypeErrorMessage = buildNetFaultMessage("2002", "Invalid destination type");
		AccessDeniedErrorMessage = buildNetFaultMessage("3201", "Access denied");
		AuthenticationFailedErrorMessage = buildNetFaultMessage("3101", "Authentication failed");
	}

	public NetFault(String code, String message)
	{
		this.code = code;
		this.message = message;
	}

	public void setActionId(String actionId)
	{
		this.actionId = actionId;
	}

	public String getActionId()
	{
		return actionId;
	}

	public String getCode()
	{
		return code;
	}

	public String getMessage()
	{
		return message;
	}

	public void setDetail(String detail)
	{
		this.detail = detail;
	}

	public String getDetail()
	{
		return detail;
	}

	public static NetMessage buildNetFaultMessage(String code, String message)
	{
		NetFault fault = new NetFault(code, message);
		NetAction action = new NetAction(ActionType.FAULT);
		action.setFaultMessage(fault);
		NetMessage msg = new NetMessage(action);
		return msg;
	}

	public static NetMessage getMessageFaultWithActionId(NetMessage message, String actionId)
	{
		NetFault fault = message.getAction().getFaultMessage();
		NetFault newFault = new NetFault(fault.getCode(), fault.getMessage());
		newFault.setActionId(actionId);
		newFault.setDetail(fault.getDetail());

		NetAction action = new NetAction(ActionType.FAULT);
		action.setFaultMessage(newFault);

		return new NetMessage(action, message.getHeaders());
	}

	public static NetMessage getMessageFaultWithDetail(NetMessage message, String detail)
	{
		NetFault fault = message.getAction().getFaultMessage();
		NetFault newFault = new NetFault(fault.getCode(), fault.getMessage());
		newFault.setActionId(fault.getActionId());
		newFault.setDetail(detail);

		NetAction action = new NetAction(ActionType.FAULT);
		action.setFaultMessage(newFault);

		return new NetMessage(action, message.getHeaders());
	}
}