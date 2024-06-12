/******************************************************************************************************
 * 
 * 		Connected Solutions @ Eaton (Moon Township, PA)
 * 	
 * 		@author Jonathan Tan (JonathanTan@eaton.com)
 * 		@date 7/21/2023
 * 
 * ****************************************************************************************************
 * 
 * 		AutoCalibration.java
 * 
 * 		Purpose: Create a thread to perform auto calibration, update status to 
 * 				 mainGUIcontroller label. Auto calibration consists of two steps,
 * 				 step 1 is calibrating voltage and current gain; step 2 is 
 * 				 calibrating power. In the code below you can find step 1 and step
 * 				 2 obviously separated. Tips: Open this code in VScode, use the 
 * 				 minimap for quicker navigation.
 * 
 * 		For more info, checkout this project's confluence page: https://confluence-prod.tcc.etn.com/pages/viewpage.action?pageId=299547479
 * 
 * *****************************************************************************************************/

package eaton.cs.sb2fw.SBLCP_local_terminal.tests;

import eaton.cs.sb2fw.SBLCP_local_terminal.util.*;
import eaton.cs.sb2fw.SBLCP_local_terminal.GUI.*;

public class AutoCalibrationTest {
	int ph0_v_calfac;
	int ph0_i_calfac;
	int ph0_ph_calfac;
	int ph1_v_calfac;
	int ph1_i_calfac;
	int ph1_ph_calfac;
	
	SB2 currentSB2Reference;
	Thread autoCalibrationThread;
	volatile int autoCalibrationStatus = -1;
	
	public AutoCalibrationTest() {
		currentSB2Reference = MainGUIcontroller.currentSB2;
	}
	
	public int getAutoCalibrationStatus() {
		return autoCalibrationStatus;
	}
	
	// Timing analysis
	/*private*/public int op_1_getUDPSequenceTime_ms = -1;
	/*private*/public int op_2_manufaturingModeOn_ms = -1;
	/*private*/public int op_3_resetCalFacToZero_ms = -1;
	/*private*/public int op_3b_wait2secAfterResettingCalFac_ms = -1;
	/*private*/public int op_4_getTelemData_ms = -1;
	/*private*/public int op_5_getYokoReadings_ms = -1;
	/*private*/public int op_6_sendNewCalFac_ms = -1;
	/*private*/public int op_7_getDeviceStatus_ms = -1;
	/*private*/public int op_8_getTelem_ms = -1;
	/*private*/public int op_9_getYokoReadings2ndTime_ms = -1;
	/*private*/public int op_10_setPowerCalFac = -1;
	/*private*/public int op_11_getDeviceStatus_ms = -1;
	/*private*/public int op_12_getTelem_ms = -1;
	/*private*/public int op_13_resetPowerCalFac = -1;
	
	/**
	 * States to keep track of responses
	 */
	private int pre = -1;
	private int post = -1;
	private int blocker_counter = 0;
	private final int MAX_BLOCK_COUNTER = 10000;
	private final int POLL_TIME = 10;
	// make sure it is close enough
	// if not close enough recalculate cal fac and re set
	private final double ACCEPTABLE_ERROR_PERCENT_FOR_STEP_1 = 0.02; // Unit is %
	private final double ACCEPTABLE_ERROR_PERCENT_FOR_STEP_2 = 0.05; // Unit is %
	
	
	public void runAutoCalibration(MainGUIcontroller mainGUIcontroller, YokogawaComm yc, OmicronComm oc, SblcpSerialCom sc) {
		autoCalibrationThread = new Thread(() -> {
			int retryCounter = 0;
			while (!Thread.currentThread().isInterrupted()) {
				int calFac;
				double[] currentYokoReadings = null;
				// Note: Lines marked with "Response Checker" are lines that ensure we wait till we receive a new message before proceeding.
				
				
				//    ______   ________  ________        __    __  ________  __    __  ________        __    __  _______   _______  
				//   /      \ /        |/        |      /  \  /  |/        |/  |  /  |/        |      /  |  /  |/       \ /       \ 
				//  /$$$$$$  |$$$$$$$$/ $$$$$$$$/       $$  \ $$ |$$$$$$$$/ $$ |  $$ |$$$$$$$$/       $$ |  $$ |$$$$$$$  |$$$$$$$  |
				//  $$ | _$$/ $$ |__       $$ |         $$$  \$$ |$$ |__    $$  \/$$/    $$ |         $$ |  $$ |$$ |  $$ |$$ |__$$ |
				//  $$ |/    |$$    |      $$ |         $$$$  $$ |$$    |    $$  $$<     $$ |         $$ |  $$ |$$ |  $$ |$$    $$/ 
				//  $$ |$$$$ |$$$$$/       $$ |         $$ $$ $$ |$$$$$/      $$$$  \    $$ |         $$ |  $$ |$$ |  $$ |$$$$$$$/  
				//  $$ \__$$ |$$ |_____    $$ |         $$ |$$$$ |$$ |_____  $$ /$$  |   $$ |         $$ \__$$ |$$ |__$$ |$$ |      
				//  $$    $$/ $$       |   $$ |         $$ | $$$ |$$       |$$ |  $$ |   $$ |         $$    $$/ $$    $$/ $$ |      
				//   $$$$$$/  $$$$$$$$/    $$/          $$/   $$/ $$$$$$$$/ $$/   $$/    $$/           $$$$$$/  $$$$$$$/  $$/       
				//                                                                                                                  
				// Get next UDP
				op_1_getUDPSequenceTime_ms = CallSBLCPCommand.call_NonCustom_SBLCPcommand_blocking(sc, CallSBLCPCommand.GET_NEXT_EXPECTED_UDP_SEQUENCE_NUMBER, Thread.currentThread(), 3000);
				// Logging
				mainGUIcontroller.modifyAutoCalibProgressIndicatorAndLabel(true, "Getting next UDP...");
				mainGUIcontroller.systemOutPrintlnAndPrintLog("runAutoCalibration(): Getting next UDP...", LogLevel.INFO);
				if (op_1_getUDPSequenceTime_ms < 0) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					}
					// Try again
					op_1_getUDPSequenceTime_ms = CallSBLCPCommand.call_NonCustom_SBLCPcommand_blocking(sc, "1", Thread.currentThread(), 3000);
				}
				if (op_1_getUDPSequenceTime_ms < 0) {
					// Still failed, end thread
					mainGUIcontroller.modifyAutoCalibProgressIndicatorAndLabel(false, "Getting UDP Failed. Connection Error.");
					Thread.currentThread().interrupt();
					break;
				}
					

				if (Thread.currentThread().isInterrupted()) break;	// Interrupt checker
				
				
				//    ______               __            __       __                                       ______            
				//   /      \             /  |          /  \     /  |                                     /      \           
				//  /$$$$$$  |  ______   _$$ |_         $$  \   /$$ |  ______   _______   __    __       /$$$$$$  | _______  
				//  $$ \__$$/  /      \ / $$   |        $$$  \ /$$$ | /      \ /       \ /  |  /  |      $$ |  $$ |/       \ 
				//  $$      \ /$$$$$$  |$$$$$$/         $$$$  /$$$$ | $$$$$$  |$$$$$$$  |$$ |  $$ |      $$ |  $$ |$$$$$$$  |
				//   $$$$$$  |$$    $$ |  $$ | __       $$ $$ $$/$$ | /    $$ |$$ |  $$ |$$ |  $$ |      $$ |  $$ |$$ |  $$ |
				//  /  \__$$ |$$$$$$$$/   $$ |/  |      $$ |$$$/ $$ |/$$$$$$$ |$$ |  $$ |$$ \__$$ |      $$ \__$$ |$$ |  $$ |
				//  $$    $$/ $$       |  $$  $$/       $$ | $/  $$ |$$    $$ |$$ |  $$ |$$    $$/       $$    $$/ $$ |  $$ |
				//   $$$$$$/   $$$$$$$/    $$$$/        $$/      $$/  $$$$$$$/ $$/   $$/  $$$$$$/         $$$$$$/  $$/   $$/ 
				//                                                                                                           
				// Turn on Manufacturing Mode
				op_2_manufaturingModeOn_ms = CallSBLCPCommand.call_NonCustom_SBLCPcommand_blocking(sc, CallSBLCPCommand.SET_MANUFACTURING_MODE_ON, Thread.currentThread(), 3000);
				// Logging
				mainGUIcontroller.modifyAutoCalibProgressIndicatorAndLabel(true, "Turning On Manu Mode...");
				mainGUIcontroller.systemOutPrintlnAndPrintLog("runAutoCalibration(): Turning On Manufacturing Mode...", LogLevel.INFO);
				if (op_2_manufaturingModeOn_ms < 0) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					}
					// Try again
					op_2_manufaturingModeOn_ms = CallSBLCPCommand.call_NonCustom_SBLCPcommand_blocking(sc, CallSBLCPCommand.SET_MANUFACTURING_MODE_ON, Thread.currentThread(), 3000);
				}
				if (op_2_manufaturingModeOn_ms < 0) {
					// Still failed, end thread
					mainGUIcontroller.modifyAutoCalibProgressIndicatorAndLabel(false, "Fail to set manufacturing mode on. Connection Error.");
					Thread.currentThread().interrupt();
					break;
				}
				
				
				if (Thread.currentThread().isInterrupted()) break;	// Interrupt checker


				//    ______   ________  ________  _______           __   
				//   /      \ /        |/        |/       \        _/  |  
				//  /$$$$$$  |$$$$$$$$/ $$$$$$$$/ $$$$$$$  |      / $$ |  
				//  $$ \__$$/    $$ |   $$ |__    $$ |__$$ |      $$$$ |  
				//  $$      \    $$ |   $$    |   $$    $$/         $$ |  
				//   $$$$$$  |   $$ |   $$$$$/    $$$$$$$/          $$ |  
				//  /  \__$$ |   $$ |   $$ |_____ $$ |             _$$ |_ 
				//  $$    $$/    $$ |   $$       |$$ |            / $$   |
				//   $$$$$$/     $$/    $$$$$$$$/ $$/             $$$$$$/ 
				//                                                        
				// Step 1: Calibrate and verify V and I
				while (!Thread.currentThread().isInterrupted()) {
					//   _______                                   __             ______             __        ________                  
					//  /       \                                 /  |           /      \           /  |      /        |                 
					//  $$$$$$$  |  ______    _______   ______   _$$ |_         /$$$$$$  |  ______  $$ |      $$$$$$$$/______    _______ 
					//  $$ |__$$ | /      \  /       | /      \ / $$   |        $$ |  $$/  /      \ $$ |      $$ |__  /      \  /       |
					//  $$    $$< /$$$$$$  |/$$$$$$$/ /$$$$$$  |$$$$$$/         $$ |       $$$$$$  |$$ |      $$    | $$$$$$  |/$$$$$$$/ 
					//  $$$$$$$  |$$    $$ |$$      \ $$    $$ |  $$ | __       $$ |   __  /    $$ |$$ |      $$$$$/  /    $$ |$$ |      
					//  $$ |  $$ |$$$$$$$$/  $$$$$$  |$$$$$$$$/   $$ |/  |      $$ \__/  |/$$$$$$$ |$$ |      $$ |   /$$$$$$$ |$$ \_____ 
					//  $$ |  $$ |$$       |/     $$/ $$       |  $$  $$/       $$    $$/ $$    $$ |$$ |      $$ |   $$    $$ |$$       |
					//  $$/   $$/  $$$$$$$/ $$$$$$$/   $$$$$$$/    $$$$/         $$$$$$/   $$$$$$$/ $$/       $$/     $$$$$$$/  $$$$$$$/ 
					//                                                                                                                   
					// Reset cal factors to 0s
					op_3_resetCalFacToZero_ms = resetCalFactors(mainGUIcontroller, sc);
					// Logging
					mainGUIcontroller.modifyAutoCalibProgressIndicatorAndLabel(true, "Reset cal factors to 0s...");
					if (op_3_resetCalFacToZero_ms < 0) {
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							Thread.currentThread().interrupt();
						}
						// Try again
						op_3_resetCalFacToZero_ms = resetCalFactors(mainGUIcontroller, sc);
					}
					if (op_3_resetCalFacToZero_ms < 0) {
						// Still failed, end thread
						mainGUIcontroller.modifyAutoCalibProgressIndicatorAndLabel(false, "Fail to reset cal factors. Connection Error.");
						Thread.currentThread().interrupt();
						break;
					}
					
					
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					}
					
					
					if (Thread.currentThread().isInterrupted()) break;	// Interrupt checker

					
					try {
						mainGUIcontroller.systemOutPrintlnAndPrintLog("runAutoCalibration(): Waiting for 2.0 seconds.", LogLevel.DEBUG);
						mainGUIcontroller.modifyAutoCalibProgressIndicatorAndLabel(true, "Waiting for steady state (2sec)...");
						Thread.sleep(2000);
						op_3b_wait2secAfterResettingCalFac_ms = 2000;
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
						e.printStackTrace();
					} catch (NullPointerException e) {
						// This happens when outputting to log idk why (and frankly I dont give a shit no more after everything)
						// :)
					}
					
					if (Thread.currentThread().isInterrupted()) break;	// Interrupt checker


					//    ______               __            ________         __                         
					//   /      \             /  |          /        |       /  |                        
					//  /$$$$$$  |  ______   _$$ |_         $$$$$$$$/______  $$ |  ______   _____  ____  
					//  $$ | _$$/  /      \ / $$   |           $$ | /      \ $$ | /      \ /     \/    \ 
					//  $$ |/    |/$$$$$$  |$$$$$$/            $$ |/$$$$$$  |$$ |/$$$$$$  |$$$$$$ $$$$  |
					//  $$ |$$$$ |$$    $$ |  $$ | __          $$ |$$    $$ |$$ |$$    $$ |$$ | $$ | $$ |
					//  $$ \__$$ |$$$$$$$$/   $$ |/  |         $$ |$$$$$$$$/ $$ |$$$$$$$$/ $$ | $$ | $$ |
					//  $$    $$/ $$       |  $$  $$/          $$ |$$       |$$ |$$       |$$ | $$ | $$ |
					//   $$$$$$/   $$$$$$$/    $$$$/           $$/  $$$$$$$/ $$/  $$$$$$$/ $$/  $$/  $$/ 
					//                                                                                   
					// Get telem data
					op_4_getTelemData_ms = CallSBLCPCommand.call_NonCustom_SBLCPcommand_blocking(sc, CallSBLCPCommand.GET_METER_TELEMETRT_DATA, Thread.currentThread(), 3000);
					// Logging
					mainGUIcontroller.modifyAutoCalibProgressIndicatorAndLabel(true, "Getting averaged data...");
					mainGUIcontroller.systemOutPrintlnAndPrintLog("runAutoCalibration(): Getting averaged data...", LogLevel.INFO);
					if (op_4_getTelemData_ms < 0) {
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							Thread.currentThread().interrupt();
						}
						// Try again
						op_4_getTelemData_ms = CallSBLCPCommand.call_NonCustom_SBLCPcommand_blocking(sc, CallSBLCPCommand.GET_METER_TELEMETRT_DATA, Thread.currentThread(), 3000);
					}
					if (op_4_getTelemData_ms < 0) {
						// Still failed, end thread
						mainGUIcontroller.modifyAutoCalibProgressIndicatorAndLabel(false, "Fail to get average data. Connection Error.");
						Thread.currentThread().interrupt();
						break;
					}
					
					
					if (Thread.currentThread().isInterrupted()) break;	// Interrupt checker
					
					
					currentYokoReadings = yc.getPh0Ph1AllValAtOnce();
					// Wait
					try {
						mainGUIcontroller.systemOutPrintlnAndPrintLog("runAutoCalibration(): Waiting for 1.0 second.", LogLevel.DEBUG);
						Thread.sleep(1000);
						op_5_getYokoReadings_ms = 1000;
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
						e.printStackTrace();
					} catch (NullPointerException e) {
						// This happens when outputting to log idk why (and frankly I dont give a shit no more after everything)
						// :)
					}
					

					if (Thread.currentThread().isInterrupted()) break;	// Interrupt checker

					
					// The below four chunks (comparing yoko and sb) is very much hard coded
					// because I am too tired to put it in a loop sorry
					// Set indicator
					mainGUIcontroller.modifyAutoCalibProgressIndicatorAndLabel(true, "Comparing breaker and yoko...");
					// phase A voltage
					double currentBreakerPh0VoltageReading = Double.valueOf(currentSB2Reference.getIndividualState("phase_A_rms_voltage").getValue());
					double currentYokoPh0VoltageReading = currentYokoReadings[0];
					calFac = calculateGainCalFac(currentBreakerPh0VoltageReading, currentYokoPh0VoltageReading);
					// Set ph0 Voltage cal fac
					try {
						currentSB2Reference.setEOLParamState("Ade9000_AvGainField", calFac);
					} catch (NoSuchFieldException e) {
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					}
					// Debug
					mainGUIcontroller.systemOutPrintlnAndPrintLog("runAutoCalibration(): Setting ph0 voltage.", LogLevel.DEBUG);
					
					// phase A current
					double currentBreakerPh0CurrentReading = Double.valueOf(currentSB2Reference.getIndividualState("phase_A_rms_current").getValue());
					double currentYokoPh0CurrentReading = currentYokoReadings[1];
					calFac = calculateGainCalFac(currentBreakerPh0CurrentReading, currentYokoPh0CurrentReading);
					// Set ph0 Voltage cal fac
					try {
						currentSB2Reference.setEOLParamState("Ade9000_AiGainField", calFac);
					} catch (NoSuchFieldException e) {
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					}
					// Debug
					mainGUIcontroller.systemOutPrintlnAndPrintLog("runAutoCalibration(): Setting ph0 current.", LogLevel.DEBUG);
					
					// phase B voltage
					double currentBreakerPh1VoltageReading = Double.valueOf(currentSB2Reference.getIndividualState("phase_B_rms_voltage").getValue());
					double currentYokoPh1VoltageReading = currentYokoReadings[13];
					calFac = calculateGainCalFac(currentBreakerPh1VoltageReading, currentYokoPh1VoltageReading);
					// Set ph0 Voltage cal fac
					try {
						currentSB2Reference.setEOLParamState("Ade9000_BvGainField", calFac);
					} catch (NoSuchFieldException e) {
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					}
					// Debug
					mainGUIcontroller.systemOutPrintlnAndPrintLog("runAutoCalibration(): Setting ph1 voltage.", LogLevel.DEBUG);
					
					// phase B current
					double currentBreakerPh1CurrentReading = Double.valueOf(currentSB2Reference.getIndividualState("phase_B_rms_current").getValue());
					double currentYokoPh1CurrentReading = currentYokoReadings[14];
					calFac = calculateGainCalFac(currentBreakerPh1CurrentReading, -1.0 * currentYokoPh1CurrentReading);
					// Set ph0 Voltage cal fac
					try {
						currentSB2Reference.setEOLParamState("Ade9000_BiGainField", calFac);
					} catch (NoSuchFieldException e) {
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					}
					// Debug
					mainGUIcontroller.systemOutPrintlnAndPrintLog("runAutoCalibration(): Setting ph1 current.", LogLevel.DEBUG);
					
					
					// ############### Done comparing ###############
					

					if (Thread.currentThread().isInterrupted()) break;	// Interrupt checker


					//    ______               __             ______             __        ________                  
					//   /      \             /  |           /      \           /  |      /        |                 
					//  /$$$$$$  |  ______   _$$ |_         /$$$$$$  |  ______  $$ |      $$$$$$$$/______    _______ 
					//  $$ \__$$/  /      \ / $$   |        $$ |  $$/  /      \ $$ |      $$ |__  /      \  /       |
					//  $$      \ /$$$$$$  |$$$$$$/         $$ |       $$$$$$  |$$ |      $$    | $$$$$$  |/$$$$$$$/ 
					//   $$$$$$  |$$    $$ |  $$ | __       $$ |   __  /    $$ |$$ |      $$$$$/  /    $$ |$$ |      
					//  /  \__$$ |$$$$$$$$/   $$ |/  |      $$ \__/  |/$$$$$$$ |$$ |      $$ |   /$$$$$$$ |$$ \_____ 
					//  $$    $$/ $$       |  $$  $$/       $$    $$/ $$    $$ |$$ |      $$ |   $$    $$ |$$       |
					//   $$$$$$/   $$$$$$$/    $$$$/         $$$$$$/   $$$$$$$/ $$/       $$/     $$$$$$$/  $$$$$$$/ 
					//                                                                                               
					/*Response Checker*/	pre = MainGUIcontroller.currentSB2.getResponseCounter();
					/*Response Checker*/	post = pre;
					// send cal fac (set to object, create string, call z function)
					String calibMessageToSend = currentSB2Reference.createSEND_SET_BREAKER_EOL_CALIB_PARAMcustomMessage();
					op_6_sendNewCalFac_ms = CallSBLCPCommand.call_Custom_SBLCPcommand_blocking(sc, calibMessageToSend, Thread.currentThread(), 4000);
					// Logging
					mainGUIcontroller.modifyAutoCalibProgressIndicatorAndLabel(true, "Setting Cal Fac...");
					mainGUIcontroller.systemOutPrintlnAndPrintLog("runAutoCalibration(): Setting Cal Fac...", LogLevel.INFO);
					if (op_6_sendNewCalFac_ms < 0) {
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							Thread.currentThread().interrupt();
						}
						// Try again
						op_6_sendNewCalFac_ms = CallSBLCPCommand.call_Custom_SBLCPcommand_blocking(sc, calibMessageToSend, Thread.currentThread(), 4000);
					}
					if (op_6_sendNewCalFac_ms < 0) {
						// Still failed, end thread
						mainGUIcontroller.modifyAutoCalibProgressIndicatorAndLabel(false, "Fail to Set Cal Factor. Connection Error.");
						Thread.currentThread().interrupt();
						break;
					}
					
					
					if (Thread.currentThread().isInterrupted()) break;	// Interrupt checker
					
					
					// Set indicator
					mainGUIcontroller.modifyAutoCalibProgressIndicatorAndLabel(true, "Checking if calib is good...");
					
					
					//    ______               __            _______                               ______    __                  __     
					//   /      \             /  |          /       \                             /      \  /  |                /  |    
					//  /$$$$$$  |  ______   _$$ |_         $$$$$$$  |  ______   __     __       /$$$$$$  |_$$ |_     ______   _$$ |_   
					//  $$ | _$$/  /      \ / $$   |        $$ |  $$ | /      \ /  \   /  |      $$ \__$$// $$   |   /      \ / $$   |  
					//  $$ |/    |/$$$$$$  |$$$$$$/         $$ |  $$ |/$$$$$$  |$$  \ /$$/       $$      \$$$$$$/    $$$$$$  |$$$$$$/   
					//  $$ |$$$$ |$$    $$ |  $$ | __       $$ |  $$ |$$    $$ | $$  /$$/         $$$$$$  | $$ | __  /    $$ |  $$ | __ 
					//  $$ \__$$ |$$$$$$$$/   $$ |/  |      $$ |__$$ |$$$$$$$$/   $$ $$/         /  \__$$ | $$ |/  |/$$$$$$$ |  $$ |/  |
					//  $$    $$/ $$       |  $$  $$/       $$    $$/ $$       |   $$$/          $$    $$/  $$  $$/ $$    $$ |  $$  $$/ 
					//   $$$$$$/   $$$$$$$/    $$$$/        $$$$$$$/   $$$$$$$/     $/            $$$$$$/    $$$$/   $$$$$$$/    $$$$/  
					//                                                                                                                  
					// Get Device Status
					op_7_getDeviceStatus_ms = CallSBLCPCommand.call_NonCustom_SBLCPcommand_blocking(sc, CallSBLCPCommand.GET_DEVICE_STATUS, Thread.currentThread(), 3000);
					// Logging
					mainGUIcontroller.modifyAutoCalibProgressIndicatorAndLabel(true, "Getting device status...");
					mainGUIcontroller.systemOutPrintlnAndPrintLog("runAutoCalibration(): Getting device status...", LogLevel.INFO);
					if (op_7_getDeviceStatus_ms < 0) {
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							Thread.currentThread().interrupt();
						}
						// Try again
						op_7_getDeviceStatus_ms = CallSBLCPCommand.call_NonCustom_SBLCPcommand_blocking(sc, CallSBLCPCommand.GET_DEVICE_STATUS, Thread.currentThread(), 3000);
					}
					if (op_7_getDeviceStatus_ms < 0) {
						// Still failed, end thread
						mainGUIcontroller.modifyAutoCalibProgressIndicatorAndLabel(false, "Fail to get device status. Connection Error.");
						Thread.currentThread().interrupt();
						break;
					}
					
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					}
					

					if (Thread.currentThread().isInterrupted()) break;	// Interrupt checker


					//    ______               __            ________         __                         
					//   /      \             /  |          /        |       /  |                        
					//  /$$$$$$  |  ______   _$$ |_         $$$$$$$$/______  $$ |  ______   _____  ____  
					//  $$ | _$$/  /      \ / $$   |           $$ | /      \ $$ | /      \ /     \/    \ 
					//  $$ |/    |/$$$$$$  |$$$$$$/            $$ |/$$$$$$  |$$ |/$$$$$$  |$$$$$$ $$$$  |
					//  $$ |$$$$ |$$    $$ |  $$ | __          $$ |$$    $$ |$$ |$$    $$ |$$ | $$ | $$ |
					//  $$ \__$$ |$$$$$$$$/   $$ |/  |         $$ |$$$$$$$$/ $$ |$$$$$$$$/ $$ | $$ | $$ |
					//  $$    $$/ $$       |  $$  $$/          $$ |$$       |$$ |$$       |$$ | $$ | $$ |
					//   $$$$$$/   $$$$$$$/    $$$$/           $$/  $$$$$$$/ $$/  $$$$$$$/ $$/  $$/  $$/ 
					//                                                                                   
					// Get telem
					op_8_getTelem_ms = CallSBLCPCommand.call_NonCustom_SBLCPcommand_blocking(sc, CallSBLCPCommand.GET_METER_TELEMETRT_DATA, Thread.currentThread(), 3000);
					// Logging
					mainGUIcontroller.modifyAutoCalibProgressIndicatorAndLabel(true, "Getting averaged data...");
					mainGUIcontroller.systemOutPrintlnAndPrintLog("runAutoCalibration(): Getting averaged data...", LogLevel.INFO);
					if (op_8_getTelem_ms < 0) {
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							Thread.currentThread().interrupt();
						}
						// Try again
						op_8_getTelem_ms = CallSBLCPCommand.call_NonCustom_SBLCPcommand_blocking(sc, CallSBLCPCommand.GET_METER_TELEMETRT_DATA, Thread.currentThread(), 3000);
					}
					if (op_8_getTelem_ms < 0) {
						// Still failed, end thread
						mainGUIcontroller.modifyAutoCalibProgressIndicatorAndLabel(false, "Fail to get averaged data. Connection Error.");
						Thread.currentThread().interrupt();
						break;
					}

					
					//   __     __                     __   ______           
					//  /  |   /  |                   /  | /      \          
					//  $$ |   $$ | ______    ______  $$/ /$$$$$$  |__    __ 
					//  $$ |   $$ |/      \  /      \ /  |$$ |_ $$//  |  /  |
					//  $$  \ /$$//$$$$$$  |/$$$$$$  |$$ |$$   |   $$ |  $$ |
					//   $$  /$$/ $$    $$ |$$ |  $$/ $$ |$$$$/    $$ |  $$ |
					//    $$ $$/  $$$$$$$$/ $$ |      $$ |$$ |     $$ \__$$ |
					//     $$$/   $$       |$$ |      $$ |$$ |     $$    $$ |
					//      $/     $$$$$$$/ $$/       $$/ $$/       $$$$$$$ |
					//                                             /  \__$$ |
					//                                             $$    $$/ 
					//                                              $$$$$$/  
					boolean phaseAVoltageAcceptable = false, phaseACurrentAcceptable = false, phaseBVoltageAcceptable = false, phaseBCurrentAcceptable = false;
					try {
						phaseAVoltageAcceptable = acceptableOrNot(Double.valueOf(currentSB2Reference.getIndividualState("phase_A_rms_voltage").getValue()), currentYokoPh0VoltageReading, ACCEPTABLE_ERROR_PERCENT_FOR_STEP_1);
						phaseACurrentAcceptable = acceptableOrNot(Double.valueOf(currentSB2Reference.getIndividualState("phase_A_rms_current").getValue()), currentYokoPh0CurrentReading, ACCEPTABLE_ERROR_PERCENT_FOR_STEP_1);
						phaseBVoltageAcceptable = acceptableOrNot(Double.valueOf(currentSB2Reference.getIndividualState("phase_B_rms_voltage").getValue()), currentYokoPh1VoltageReading, ACCEPTABLE_ERROR_PERCENT_FOR_STEP_1);
						phaseBCurrentAcceptable = acceptableOrNot(Double.valueOf(currentSB2Reference.getIndividualState("phase_B_rms_current").getValue()), currentYokoPh1CurrentReading, ACCEPTABLE_ERROR_PERCENT_FOR_STEP_1);
					} catch (NumberFormatException e) {
						e.printStackTrace();
					}
					
					if (Thread.currentThread().isInterrupted()) break;	// Interrupt checker
					
					if (!(phaseAVoltageAcceptable && phaseACurrentAcceptable && phaseBVoltageAcceptable && phaseBCurrentAcceptable)) {
						// bu he ge
						// Set indicator
						String whichOneFailed = "";
						if (!phaseAVoltageAcceptable) whichOneFailed += "phAVol";
						if (!phaseACurrentAcceptable) whichOneFailed += "phACur";
						if (!phaseBVoltageAcceptable) whichOneFailed += "phBVol";
						if (!phaseBCurrentAcceptable) whichOneFailed += "phBCur";	// It will be a shit show if all fails
						
						mainGUIcontroller.modifyAutoCalibProgressIndicatorAndLabel(true, "Calibration Failed. Retrying..." + whichOneFailed);
						try {
							Thread.sleep(500);
						} catch (InterruptedException e) {
							Thread.currentThread().interrupt();
							e.printStackTrace();
						}
						

						if (Thread.currentThread().isInterrupted()) break;	// Interrupt checker
						
						
						++retryCounter;

						if (retryCounter > 1) {
							// Set indicator
							mainGUIcontroller.modifyAutoCalibProgressIndicatorAndLabel(false, "Step 1 Calibration Failed: " + whichOneFailed);
							try {
								Thread.sleep(500);
							} catch (InterruptedException e) {
								Thread.currentThread().interrupt();
								e.printStackTrace();
							}
							retryCounter = 0;
							Thread.currentThread().interrupt();
							break;
						}
						
						continue;
					} else {
						// Calibration success!
						
						// Set indicator
						mainGUIcontroller.modifyAutoCalibProgressIndicatorAndLabel(false, "Auto Calibration Step 1 Done");
						try {
							Thread.sleep(500);
						} catch (InterruptedException e) {
							Thread.currentThread().interrupt();
							e.printStackTrace();
						}
						retryCounter = 0;
						break;
					}
				}	// End of step 1 loop


				if (Thread.currentThread().isInterrupted()) break;	// Interrupt checker


				//    ______   ________  ________  _______          ______  
				//   /      \ /        |/        |/       \        /      \ 
				//  /$$$$$$  |$$$$$$$$/ $$$$$$$$/ $$$$$$$  |      /$$$$$$  |
				//  $$ \__$$/    $$ |   $$ |__    $$ |__$$ |      $$____$$ |
				//  $$      \    $$ |   $$    |   $$    $$/        /    $$/ 
				//   $$$$$$  |   $$ |   $$$$$/    $$$$$$$/        /$$$$$$/  
				//  /  \__$$ |   $$ |   $$ |_____ $$ |            $$ |_____ 
				//  $$    $$/    $$ |   $$       |$$ |            $$       |
				//   $$$$$$/     $$/    $$$$$$$$/ $$/             $$$$$$$$/ 
				//                                                          
				
				// // Set Omicron to 120V/10A/60deg
				// oc.modifyOmicronValues(120, 0, 10, -60, 120, 180, 10, 120);
				// // Logging
				// mainGUIcontroller.modifyAutoCalibProgressIndicatorAndLabel(true, "Setting Omicron to 120V/10A/60deg...");
				// mainGUIcontroller.systemOutPrintlnAndPrintLog("runAutoCalibration(): Setting Omicron to 120V/10A/60deg...", LogLevel.INFO);
				// try {
				// 	Thread.sleep(500);
				// } catch (InterruptedException e) {
				// 	Thread.currentThread().interrupt();
				// 	e.printStackTrace();
				// }
				// currentYokoReadings = yc.getPh0Ph1AllValAtOnce();
				// // Wait
				// try {
				// 	mainGUIcontroller.systemOutPrintlnAndPrintLog("runAutoCalibration(): Waiting for 1.0 second.", LogLevel.DEBUG);
				// 	Thread.sleep(1000);
				// 	op_9_getYokoReadings2ndTime_ms = 1000;
				// } catch (InterruptedException e) {
				// 	Thread.currentThread().interrupt();
				// 	e.printStackTrace();
				// } catch (NullPointerException e) {
				// 	// This happens when outputting to log idk why (and frankly I dont give a shit no more after everything)
				// 	// :)
				// }

				// Step 2: Calibrate power at 60deg
				while (!Thread.currentThread().isInterrupted()) {
					// phase A real power
					double currentBreakerPhARealReading = Double.valueOf(currentSB2Reference.getIndividualState("phase_A_active_power").getValue());
					double currentYokoPhARealReading = currentYokoReadings[2];
					calFac = calculatePhaseCalFac(currentBreakerPhARealReading, currentYokoPhARealReading);
					// Set ph0 Voltage cal fac
					try {
						currentSB2Reference.setEOLParamState("Ade9000_APhCal0Field", calFac);
					} catch (NoSuchFieldException e) {
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					}

					// phase B real power
					double currentBreakerPhBRealReading = Double.valueOf(currentSB2Reference.getIndividualState("phase_B_active_power").getValue());
					double currentYokoPhBRealReading = currentYokoReadings[15];
					calFac = calculatePhaseCalFac(currentBreakerPhBRealReading, currentYokoPhBRealReading);
					// Set ph0 Voltage cal fac
					try {
						currentSB2Reference.setEOLParamState("Ade9000_BPhCal0Field", calFac);
					} catch (NoSuchFieldException e) {
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					}


					if (Thread.currentThread().isInterrupted()) break;	// Interrupt checker
					

                    //    ______               __            _______                                                     ______             __        ________                  
					//   /      \             /  |          /       \                                                   /      \           /  |      /        |                 
					//  /$$$$$$  |  ______   _$$ |_         $$$$$$$  | ______   __   __   __   ______    ______        /$$$$$$  |  ______  $$ |      $$$$$$$$/______    _______ 
					//  $$ \__$$/  /      \ / $$   |        $$ |__$$ |/      \ /  | /  | /  | /      \  /      \       $$ |  $$/  /      \ $$ |      $$ |__  /      \  /       |
					//  $$      \ /$$$$$$  |$$$$$$/         $$    $$//$$$$$$  |$$ | $$ | $$ |/$$$$$$  |/$$$$$$  |      $$ |       $$$$$$  |$$ |      $$    | $$$$$$  |/$$$$$$$/ 
					//   $$$$$$  |$$    $$ |  $$ | __       $$$$$$$/ $$ |  $$ |$$ | $$ | $$ |$$    $$ |$$ |  $$/       $$ |   __  /    $$ |$$ |      $$$$$/  /    $$ |$$ |      
					//  /  \__$$ |$$$$$$$$/   $$ |/  |      $$ |     $$ \__$$ |$$ \_$$ \_$$ |$$$$$$$$/ $$ |            $$ \__/  |/$$$$$$$ |$$ |      $$ |   /$$$$$$$ |$$ \_____ 
					//  $$    $$/ $$       |  $$  $$/       $$ |     $$    $$/ $$   $$   $$/ $$       |$$ |            $$    $$/ $$    $$ |$$ |      $$ |   $$    $$ |$$       |
					//   $$$$$$/   $$$$$$$/    $$$$/        $$/       $$$$$$/   $$$$$/$$$$/   $$$$$$$/ $$/              $$$$$$/   $$$$$$$/ $$/       $$/     $$$$$$$/  $$$$$$$/ 
					//                                                                            
					// send cal fac (set to object, create string, call z function)
					String calibMessageToSend = currentSB2Reference.createSEND_SET_BREAKER_EOL_CALIB_PARAMcustomMessage();
					op_10_setPowerCalFac = CallSBLCPCommand.call_Custom_SBLCPcommand_blocking(sc, calibMessageToSend, Thread.currentThread(), 4000);
					// Logging
					mainGUIcontroller.modifyAutoCalibProgressIndicatorAndLabel(true, "Setting power cal fac...");
					mainGUIcontroller.systemOutPrintlnAndPrintLog("runAutoCalibration(): Setting power cal fac...", LogLevel.INFO);
					if (op_10_setPowerCalFac < 0) {
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							Thread.currentThread().interrupt();
						}
						// Try again
						op_10_setPowerCalFac = CallSBLCPCommand.call_Custom_SBLCPcommand_blocking(sc, calibMessageToSend, Thread.currentThread(), 4000);
					}
					if (op_10_setPowerCalFac < 0) {
						// Still failed, end thread
						mainGUIcontroller.modifyAutoCalibProgressIndicatorAndLabel(false, "Fail to set power cal fac. Connection Error.");
						Thread.currentThread().interrupt();
						break;
					}


					if (Thread.currentThread().isInterrupted()) break;	// Interrupt checker
					

					// Set indicator
					mainGUIcontroller.modifyAutoCalibProgressIndicatorAndLabel(true, "Checking if calib is good...");
					try {
						Thread.sleep(3000);
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
						e.printStackTrace();
					}
					
					
					//    ______               __            _______                               ______    __                  __     
					//   /      \             /  |          /       \                             /      \  /  |                /  |    
					//  /$$$$$$  |  ______   _$$ |_         $$$$$$$  |  ______   __     __       /$$$$$$  |_$$ |_     ______   _$$ |_   
					//  $$ | _$$/  /      \ / $$   |        $$ |  $$ | /      \ /  \   /  |      $$ \__$$// $$   |   /      \ / $$   |  
					//  $$ |/    |/$$$$$$  |$$$$$$/         $$ |  $$ |/$$$$$$  |$$  \ /$$/       $$      \$$$$$$/    $$$$$$  |$$$$$$/   
					//  $$ |$$$$ |$$    $$ |  $$ | __       $$ |  $$ |$$    $$ | $$  /$$/         $$$$$$  | $$ | __  /    $$ |  $$ | __ 
					//  $$ \__$$ |$$$$$$$$/   $$ |/  |      $$ |__$$ |$$$$$$$$/   $$ $$/         /  \__$$ | $$ |/  |/$$$$$$$ |  $$ |/  |
					//  $$    $$/ $$       |  $$  $$/       $$    $$/ $$       |   $$$/          $$    $$/  $$  $$/ $$    $$ |  $$  $$/ 
					//   $$$$$$/   $$$$$$$/    $$$$/        $$$$$$$/   $$$$$$$/     $/            $$$$$$/    $$$$/   $$$$$$$/    $$$$/  
					//                                                                                                                  
					/*Response Checker*/	pre = MainGUIcontroller.currentSB2.getResponseCounter();
					/*Response Checker*/	post = pre;
					// Get Device Status
					op_11_getDeviceStatus_ms = CallSBLCPCommand.call_NonCustom_SBLCPcommand_blocking(sc, CallSBLCPCommand.GET_DEVICE_STATUS, Thread.currentThread(), 3000);
					mainGUIcontroller.handleSendGetDeviceStatusButton(null);
					// Logging
					mainGUIcontroller.modifyAutoCalibProgressIndicatorAndLabel(true, "GET_DEVICE_STATUSing...");
					mainGUIcontroller.systemOutPrintlnAndPrintLog("runAutoCalibration(): GET_DEVICE_STATUSing...", LogLevel.INFO);
					if (op_11_getDeviceStatus_ms < 0) {
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							Thread.currentThread().interrupt();
						}
						// Try again
						op_11_getDeviceStatus_ms = CallSBLCPCommand.call_Custom_SBLCPcommand_blocking(sc, calibMessageToSend, Thread.currentThread(), 4000);
					}
					if (op_11_getDeviceStatus_ms < 0) {
						// Still failed, end thread
						mainGUIcontroller.modifyAutoCalibProgressIndicatorAndLabel(false, "Fail to get device status. Connection Error.");
						Thread.currentThread().interrupt();
						break;
					}
					

					if (Thread.currentThread().isInterrupted()) break;	// Interrupt checker
					
					
					try {
						Thread.sleep(3000);
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					}


					//    ______               __            ________         __                         
					//   /      \             /  |          /        |       /  |                        
					//  /$$$$$$  |  ______   _$$ |_         $$$$$$$$/______  $$ |  ______   _____  ____  
					//  $$ | _$$/  /      \ / $$   |           $$ | /      \ $$ | /      \ /     \/    \ 
					//  $$ |/    |/$$$$$$  |$$$$$$/            $$ |/$$$$$$  |$$ |/$$$$$$  |$$$$$$ $$$$  |
					//  $$ |$$$$ |$$    $$ |  $$ | __          $$ |$$    $$ |$$ |$$    $$ |$$ | $$ | $$ |
					//  $$ \__$$ |$$$$$$$$/   $$ |/  |         $$ |$$$$$$$$/ $$ |$$$$$$$$/ $$ | $$ | $$ |
					//  $$    $$/ $$       |  $$  $$/          $$ |$$       |$$ |$$       |$$ | $$ | $$ |
					//   $$$$$$/   $$$$$$$/    $$$$/           $$/  $$$$$$$/ $$/  $$$$$$$/ $$/  $$/  $$/ 
					//                                                                                   
					// Get telem
					op_12_getTelem_ms = CallSBLCPCommand.call_NonCustom_SBLCPcommand_blocking(sc, CallSBLCPCommand.GET_METER_TELEMETRT_DATA, Thread.currentThread(), 3000);
					// Logging
					mainGUIcontroller.modifyAutoCalibProgressIndicatorAndLabel(true, "Getting averaged data...");
					mainGUIcontroller.systemOutPrintlnAndPrintLog("runAutoCalibration(): Getting averaged data...", LogLevel.INFO);
					if (op_12_getTelem_ms < 0) {
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							Thread.currentThread().interrupt();
						}
						// Try again
						op_12_getTelem_ms = CallSBLCPCommand.call_NonCustom_SBLCPcommand_blocking(sc, CallSBLCPCommand.GET_METER_TELEMETRT_DATA, Thread.currentThread(), 3000);
					}
					if (op_12_getTelem_ms < 0) {
						// Still failed, end thread
						mainGUIcontroller.modifyAutoCalibProgressIndicatorAndLabel(false, "Fail to get averaged data. Connection Error.");
						Thread.currentThread().interrupt();
						break;
					}


					if (Thread.currentThread().isInterrupted()) break;	// Interrupt checker


					//   __     __                     __   ______           
					//  /  |   /  |                   /  | /      \          
					//  $$ |   $$ | ______    ______  $$/ /$$$$$$  |__    __ 
					//  $$ |   $$ |/      \  /      \ /  |$$ |_ $$//  |  /  |
					//  $$  \ /$$//$$$$$$  |/$$$$$$  |$$ |$$   |   $$ |  $$ |
					//   $$  /$$/ $$    $$ |$$ |  $$/ $$ |$$$$/    $$ |  $$ |
					//    $$ $$/  $$$$$$$$/ $$ |      $$ |$$ |     $$ \__$$ |
					//     $$$/   $$       |$$ |      $$ |$$ |     $$    $$ |
					//      $/     $$$$$$$/ $$/       $$/ $$/       $$$$$$$ |
					//                                             /  \__$$ |
					//                                             $$    $$/ 
					//                                              $$$$$$/  
					boolean phaseARealPowerAcceptable = false, phaseAReactivePowerAcceptable = false, phaseBRealPowerAcceptable = false, phaseBReactivePowerAcceptable = false;
					try {
						phaseARealPowerAcceptable = acceptableOrNot(Double.valueOf(currentSB2Reference.getIndividualState("phase_A_active_power").getValue()), currentYokoPhARealReading, ACCEPTABLE_ERROR_PERCENT_FOR_STEP_2);
						phaseAReactivePowerAcceptable = acceptableOrNot(Double.valueOf(currentSB2Reference.getIndividualState("phase_A_reactive_power").getValue()), currentYokoReadings[4], ACCEPTABLE_ERROR_PERCENT_FOR_STEP_2);
						phaseBRealPowerAcceptable = acceptableOrNot(Double.valueOf(currentSB2Reference.getIndividualState("phase_B_active_power").getValue()), currentYokoPhBRealReading, ACCEPTABLE_ERROR_PERCENT_FOR_STEP_2);
						phaseBReactivePowerAcceptable = acceptableOrNot(Double.valueOf(currentSB2Reference.getIndividualState("phase_B_reactive_power").getValue()), currentYokoReadings[17], ACCEPTABLE_ERROR_PERCENT_FOR_STEP_2);
					} catch (NumberFormatException e) {
						e.printStackTrace();
					}
					
					if (Thread.currentThread().isInterrupted()) break;	// Interrupt checker
					
					if (!(phaseARealPowerAcceptable && phaseAReactivePowerAcceptable && phaseBRealPowerAcceptable && phaseBReactivePowerAcceptable)) {
						// bu he ge
						// Set indicator
						String whichOneFailed = "";
						if (!phaseARealPowerAcceptable) whichOneFailed += "phAReal";
						if (!phaseAReactivePowerAcceptable) whichOneFailed += "phAReactive";
						if (!phaseBRealPowerAcceptable) whichOneFailed += "phBReal";
						if (!phaseBReactivePowerAcceptable) whichOneFailed += "phBReactive";	// It will be a shit show if all fails
						
						mainGUIcontroller.modifyAutoCalibProgressIndicatorAndLabel(true, "Calibration Failed. Retrying..." + whichOneFailed);
						try {
							Thread.sleep(500);
						} catch (InterruptedException e) {
							Thread.currentThread().interrupt();
							e.printStackTrace();
						}
						

						if (Thread.currentThread().isInterrupted()) break;	// Interrupt checker
						
						
						++retryCounter;

						if (retryCounter > 1) {
							// Set indicator
							mainGUIcontroller.modifyAutoCalibProgressIndicatorAndLabel(false, "Step 2 Calibration Failed: " + whichOneFailed);
							try {
								Thread.sleep(500);
							} catch (InterruptedException e) {
								Thread.currentThread().interrupt();
								e.printStackTrace();
							}
							retryCounter = 0;
							break;
						}


						//   _______                                   __            _______          ______             __        ________                  
						//  /       \                                 /  |          /       \        /      \           /  |      /        |                 
						//  $$$$$$$  |  ______    _______   ______   _$$ |_         $$$$$$$  |      /$$$$$$  |  ______  $$ |      $$$$$$$$/______    _______ 
						//  $$ |__$$ | /      \  /       | /      \ / $$   |        $$ |__$$ |      $$ |  $$/  /      \ $$ |      $$ |__  /      \  /       |
						//  $$    $$< /$$$$$$  |/$$$$$$$/ /$$$$$$  |$$$$$$/         $$    $$/       $$ |       $$$$$$  |$$ |      $$    | $$$$$$  |/$$$$$$$/ 
						//  $$$$$$$  |$$    $$ |$$      \ $$    $$ |  $$ | __       $$$$$$$/        $$ |   __  /    $$ |$$ |      $$$$$/  /    $$ |$$ |      
						//  $$ |  $$ |$$$$$$$$/  $$$$$$  |$$$$$$$$/   $$ |/  |      $$ |            $$ \__/  |/$$$$$$$ |$$ |      $$ |   /$$$$$$$ |$$ \_____ 
						//  $$ |  $$ |$$       |/     $$/ $$       |  $$  $$/       $$ |            $$    $$/ $$    $$ |$$ |      $$ |   $$    $$ |$$       |
						//  $$/   $$/  $$$$$$$/ $$$$$$$/   $$$$$$$/    $$$$/        $$/              $$$$$$/   $$$$$$$/ $$/       $$/     $$$$$$$/  $$$$$$$/ 
						//                                                                                                                                   
						// Reset all power cal fac
						try {
							currentSB2Reference.setEOLParamState("Ade9000_APhCal0Field", 0);
							currentSB2Reference.setEOLParamState("Ade9000_BPhCal0Field", 0);
						} catch (NoSuchFieldException e) {
							e.printStackTrace();
						} catch (IllegalAccessException e) {
							e.printStackTrace();
						}
						// send cal fac (set to object, create string, call z function)
						calibMessageToSend = currentSB2Reference.createSEND_SET_BREAKER_EOL_CALIB_PARAMcustomMessage();
						op_13_resetPowerCalFac = CallSBLCPCommand.call_Custom_SBLCPcommand_blocking(sc, calibMessageToSend, Thread.currentThread(), 4000);
						// Logging
						mainGUIcontroller.modifyAutoCalibProgressIndicatorAndLabel(true, "Resetting power cal fac...");
						mainGUIcontroller.systemOutPrintlnAndPrintLog("runAutoCalibration(): Resetting power cal fac...", LogLevel.INFO);
						if (op_13_resetPowerCalFac < 0) {
							try {
								Thread.sleep(1000);
							} catch (InterruptedException e) {
								Thread.currentThread().interrupt();
							}
							// Try again
							op_13_resetPowerCalFac = CallSBLCPCommand.call_Custom_SBLCPcommand_blocking(sc, calibMessageToSend, Thread.currentThread(), 4000);
						}
						if (op_13_resetPowerCalFac < 0) {
							// Still failed, end thread
							mainGUIcontroller.modifyAutoCalibProgressIndicatorAndLabel(false, "Reset power cal fac. Connection Error.");
							Thread.currentThread().interrupt();
							break;
						}
						
						
						try {
							Thread.sleep(2000);
						} catch (InterruptedException e) {
							Thread.currentThread().interrupt();
						}
						
						
						continue;
					} else {
						// Calibration success!
						
						// Set indicator
						mainGUIcontroller.modifyAutoCalibProgressIndicatorAndLabel(false, "Auto Calibration Step 1,2 Done");
						try {
							Thread.sleep(500);
						} catch (InterruptedException e) {
							Thread.currentThread().interrupt();
							e.printStackTrace();
						}
						retryCounter = 0;
						break;
					}
				}	// End of Step 2 loop


				if (Thread.currentThread().isInterrupted()) break;	// Interrupt checker


				// Calibration done, break out of while loop
				break;
			}	// End of main while loop


			// Print timing analysis this is temporary I would put this into GUI soon TODO
			System.out.println("Timing Analysis:\nop_1_getUDPSequenceTime_ms = " + op_1_getUDPSequenceTime_ms + "\n" + 
												 "op_2_manufaturingModeOn_ms = " + op_2_manufaturingModeOn_ms + "\n" + 
												 "op_3_resetCalFacToZero_ms = " + op_3_resetCalFacToZero_ms + "\n" + 
												 "op_3b_wait2secAfterResettingCalFac_ms = " + op_3b_wait2secAfterResettingCalFac_ms + "\n" + 
												 "op_4_getTelemData_ms = " + op_4_getTelemData_ms + "\n" + 
												 "op_5_getYokoReadings_ms = " + op_5_getYokoReadings_ms + "\n" + 
												 "op_6_sendNewCalFac_ms = " + op_6_sendNewCalFac_ms + "\n" + 
												 "op_7_getDeviceStatus_ms = " + op_7_getDeviceStatus_ms + "\n" + 
												 "op_8_getTelem_ms = " + op_8_getTelem_ms + "\n" + 
												 "op_9_getYokoReadings2ndTime_ms = " + op_9_getYokoReadings2ndTime_ms + "\n" +
												 "op_10_setPowerCalFac = " + op_10_setPowerCalFac + "\n" +
												 "op_11_getDeviceStatus_ms = " + op_11_getDeviceStatus_ms + "\n" +
												 "op_12_getTelem_ms = " + op_11_getDeviceStatus_ms + "\n" +
												 "op_13_resetPowerCalFac = " + op_13_resetPowerCalFac);
			int total = op_1_getUDPSequenceTime_ms 
						+ op_2_manufaturingModeOn_ms 
						+ op_3_resetCalFacToZero_ms 
						+ op_3b_wait2secAfterResettingCalFac_ms
						+ op_4_getTelemData_ms 
						+ op_5_getYokoReadings_ms 
						+ op_6_sendNewCalFac_ms 
						+ op_7_getDeviceStatus_ms 
						+ op_8_getTelem_ms 
						+ op_9_getYokoReadings2ndTime_ms 
						+ op_10_setPowerCalFac 
						+ op_11_getDeviceStatus_ms 
						+ op_12_getTelem_ms
						+ op_13_resetPowerCalFac;
			System.out.println("Total (ms): " + total);
		});
		
		autoCalibrationThread.start();
	}

	/**
	 * 
	 * @return 1 = success; -1 = fail
	 */
	public int forceStop() {	// Probably working now
		if (autoCalibrationThread != null && autoCalibrationThread.isAlive()) {
			autoCalibrationThread.interrupt();
			try {
				autoCalibrationThread.join();
			} catch (InterruptedException e) {
				// hi
			}
		}
		
		final int TIMEOUT_COUNTER = 1000;
		int counter = 0;
		while (autoCalibrationThread.isAlive()) {
			// Blocking function (with timeout counter): Wait till it is not alive
			++counter;
			if (counter > TIMEOUT_COUNTER)  return -1;	// Time out
			
			// Spam interrupt (top notch programming skills right here lmao)
			autoCalibrationThread.interrupt();
			
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		autoCalibrationThread = null;
		return 1;
	}

	private int resetCalFactors(MainGUIcontroller mainGUIcontroller, SblcpSerialCom sc) {
		// Retrieve the values from the text boxes and send it.
		try {
			currentSB2Reference.setEOLParamState("Ade9000_APhCal0Field", 0);
			currentSB2Reference.setEOLParamState("Ade9000_AiGainField", 0);
			currentSB2Reference.setEOLParamState("Ade9000_AiRmsSOSField", 0);
			currentSB2Reference.setEOLParamState("Ade9000_ApGainField", 0);
			currentSB2Reference.setEOLParamState("Ade9000_AvGainField", 0);
			currentSB2Reference.setEOLParamState("Ade9000_BPhCal0Field", 0);
			currentSB2Reference.setEOLParamState("Ade9000_BiGainField", 0);
			currentSB2Reference.setEOLParamState("Ade9000_BiRmsSOSField", 0);
			currentSB2Reference.setEOLParamState("Ade9000_BpGainField", 0);
			currentSB2Reference.setEOLParamState("Ade9000_BvGainField", 0);
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}

		// Send the message
		return CallSBLCPCommand.call_Custom_SBLCPcommand_blocking(sc, currentSB2Reference.createSEND_SET_BREAKER_EOL_CALIB_PARAMcustomMessage(), null, 4000);
	}
	
	private int calculateGainCalFac(double breakerReading, double yokoReading) {
		return (int)(((yokoReading - breakerReading) / breakerReading) * Math.pow(2.0, 27.0));
	}
	
	public static int calculatePhaseCalFac(double breakerReading, double yokoReading) {
		final double PI = 3.1415927;
		double omega = 2 * PI * 60 / 4000;
		double degree = ((yokoReading - breakerReading) / yokoReading) * 100.0 / 3.0;
		double radian = degree * PI / 180;
		double top = Math.sin(radian - omega) + Math.sin(omega);
		double bottom = Math.sin(2 * omega - radian);
		
		return (int) ((top / bottom) * Math.pow(2.0, 27.0));
	}
	
	
	/**
	 * 
	 * @param subject
	 * @param reference
	 * @param error_percent For example, 80%, or 0.01%
	 * @return true if acceptable (within error_percent); false if not acceptable (not within error_percent).
	 */
	public static boolean acceptableOrNot(double subject, double reference, double error_percent) {
		double difference = Math.abs(subject - reference);
		
		double error = Math.abs(reference * error_percent / 100);
		
		if (difference > error) {
			return false;
		} else {
			return true;
		}
	}

}
