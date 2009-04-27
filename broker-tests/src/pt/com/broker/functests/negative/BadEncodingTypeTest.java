package pt.com.broker.functests.negative;

import pt.com.broker.functests.helpers.GenericNegativeTest;

public class BadEncodingTypeTest extends GenericNegativeTest
{

	public BadEncodingTypeTest()
	{
		super("Bad Encoding Type Test");
		
		setDataToSend(new byte[] { 0, (byte) 0xff, 0, 0, (byte) 0, (byte) 0, (byte) 0, (byte) 2, 0, 0 });
	}
	
	public void addConsequece(){}
}
