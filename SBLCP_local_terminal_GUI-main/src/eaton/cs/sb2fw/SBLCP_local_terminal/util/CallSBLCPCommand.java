/******************************************************************************************************
 * 
 * 		Connected Solutions @ Eaton (Moon Township, PA)
 * 	
 * 		@author Jonathan Tan (JonathanTan@eaton.com)
 * 		@date 7/24/2023
 * 
 * ****************************************************************************************************
 * 
 * 		CallSBLCPCommand.java
 * 
 * 		Purpose: This class houses the ONLY function to send non custom messages (commands that don't 
 * 				 have a body), and the ONLY function to send custom messages (commands with variable
 * 				 body, like the set calib param command).
 * 
 * 		For more info, checkout this project's confluence page: https://confluence-prod.tcc.etn.com/pages/viewpage.action?pageId=299547479
 * 
 * *****************************************************************************************************/

package eaton.cs.sb2fw.SBLCP_local_terminal.util;

import eaton.cs.sb2fw.SBLCP_local_terminal.GUI.MainGUIcontroller;

public class CallSBLCPCommand {
	/**
	 * For more info check out: https://confluence-prod.tcc.etn.com/pages/viewpage.action?pageId=299547479#DesktopSoftwareforCommunicatingwithSB2usingSBLCP.-Backendstuff(Ignoreifyouarejustusingthesoftware):
	 */
	public final static String GET_NEXT_EXPECTED_UDP_SEQUENCE_NUMBER	= "1";
	public final static String SET_NEXT_EXPECTED_UDP_SEQUENCE_NUMBER	= "a";
	public final static String GET_REMOTE_HANDLE_POSISTION				= "2";
	public final static String GET_PRIMARY_HANDLE_STATUS				= "e";
	public final static String SET_REMOTE_HANDLE_POSISTION_open			= "5";
	public final static String SET_REMOTE_HANDLE_POSISTION_close		= "b";
	public final static String SET_REMOTE_HANDLE_POSISTION_toggle		= "c";
	public final static String SET_MANUFACTURING_MODE_ON				= "3";
	public final static String SET_MANUFACTURING_MODE_OFF				= "9";
	public final static String GET_DEVICE_STATUS						= "4";
	public final static String GET_METER_TELEMETRT_DATA					= "f";
	public final static String SET_LED_TO_USER_DEFINED_COLOUR			= "6";
	public final static String GET_BREAKER_EOL_CALIB_PARAM             	= "8";
	public final static String SET_BREAKER_EOL_CALIB_PARAM             	= "7";
	public final static String RESET_ENERGY								= "d";
	public final static String CUSTOM_MESSAGE							= "z";
	public final static String MIKE_ENDURANCE_TEST_START				= "k";
	public final static String MIKE_ENDURANCE_TEST_STOP					= "l";
	public final static String REBOOT_ESP32								= ")";
	public final static String GET_TRIP_CODE							= "(";
	public final static String GET_MAC_ADDRESS							= "-";
	public final static String SET_DEVICE_LOCK							= "/";
	/**
	 * 
	 * @param sc The serialCom object used to send the data.
	 * @param commandChar The data to be sent, usually a char, therefore command*Char*.
	 * @param t The thread, if any.
	 * @param MAX_TIME_WAITING_MS Timeout counter (in ms).
	 * @return -1 if no response received. Time waiting for response if received response.
	 */
	public static int call_NonCustom_SBLCPcommand_blocking(SblcpSerialCom sc,
														   String commandChar,
														   Thread t,
														   int MAX_TIME_WAITING_MS)
	{
		final int MAX_INTERVAL_MS = 50;
		System.out.println("Max Timeout: ");
		System.out.println(MAX_INTERVAL_MS);
		int pre = MainGUIcontroller.currentSB2.getResponseCounter();
   		int post = pre;
   		// Call command
   		System.out.println("Inside SBLCP_Blocking");
   		System.out.println(commandChar);
   		sc.sendData(commandChar);
   		
   		if (MAX_TIME_WAITING_MS == 0) return 0;		// If commands like reboot doesnt have a response, dont even bother waiting for the response
   		
   		int interval_counter = 0;
   		
   		while (pre == post) {
   			post = MainGUIcontroller.currentSB2.getResponseCounter();
   			
   			// Wait
			try {
				Thread.sleep(MAX_INTERVAL_MS);
			} catch (InterruptedException e) {
				if (t != null) {
					t.interrupt();
					return -1;
				} else {
					return -1;
				}
			}
			
			interval_counter += MAX_INTERVAL_MS;
			
			if (interval_counter >= MAX_TIME_WAITING_MS) {
				// Max time hit
				return -1;
			}
   		}
   		
   		return interval_counter;
	}

	/**
	 * 
	 * @param sc The serialCom object used to send the data.
	 * @param commandChar The data to be sent, usually a char, therefore command*Char*.
	 * @param t The thread, if any.
	 * @param MAX_TIME_WAITING_MS Timeout counter (in ms).
	 * @return -1 if no response received. Time waiting for response if received response.
	 */
	public static int call_Custom_SBLCPcommand_blocking(SblcpSerialCom sc,
														String commandBody,
														Thread t,
														int MAX_TIME_WAITING_MS)
	{
		final int MAX_INTERVAL_MS = 50;
		
		int pre = MainGUIcontroller.currentSB2.getResponseCounter();
   		int post = pre;
   		
   		// Call custom command
   		sc.sendData("z");
   		// Send command body
   		sc.sendData(commandBody);
   		// Call custom command
   		sc.sendData("z");
   		// Send command body
   		sc.sendData(commandBody);
   		// Why send it twice? For some reason that I still don't understand, if I send once it will fail, but if I send it again it will succeed. Basically, the first time sending it the c code will receive duplicated characters. Like if I send "123", the c code will receive "112333". I have no fking clue why and it drove me nuts (I am over it now, kinda). So if you, someday, whoever is looking at this code, figured it out how to send once and let the c code process it properly (without duplicated characters), please let me know, if I am not working at Eaton anymore, email me at jonathansh1115@gmail.com/jona1115@iastate.edu. Let me die happily please.
   		
   		int interval_counter = 0;
   		
   		while (pre == post) {
   			post = MainGUIcontroller.currentSB2.getResponseCounter();
   			
   			// Wait
			try {
				Thread.sleep(MAX_INTERVAL_MS);
			} catch (InterruptedException e) {
				if (t != null) {
					t.interrupt();
					return -1;
				} else {
					return -1;
				}
			}
			
			interval_counter += MAX_INTERVAL_MS;
			
			if (interval_counter >= MAX_TIME_WAITING_MS) {
				// Max time hit
				return -1;
			}
   		}
   		
   		return interval_counter;
	}
}
