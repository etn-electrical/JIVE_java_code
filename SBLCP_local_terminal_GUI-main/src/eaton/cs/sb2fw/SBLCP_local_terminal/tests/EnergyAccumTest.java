/**
 * EnergyAccumTest.java
 * Purpose: Create a thread to run the energy accumulation test, update status to 
 * 			mainGUIcontroller label.
 * 
 * @author Jonathan Tan (JonathanTan@eaton.com)
 * 
 * For more info, checkout this project's confluence page:
 * https://confluence-prod.tcc.etn.com/pages/viewpage.action?pageId=299547479
 */

package eaton.cs.sb2fw.SBLCP_local_terminal.tests;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;

import eaton.cs.sb2fw.SBLCP_local_terminal.GUI.MainGUIcontroller;
import eaton.cs.sb2fw.SBLCP_local_terminal.util.*;

public class EnergyAccumTest {
	public EnergyAccumTest() {
		// ^v^ meow~
	}
	
	private boolean energyAccumTestIsRunning = false;
	private Thread energyAccumTestThread;
	private Process energyAccumProcess;
	
	/**
	 * These variables are for response tracking
	 */
	private int pre = -1;
	private int post = -1;
	final private int MAX_BLOCK_COUNTER = 2000;
	
	public void startEnergyAccumTest(MainGUIcontroller mainGUIcontroller, 
									 SB2 currentSB2reference,
									 OmicronComm oc, 
									 YokogawaComm yc, 
									 int hour, 
									 int min, 
									 int sec, 
									 int interval_ms) {
		startEnergyAccumProcess();
		
		energyAccumTestThread = new Thread(() -> {
			while (!Thread.currentThread().isInterrupted()) {
				//    ______               __             ______          ______                                    
				//   /      \             /  |           /      \        /      \                                   
				//  /$$$$$$  |  ______   _$$ |_         /$$$$$$  |      /$$$$$$  | _____  ____    ______    _______ 
				//  $$ \__$$/  /      \ / $$   |        $$$  \$$ |      $$ |__$$ |/     \/    \  /      \  /       |
				//  $$      \ /$$$$$$  |$$$$$$/         $$$$  $$ |      $$    $$ |$$$$$$ $$$$  |/$$$$$$  |/$$$$$$$/ 
				//   $$$$$$  |$$    $$ |  $$ | __       $$ $$ $$ |      $$$$$$$$ |$$ | $$ | $$ |$$ |  $$ |$$      \ 
				//  /  \__$$ |$$$$$$$$/   $$ |/  |      $$ \$$$$ |      $$ |  $$ |$$ | $$ | $$ |$$ |__$$ | $$$$$$  |
				//  $$    $$/ $$       |  $$  $$/       $$   $$$/       $$ |  $$ |$$ | $$ | $$ |$$    $$/ /     $$/ 
				//   $$$$$$/   $$$$$$$/    $$$$/         $$$$$$/        $$/   $$/ $$/  $$/  $$/ $$$$$$$/  $$$$$$$/  
				//                                                                              $$ |                
				//                                                                              $$ |                
				//                                                                              $$/                 
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
				mainGUIcontroller.modifyEnergyAccumTestProgressIndicatorAndLabel(true, temp);
				
				
				//   _______                                   __            __      __         __                 
				//  /       \                                 /  |          /  \    /  |       /  |                
				//  $$$$$$$  |  ______    _______   ______   _$$ |_         $$  \  /$$/______  $$ |   __   ______  
				//  $$ |__$$ | /      \  /       | /      \ / $$   |         $$  \/$$//      \ $$ |  /  | /      \ 
				//  $$    $$< /$$$$$$  |/$$$$$$$/ /$$$$$$  |$$$$$$/           $$  $$//$$$$$$  |$$ |_/$$/ /$$$$$$  |
				//  $$$$$$$  |$$    $$ |$$      \ $$    $$ |  $$ | __          $$$$/ $$ |  $$ |$$   $$<  $$ |  $$ |
				//  $$ |  $$ |$$$$$$$$/  $$$$$$  |$$$$$$$$/   $$ |/  |          $$ | $$ \__$$ |$$$$$$  \ $$ \__$$ |
				//  $$ |  $$ |$$       |/     $$/ $$       |  $$  $$/           $$ | $$    $$/ $$ | $$  |$$    $$/ 
				//  $$/   $$/  $$$$$$$/ $$$$$$$/   $$$$$$$/    $$$$/            $$/   $$$$$$/  $$/   $$/  $$$$$$/  
				//                                                                                                 
				yc.resetIntegration();
				mainGUIcontroller.modifyEnergyAccumTestProgressIndicatorAndLabel(true, "Resetting Yokogawa Integration...");


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
				mainGUIcontroller.handleSendGetNextExpectedUDPSequenceNumberButton(null);
				mainGUIcontroller.modifyEnergyAccumTestProgressIndicatorAndLabel(true, "Getting Next UDP Sequence Number...");


				if (Thread.currentThread().isInterrupted()) break;	// Interrupt check


				//   _______   ________   ______   ________  ________      ________  __    __  ________  _______    ______   __      __ 
				//  /       \ /        | /      \ /        |/        |    /        |/  \  /  |/        |/       \  /      \ /  \    /  |
				//  $$$$$$$  |$$$$$$$$/ /$$$$$$  |$$$$$$$$/ $$$$$$$$/     $$$$$$$$/ $$  \ $$ |$$$$$$$$/ $$$$$$$  |/$$$$$$  |$$  \  /$$/ 
				//  $$ |__$$ |$$ |__    $$ \__$$/ $$ |__       $$ |       $$ |__    $$$  \$$ |$$ |__    $$ |__$$ |$$ | _$$/  $$  \/$$/  
				//  $$    $$< $$    |   $$      \ $$    |      $$ |       $$    |   $$$$  $$ |$$    |   $$    $$< $$ |/    |  $$  $$/   
				//  $$$$$$$  |$$$$$/     $$$$$$  |$$$$$/       $$ |       $$$$$/    $$ $$ $$ |$$$$$/    $$$$$$$  |$$ |$$$$ |   $$$$/    
				//  $$ |  $$ |$$ |_____ /  \__$$ |$$ |_____    $$ |       $$ |_____ $$ |$$$$ |$$ |_____ $$ |  $$ |$$ \__$$ |    $$ |    
				//  $$ |  $$ |$$       |$$    $$/ $$       |   $$ |______ $$       |$$ | $$$ |$$       |$$ |  $$ |$$    $$/     $$ |    
				//  $$/   $$/ $$$$$$$$/  $$$$$$/  $$$$$$$$/    $$//      |$$$$$$$$/ $$/   $$/ $$$$$$$$/ $$/   $$/  $$$$$$/      $$/     
				//                                                $$$$$$/                                                               
				mainGUIcontroller.handleSendResetEnergyButton(null);
				mainGUIcontroller.modifyEnergyAccumTestProgressIndicatorAndLabel(true, "Resetting Breaker Energy...");

				
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
					mainGUIcontroller.modifyEnergyAccumTestProgressIndicatorAndLabel(true, "Waiting (1.5s)...");
					Thread.sleep(1500);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					e.printStackTrace();
					break;
				}
				

				//    ______   __                            __              _______                                 __                                   ______  
				//   /      \ /  |                          /  |            /       \                               /  |                                 /      \ 
				//  /$$$$$$  |$$ |____    ______    _______ $$ |   __       $$$$$$$  |  ______    ______    ______  $$ |   __   ______    ______        /$$$$$$  |
				//  $$ |  $$/ $$      \  /      \  /       |$$ |  /  |      $$ |__$$ | /      \  /      \  /      \ $$ |  /  | /      \  /      \       $$$  \$$ |
				//  $$ |      $$$$$$$  |/$$$$$$  |/$$$$$$$/ $$ |_/$$/       $$    $$< /$$$$$$  |/$$$$$$  | $$$$$$  |$$ |_/$$/ /$$$$$$  |/$$$$$$  |      $$$$  $$ |
				//  $$ |   __ $$ |  $$ |$$    $$ |$$ |      $$   $$<        $$$$$$$  |$$ |  $$/ $$    $$ | /    $$ |$$   $$<  $$    $$ |$$ |  $$/       $$ $$ $$ |
				//  $$ \__/  |$$ |  $$ |$$$$$$$$/ $$ \_____ $$$$$$  \       $$ |__$$ |$$ |      $$$$$$$$/ /$$$$$$$ |$$$$$$  \ $$$$$$$$/ $$ |            $$ \$$$$ |
				//  $$    $$/ $$ |  $$ |$$       |$$       |$$ | $$  |      $$    $$/ $$ |      $$       |$$    $$ |$$ | $$  |$$       |$$ |            $$   $$$/ 
				//   $$$$$$/  $$/   $$/  $$$$$$$/  $$$$$$$/ $$/   $$/       $$$$$$$/  $$/        $$$$$$$/  $$$$$$$/ $$/   $$/  $$$$$$$/ $$/              $$$$$$/  
				//                                                                                                                                                
				// Check that breaker energy is at 0
				mainGUIcontroller.modifyEnergyAccumTestProgressIndicatorAndLabel(true, "Making Sure Breaker Energy is Resetted...");
				double ph0_active_energy = Double.parseDouble(currentSB2reference.getIndividualState("phase_A_active_energy").getValue());
				double ph0_reactive_energy = Double.parseDouble(currentSB2reference.getIndividualState("phase_A_reactive_energy").getValue());
				double ph0_app_energy = Double.parseDouble(currentSB2reference.getIndividualState("phase_A_app_energy").getValue());
				double ph1_active_energy = Double.parseDouble(currentSB2reference.getIndividualState("phase_B_active_energy").getValue());
				double ph1_reactive_energy = Double.parseDouble(currentSB2reference.getIndividualState("phase_B_reactive_energy").getValue());
				double ph1_app_energy = Double.parseDouble(currentSB2reference.getIndividualState("phase_B_app_energy").getValue());
				if ((ph0_active_energy >= 10000) &&
					(ph0_reactive_energy >= 10000) &&
					(ph0_app_energy >= 10000) &&
					(ph1_active_energy >= 10000) &&
					(ph1_reactive_energy >= 10000) &&
					(ph1_app_energy >= 10000)) {
					mainGUIcontroller.modifyEnergyAccumTestProgressIndicatorAndLabel(true, "Breaker Energy NOT Resetted...");
					continue;	// Re-reset energy
				}
				

				//    ______   __                            __              __      __         __                         ______  
				//   /      \ /  |                          /  |            /  \    /  |       /  |                       /      \ 
				//  /$$$$$$  |$$ |____    ______    _______ $$ |   __       $$  \  /$$/______  $$ |   __   ______        /$$$$$$  |
				//  $$ |  $$/ $$      \  /      \  /       |$$ |  /  |       $$  \/$$//      \ $$ |  /  | /      \       $$$  \$$ |
				//  $$ |      $$$$$$$  |/$$$$$$  |/$$$$$$$/ $$ |_/$$/         $$  $$//$$$$$$  |$$ |_/$$/ /$$$$$$  |      $$$$  $$ |
				//  $$ |   __ $$ |  $$ |$$    $$ |$$ |      $$   $$<           $$$$/ $$ |  $$ |$$   $$<  $$ |  $$ |      $$ $$ $$ |
				//  $$ \__/  |$$ |  $$ |$$$$$$$$/ $$ \_____ $$$$$$  \           $$ | $$ \__$$ |$$$$$$  \ $$ \__$$ |      $$ \$$$$ |
				//  $$    $$/ $$ |  $$ |$$       |$$       |$$ | $$  |          $$ | $$    $$/ $$ | $$  |$$    $$/       $$   $$$/ 
				//   $$$$$$/  $$/   $$/  $$$$$$$/  $$$$$$$/ $$/   $$/           $$/   $$$$$$/  $$/   $$/  $$$$$$/         $$$$$$/  
				//                                                                                                                 
				// Check that yoko energy is at 0
				mainGUIcontroller.modifyEnergyAccumTestProgressIndicatorAndLabel(true, "Making Sure Yokogawa Energy is Resetted...");
				double[] currentYokoReadings = yc.getPh0Ph1AllValAtOnce();
				if (Thread.currentThread().isInterrupted()) break;	// Interrupt check
				//								vvv This number because when Yoko increment is uninitialized, it will output NaN or INF which is parsed into 1E120
				if (!(currentYokoReadings[8] >= 100000000000000000000000000000000.0) &&			// phA WH
					!(currentYokoReadings[9] >= 100000000000000000000000000000000.0) &&			// phA WHP
					!(currentYokoReadings[10] >= 100000000000000000000000000000000.0) &&		// phA WHM
					!(currentYokoReadings[11] >= 100000000000000000000000000000000.0) &&		// phA WS
					!(currentYokoReadings[12] >= 100000000000000000000000000000000.0) &&		// phA WQ
					!(currentYokoReadings[21] >= 100000000000000000000000000000000.0) &&		// phB WH
					!(currentYokoReadings[22] >= 100000000000000000000000000000000.0) &&		// phB WHP
					!(currentYokoReadings[23] >= 100000000000000000000000000000000.0) &&		// phB WHM
					!(currentYokoReadings[24] >= 100000000000000000000000000000000.0) &&		// phB WS
					!(currentYokoReadings[25] >= 100000000000000000000000000000000.0)) {		// phB WQ
					mainGUIcontroller.modifyEnergyAccumTestProgressIndicatorAndLabel(true, "Yokogawa NOT Resetted...");
					continue;	// Re-reset energy
				}
				
				
				if (Thread.currentThread().isInterrupted()) break;	// Interrupt check
				

				mainGUIcontroller.modifyEnergyAccumTestProgressIndicatorAndLabel(true, "Setting Yokogawa Integration Timer...");
				yc.setIntegrationTimer(hour, min, sec);


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
					Thread.sleep(500);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					e.printStackTrace();
					break;
				}


				if (Thread.currentThread().isInterrupted()) break;	// Interrupt check
				

				//   __      __  ______          ______    __                            __            ______              __     
				//  /  \    /  |/      \        /      \  /  |                          /  |          /      |            /  |    
				//  $$  \  /$$//$$$$$$  |      /$$$$$$  |_$$ |_     ______    ______   _$$ |_         $$$$$$/  _______   _$$ |_   
				//   $$  \/$$/ $$ |  $$/       $$ \__$$// $$   |   /      \  /      \ / $$   |          $$ |  /       \ / $$   |  
				//    $$  $$/  $$ |            $$      \$$$$$$/    $$$$$$  |/$$$$$$  |$$$$$$/           $$ |  $$$$$$$  |$$$$$$/   
				//     $$$$/   $$ |   __        $$$$$$  | $$ | __  /    $$ |$$ |  $$/   $$ | __         $$ |  $$ |  $$ |  $$ | __ 
				//      $$ |   $$ \__/  |      /  \__$$ | $$ |/  |/$$$$$$$ |$$ |        $$ |/  |       _$$ |_ $$ |  $$ |  $$ |/  |
				//      $$ |   $$    $$/       $$    $$/  $$  $$/ $$    $$ |$$ |        $$  $$/       / $$   |$$ |  $$ |  $$  $$/ 
				//      $$/     $$$$$$/         $$$$$$/    $$$$/   $$$$$$$/ $$/          $$$$/        $$$$$$/ $$/   $$/    $$$$/  
				//                                                                                                                
				mainGUIcontroller.modifyEnergyAccumTestProgressIndicatorAndLabel(true, "Starting Yoko Integration...");
				yc.startIntegration();
				energyAccumTestIsRunning = true;


				if (Thread.currentThread().isInterrupted()) break;	// Interrupt check
				

				//    ______               __              __     ______          ______                                    
				//   /      \             /  |           _/  |   /      \        /      \                                   
				//  /$$$$$$  |  ______   _$$ |_         / $$ |  /$$$$$$  |      /$$$$$$  | _____  ____    ______    _______ 
				//  $$ \__$$/  /      \ / $$   |        $$$$ |  $$$  \$$ |      $$ |__$$ |/     \/    \  /      \  /       |
				//  $$      \ /$$$$$$  |$$$$$$/           $$ |  $$$$  $$ |      $$    $$ |$$$$$$ $$$$  |/$$$$$$  |/$$$$$$$/ 
				//   $$$$$$  |$$    $$ |  $$ | __         $$ |  $$ $$ $$ |      $$$$$$$$ |$$ | $$ | $$ |$$ |  $$ |$$      \ 
				//  /  \__$$ |$$$$$$$$/   $$ |/  |       _$$ |_ $$ \$$$$ |      $$ |  $$ |$$ | $$ | $$ |$$ |__$$ | $$$$$$  |
				//  $$    $$/ $$       |  $$  $$/       / $$   |$$   $$$/       $$ |  $$ |$$ | $$ | $$ |$$    $$/ /     $$/ 
				//   $$$$$$/   $$$$$$$/    $$$$/        $$$$$$/  $$$$$$/        $$/   $$/ $$/  $$/  $$/ $$$$$$$/  $$$$$$$/  
				//                                                                                      $$ |                
				//                                                                                      $$ |                
				//                                                                                      $$/                 
				oc.modifyOmicronValues(mainGUIcontroller.sbOmicYokoOmicSettingsPh0Voltage, 
						   			   mainGUIcontroller.sbOmicYokoOmicSettingsPh0VoltagePhase, 
						   			   mainGUIcontroller.sbOmicYokoOmicSettingsPh0Current, 
						   			   mainGUIcontroller.sbOmicYokoOmicSettingsPh0CurrentPhase, 
						   			   mainGUIcontroller.sbOmicYokoOmicSettingsPh1Voltage, 
						   			   mainGUIcontroller.sbOmicYokoOmicSettingsPh1VoltagePhase, 
						   			   mainGUIcontroller.sbOmicYokoOmicSettingsPh1Current, 
						   			   mainGUIcontroller.sbOmicYokoOmicSettingsPh1CurrentPhase);
				temp = "Setting Omicron to: " + 
					   mainGUIcontroller.sbOmicYokoOmicSettingsPh0Voltage + "@" + mainGUIcontroller.sbOmicYokoOmicSettingsPh0VoltagePhase + 
					   ",0@" + mainGUIcontroller.sbOmicYokoOmicSettingsPh0CurrentPhase + 
					   "; " + 
					   mainGUIcontroller.sbOmicYokoOmicSettingsPh1Voltage + "@" + mainGUIcontroller.sbOmicYokoOmicSettingsPh1VoltagePhase + 
					   "0@" + mainGUIcontroller.sbOmicYokoOmicSettingsPh1CurrentPhase;
				mainGUIcontroller.modifyEnergyAccumTestProgressIndicatorAndLabel(true, temp);


				if (Thread.currentThread().isInterrupted()) break;	// Interrupt check

				
				//    ______    __                            __            _______                                 __                            ________                     __     
				//   /      \  /  |                          /  |          /       \                               /  |                          /        |                   /  |    
				//  /$$$$$$  |_$$ |_     ______    ______   _$$ |_         $$$$$$$  | __    __  _______   _______  $$/  _______    ______        $$$$$$$$/______    _______  _$$ |_   
				//  $$ \__$$// $$   |   /      \  /      \ / $$   |        $$ |__$$ |/  |  /  |/       \ /       \ /  |/       \  /      \          $$ | /      \  /       |/ $$   |  
				//  $$      \$$$$$$/    $$$$$$  |/$$$$$$  |$$$$$$/         $$    $$< $$ |  $$ |$$$$$$$  |$$$$$$$  |$$ |$$$$$$$  |/$$$$$$  |         $$ |/$$$$$$  |/$$$$$$$/ $$$$$$/   
				//   $$$$$$  | $$ | __  /    $$ |$$ |  $$/   $$ | __       $$$$$$$  |$$ |  $$ |$$ |  $$ |$$ |  $$ |$$ |$$ |  $$ |$$ |  $$ |         $$ |$$    $$ |$$      \   $$ | __ 
				//  /  \__$$ | $$ |/  |/$$$$$$$ |$$ |        $$ |/  |      $$ |  $$ |$$ \__$$ |$$ |  $$ |$$ |  $$ |$$ |$$ |  $$ |$$ \__$$ |         $$ |$$$$$$$$/  $$$$$$  |  $$ |/  |
				//  $$    $$/  $$  $$/ $$    $$ |$$ |        $$  $$/       $$ |  $$ |$$    $$/ $$ |  $$ |$$ |  $$ |$$ |$$ |  $$ |$$    $$ |         $$ |$$       |/     $$/   $$  $$/ 
				//   $$$$$$/    $$$$/   $$$$$$$/ $$/          $$$$/        $$/   $$/  $$$$$$/  $$/   $$/ $$/   $$/ $$/ $$/   $$/  $$$$$$$ |         $$/  $$$$$$$/ $$$$$$$/     $$$$/  
				//                                                                                                               /  \__$$ |                                           
				//                                                                                                               $$    $$/                                            
				//                                                                                                                $$$$$$/                                             
				final int WAIT_FOR_UDP_COMMAND_MS = 1000;
				final int WAIT_FOR_YOKO_BREAKER_READING_MS = 1500;
				int end_timer_ms = sec * 1000 + min * 60 * 1000 + hour * 60 * 60 * 1000;
				int counter_ms = 0;
				/*
				 * This while loop will run every 500+1000+1000 ms, and every interval_ms ms, it will use python file to 
				 * write to csv and plot to graph. 
				 */
				while ((counter_ms <= end_timer_ms) && !Thread.currentThread().isInterrupted()) {
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
					/*Response Checker*/	pre = MainGUIcontroller.currentSB2.getResponseCounter();
					/*Response Checker*/	post = pre;
					// Get next UDP
					mainGUIcontroller.handleSendGetNextExpectedUDPSequenceNumberButton(null);
					// Logging
					mainGUIcontroller.modifyEnergyAccumTestProgressIndicatorAndLabel(true, "Getting next UDP...");
					mainGUIcontroller.systemOutPrintlnAndPrintLog("runAutoCalibration(): Getting next UDP...", LogLevel.INFO);
					/*Response Checker*/	int blocker_counter = 0;
					/*Response Checker*/	while (pre == post) {		// Blocking function to wait for response counter to update
					/*Response Checker*/		post = MainGUIcontroller.currentSB2.getResponseCounter();
					/*Response Checker*/		// Wait
					/*Response Checker*/		try {
					/*Response Checker*/			Thread.sleep(10);
					/*Response Checker*/		} catch (InterruptedException e) {
					/*Response Checker*/			Thread.currentThread().interrupt();
					/*Response Checker*/			e.printStackTrace();
					/*Response Checker*/		}
					/*Response Checker*/		blocker_counter += 10;
					/*Response Checker*/		if (blocker_counter >= MAX_BLOCK_COUNTER) {
					/*Response Checker*/			System.out.println("MAX_BLOCK_COUNTER hit.");
					/*Response Checker*/			mainGUIcontroller.modifyEnergyAccumTestProgressIndicatorAndLabel(false, "Getting Next UDP Failed. Proceeding like it didn't happen...");
					/*Response Checker*/			break;
					/*Response Checker*/		}
					/*Response Checker*/	}
					/*Response Checker*/	try {	// Sleep the remaining 
					/*Response Checker*/		Thread.sleep((WAIT_FOR_UDP_COMMAND_MS - blocker_counter) >= 0 ? (WAIT_FOR_UDP_COMMAND_MS - blocker_counter) : 0);
					/*Response Checker*/	} catch (InterruptedException e) {
					/*Response Checker*/		Thread.currentThread().interrupt();
					/*Response Checker*/		e.printStackTrace();
					/*Response Checker*/	}
					/*Response Checker*/	pre = -1;		// Reset flags
					/*Response Checker*/	post = -1;		// Reset flags
					counter_ms += blocker_counter;
					

					if (Thread.currentThread().isInterrupted()) break;	// Interrupt check
					
					
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
					// Re-grab data
					currentYokoReadings = yc.getPh0Ph1AllValAtOnce();
					mainGUIcontroller.handleSendGetDeviceStatusButton(null);
					// Wait for 1.5s
					try {
						mainGUIcontroller.modifyEnergyAccumTestProgressIndicatorAndLabel(true, "Regrabbing Data...");
						Thread.sleep(WAIT_FOR_YOKO_BREAKER_READING_MS);
						counter_ms += WAIT_FOR_YOKO_BREAKER_READING_MS;
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
						e.printStackTrace();
						break;
					}
					ph0_active_energy = Double.parseDouble(currentSB2reference.getIndividualState("phase_A_active_energy").getValue());
//					ph0_reactive_energy = Double.parseDouble(currentSB2reference.getIndividualState("phase_A_reactive_energy").getValue());
//					ph0_app_energy = Double.parseDouble(currentSB2reference.getIndividualState("phase_A_app_energy").getValue());
					ph1_active_energy = Double.parseDouble(currentSB2reference.getIndividualState("phase_B_active_energy").getValue());
//					ph1_reactive_energy = Double.parseDouble(currentSB2reference.getIndividualState("phase_B_reactive_energy").getValue());
//					ph1_app_energy = Double.parseDouble(currentSB2reference.getIndividualState("phase_B_app_energy").getValue());


					if (Thread.currentThread().isInterrupted()) break;	// Interrupt check
					
					
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
					// Write to csv and plot (using python function)
					mainGUIcontroller.modifyEnergyAccumTestProgressIndicatorAndLabel(true, "Plotting graph...");
					String toSendToPython;
					toSendToPython = "";
					toSendToPython += counter_ms;
					toSendToPython += ",";
					toSendToPython += currentYokoReadings[8];
					toSendToPython += ",";
					toSendToPython += ph0_active_energy * 1000;
					toSendToPython += ",";
					toSendToPython += currentYokoReadings[21];
					toSendToPython += ",";
					toSendToPython += ph1_active_energy * 1000;
					try {
						pythonAppInputSender(energyAccumProcess, toSendToPython);
					} catch (IOException e) {
						e.printStackTrace();
					}
					
					
					if (Thread.currentThread().isInterrupted()) break;	// Interrupt check
					
					
					//   __       __            __    __             ______                           ______              __                                              __ 
					//  /  |  _  /  |          /  |  /  |           /      \                         /      |            /  |                                            /  |
					//  $$ | / \ $$ |  ______  $$/  _$$ |_         /$$$$$$  |______    ______        $$$$$$/  _______   _$$ |_     ______    ______   __     __  ______  $$ |
					//  $$ |/$  \$$ | /      \ /  |/ $$   |        $$ |_ $$//      \  /      \         $$ |  /       \ / $$   |   /      \  /      \ /  \   /  |/      \ $$ |
					//  $$ /$$$  $$ | $$$$$$  |$$ |$$$$$$/         $$   |  /$$$$$$  |/$$$$$$  |        $$ |  $$$$$$$  |$$$$$$/   /$$$$$$  |/$$$$$$  |$$  \ /$$/ $$$$$$  |$$ |
					//  $$ $$/$$ $$ | /    $$ |$$ |  $$ | __       $$$$/   $$ |  $$ |$$ |  $$/         $$ |  $$ |  $$ |  $$ | __ $$    $$ |$$ |  $$/  $$  /$$/  /    $$ |$$ |
					//  $$$$/  $$$$ |/$$$$$$$ |$$ |  $$ |/  |      $$ |    $$ \__$$ |$$ |             _$$ |_ $$ |  $$ |  $$ |/  |$$$$$$$$/ $$ |        $$ $$/  /$$$$$$$ |$$ |
					//  $$$/    $$$ |$$    $$ |$$ |  $$  $$/       $$ |    $$    $$/ $$ |            / $$   |$$ |  $$ |  $$  $$/ $$       |$$ |         $$$/   $$    $$ |$$ |
					//  $$/      $$/  $$$$$$$/ $$/    $$$$/        $$/      $$$$$$/  $$/             $$$$$$/ $$/   $$/    $$$$/   $$$$$$$/ $$/           $/     $$$$$$$/ $$/ 
					//                                                                                                                                                       
					// Wait for the remaining time so we plot data at the right interval
					try {
						mainGUIcontroller.modifyEnergyAccumTestProgressIndicatorAndLabel(true, "Just sitting here waiting to collect data again...");
						int remaining = interval_ms - WAIT_FOR_UDP_COMMAND_MS - WAIT_FOR_YOKO_BREAKER_READING_MS;
						Thread.sleep(remaining);
						counter_ms += remaining;
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
						e.printStackTrace();
						break;
					}
				} // End of main test while loop
				

				if (Thread.currentThread().isInterrupted()) break;	// Interrupt check
				
				
				//    ______               __             ______          ______                                    
				//   /      \             /  |           /      \        /      \                                   
				//  /$$$$$$  |  ______   _$$ |_         /$$$$$$  |      /$$$$$$  | _____  ____    ______    _______ 
				//  $$ \__$$/  /      \ / $$   |        $$$  \$$ |      $$ |__$$ |/     \/    \  /      \  /       |
				//  $$      \ /$$$$$$  |$$$$$$/         $$$$  $$ |      $$    $$ |$$$$$$ $$$$  |/$$$$$$  |/$$$$$$$/ 
				//   $$$$$$  |$$    $$ |  $$ | __       $$ $$ $$ |      $$$$$$$$ |$$ | $$ | $$ |$$ |  $$ |$$      \ 
				//  /  \__$$ |$$$$$$$$/   $$ |/  |      $$ \$$$$ |      $$ |  $$ |$$ | $$ | $$ |$$ |__$$ | $$$$$$  |
				//  $$    $$/ $$       |  $$  $$/       $$   $$$/       $$ |  $$ |$$ | $$ | $$ |$$    $$/ /     $$/ 
				//   $$$$$$/   $$$$$$$/    $$$$/         $$$$$$/        $$/   $$/ $$/  $$/  $$/ $$$$$$$/  $$$$$$$/  
				//                                                                              $$ |                
				//                                                                              $$ |                
				//                                                                              $$/                 
				oc.modifyOmicronValues(mainGUIcontroller.sbOmicYokoOmicSettingsPh0Voltage, 
						   			   mainGUIcontroller.sbOmicYokoOmicSettingsPh0VoltagePhase, 
						   			   0, 
						   			   mainGUIcontroller.sbOmicYokoOmicSettingsPh0CurrentPhase, 
						   			   mainGUIcontroller.sbOmicYokoOmicSettingsPh1Voltage, 
						   			   mainGUIcontroller.sbOmicYokoOmicSettingsPh1VoltagePhase, 
						   			   0, 
						   			   mainGUIcontroller.sbOmicYokoOmicSettingsPh1CurrentPhase);
				// Set Indicator
				temp = "Setting Omicron to: " + 
					   mainGUIcontroller.sbOmicYokoOmicSettingsPh0Voltage + "@" + mainGUIcontroller.sbOmicYokoOmicSettingsPh0VoltagePhase + 
					   ",0@" + mainGUIcontroller.sbOmicYokoOmicSettingsPh0CurrentPhase + 
					   "; " + 
					   mainGUIcontroller.sbOmicYokoOmicSettingsPh1Voltage + "@" + mainGUIcontroller.sbOmicYokoOmicSettingsPh1VoltagePhase + 
					   "0@" + mainGUIcontroller.sbOmicYokoOmicSettingsPh1CurrentPhase;
				mainGUIcontroller.modifyEnergyAccumTestProgressIndicatorAndLabel(true, temp);
				
				
				if (Thread.currentThread().isInterrupted()) break;	// Interrupt check
				
				
				//   __      __  ______          ______    __                                ______              __     
				//  /  \    /  |/      \        /      \  /  |                              /      |            /  |    
				//  $$  \  /$$//$$$$$$  |      /$$$$$$  |_$$ |_     ______    ______        $$$$$$/  _______   _$$ |_   
				//   $$  \/$$/ $$ |  $$/       $$ \__$$// $$   |   /      \  /      \         $$ |  /       \ / $$   |  
				//    $$  $$/  $$ |            $$      \$$$$$$/   /$$$$$$  |/$$$$$$  |        $$ |  $$$$$$$  |$$$$$$/   
				//     $$$$/   $$ |   __        $$$$$$  | $$ | __ $$ |  $$ |$$ |  $$ |        $$ |  $$ |  $$ |  $$ | __ 
				//      $$ |   $$ \__/  |      /  \__$$ | $$ |/  |$$ \__$$ |$$ |__$$ |       _$$ |_ $$ |  $$ |  $$ |/  |
				//      $$ |   $$    $$/       $$    $$/  $$  $$/ $$    $$/ $$    $$/       / $$   |$$ |  $$ |  $$  $$/ 
				//      $$/     $$$$$$/         $$$$$$/    $$$$/   $$$$$$/  $$$$$$$/        $$$$$$/ $$/   $$/    $$$$/  
				//                                                          $$ |                                        
				//                                                          $$ |                                        
				//                                                          $$/                                         
				mainGUIcontroller.modifyEnergyAccumTestProgressIndicatorAndLabel(true, "Stopping integration...");
				yc.stopIntegration();


				if (Thread.currentThread().isInterrupted()) break;	// Interrupt check


				//   ________  __                      __        _______   __              __     
				//  /        |/  |                    /  |      /       \ /  |            /  |    
				//  $$$$$$$$/ $$/  _______    ______  $$ |      $$$$$$$  |$$ |  ______   _$$ |_   
				//  $$ |__    /  |/       \  /      \ $$ |      $$ |__$$ |$$ | /      \ / $$   |  
				//  $$    |   $$ |$$$$$$$  | $$$$$$  |$$ |      $$    $$/ $$ |/$$$$$$  |$$$$$$/   
				//  $$$$$/    $$ |$$ |  $$ | /    $$ |$$ |      $$$$$$$/  $$ |$$ |  $$ |  $$ | __ 
				//  $$ |      $$ |$$ |  $$ |/$$$$$$$ |$$ |      $$ |      $$ |$$ \__$$ |  $$ |/  |
				//  $$ |      $$ |$$ |  $$ |$$    $$ |$$ |      $$ |      $$ |$$    $$/   $$  $$/ 
				//  $$/       $$/ $$/   $$/  $$$$$$$/ $$/       $$/       $$/  $$$$$$/     $$$$/  
				//                                                                                
				// Write to csv and plot (using python function)
				String toSendToPython;
				toSendToPython = "";
				toSendToPython += counter_ms;
				toSendToPython += ",";
				toSendToPython += currentYokoReadings[8];
				toSendToPython += ",";
				toSendToPython += ph0_active_energy * 1000;
				toSendToPython += ",";
				toSendToPython += currentYokoReadings[21];
				toSendToPython += ",";
				toSendToPython += ph1_active_energy * 1000;
				try {
					pythonAppInputSender(energyAccumProcess, toSendToPython);
					pythonAppInputSender(energyAccumProcess, "showplot");
					pythonAppInputSender(energyAccumProcess, "shutdown");
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				
				energyAccumTestIsRunning = false;
				mainGUIcontroller.modifyEnergyAccumTestProgressIndicatorAndLabel(false, "Test Ended. Checkout the beautiful plot! :)");


				// If we are here, it means we completed the test and ready to call it a day (interrupt thread) and go home (break)
				Thread.currentThread().interrupt();	// Just to be sure
				break;
			}

			yc.stopIntegration();	// Just to be safe
		});
		energyAccumTestThread.start();
	}
	
	public void stopEnergyAccumTest(MainGUIcontroller mainGUIcontroller, YokogawaComm yc) {
		if (energyAccumTestIsRunning) {
			killEnergyAccumTestThread();
			yc.stopIntegration();
			energyAccumTestIsRunning = false;
			
			try {
				pythonAppInputSender(energyAccumProcess, "showplot");
				pythonAppInputSender(energyAccumProcess, "shutdown");
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			mainGUIcontroller.modifyEnergyAccumTestProgressIndicatorAndLabel(false, "Test Stopped.");
		}
	}
	
	public void showPlot() {
		try {
			pythonAppInputSender(energyAccumProcess, "showplot");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void startEnergyAccumProcess() {
		energyAccumProcess = pythonAppTalker("python/energyAccumTestPlotter.py", "");
	}
	
	private void killEnergyAccumTestThread() {
		if (energyAccumTestThread != null && energyAccumTestThread.isAlive()) {
			energyAccumTestThread.interrupt();
			try {
				energyAccumTestThread.join();
			} catch (InterruptedException e) {
				// hi
			}
			energyAccumTestThread = null;
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
}
