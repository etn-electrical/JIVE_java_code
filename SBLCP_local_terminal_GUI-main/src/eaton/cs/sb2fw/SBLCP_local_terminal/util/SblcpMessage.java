/******************************************************************************************************
 * 
 * 		Connected Solutions @ Eaton (Moon Township, PA)
 * 	
 * 		@author Jonathan Tan (JonathanTan@eaton.com)
 * 		@date 5/29/2023
 * 
 * ****************************************************************************************************
 * 
 * 		SblcpMessage.java
 * 
 * 		Purpose	:	To be honest with you, I created this Class when I still have no idea what I 
 * 					am doing, so this Class is just some random stuff that I am now too lazy to 
 * 					move it to somewhere else.
 * 
 * 		For more info, checkout this project's confluence page: https://confluence-prod.tcc.etn.com/pages/viewpage.action?pageId=299547479
 * 
 * *****************************************************************************************************/


package eaton.cs.sb2fw.SBLCP_local_terminal.util;

public class SblcpMessage {
	// Todo: remove cus it should be in MessageListEnum.java as a Enum
//	final int GET_NEXT_EXPECTED_UDP_SEQUENCE_NUMBER			= 0x0000;
//	final int GET_DEVICE_STATUS								= 0x00FF;
//	final int GET_REMOTE_HANDLE_POSISTION					= 0x0100;
//	final int GET_METER_TELEMETRT_DATA						= 0x0200;
//	final int SET_NEXT_EXPECTED_UDP_SEQUENCE_NUMBER			= 0x8000;
//	final int SET_REMOTE_HANDLE_POSISTION					= 0x8100;
//	final int SET_LED_TO_USER_DEFINED_COLOUR				= 0x8300;
//	final int SET_BREAKER_EOL_CALIB_PARAM 					= 0x8400;
//	final int GET_BREAKER_EOL_CALIB_PARAM					= 0x8500;
//	final int SET_MANUFACTURING_MODE_ON						= 0x9100;
//	final int SET_MANUFACTURING_MODE_OFF					= 0x9200;
//	final int GET_TRIP_CODE									= 0x0006;

	// Todo: remove cus it should be in SendCommandEnum.java as a Enum
//	final int SEND_GET_NEXT_EXPECTED_UDP_SEQUENCE_NUMBER	= 0x0000;
//	final int SEND_GET_DEVICE_STATUS						= 0xFF00;
//	final int SEND_GET_REMOTE_HANDLE_POSISTION				= 0x0001;
//	final int SEND_GET_METER_TELEMETRT_DATA					= 0x0002;
//	final int SEND_SET_NEXT_EXPECTED_UDP_SEQUENCE_NUMBER	= 0x0080;
//	final int SEND_SET_REMOTE_HANDLE_POSISTION				= 0x0081;
//	final int SEND_SET_LED_TO_USER_DEFINED_COLOUR			= 0x0083;
//	final int SEND_SET_BREAKER_EOL_CALIB_PARAM				= 0x0084;
//	final int SEND_GET_BREAKER_EOL_CALIB_PARAM				= 0x0085;
//	final int SEND_SET_MANUFACTURING_MODE_ON				= 0x0091;
//	final int SEND_SET_MANUFACTURING_MODE_OFF				= 0x0092;

	final int HEADER_OFFSET		= 10;
	final int STAR_OF_MESSAGE 	= 0x4d4e5445; //0x45544e4d
	final int SERIAL_NO_LENGTH 	= 16;
	
	
	// Local variable (or, how Java people like to call it, state)
	private byte[] raw_message;	// many bits
	private byte[] unhashed_raw_message_byteArr;
	private int unhashed_raw_message_int;
	private int start_byte;		// 32 bit
	private int seq_number;		// 32 bit
	private int msg_code;		// 16 bit
	private int msg_data;		// 16 bit
	private int msg_hash;		// 256 bit
	
//	#define EXTRACT_DATA_1_BYTE(msg, offset)   		(msg[offset])
//	#define EXTRACT_DATA_2_BYTE(msg, offset)    	(msg[offset] << 8 | msg[offset + 1])
//	#define EXTRACT_REV_2_BYTE(msg, offset)    		(msg[offset + 1] << 8 | msg[offset])
//	#define EXTRACT_REV_DATA_4_BYTE(msg, offset)	((msg[offset] << 24) | (msg[offset + 1] << 16) | (msg[offset + 2] << 8) | msg[offset + 3])
//	#define EXTRACT_DATA_4_BYTE(msg, offset)		((msg[offset + 3] << 24) | (msg[offset + 2] << 16) | (msg[offset + 1] << 8) | msg[offset])
	
	// A message has:
	// 32 bit start byte (0x45544E4D aka "ETNM" for client->breaker OR 0x45544E53 aka "ETNS" for breaker->client)
	// 32 bit sequence number
	// 16 bit msg code
	// n bit msg data
	// 8*32 = 256 bit hash
	
	// IDEAS:
	//
	// 1. Since there's two types of msg, ETNM (Coordinator to Node)
	// and ETNS (Node to Coordinator), idea: I would have a state
	// besides "start byte" that is the type of the msg like "c2n"
	// or "n2c" or something like that.
	//
	// 2. There would be a private method that is responsible of hashing
	// and un-hashing the msg.
	//
	// 3. Message									Message Code
	// 	Get Next Expected UDP Sequence Number			0x0000
	// 	Get Device Status								0x00FF
	// 	Get Breaker Remote Handle Position				0x0100
	// 	Get Meter Telemetry Data						0x0200
	// 	Set Next Expected UDP Sequence Number			0x8000
	// 	Set Breaker Remote Handle Position				0x8100
	// 	Set Bargraph LED to User Defined Color			0x8300
	// Maybe have some state to track the type of msg?
	//
	// 
	
	/**
	 * Constructor
	 * @param givenRawMessage
	 */
	public SblcpMessage(byte[] givenRawMessage) {
		raw_message = givenRawMessage;
		
		// So that during development I wont see tons of errors because I pass null as the argument
		if (givenRawMessage == null) return;
		
		unhashed_raw_message_byteArr = unhashMessage(raw_message);
		unhashed_raw_message_int = convertByteArrToInt(unhashed_raw_message_byteArr);
		
		// Extract data
		start_byte = extractData4Byte(unhashed_raw_message_byteArr, 0); // udp_client.c Line 766: start_msg = EXTRACT_DATA_4_BYTE(rx_buffer, 0);
		seq_number = extractData4Byte(unhashed_raw_message_byteArr, 4); // udp_client.c Line 766: EXTRACT_DATA_4_BYTE(rx_buffer, 4)
		msg_code = extractData2Byte(unhashed_raw_message_byteArr, 8); // udp_client.c Line 767: start_msg = message_code = EXTRACT_DATA_2_BYTE(rx_buffer, 8);
//		msg_data = 
//		meg_hash = 
	}
	
	public byte[] unhashMessage(byte[] givenHashedMessage) {
		// TODO
		return new byte[0];
	}
	
	public int convertByteArrToInt(byte[] givenByteArr) {
		// TODO
		return 0;
	}
	
	// UNTESTED
	public static int extractData1Byte(byte[] msg, int offset) {
        return msg[offset] & 0xFF;
    }

	// UNTESTED
    public static int extractData2Byte(byte[] msg, int offset) {
        return ((msg[offset] & 0xFF) << 8) | (msg[offset + 1] & 0xFF);
    }

    // UNTESTED
    public static int extractRev2Byte(byte[] msg, int offset) {
        return ((msg[offset + 1] & 0xFF) << 8) | (msg[offset] & 0xFF);
    }

    // UNTESTED
    public static int extractRevData4Byte(byte[] msg, int offset) {
        return ((msg[offset] & 0xFF) << 24) | ((msg[offset + 1] & 0xFF) << 16) | ((msg[offset + 2] & 0xFF) << 8) | (msg[offset + 3] & 0xFF);
    }

    // UNTESTED
    public static int extractData4Byte(byte[] msg, int offset) {
        return ((msg[offset + 3] & 0xFF) << 24) | ((msg[offset + 2] & 0xFF) << 16) | ((msg[offset + 1] & 0xFF) << 8) | (msg[offset] & 0xFF);
    }
}
