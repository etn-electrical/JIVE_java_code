/******************************************************************************************************
 * 
 * 		Connected Solutions @ Eaton (Moon Township, PA)
 * 	
 * 		@author Jonathan Tan (JonathanTan@eaton.com)
 * 		@date 6/6/2023
 * 
 * ****************************************************************************************************
 * 
 * 		SB2.java
 * 		Purpose: This is a SB2 object that saves the state of the breaker we are currently talking
 * 				 to. The states we save are: GET_DEVICE_STATUS's info, and calibration param values.
 * 
 * 		For more info, checkout this project's confluence page: https://confluence-prod.tcc.etn.com/pages/viewpage.action?pageId=299547479
 * 
 * *****************************************************************************************************/

package eaton.cs.sb2fw.SBLCP_local_terminal.util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import javafx.util.Pair;

/**
 * The point of this class is just to save all the variables (I dont want all these variables 
 * in the main controller class, so messy!).
 */
public class SB2 {
	public SB2() {
		// Hi, I am a constructor that does nothing :)
	}
	
	// ###########################################################################
	// ---------------------------------------------------------------------------
	// 			Response Counter related functions
	// ---------------------------------------------------------------------------
	private int response_counter = -1;
	public void updateResponseCounter(int givenNumber) {
		response_counter = givenNumber;
	}
	public int getResponseCounter() {
		return response_counter;
	}
	// ---------------------------------------------------------------------------
	
	
	// ###########################################################################
	// ---------------------------------------------------------------------------
	// 			Below are EOL Calib Param stuff
	// ---------------------------------------------------------------------------
	// ###########################################################################
	
	// Variables (or how Java people like to call it, states) for device status
	private int breaker_state;
	private int primary_handle_status;
	private int path_status;
	private int meter_update_no;
	private int period;
	private double phase_A_line_frequency;
	private double phase_A_rms_voltage;
	private double phase_A_rms_current;
	private double phase_A_power_factor;
	private double phase_A_active_power;
	private double phase_A_reactive_power;
	private double phase_A_app_power;
	private double phase_A_active_energy;
	private double phase_A_reactive_energy;
	private double phase_A_app_energy;
	private double phase_A_reverse_active_energy;
	private double phase_A_reverse_reactive_energy;
	private double phase_A_reverse_app_energy;
	private double phase_B_line_frequency;
	private double phase_B_rms_voltage;
	private double phase_B_rms_current;
	private double phase_B_power_factor;
	private double phase_B_active_power;
	private double phase_B_reactive_power;
	private double phase_B_app_power;
	private double phase_B_active_energy;
	private double phase_B_reactive_energy;
	private double phase_B_app_energy;
	private double phase_B_reverse_active_energy;
	private double phase_B_reverse_reactive_energy;
	private double phase_B_reverse_app_energy;
	private int phase_A_Q1_act_energy;
	private int phase_A_Q2_act_energy;
	private int phase_A_Q3_act_energy;
	private int phase_A_Q4_act_energy;
	private int phase_A_Q5_act_energy;
	private int phase_A_Q1_reactive_energy;
	private int phase_A_Q2_reactive_energy;
	private int phase_A_Q3_reactive_energy;
	private int phase_A_Q4_reactive_energy;
	private int phase_A_Q5_reactive_energy;
	private int phase_A_Q1_app_energy;
	private int phase_A_Q2_app_energy;
	private int phase_A_Q3_app_energy;
	private int phase_A_Q4_app_energy;
	private int phase_A_Q5_app_energy;
	private int phase_B_Q1_act_energy;
	private int phase_B_Q2_act_energy;
	private int phase_B_Q3_act_energy;
	private int phase_B_Q4_act_energy;
	private int phase_B_Q5_act_energy;
	private int phase_B_Q1_reactive_energy;
	private int phase_B_Q2_reactive_energy;
	private int phase_B_Q3_reactive_energy;
	private int phase_B_Q4_reactive_energy;
	private int phase_B_Q5_reactive_energy;
	private int phase_B_Q1_app_energy;
	private int phase_B_Q2_app_energy;
	private int phase_B_Q3_app_energy;
	private int phase_B_Q4_app_energy;
	private int phase_B_Q5_app_energy;
	private double phase_to_phase_voltage;
	
	// Should not use
	private int deviceStatusMessageVersion = 0;	// A counter tell us what number of times we received new data from 
												// the GET_DEVICE_STATUS command
	
	public void updateAllBreakerStates(ArrayList<Pair<String, String>> givenArrLi) {
		for (int i = 0; i < givenArrLi.size(); ++i) {
			updateIndividualState(givenArrLi.get(i));
		}
	}

	public void updateIndividualState(Pair<String, String> givenPair) {
		if (givenPair.getKey().equals("breaker_state")) {
			try {
				int temp = Integer.parseInt(givenPair.getValue());
				breaker_state = temp;
			} catch (NumberFormatException e) {
				return;
			}
		} else if (givenPair.getKey().equals("primary_handle_status")) {
			try {
				int temp = Integer.parseInt(givenPair.getValue());
				primary_handle_status = temp;
			} catch (NumberFormatException e) {
				return;
			}
		} else if (givenPair.getKey().equals("path_status")) {
			try {
				int temp = Integer.parseInt(givenPair.getValue());
				path_status = temp;
			} catch (NumberFormatException e) {
				return;
			}
		} else if (givenPair.getKey().equals("meter_update_no")) {
			try {
				int temp = Integer.parseInt(givenPair.getValue());
				meter_update_no = temp;
			} catch (NumberFormatException e) {
				return;
			}
		} else if (givenPair.getKey().equals("period")) {
			try {
				int temp = Integer.parseInt(givenPair.getValue());
				period = temp;
			} catch (NumberFormatException e) {
				return;
			}
		} else if (givenPair.getKey().equals("phase_A_line_frequency")) {
			try {
				double temp = Double.parseDouble(givenPair.getValue());
				phase_A_line_frequency = temp;
			} catch (NumberFormatException e) {
				return;
			}
		} else if (givenPair.getKey().equals("phase_A_rms_voltage")) {
			try {
				double temp = Double.parseDouble(givenPair.getValue());
				phase_A_rms_voltage = temp;
			} catch (NumberFormatException e) {
				return;
			}
		} else if (givenPair.getKey().equals("phase_A_rms_current")) {
			try {
				double temp = Double.parseDouble(givenPair.getValue());
				phase_A_rms_current = temp;
			} catch (NumberFormatException e) {
				return;
			}
		} else if (givenPair.getKey().equals("phase_A_power_factor")) {
			try {
				double temp = Double.parseDouble(givenPair.getValue());
				phase_A_power_factor = temp;
			} catch (NumberFormatException e) {
				return;
			}
		} else if (givenPair.getKey().equals("phase_A_active_power")) {
			try {
				double temp = Double.parseDouble(givenPair.getValue());
				phase_A_active_power = temp;
			} catch (NumberFormatException e) {
				return;
			}
		} else if (givenPair.getKey().equals("phase_A_reactive_power")) {
			try {
				double temp = Double.parseDouble(givenPair.getValue());
				phase_A_reactive_power = temp;
			} catch (NumberFormatException e) {
				return;
			}
		} else if (givenPair.getKey().equals("phase_A_app_power")) {
			try {
				double temp = Double.parseDouble(givenPair.getValue());
				phase_A_app_power = temp;
			} catch (NumberFormatException e) {
				return;
			}
		} else if (givenPair.getKey().equals("phase_A_active_energy")) {
			try {
				double temp = Double.parseDouble(givenPair.getValue());
				phase_A_active_energy = temp;
			} catch (NumberFormatException e) {
				return;
			}
		} else if (givenPair.getKey().equals("phase_A_reactive_energy")) {
			try {
				double temp = Double.parseDouble(givenPair.getValue());
				phase_A_reactive_energy = temp;
			} catch (NumberFormatException e) {
				return;
			}
		} else if (givenPair.getKey().equals("phase_A_app_energy")) {
			try {
				double temp = Double.parseDouble(givenPair.getValue());
				phase_A_app_energy = temp;
			} catch (NumberFormatException e) {
				return;
			}
		} else if (givenPair.getKey().equals("phase_A_reverse_active_energy")) {
			try {
				double temp = Double.parseDouble(givenPair.getValue());
				phase_A_reverse_active_energy = temp;
			} catch (NumberFormatException e) {
				return;
			}
		} else if (givenPair.getKey().equals("phase_A_reverse_reactive_energy")) {
			try {
				double temp = Double.parseDouble(givenPair.getValue());
				phase_A_reverse_reactive_energy = temp;
			} catch (NumberFormatException e) {
				return;
			}
		} else if (givenPair.getKey().equals("phase_A_reverse_app_energy")) {
			try {
				double temp = Double.parseDouble(givenPair.getValue());
				phase_A_reverse_app_energy = temp;
			} catch (NumberFormatException e) {
				return;
			}
		} else if (givenPair.getKey().equals("phase_B_line_frequency")) {
			try {
				double temp = Double.parseDouble(givenPair.getValue());
				phase_B_line_frequency = temp;
			} catch (NumberFormatException e) {
				return;
			}
		} else if (givenPair.getKey().equals("phase_B_rms_voltage")) {
			try {
				double temp = Double.parseDouble(givenPair.getValue());
				phase_B_rms_voltage = temp;
			} catch (NumberFormatException e) {
				return;
			}
		} else if (givenPair.getKey().equals("phase_B_rms_current")) {
			try {
				double temp = Double.parseDouble(givenPair.getValue());
				phase_B_rms_current = temp;
			} catch (NumberFormatException e) {
				return;
			}
		} else if (givenPair.getKey().equals("phase_B_power_factor")) {
			try {
				double temp = Double.parseDouble(givenPair.getValue());
				phase_B_power_factor = temp;
			} catch (NumberFormatException e) {
				return;
			}
		} else if (givenPair.getKey().equals("phase_B_active_power")) {
			try {
				double temp = Double.parseDouble(givenPair.getValue());
				phase_B_active_power = temp;
			} catch (NumberFormatException e) {
				return;
			}
		} else if (givenPair.getKey().equals("phase_B_reactive_power")) {
			try {
				double temp = Double.parseDouble(givenPair.getValue());
				phase_B_reactive_power = temp;
			} catch (NumberFormatException e) {
				return;
			}
		} else if (givenPair.getKey().equals("phase_B_app_power")) {
			try {
				double temp = Double.parseDouble(givenPair.getValue());
				phase_B_app_power = temp;
			} catch (NumberFormatException e) {
				return;
			}
		} else if (givenPair.getKey().equals("phase_B_active_energy")) {
			try {
				double temp = Double.parseDouble(givenPair.getValue());
				phase_B_active_energy = temp;
			} catch (NumberFormatException e) {
				return;
			}
		} else if (givenPair.getKey().equals("phase_B_reactive_energy")) {
			try {
				double temp = Double.parseDouble(givenPair.getValue());
				phase_B_reactive_energy = temp;
			} catch (NumberFormatException e) {
				return;
			}
		} else if (givenPair.getKey().equals("phase_B_app_energy")) {
			try {
				double temp = Double.parseDouble(givenPair.getValue());
				phase_B_app_energy = temp;
			} catch (NumberFormatException e) {
				return;
			}
		} else if (givenPair.getKey().equals("phase_B_reverse_active_energy")) {
			try {
				double temp = Double.parseDouble(givenPair.getValue());
				phase_B_reverse_active_energy = temp;
			} catch (NumberFormatException e) {
				return;
			}
		} else if (givenPair.getKey().equals("phase_B_reverse_reactive_energy")) {
			try {
				double temp = Double.parseDouble(givenPair.getValue());
				phase_B_reverse_reactive_energy = temp;
			} catch (NumberFormatException e) {
				return;
			}
		} else if (givenPair.getKey().equals("phase_B_reverse_app_energy")) {
			try {
				double temp = Double.parseDouble(givenPair.getValue());
				phase_B_reverse_app_energy = temp;
			} catch (NumberFormatException e) {
				return;
			}
		} else if (givenPair.getKey().equals("phase_A_Q1_act_energy")) {
			try {
				int temp = Integer.parseInt(givenPair.getValue());
				phase_A_Q1_act_energy = temp;
			} catch (NumberFormatException e) {
				return;
			}
		} else if (givenPair.getKey().equals("phase_A_Q2_act_energy")) {
			try {
				int temp = Integer.parseInt(givenPair.getValue());
				phase_A_Q2_act_energy = temp;
			} catch (NumberFormatException e) {
				return;
			}
		} else if (givenPair.getKey().equals("phase_A_Q3_act_energy")) {
			try {
				int temp = Integer.parseInt(givenPair.getValue());
				phase_A_Q3_act_energy = temp;
			} catch (NumberFormatException e) {
				return;
			}
		} else if (givenPair.getKey().equals("phase_A_Q4_act_energy")) {
			try {
				int temp = Integer.parseInt(givenPair.getValue());
				phase_A_Q4_act_energy = temp;
			} catch (NumberFormatException e) {
				return;
			}
		} else if (givenPair.getKey().equals("phase_A_Q5_act_energy")) {
			try {
				int temp = Integer.parseInt(givenPair.getValue());
				phase_A_Q5_act_energy = temp;
			} catch (NumberFormatException e) {
				return;
			}
		} else if (givenPair.getKey().equals("phase_A_Q1_reactive_energy")) {
			try {
				int temp = Integer.parseInt(givenPair.getValue());
				phase_A_Q1_reactive_energy = temp;
			} catch (NumberFormatException e) {
				return;
			}
		} else if (givenPair.getKey().equals("phase_A_Q2_reactive_energy")) {
			try {
				int temp = Integer.parseInt(givenPair.getValue());
				phase_A_Q2_reactive_energy = temp;
			} catch (NumberFormatException e) {
				return;
			}
		} else if (givenPair.getKey().equals("phase_A_Q3_reactive_energy")) {
			try {
				int temp = Integer.parseInt(givenPair.getValue());
				phase_A_Q3_reactive_energy = temp;
			} catch (NumberFormatException e) {
				return;
			}
		} else if (givenPair.getKey().equals("phase_A_Q4_reactive_energy")) {
			try {
				int temp = Integer.parseInt(givenPair.getValue());
				phase_A_Q4_reactive_energy = temp;
			} catch (NumberFormatException e) {
				return;
			}
		} else if (givenPair.getKey().equals("phase_A_Q5_reactive_energy")) {
			try {
				int temp = Integer.parseInt(givenPair.getValue());
				phase_A_Q5_reactive_energy = temp;
			} catch (NumberFormatException e) {
				return;
			}
		} else if (givenPair.getKey().equals("phase_A_Q1_app_energy")) {
			try {
				int temp = Integer.parseInt(givenPair.getValue());
				phase_A_Q1_app_energy = temp;
			} catch (NumberFormatException e) {
				return;
			}
		} else if (givenPair.getKey().equals("phase_A_Q2_app_energy")) {
			try {
				int temp = Integer.parseInt(givenPair.getValue());
				phase_A_Q2_app_energy = temp;
			} catch (NumberFormatException e) {
				return;
			}
		} else if (givenPair.getKey().equals("phase_A_Q3_app_energy")) {
			try {
				int temp = Integer.parseInt(givenPair.getValue());
				phase_A_Q3_app_energy = temp;
			} catch (NumberFormatException e) {
				return;
			}
		} else if (givenPair.getKey().equals("phase_A_Q4_app_energy")) {
			try {
				int temp = Integer.parseInt(givenPair.getValue());
				phase_A_Q4_app_energy = temp;
			} catch (NumberFormatException e) {
				return;
			}
		} else if (givenPair.getKey().equals("phase_A_Q5_app_energy")) {
			try {
				int temp = Integer.parseInt(givenPair.getValue());
				phase_A_Q5_app_energy = temp;
			} catch (NumberFormatException e) {
				return;
			}
		} else if (givenPair.getKey().equals("phase_B_Q1_act_energy")) {
			try {
				int temp = Integer.parseInt(givenPair.getValue());
				phase_B_Q1_act_energy = temp;
			} catch (NumberFormatException e) {
				return;
			}
		} else if (givenPair.getKey().equals("phase_B_Q2_act_energy")) {
			try {
				int temp = Integer.parseInt(givenPair.getValue());
				phase_B_Q2_act_energy = temp;
			} catch (NumberFormatException e) {
				return;
			}
		} else if (givenPair.getKey().equals("phase_B_Q3_act_energy")) {
			try {
				int temp = Integer.parseInt(givenPair.getValue());
				phase_B_Q3_act_energy = temp;
			} catch (NumberFormatException e) {
				return;
			}
		} else if (givenPair.getKey().equals("phase_B_Q4_act_energy")) {
			try {
				int temp = Integer.parseInt(givenPair.getValue());
				phase_B_Q4_act_energy = temp;
			} catch (NumberFormatException e) {
				return;
			}
		} else if (givenPair.getKey().equals("phase_B_Q5_act_energy")) {
			try {
				int temp = Integer.parseInt(givenPair.getValue());
				phase_B_Q5_act_energy = temp;
			} catch (NumberFormatException e) {
				return;
			}
		} else if (givenPair.getKey().equals("phase_B_Q1_reactive_energy")) {
			try {
				int temp = Integer.parseInt(givenPair.getValue());
				phase_B_Q1_reactive_energy = temp;
			} catch (NumberFormatException e) {
				return;
			}
		} else if (givenPair.getKey().equals("phase_B_Q2_reactive_energy")) {
			try {
				int temp = Integer.parseInt(givenPair.getValue());
				phase_B_Q2_reactive_energy = temp;
			} catch (NumberFormatException e) {
				return;
			}
		} else if (givenPair.getKey().equals("phase_B_Q3_reactive_energy")) {
			try {
				int temp = Integer.parseInt(givenPair.getValue());
				phase_B_Q3_reactive_energy = temp;
			} catch (NumberFormatException e) {
				return;
			}
		} else if (givenPair.getKey().equals("phase_B_Q4_reactive_energy")) {
			try {
				int temp = Integer.parseInt(givenPair.getValue());
				phase_B_Q4_reactive_energy = temp;
			} catch (NumberFormatException e) {
				return;
			}
		} else if (givenPair.getKey().equals("phase_B_Q5_reactive_energy")) {
			try {
				int temp = Integer.parseInt(givenPair.getValue());
				phase_B_Q5_reactive_energy = temp;
			} catch (NumberFormatException e) {
				return;
			}
		} else if (givenPair.getKey().equals("phase_B_Q1_app_energy")) {
			try {
				int temp = Integer.parseInt(givenPair.getValue());
				phase_B_Q1_app_energy = temp;
			} catch (NumberFormatException e) {
				return;
			}
		} else if (givenPair.getKey().equals("phase_B_Q2_app_energy")) {
			try {
				int temp = Integer.parseInt(givenPair.getValue());
				phase_B_Q2_app_energy = temp;
			} catch (NumberFormatException e) {
				return;
			}
		} else if (givenPair.getKey().equals("phase_B_Q3_app_energy")) {
			try {
				int temp = Integer.parseInt(givenPair.getValue());
				phase_B_Q3_app_energy = temp;
			} catch (NumberFormatException e) {
				return;
			}
		} else if (givenPair.getKey().equals("phase_B_Q4_app_energy")) {
			try {
				int temp = Integer.parseInt(givenPair.getValue());
				phase_B_Q4_app_energy = temp;
			} catch (NumberFormatException e) {
				return;
			}
		} else if (givenPair.getKey().equals("phase_B_Q5_app_energy")) {
			try {
				int temp = Integer.parseInt(givenPair.getValue());
				phase_B_Q5_app_energy = temp;
			} catch (NumberFormatException e) {
				return;
			}
		} else if (givenPair.getKey().equals("phase_to_phase_voltage")) {
			try {
				double temp = Double.parseDouble(givenPair.getValue());
				phase_to_phase_voltage = temp;
			} catch (NumberFormatException e) {
				return;
			}
		}
	}

	public Pair<String, String> getIndividualState(String givenStateKey) {
		String temp = "";
		
		if (givenStateKey.equals("breaker_state")) {
			temp = String.valueOf(breaker_state);
		} else if (givenStateKey.equals("primary_handle_status")) {
			temp = String.valueOf(primary_handle_status);
		} else if (givenStateKey.equals("path_status")) {
			temp = String.valueOf(path_status);
		} else if (givenStateKey.equals("meter_update_no")) {
			temp = String.valueOf(meter_update_no);
		} else if (givenStateKey.equals("period")) {
			temp = String.valueOf(period);
		} else if (givenStateKey.equals("phase_A_line_frequency")) {
			temp = String.valueOf(phase_A_line_frequency);
		} else if (givenStateKey.equals("phase_A_rms_voltage")) {
			temp = String.valueOf(phase_A_rms_voltage);
		} else if (givenStateKey.equals("phase_A_rms_current")) {
			temp = String.valueOf(phase_A_rms_current);
		} else if (givenStateKey.equals("phase_A_power_factor")) {
			temp = String.valueOf(phase_A_power_factor);
		} else if (givenStateKey.equals("phase_A_active_power")) {
			temp = String.valueOf(phase_A_active_power);
		} else if (givenStateKey.equals("phase_A_reactive_power")) {
			temp = String.valueOf(phase_A_reactive_power);
		} else if (givenStateKey.equals("phase_A_app_power")) {
			temp = String.valueOf(phase_A_app_power);
		} else if (givenStateKey.equals("phase_A_active_energy")) {
			temp = String.valueOf(phase_A_active_energy);
		} else if (givenStateKey.equals("phase_A_reactive_energy")) {
			temp = String.valueOf(phase_A_reactive_energy);
		} else if (givenStateKey.equals("phase_A_app_energy")) {
			temp = String.valueOf(phase_A_app_energy);
		} else if (givenStateKey.equals("phase_A_reverse_active_energy")) {
			temp = String.valueOf(phase_A_reverse_active_energy);
		} else if (givenStateKey.equals("phase_A_reverse_reactive_energy")) {
			temp = String.valueOf(phase_A_reverse_reactive_energy);
		} else if (givenStateKey.equals("phase_A_reverse_app_energy")) {
			temp = String.valueOf(phase_A_reverse_app_energy);
		} else if (givenStateKey.equals("phase_B_line_frequency")) {
			temp = String.valueOf(phase_B_line_frequency);
		} else if (givenStateKey.equals("phase_B_rms_voltage")) {
			temp = String.valueOf(phase_B_rms_voltage);
		} else if (givenStateKey.equals("phase_B_rms_current")) {
			temp = String.valueOf(phase_B_rms_current);
		} else if (givenStateKey.equals("phase_B_power_factor")) {
			temp = String.valueOf(phase_B_power_factor);
		} else if (givenStateKey.equals("phase_B_active_power")) {
			temp = String.valueOf(phase_B_active_power);
		} else if (givenStateKey.equals("phase_B_reactive_power")) {
			temp = String.valueOf(phase_B_reactive_power);
		} else if (givenStateKey.equals("phase_B_app_power")) {
			temp = String.valueOf(phase_B_app_power);
		} else if (givenStateKey.equals("phase_B_active_energy")) {
			temp = String.valueOf(phase_B_active_energy);
		} else if (givenStateKey.equals("phase_B_reactive_energy")) {
			temp = String.valueOf(phase_B_reactive_energy);
		} else if (givenStateKey.equals("phase_B_app_energy")) {
			temp = String.valueOf(phase_B_app_energy);
		} else if (givenStateKey.equals("phase_B_reverse_active_energy")) {
			temp = String.valueOf(phase_B_reverse_active_energy);
		} else if (givenStateKey.equals("phase_B_reverse_reactive_energy")) {
			temp = String.valueOf(phase_B_reverse_reactive_energy);
		} else if (givenStateKey.equals("phase_B_reverse_app_energy")) {
			temp = String.valueOf(phase_B_reverse_app_energy);
		} else if (givenStateKey.equals("phase_A_Q1_act_energy")) {
			temp = String.valueOf(phase_A_Q1_act_energy);
		} else if (givenStateKey.equals("phase_A_Q2_act_energy")) {
			temp = String.valueOf(phase_A_Q2_act_energy);
		} else if (givenStateKey.equals("phase_A_Q3_act_energy")) {
			temp = String.valueOf(phase_A_Q3_act_energy);
		} else if (givenStateKey.equals("phase_A_Q4_act_energy")) {
			temp = String.valueOf(phase_A_Q4_act_energy);
		} else if (givenStateKey.equals("phase_A_Q5_act_energy")) {
			temp = String.valueOf(phase_A_Q5_act_energy);
		} else if (givenStateKey.equals("phase_A_Q1_reactive_energy")) {
			temp = String.valueOf(phase_A_Q1_reactive_energy);
		} else if (givenStateKey.equals("phase_A_Q2_reactive_energy")) {
			temp = String.valueOf(phase_A_Q2_reactive_energy);
		} else if (givenStateKey.equals("phase_A_Q3_reactive_energy")) {
			temp = String.valueOf(phase_A_Q3_reactive_energy);
		} else if (givenStateKey.equals("phase_A_Q4_reactive_energy")) {
			temp = String.valueOf(phase_A_Q4_reactive_energy);
		} else if (givenStateKey.equals("phase_A_Q5_reactive_energy")) {
			temp = String.valueOf(phase_A_Q5_reactive_energy);
		} else if (givenStateKey.equals("phase_A_Q1_app_energy")) {
			temp = String.valueOf(phase_A_Q1_app_energy);
		} else if (givenStateKey.equals("phase_A_Q2_app_energy")) {
			temp = String.valueOf(phase_A_Q2_app_energy);
		} else if (givenStateKey.equals("phase_A_Q3_app_energy")) {
			temp = String.valueOf(phase_A_Q3_app_energy);
		} else if (givenStateKey.equals("phase_A_Q4_app_energy")) {
			temp = String.valueOf(phase_A_Q4_app_energy);
		} else if (givenStateKey.equals("phase_A_Q5_app_energy")) {
			temp = String.valueOf(phase_A_Q5_app_energy);
		} else if (givenStateKey.equals("phase_B_Q1_act_energy")) {
			temp = String.valueOf(phase_B_Q1_act_energy);
		} else if (givenStateKey.equals("phase_B_Q2_act_energy")) {
			temp = String.valueOf(phase_B_Q2_act_energy);
		} else if (givenStateKey.equals("phase_B_Q3_act_energy")) {
			temp = String.valueOf(phase_B_Q3_act_energy);
		} else if (givenStateKey.equals("phase_B_Q4_act_energy")) {
			temp = String.valueOf(phase_B_Q4_act_energy);
		} else if (givenStateKey.equals("phase_B_Q5_act_energy")) {
			temp = String.valueOf(phase_B_Q5_act_energy);
		} else if (givenStateKey.equals("phase_B_Q1_reactive_energy")) {
			temp = String.valueOf(phase_B_Q1_reactive_energy);
		} else if (givenStateKey.equals("phase_B_Q2_reactive_energy")) {
			temp = String.valueOf(phase_B_Q2_reactive_energy);
		} else if (givenStateKey.equals("phase_B_Q3_reactive_energy")) {
			temp = String.valueOf(phase_B_Q3_reactive_energy);
		} else if (givenStateKey.equals("phase_B_Q4_reactive_energy")) {
			temp = String.valueOf(phase_B_Q4_reactive_energy);
		} else if (givenStateKey.equals("phase_B_Q5_reactive_energy")) {
			temp = String.valueOf(phase_B_Q5_reactive_energy);
		} else if (givenStateKey.equals("phase_B_Q1_app_energy")) {
			temp = String.valueOf(phase_B_Q1_app_energy);
		} else if (givenStateKey.equals("phase_B_Q2_app_energy")) {
			temp = String.valueOf(phase_B_Q2_app_energy);
		} else if (givenStateKey.equals("phase_B_Q3_app_energy")) {
			temp = String.valueOf(phase_B_Q3_app_energy);
		} else if (givenStateKey.equals("phase_B_Q4_app_energy")) {
			temp = String.valueOf(phase_B_Q4_app_energy);
		} else if (givenStateKey.equals("phase_B_Q5_app_energy")) {
			temp = String.valueOf(phase_B_Q5_app_energy);
		} else if (givenStateKey.equals("phase_to_phase_voltage")) {
			temp = String.valueOf(phase_to_phase_voltage);
		}
		
		Pair<String, String> toReturn = new Pair<String, String>(givenStateKey, temp);
		
		return toReturn;
	}

	public void incrementDeviceStatusMessageVersion() {
		++deviceStatusMessageVersion;
	}
	
	public int getDeviceStatusMessageVersion() {
		return deviceStatusMessageVersion;
	}

	// ###########################################################################
	// ---------------------------------------------------------------------------
	// 				Below are EOL Calib Param stuff
	// ---------------------------------------------------------------------------
	// ###########################################################################

	// Variables (or how Java people like to call it, states) for EOL calib param
	private int Ade9000_APhCal0Field;
    private int Ade9000_AiGainField;
    private int Ade9000_AiRmsSOSField;
    private int Ade9000_ApGainField;
    private int Ade9000_AvGainField;
    private int Ade9000_BPhCal0Field;
    private int Ade9000_BiGainField;
    private int Ade9000_BiRmsSOSField;
    private int Ade9000_BpGainField;
    private int Ade9000_BvGainField;

	public int getEOLParamState(String fieldName) throws NoSuchFieldException, IllegalAccessException {
		Field field = getClass().getDeclaredField(fieldName);
		field.setAccessible(true);
		return field.getInt(this);
	}

	public void setEOLParamState(String fieldName, int givenValue) throws NoSuchFieldException, IllegalAccessException {
		Field field = getClass().getDeclaredField(fieldName);
		field.setAccessible(true);
		field.setInt(this, givenValue); // work?
	}

	public String createSEND_SET_BREAKER_EOL_CALIB_PARAMcustomMessage() {
		String toReturn = "#,999,0x00000000,0x0084,";
		toReturn += Integer.toString(Ade9000_APhCal0Field) + ";";
		toReturn += Integer.toString(Ade9000_AiGainField) + ";";
		toReturn += Integer.toString(Ade9000_AiRmsSOSField) + ";";
		toReturn += Integer.toString(Ade9000_ApGainField) + ";";
		toReturn += Integer.toString(Ade9000_AvGainField) + ";";
		toReturn += Integer.toString(Ade9000_BPhCal0Field) + ";";
		toReturn += Integer.toString(Ade9000_BiGainField) + ";";
		toReturn += Integer.toString(Ade9000_BiRmsSOSField) + ";";
		toReturn += Integer.toString(Ade9000_BpGainField) + ";";
		toReturn += Integer.toString(Ade9000_BvGainField) + ";";
		toReturn += "2432431";
		toReturn += "\n";	// Add a newline to the end for c code to check for string completeness

		return toReturn;
	}

	
	// ###########################################################################
	// ---------------------------------------------------------------------------
	// 				Below are the Handle Position stuff
	// ---------------------------------------------------------------------------
	// ###########################################################################
	private int handle_position;	// 0 - Open; 1 - Close
	
	public int getHandlePositionState() {
		return handle_position;
	}
	
	public void setHandlePositionState(int newHandlePosition) {
		handle_position = newHandlePosition;
	}
	
}
