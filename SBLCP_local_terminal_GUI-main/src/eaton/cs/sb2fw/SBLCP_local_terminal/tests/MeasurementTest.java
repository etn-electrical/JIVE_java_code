/******************************************************************************************************
 * 
 * 		Connected Solutions @ Eaton (Moon Township, PA)
 * 	
 * 		@author Jonathan Tan (JonathanTan@eaton.com)
 * 		@date 7/28/2023
 * 
 * ****************************************************************************************************
 * 
 * 		MeasurementTest.java
 * 
 * 		Purpose: Create a thread to perform the measurement test, update status to 
 * 				 mainGUIcontroller label.
 * 				 The measurement test's idea is: there's a list of currents we want
 * 				 to test, and a list of phases we want to test. Two nested loops are
 * 				 used to complete this task. The outer loop loops through the phase
 * 				 list, and the inner loop loops through the current list.
 * 				 Users can choose if they want to average the readings for currents
 * 				 and powers for current under 1A, there's a inner inner loop that does
 * 				 this averaging.
 * 
 * 		For more info, checkout this project's confluence page: https://confluence-prod.tcc.etn.com/pages/viewpage.action?pageId=299547479
 * 
 * *****************************************************************************************************/

package eaton.cs.sb2fw.SBLCP_local_terminal.tests;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import javafx.util.Pair;

import eaton.cs.sb2fw.SBLCP_local_terminal.GUI.MainGUIcontroller;
import eaton.cs.sb2fw.SBLCP_local_terminal.util.CallSBLCPCommand;
import eaton.cs.sb2fw.SBLCP_local_terminal.util.LogLevel;
import eaton.cs.sb2fw.SBLCP_local_terminal.util.OmicronComm;
import eaton.cs.sb2fw.SBLCP_local_terminal.util.SB2;
import eaton.cs.sb2fw.SBLCP_local_terminal.util.SblcpSerialCom;
import eaton.cs.sb2fw.SBLCP_local_terminal.util.YokogawaComm;

public class MeasurementTest {
	public MeasurementTest() {
		//                              Woof~
		//                       __    / 
		// (\,------------------'()'--o  
		//  (_    _____________    /~"
		//   (_)_)            (_)_)
	}
	
	private boolean measurementTestIsRunning = false;
	private Thread measurementTestThread;
	private Process measurementProcess_current;
	private Process measurementProcess_power;
	private Process measurementProcess_averageReadings_current;
	private Process measurementProcess_averageReadings_power;
	
	double[] currentYokoReadings;
	double phase_A_rms_current;
	double phase_A_active_power;
	double phase_B_rms_current;
	double phase_B_active_power;
	
	private final static int AVERAGE_COUNT = 100;
	private final static int MAX_RETRY_ALLOWED = 10;
	private int retries;
	
	/**
	 * These variables are for response tracking
	 */
	private int pre = -1;
	private int post = -1;
	final private int MAX_BLOCK_COUNTER = 5000;
	
	public void startMeasurementTest(MainGUIcontroller mainGUIcontroller, 
									 SblcpSerialCom sc,
									 SB2 currentSB2reference,
									 OmicronComm oc, 
									 YokogawaComm yc, 
									 ArrayList<Double> givenArrLiForCurrents,
									 ArrayList<Double> givenArrLiForPhases,
									 boolean averageBreakerReadingsForCurrentUnderOneAmp) {		
		measurementTestThread = new Thread(() -> {
			while (!Thread.currentThread().isInterrupted()) {
				measurementTestIsRunning = true;
				mainGUIcontroller.printToSerialPortOutputBox = false;		// Disable output prints
				
				oc.modifyOmicronValues(mainGUIcontroller.sbOmicYokoOmicSettingsPh0Voltage, 
						   			   mainGUIcontroller.sbOmicYokoOmicSettingsPh0VoltagePhase, 
						   			   0, 
						   			   mainGUIcontroller.sbOmicYokoOmicSettingsPh0CurrentPhase, 
						   			   mainGUIcontroller.sbOmicYokoOmicSettingsPh1Voltage, 
						   			   mainGUIcontroller.sbOmicYokoOmicSettingsPh1VoltagePhase, 
						   			   0, 
						   			   mainGUIcontroller.sbOmicYokoOmicSettingsPh1CurrentPhase);
				// Set Indicator
				String temp = "Setting Omicron to: " + 
							  mainGUIcontroller.sbOmicYokoOmicSettingsPh0Voltage + "@" + mainGUIcontroller.sbOmicYokoOmicSettingsPh0VoltagePhase + 
							  ",0@" + mainGUIcontroller.sbOmicYokoOmicSettingsPh0CurrentPhase + 
							  "; " + 
							  mainGUIcontroller.sbOmicYokoOmicSettingsPh1Voltage + "@" + mainGUIcontroller.sbOmicYokoOmicSettingsPh1VoltagePhase + 
							  "0@" + mainGUIcontroller.sbOmicYokoOmicSettingsPh1CurrentPhase;
				mainGUIcontroller.modifyMeasurementTestProgressIndicatorAndLabel(true, temp);
				
				
				if (Thread.currentThread().isInterrupted()) break;	// Interrupt check
				

				//    ______               __            __    __                        __            __    __  _______   _______  
				//   /      \             /  |          /  \  /  |                      /  |          /  |  /  |/       \ /       \ 
				//  /$$$$$$  |  ______   _$$ |_         $$  \ $$ |  ______   __    __  _$$ |_         $$ |  $$ |$$$$$$$  |$$$$$$$  |
				//  $$ | _$$/  /      \ / $$   |        $$$  \$$ | /      \ /  \  /  |/ $$   |        $$ |  $$ |$$ |  $$ |$$ |__$$ |
				//  $$ |/    |/$$$$$$  |$$$$$$/         $$$$  $$ |/$$$$$$  |$$  \/$$/ $$$$$$/         $$ |  $$ |$$ |  $$ |$$    $$/ 
				//  $$ |$$$$ |$$    $$ |  $$ | __       $$ $$ $$ |$$    $$ | $$  $$<    $$ | __       $$ |  $$ |$$ |  $$ |$$$$$$$/  
				//  $$ \__$$ |$$$$$$$$/   $$ |/  |      $$ |$$$$ |$$$$$$$$/  /$$$$  \   $$ |/  |      $$ \__$$ |$$ |__$$ |$$ |      
				//  $$    $$/ $$       |  $$  $$/       $$ | $$$ |$$       |/$$/ $$  |  $$  $$/       $$    $$/ $$    $$/ $$ |      
				//   $$$$$$/   $$$$$$$/    $$$$/        $$/   $$/  $$$$$$$/ $$/   $$/    $$$$/         $$$$$$/  $$$$$$$/  $$/       
				//                                                                                                                  
				mainGUIcontroller.modifyMeasurementTestProgressIndicatorAndLabel(true, "Getting Next UDP Sequence Number...");
				CallSBLCPCommand.call_NonCustom_SBLCPcommand_blocking(sc, CallSBLCPCommand.SET_NEXT_EXPECTED_UDP_SEQUENCE_NUMBER, Thread.currentThread(), 3000);
				CallSBLCPCommand.call_NonCustom_SBLCPcommand_blocking(sc, CallSBLCPCommand.GET_NEXT_EXPECTED_UDP_SEQUENCE_NUMBER, Thread.currentThread(), 3000);


				if (Thread.currentThread().isInterrupted()) break;	// Interrupt check


				//   __       __            __    __     
				//  /  |  _  /  |          /  |  /  |    
				//  $$ | / \ $$ |  ______  $$/  _$$ |_   
				//  $$ |/$  \$$ | /      \ /  |/ $$   |  
				//  $$ /$$$  $$ | $$$$$$  |$$ |$$$$$$/   
				//  $$ $$/$$ $$ | /    $$ |$$ |  $$ | __ 
				//  $$$$/  $$$$ |/$$$$$$$ |$$ |  $$ |/  |
				//  $$$/    $$$ |$$    $$ |$$ |  $$  $$/ 
				//  $$/      $$/  $$$$$$$/ $$/    $$$$/  
				//                                       
				try {
					mainGUIcontroller.modifyMeasurementTestProgressIndicatorAndLabel(true, "Waiting (1s)...");
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					e.printStackTrace();
					break;
				}


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
				mainGUIcontroller.modifyMeasurementTestProgressIndicatorAndLabel(true, "Turning on Manufacturing Mode...");
				CallSBLCPCommand.call_NonCustom_SBLCPcommand_blocking(sc, CallSBLCPCommand.SET_MANUFACTURING_MODE_ON, Thread.currentThread(), 3000);


				//   __       __            __    __     
				//  /  |  _  /  |          /  |  /  |    
				//  $$ | / \ $$ |  ______  $$/  _$$ |_   
				//  $$ |/$  \$$ | /      \ /  |/ $$   |  
				//  $$ /$$$  $$ | $$$$$$  |$$ |$$$$$$/   
				//  $$ $$/$$ $$ | /    $$ |$$ |  $$ | __ 
				//  $$$$/  $$$$ |/$$$$$$$ |$$ |  $$ |/  |
				//  $$$/    $$$ |$$    $$ |$$ |  $$  $$/ 
				//  $$/      $$/  $$$$$$$/ $$/    $$$$/  
				//                                       
				try {
					mainGUIcontroller.modifyMeasurementTestProgressIndicatorAndLabel(true, "Waiting (1.1s)...");
					Thread.sleep(1100);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					e.printStackTrace();
					break;
				}


				if (Thread.currentThread().isInterrupted()) break;	// Interrupt check
				

				//   __                                            _______   __                                               
				//  /  |                                          /       \ /  |                                              
				//  $$ |        ______    ______    ______        $$$$$$$  |$$ |____    ______    _______   ______    _______ 
				//  $$ |       /      \  /      \  /      \       $$ |__$$ |$$      \  /      \  /       | /      \  /       |
				//  $$ |      /$$$$$$  |/$$$$$$  |/$$$$$$  |      $$    $$/ $$$$$$$  | $$$$$$  |/$$$$$$$/ /$$$$$$  |/$$$$$$$/ 
				//  $$ |      $$ |  $$ |$$ |  $$ |$$ |  $$ |      $$$$$$$/  $$ |  $$ | /    $$ |$$      \ $$    $$ |$$      \ 
				//  $$ |_____ $$ \__$$ |$$ \__$$ |$$ |__$$ |      $$ |      $$ |  $$ |/$$$$$$$ | $$$$$$  |$$$$$$$$/  $$$$$$  |
				//  $$       |$$    $$/ $$    $$/ $$    $$/       $$ |      $$ |  $$ |$$    $$ |/     $$/ $$       |/     $$/ 
				//  $$$$$$$$/  $$$$$$/   $$$$$$/  $$$$$$$/        $$/       $$/   $$/  $$$$$$$/ $$$$$$$/   $$$$$$$/ $$$$$$$/  
				//                                $$ |                                                                        
				//                                $$ |                                                                        
				//                                $$/                                                                                                                                                                 $$$$$$/                                             
				for (int phases = 0; phases < givenArrLiForPhases.size(); ++phases) {
					mainGUIcontroller.modifyMeasurementTestProgressIndicatorAndLabel(true, "Running at Phase of " + givenArrLiForPhases.get(phases));
					//    ______               __            ______  _______   __        ______                             
					//   /      \             /  |          /      |/       \ /  |      /      |                            
					//  /$$$$$$  |  ______   _$$ |_         $$$$$$/ $$$$$$$  |$$ |____  $$$$$$/        ____        __    __ 
					//  $$ \__$$/  /      \ / $$   |          $$ |  $$ |__$$ |$$      \   $$ |        /    |      /  \  /  |
					//  $$      \ /$$$$$$  |$$$$$$/           $$ |  $$    $$/ $$$$$$$  |  $$ |        $$$$/       $$  \/$$/ 
					//   $$$$$$  |$$    $$ |  $$ | __         $$ |  $$$$$$$/  $$ |  $$ |  $$ |        /    |       $$  $$<  
					//  /  \__$$ |$$$$$$$$/   $$ |/  |       _$$ |_ $$ |      $$ |  $$ | _$$ |_       $$$$/        /$$$$  \ 
					//  $$    $$/ $$       |  $$  $$/       / $$   |$$ |      $$ |  $$ |/ $$   |                  /$$/ $$  |
					//   $$$$$$/   $$$$$$$/    $$$$/        $$$$$$/ $$/       $$/   $$/ $$$$$$/                   $$/   $$/ 
					//                                                                                                      
					setCurrentPhaseToXandUpdateLabel(givenArrLiForPhases.get(phases), mainGUIcontroller, oc);


					try {
						mainGUIcontroller.modifyMeasurementTestProgressIndicatorAndLabel(true, "Waiting 2s for yoko to reach steady state...");
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
						e.printStackTrace();
						break;
					}


					if (Thread.currentThread().isInterrupted()) break;	// Interrupt check
					

					//    ______               __            ______                     ______  
					//   /      \             /  |          /      |                   /      \ 
					//  /$$$$$$  |  ______   _$$ |_         $$$$$$/        ____       /$$$$$$  |
					//  $$ \__$$/  /      \ / $$   |          $$ |        /    |      $$$  \$$ |
					//  $$      \ /$$$$$$  |$$$$$$/           $$ |        $$$$/       $$$$  $$ |
					//   $$$$$$  |$$    $$ |  $$ | __         $$ |        /    |      $$ $$ $$ |
					//  /  \__$$ |$$$$$$$$/   $$ |/  |       _$$ |_       $$$$/       $$ \$$$$ |
					//  $$    $$/ $$       |  $$  $$/       / $$   |                  $$   $$$/ 
					//   $$$$$$/   $$$$$$$/    $$$$/        $$$$$$/                    $$$$$$/  
					//                                                                          
					setCurrentToXandUpdateLabel(0, mainGUIcontroller, oc);

					
					if (Thread.currentThread().isInterrupted()) break;	// Interrupt check


					//   __                                             ______                                                       __              
					//  /  |                                           /      \                                                     /  |             
					//  $$ |        ______    ______    ______        /$$$$$$  | __    __   ______    ______    ______   _______   _$$ |_    _______ 
					//  $$ |       /      \  /      \  /      \       $$ |  $$/ /  |  /  | /      \  /      \  /      \ /       \ / $$   |  /       |
					//  $$ |      /$$$$$$  |/$$$$$$  |/$$$$$$  |      $$ |      $$ |  $$ |/$$$$$$  |/$$$$$$  |/$$$$$$  |$$$$$$$  |$$$$$$/  /$$$$$$$/ 
					//  $$ |      $$ |  $$ |$$ |  $$ |$$ |  $$ |      $$ |   __ $$ |  $$ |$$ |  $$/ $$ |  $$/ $$    $$ |$$ |  $$ |  $$ | __$$      \ 
					//  $$ |_____ $$ \__$$ |$$ \__$$ |$$ |__$$ |      $$ \__/  |$$ \__$$ |$$ |      $$ |      $$$$$$$$/ $$ |  $$ |  $$ |/  |$$$$$$  |
					//  $$       |$$    $$/ $$    $$/ $$    $$/       $$    $$/ $$    $$/ $$ |      $$ |      $$       |$$ |  $$ |  $$  $$//     $$/ 
					//  $$$$$$$$/  $$$$$$/   $$$$$$/  $$$$$$$/         $$$$$$/   $$$$$$/  $$/       $$/        $$$$$$$/ $$/   $$/    $$$$/ $$$$$$$/  
					//                                $$ |                                                                                           
					//                                $$ |                                                                                           
					//                                $$/                                                                                            
					// "Open" Python file
					measurementProcess_current = startMeasurementTestProcess("Step Current Test for Phase " + givenArrLiForPhases.get(phases));
					measurementProcess_power = startMeasurementTestProcess("Step Power Test for Phase " + givenArrLiForPhases.get(phases));
					for (int currents = 0; currents < givenArrLiForCurrents.size(); ++currents) {
						//    ______               __            ______                             
						//   /      \             /  |          /      |                            
						//  /$$$$$$  |  ______   _$$ |_         $$$$$$/        ____        __    __ 
						//  $$ \__$$/  /      \ / $$   |          $$ |        /    |      /  \  /  |
						//  $$      \ /$$$$$$  |$$$$$$/           $$ |        $$$$/       $$  \/$$/ 
						//   $$$$$$  |$$    $$ |  $$ | __         $$ |        /    |       $$  $$<  
						//  /  \__$$ |$$$$$$$$/   $$ |/  |       _$$ |_       $$$$/        /$$$$  \ 
						//  $$    $$/ $$       |  $$  $$/       / $$   |                  /$$/ $$  |
						//   $$$$$$/   $$$$$$$/    $$$$/        $$$$$$/                   $$/   $$/ 
						//                                                                          
						setCurrentToXandUpdateLabel(givenArrLiForCurrents.get(currents), mainGUIcontroller, oc);
						

						if (Thread.currentThread().isInterrupted()) break;	// Interrupt check


						try {
							mainGUIcontroller.modifyMeasurementTestProgressIndicatorAndLabel(true, "Waiting 2s for breaker and yoko to reach steady state...");
							Thread.sleep(2000);
						} catch (InterruptedException e) {
							Thread.currentThread().interrupt();
							e.printStackTrace();
							break;
						}
						
						
						if (Thread.currentThread().isInterrupted()) break;	// Interrupt check

						
						if (averageBreakerReadingsForCurrentUnderOneAmp && givenArrLiForCurrents.get(currents) < 1) {
							// "Open" measurementTestPlotter_multipleReadings.py file
							measurementProcess_averageReadings_current = startMeasurementTestProcess_Average(AVERAGE_COUNT + " current reading at " + givenArrLiForCurrents.get(currents) + "A");
							measurementProcess_averageReadings_power = startMeasurementTestProcess_Average(AVERAGE_COUNT + " power reading at " + givenArrLiForCurrents.get(currents) + "A");
							//    ______                      __               ______                             _______               __               
							//   /      \                    /  |             /      \                           /       \             /  |              
							//  /$$$$$$  |  ______   ______  $$ |____        /$$$$$$  | __     __  ______        $$$$$$$  |  ______   _$$ |_     ______  
							//  $$ | _$$/  /      \ /      \ $$      \       $$ |__$$ |/  \   /  |/      \       $$ |  $$ | /      \ / $$   |   /      \ 
							//  $$ |/    |/$$$$$$  |$$$$$$  |$$$$$$$  |      $$    $$ |$$  \ /$$//$$$$$$  |      $$ |  $$ | $$$$$$  |$$$$$$/    $$$$$$  |
							//  $$ |$$$$ |$$ |  $$/ /    $$ |$$ |  $$ |      $$$$$$$$ | $$  /$$/ $$ |  $$ |      $$ |  $$ | /    $$ |  $$ | __  /    $$ |
							//  $$ \__$$ |$$ |     /$$$$$$$ |$$ |__$$ |      $$ |  $$ |  $$ $$/  $$ \__$$ |      $$ |__$$ |/$$$$$$$ |  $$ |/  |/$$$$$$$ |
							//  $$    $$/ $$ |     $$    $$ |$$    $$/       $$ |  $$ |   $$$/   $$    $$ |      $$    $$/ $$    $$ |  $$  $$/ $$    $$ |
							//   $$$$$$/  $$/       $$$$$$$/ $$$$$$$/        $$/   $$/     $/     $$$$$$$ |      $$$$$$$/   $$$$$$$/    $$$$/   $$$$$$$/ 
							//                                                                   /  \__$$ |                                              
							//                                                                   $$    $$/                                               
							//                                                                    $$$$$$/                                                
							double breakerPhaseASum_current = 0;
							double breakerPhaseBSum_current = 0;
							double yokoPhaseASum_current = 0;
							double yokoPhaseBSum_current = 0;
							
							double breakerPhaseASum_power = 0;
							double breakerPhaseBSum_power = 0;
							double yokoPhaseASum_power = 0;
							double yokoPhaseBSum_power = 0;
							
							for (int i = 0; i < AVERAGE_COUNT; ++i) {
								//   _______                                          __              _______               __               
								//  /       \                                        /  |            /       \             /  |              
								//  $$$$$$$  |  ______    ______    ______   ______  $$ |____        $$$$$$$  |  ______   _$$ |_     ______  
								//  $$ |__$$ | /      \  /      \  /      \ /      \ $$      \       $$ |  $$ | /      \ / $$   |   /      \ 
								//  $$    $$< /$$$$$$  |/$$$$$$  |/$$$$$$  |$$$$$$  |$$$$$$$  |      $$ |  $$ | $$$$$$  |$$$$$$/    $$$$$$  |
								//  $$$$$$$  |$$    $$ |$$ |  $$ |$$ |  $$/ /    $$ |$$ |  $$ |      $$ |  $$ | /    $$ |  $$ | __  /    $$ |
								//  $$ |  $$ |$$$$$$$$/ $$ \__$$ |$$ |     /$$$$$$$ |$$ |__$$ |      $$ |__$$ |/$$$$$$$ |  $$ |/  |/$$$$$$$ |
								//  $$ |  $$ |$$       |$$    $$ |$$ |     $$    $$ |$$    $$/       $$    $$/ $$    $$ |  $$  $$/ $$    $$ |
								//  $$/   $$/  $$$$$$$/  $$$$$$$ |$$/       $$$$$$$/ $$$$$$$/        $$$$$$$/   $$$$$$$/    $$$$/   $$$$$$$/ 
								//                      /  \__$$ |                                                                           
								//                      $$    $$/                                                                            
								//                       $$$$$$/                                                                             
								int tempI = i + 1;
								mainGUIcontroller.modifyMeasurementTestProgressIndicatorAndLabel(true, "Grabbing #" + tempI + " data (Averaged) for current " + givenArrLiForCurrents.get(currents) + "A...");
								currentYokoReadings = yc.getPh0Ph1AllValAtOnce();
								retries = 0;
								while (true) {
									// Get telem data
									int timeToGetTelemData = CallSBLCPCommand.call_NonCustom_SBLCPcommand_blocking(sc, CallSBLCPCommand.GET_METER_TELEMETRT_DATA, Thread.currentThread(), 3000);
									while (timeToGetTelemData < 0) {
										try {
											Thread.sleep(2500);
										} catch (InterruptedException e) {
											Thread.currentThread().interrupt();
										}
										// Try again
										CallSBLCPCommand.call_NonCustom_SBLCPcommand_blocking(sc, CallSBLCPCommand.SET_NEXT_EXPECTED_UDP_SEQUENCE_NUMBER, Thread.currentThread(), 3000);
										CallSBLCPCommand.call_NonCustom_SBLCPcommand_blocking(sc, CallSBLCPCommand.GET_NEXT_EXPECTED_UDP_SEQUENCE_NUMBER, Thread.currentThread(), 3000);
										timeToGetTelemData = CallSBLCPCommand.call_NonCustom_SBLCPcommand_blocking(sc, CallSBLCPCommand.GET_METER_TELEMETRT_DATA, Thread.currentThread(), 3000);
										
										++retries;
										
										if (retries > MAX_RETRY_ALLOWED) {
											mainGUIcontroller.modifyMeasurementTestProgressIndicatorAndLabel(false, "Something is wrong, breaker wont respond, test ended.");
											Thread.currentThread().interrupt();
											break;
										}
										
										if (Thread.currentThread().isInterrupted()) break;	// Interrupt check
									}
									
									if (Thread.currentThread().isInterrupted()) break;	// Interrupt check
									break;
								}


								if (Thread.currentThread().isInterrupted()) break;	// Interrupt check

								
								// Wait for 2s
								try {
									Thread.sleep(200);
								} catch (InterruptedException e) {
									Thread.currentThread().interrupt();
									e.printStackTrace();
									break;
								}

								// Add to current sum
								phase_A_rms_current = Double.parseDouble(currentSB2reference.getIndividualState("phase_A_rms_current").getValue());
								phase_B_rms_current = Double.parseDouble(currentSB2reference.getIndividualState("phase_B_rms_current").getValue());
								breakerPhaseASum_current += phase_A_rms_current;
								breakerPhaseBSum_current += phase_B_rms_current;
								yokoPhaseASum_current += currentYokoReadings[1];
								yokoPhaseBSum_current += currentYokoReadings[14];
								// Add to power sum
								phase_A_active_power = Double.parseDouble(currentSB2reference.getIndividualState("phase_A_active_power").getValue());
								phase_B_active_power = Double.parseDouble(currentSB2reference.getIndividualState("phase_B_active_power").getValue());
								breakerPhaseASum_power += phase_A_active_power;
								breakerPhaseBSum_power += phase_B_active_power;
								yokoPhaseASum_power += currentYokoReadings[2];
								yokoPhaseBSum_power += currentYokoReadings[15];


								// Plot using measurementTestPlotter_multipleReadings.py
								String toSendToMeasurementTestPlotterMultipleReadings_current = "";
								toSendToMeasurementTestPlotterMultipleReadings_current += i + 1;
								toSendToMeasurementTestPlotterMultipleReadings_current += ",";
								toSendToMeasurementTestPlotterMultipleReadings_current += currentYokoReadings[1];
								toSendToMeasurementTestPlotterMultipleReadings_current += ",";
								toSendToMeasurementTestPlotterMultipleReadings_current += phase_A_rms_current;
								toSendToMeasurementTestPlotterMultipleReadings_current += ",";
								toSendToMeasurementTestPlotterMultipleReadings_current += currentYokoReadings[14];
								toSendToMeasurementTestPlotterMultipleReadings_current += ",";
								toSendToMeasurementTestPlotterMultipleReadings_current += phase_B_rms_current;
								try {
									pythonAppInputSender(measurementProcess_averageReadings_current, toSendToMeasurementTestPlotterMultipleReadings_current);
								} catch (IOException e) {
									e.printStackTrace();
								}
								String toSendToMeasurementTestPlotterMultipleReadings_power = "";
								toSendToMeasurementTestPlotterMultipleReadings_power += i + 1;
								toSendToMeasurementTestPlotterMultipleReadings_power += ",";
								toSendToMeasurementTestPlotterMultipleReadings_power += currentYokoReadings[2];
								toSendToMeasurementTestPlotterMultipleReadings_power += ",";
								toSendToMeasurementTestPlotterMultipleReadings_power += phase_A_active_power;
								toSendToMeasurementTestPlotterMultipleReadings_power += ",";
								toSendToMeasurementTestPlotterMultipleReadings_power += currentYokoReadings[15];
								toSendToMeasurementTestPlotterMultipleReadings_power += ",";
								toSendToMeasurementTestPlotterMultipleReadings_power += phase_B_active_power;
								try {
									pythonAppInputSender(measurementProcess_averageReadings_power, toSendToMeasurementTestPlotterMultipleReadings_power);
								} catch (IOException e) {
									e.printStackTrace();
								}
							}

							try {
								pythonAppInputSender(measurementProcess_averageReadings_current, "showplot");
								pythonAppInputSender(measurementProcess_averageReadings_current, "shutdown");
								pythonAppInputSender(measurementProcess_averageReadings_power, "showplot");
								pythonAppInputSender(measurementProcess_averageReadings_power, "shutdown");
							} catch (IOException e) {
								e.printStackTrace();
							}
							
							measurementProcess_averageReadings_power = null;
							measurementProcess_averageReadings_current = null;

							// Average and write into respective fields so the plotting code can plot it
							Pair<String, String> tempBreakerAveragePhaseA_current = new Pair<>("phase_A_rms_current", String.valueOf(breakerPhaseASum_current / AVERAGE_COUNT));
							currentSB2reference.updateIndividualState(tempBreakerAveragePhaseA_current);
							Pair<String, String> tempBreakerAveragePhaseB_current = new Pair<>("phase_B_rms_current", String.valueOf(breakerPhaseBSum_current / AVERAGE_COUNT));
							currentSB2reference.updateIndividualState(tempBreakerAveragePhaseB_current);
							Pair<String, String> tempBreakerAveragePhaseA_power = new Pair<>("phase_A_active_power", String.valueOf(breakerPhaseASum_power / AVERAGE_COUNT));
							currentSB2reference.updateIndividualState(tempBreakerAveragePhaseA_power);
							Pair<String, String> tempBreakerAveragePhaseB_power = new Pair<>("phase_B_active_power", String.valueOf(breakerPhaseBSum_power / AVERAGE_COUNT));
							currentSB2reference.updateIndividualState(tempBreakerAveragePhaseB_power);
							
							yc.overwriteValue(1, yokoPhaseASum_current / AVERAGE_COUNT);
							yc.overwriteValue(14, yokoPhaseBSum_current / AVERAGE_COUNT);
							yc.overwriteValue(2, yokoPhaseASum_power / AVERAGE_COUNT);
							yc.overwriteValue(15, yokoPhaseBSum_power / AVERAGE_COUNT);
						} else {
							//   _______                                          __       		  _______               __               
							//  /       \                                        /  |      		 /       \             /  |              
							//  $$$$$$$  |  ______    ______    ______   ______  $$ |____  		 $$$$$$$  |  ______   _$$ |_     ______  
							//  $$ |__$$ | /      \  /      \  /      \ /      \ $$      \ 		 $$ |  $$ | /      \ / $$   |   /      \ 
							//  $$    $$< /$$$$$$  |/$$$$$$  |/$$$$$$  |$$$$$$  |$$$$$$$  |		 $$ |  $$ | $$$$$$  |$$$$$$/    $$$$$$  |
							//  $$$$$$$  |$$    $$ |$$ |  $$ |$$ |  $$/ /    $$ |$$ |  $$ |		 $$ |  $$ | /    $$ |  $$ | __  /    $$ |
							//  $$ |  $$ |$$$$$$$$/ $$ \__$$ |$$ |     /$$$$$$$ |$$ |__$$ |		 $$ |__$$ |/$$$$$$$ |  $$ |/  |/$$$$$$$ |
							//  $$ |  $$ |$$       |$$    $$ |$$ |     $$    $$ |$$    $$/ 		 $$    $$/ $$    $$ |  $$  $$/ $$    $$ |
							//  $$/   $$/  $$$$$$$/  $$$$$$$ |$$/ 		$$$$$$$/ $$$$$$$/  		 $$$$$$$/   $$$$$$$/    $$$$/   $$$$$$$/ 
							//						/  \__$$ |
							//						$$    $$/
							//						 $$$$$$/
							mainGUIcontroller.modifyMeasurementTestProgressIndicatorAndLabel(true, "Grabbing data (Non Averaged)...");
							currentYokoReadings = yc.getPh0Ph1AllValAtOnce();
							retries = 0;
							while (true) {
								// Get telem data
								int timeToGetTelemData = CallSBLCPCommand.call_NonCustom_SBLCPcommand_blocking(sc, CallSBLCPCommand.GET_METER_TELEMETRT_DATA, Thread.currentThread(), 3000);
								while (timeToGetTelemData < 0) {
									try {
										Thread.sleep(2500);
									} catch (InterruptedException e) {
										Thread.currentThread().interrupt();
									}
									// Try again
									CallSBLCPCommand.call_NonCustom_SBLCPcommand_blocking(sc, CallSBLCPCommand.SET_NEXT_EXPECTED_UDP_SEQUENCE_NUMBER, Thread.currentThread(), 3000);
									CallSBLCPCommand.call_NonCustom_SBLCPcommand_blocking(sc, CallSBLCPCommand.GET_NEXT_EXPECTED_UDP_SEQUENCE_NUMBER, Thread.currentThread(), 3000);
									timeToGetTelemData = CallSBLCPCommand.call_NonCustom_SBLCPcommand_blocking(sc, CallSBLCPCommand.GET_METER_TELEMETRT_DATA, Thread.currentThread(), 3000);
									
									++retries;
									
									if (retries > MAX_RETRY_ALLOWED) {
										mainGUIcontroller.modifyMeasurementTestProgressIndicatorAndLabel(false, "Something is wrong, breaker wont respond, test ended.");
										Thread.currentThread().interrupt();
										break;
									}
									
									if (Thread.currentThread().isInterrupted()) break;	// Interrupt check
								}
								
								if (Thread.currentThread().isInterrupted()) break;	// Interrupt check
								break;
							}
						}
						
						
						if (Thread.currentThread().isInterrupted()) break;	// Interrupt check
						

						//    ______               __            __    __                                __    __  _______   _______  
						//   /      \             /  |          /  \  /  |                              /  |  /  |/       \ /       \ 
						//  /$$$$$$  |  ______   _$$ |_         $$  \ $$ |  ______   __   __   __       $$ |  $$ |$$$$$$$  |$$$$$$$  |
						//  $$ \__$$/  /      \ / $$   |        $$$  \$$ | /      \ /  | /  | /  |      $$ |  $$ |$$ |  $$ |$$ |__$$ |
						//  $$      \ /$$$$$$  |$$$$$$/         $$$$  $$ |/$$$$$$  |$$ | $$ | $$ |      $$ |  $$ |$$ |  $$ |$$    $$/ 
						//   $$$$$$  |$$    $$ |  $$ | __       $$ $$ $$ |$$    $$ |$$ | $$ | $$ |      $$ |  $$ |$$ |  $$ |$$$$$$$/  
						//  /  \__$$ |$$$$$$$$/   $$ |/  |      $$ |$$$$ |$$$$$$$$/ $$ \_$$ \_$$ |      $$ \__$$ |$$ |__$$ |$$ |      
						//  $$    $$/ $$       |  $$  $$/       $$ | $$$ |$$       |$$   $$   $$/       $$    $$/ $$    $$/ $$ |      
						//   $$$$$$/   $$$$$$$/    $$$$/        $$/   $$/  $$$$$$$/  $$$$$/$$$$/         $$$$$$/  $$$$$$$/  $$/       
						//                                                                                                            
						// Logging
						mainGUIcontroller.modifyMeasurementTestProgressIndicatorAndLabel(true, "Setting new UDP...");
						CallSBLCPCommand.call_NonCustom_SBLCPcommand_blocking(sc, CallSBLCPCommand.SET_NEXT_EXPECTED_UDP_SEQUENCE_NUMBER, Thread.currentThread(), 3000);
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
						// Logging
						mainGUIcontroller.modifyMeasurementTestProgressIndicatorAndLabel(true, "Getting new UDP...");
						CallSBLCPCommand.call_NonCustom_SBLCPcommand_blocking(sc, CallSBLCPCommand.GET_NEXT_EXPECTED_UDP_SEQUENCE_NUMBER, Thread.currentThread(), 3000);
						

						if (Thread.currentThread().isInterrupted()) break;	// Interrupt check


						// Wait for 3s
						try {
							Thread.sleep(3000);
						} catch (InterruptedException e) {
							Thread.currentThread().interrupt();
							e.printStackTrace();
							break;
						}


						//   _______   __              __             ______                                __       
						//  /       \ /  |            /  |           /      \                              /  |      
						//  $$$$$$$  |$$ |  ______   _$$ |_         /$$$$$$  |  ______   ______    ______  $$ |____  
						//  $$ |__$$ |$$ | /      \ / $$   |        $$ | _$$/  /      \ /      \  /      \ $$      \ 
						//  $$    $$/ $$ |/$$$$$$  |$$$$$$/         $$ |/    |/$$$$$$  |$$$$$$  |/$$$$$$  |$$$$$$$  |
						//  $$$$$$$/  $$ |$$ |  $$ |  $$ | __       $$ |$$$$ |$$ |  $$/ /    $$ |$$ |  $$ |$$ |  $$ |
						//  $$ |      $$ |$$ \__$$ |  $$ |/  |      $$ \__$$ |$$ |     /$$$$$$$ |$$ |__$$ |$$ |  $$ |
						//  $$ |      $$ |$$    $$/   $$  $$/       $$    $$/ $$ |     $$    $$ |$$    $$/ $$ |  $$ |
						//  $$/       $$/  $$$$$$/     $$$$/         $$$$$$/  $$/       $$$$$$$/ $$$$$$$/  $$/   $$/ 
						//                                                                       $$ |                
						//                                                                       $$ |                
						//                                                                       $$/                 
						mainGUIcontroller.modifyMeasurementTestProgressIndicatorAndLabel(true, "Plotting beautiful graph...");
						// Write to csv and plot (using python function)
						phase_A_rms_current = Double.parseDouble(currentSB2reference.getIndividualState("phase_A_rms_current").getValue());
						phase_A_active_power = Double.parseDouble(currentSB2reference.getIndividualState("phase_A_active_power").getValue());
						phase_B_rms_current = Double.parseDouble(currentSB2reference.getIndividualState("phase_B_rms_current").getValue());
						phase_B_active_power = Double.parseDouble(currentSB2reference.getIndividualState("phase_B_active_power").getValue());
						String toSendToPython;
						// Plot current graph first
						toSendToPython = "";
						toSendToPython += givenArrLiForCurrents.get(currents);
						toSendToPython += ",";
						toSendToPython += currentYokoReadings[1];
						toSendToPython += ",";
						toSendToPython += phase_A_rms_current;
						toSendToPython += ",";
						toSendToPython += currentYokoReadings[14];
						toSendToPython += ",";
						toSendToPython += phase_B_rms_current;
						try {
							pythonAppInputSender(measurementProcess_current, toSendToPython);
						} catch (IOException e) {
							e.printStackTrace();
						}
						// Plot power graph
						toSendToPython = "";
						toSendToPython += givenArrLiForCurrents.get(currents);
						toSendToPython += ",";
						toSendToPython += currentYokoReadings[2];
						toSendToPython += ",";
						toSendToPython += phase_A_active_power;
						toSendToPython += ",";
						toSendToPython += currentYokoReadings[15];
						toSendToPython += ",";
						toSendToPython += phase_B_active_power;
						try {
							pythonAppInputSender(measurementProcess_power, toSendToPython);
						} catch (IOException e) {
							e.printStackTrace();
						}


						if (Thread.currentThread().isInterrupted()) break;	// Interrupt check
					}	// End of inner for loop looping currents

					try {
						pythonAppInputSender(measurementProcess_current, "showplot");
						pythonAppInputSender(measurementProcess_current, "shutdown");
						pythonAppInputSender(measurementProcess_power, "showplot");
						pythonAppInputSender(measurementProcess_power, "shutdown");
					} catch (IOException e) {
						e.printStackTrace();
					}
					
					// "Close" the Python file
					measurementProcess_current = null;
					measurementProcess_power = null;

					if (Thread.currentThread().isInterrupted()) break;	// Interrupt check
				}	// End of outter for loop looping phases


				if (Thread.currentThread().isInterrupted()) break;	// Interrupt check

				
				measurementTestIsRunning = false;
				mainGUIcontroller.modifyMeasurementTestProgressIndicatorAndLabel(false, "Test Ended. Checkout the beautiful plots! :)");

				mainGUIcontroller.printToSerialPortOutputBox = true;
				
				// If we are here, it means we completed the test and ready to call it a day (interrupt thread) and go home (break)
				Thread.currentThread().interrupt();	// Just to be sure
				break;
			}
		});

		measurementTestThread.start();
	}
	
	public void stopMeasurementTest(MainGUIcontroller mainGUIcontroller) {
		if (measurementTestIsRunning) {
			killMeasurementTestThread();
			measurementTestIsRunning = false;
			
			mainGUIcontroller.printToSerialPortOutputBox = true;
			
			mainGUIcontroller.printToSerialPortOutputBox = true;
			mainGUIcontroller.modifyMeasurementTestProgressIndicatorAndLabel(false, "Test Stopped.");
		}
	}
	
	public void showPlot() {
		try {
			pythonAppInputSender(measurementProcess_current, "showplot");
			pythonAppInputSender(measurementProcess_power, "showplot");
			pythonAppInputSender(measurementProcess_averageReadings_current, "showplot");
			pythonAppInputSender(measurementProcess_averageReadings_power, "showplot");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private Process startMeasurementTestProcess(String nameOfTest) {
		return pythonAppTalker("python/measurementTestPlotter.py", nameOfTest);
	}

	private Process startMeasurementTestProcess_Average(String nameOfTest) {
		return pythonAppTalker("python/measurementTestPlotter_multipleReadings.py", nameOfTest);
	}
	
	private void killMeasurementTestThread() {
		if (measurementTestThread != null && measurementTestThread.isAlive()) {
			measurementTestThread.interrupt();
			try {
				measurementTestThread.join();
			} catch (InterruptedException e) {
				// hi
			}
			measurementTestThread = null;
		}
	}
	
	/*protected*/public Process pythonAppTalker(String pythonFileLocation, String arg1) {
       try {
           // Build the command to execute the Python script
       	ProcessBuilder pb = new ProcessBuilder("python", pythonFileLocation, arg1);
           Process process = pb.start();

           // Obtain the input stream to read the Python program's output
           BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

           // Read the Python program's output in a separate thread
           Thread outputThread = new Thread(() -> {
               try {
                   String line;
                   while ((line = reader.readLine()) != null) {
                       System.out.println("Python output: " + line);
                   }
               } catch (IOException e) {
                   e.printStackTrace();
               }
           });
           outputThread.start();

           return process;
       } catch (IOException e) {
           e.printStackTrace();
           return null;
       }
   }

	/*protected*/public void pythonAppInputSender(Process process, String inputToSend) throws IOException {
       // Obtain the output stream to send keyboard input to the Python program
		OutputStream outputStream = process.getOutputStream();
		PrintWriter writer = new PrintWriter(outputStream);

		writer.println(inputToSend);
		writer.flush();
   }

   	private void setCurrentToXandUpdateLabel(double givenX, MainGUIcontroller mainGUIcontroller, OmicronComm oc) {
		oc.modifyOmicronValues(mainGUIcontroller.sbOmicYokoOmicSettingsPh0Voltage, 
							   mainGUIcontroller.sbOmicYokoOmicSettingsPh0VoltagePhase, 
							   givenX, 
							   mainGUIcontroller.sbOmicYokoOmicSettingsPh0CurrentPhase, 
							   mainGUIcontroller.sbOmicYokoOmicSettingsPh1Voltage, 
							   mainGUIcontroller.sbOmicYokoOmicSettingsPh1VoltagePhase, 
							   givenX, 
							   mainGUIcontroller.sbOmicYokoOmicSettingsPh1CurrentPhase);
		// Set Indicator
		String temp = "Setting Omicron to: " + 
					mainGUIcontroller.sbOmicYokoOmicSettingsPh0Voltage + "@" + mainGUIcontroller.sbOmicYokoOmicSettingsPh0VoltagePhase + 
					"," + givenX + "@" + mainGUIcontroller.sbOmicYokoOmicSettingsPh0CurrentPhase + 
					"; " + 
					mainGUIcontroller.sbOmicYokoOmicSettingsPh1Voltage + "@" + mainGUIcontroller.sbOmicYokoOmicSettingsPh1VoltagePhase + 
					"," + givenX + "@" + mainGUIcontroller.sbOmicYokoOmicSettingsPh1CurrentPhase;
		mainGUIcontroller.modifyMeasurementTestProgressIndicatorAndLabel(true, temp);
   }

   	private void setCurrentPhaseToXandUpdateLabel(double givenX, MainGUIcontroller mainGUIcontroller, OmicronComm oc) {
		oc.modifyOmicronValues(mainGUIcontroller.sbOmicYokoOmicSettingsPh0Voltage, 
							   mainGUIcontroller.sbOmicYokoOmicSettingsPh0VoltagePhase, 
							   mainGUIcontroller.sbOmicYokoOmicSettingsPh0Current, 
							   mainGUIcontroller.sbOmicYokoOmicSettingsPh0CurrentPhase + givenX, 
							   mainGUIcontroller.sbOmicYokoOmicSettingsPh1Voltage, 
							   mainGUIcontroller.sbOmicYokoOmicSettingsPh1VoltagePhase, 
							   mainGUIcontroller.sbOmicYokoOmicSettingsPh1Current, 
							   mainGUIcontroller.sbOmicYokoOmicSettingsPh1CurrentPhase + givenX);
		// Set Indicator
		String temp = "Setting Omicron to: " + 
					  mainGUIcontroller.sbOmicYokoOmicSettingsPh0Voltage + "@" +
					  mainGUIcontroller.sbOmicYokoOmicSettingsPh0VoltagePhase + "," +
					  mainGUIcontroller.sbOmicYokoOmicSettingsPh0Current + "@" +
					  mainGUIcontroller.sbOmicYokoOmicSettingsPh0CurrentPhase + givenX + "," +
					  mainGUIcontroller.sbOmicYokoOmicSettingsPh1Voltage + "@" +
					  mainGUIcontroller.sbOmicYokoOmicSettingsPh1VoltagePhase + "," +
					  mainGUIcontroller.sbOmicYokoOmicSettingsPh1Current + "@" +
					  mainGUIcontroller.sbOmicYokoOmicSettingsPh1CurrentPhase + givenX;
		mainGUIcontroller.modifyMeasurementTestProgressIndicatorAndLabel(true, temp);
   	}
}
