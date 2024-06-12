/******************************************************************************************************
 * 
 * 		Connected Solutions @ Eaton (Moon Township, PA)
 * 	
 * 		@author Jonathan Tan (JonathanTan@eaton.com)
 * 		@date 6/30/2023
 * 
 * ****************************************************************************************************
 * 
 * 		OmicronComm.java
 * 
 * 		Purpose	:	This Class have all the functions you need to communicate with the Python files
 * 					that controls the Omicron.
 * 
 * 		For more info, checkout this project's confluence page: https://confluence-prod.tcc.etn.com/pages/viewpage.action?pageId=299547479
 * 
 * *****************************************************************************************************/

package eaton.cs.sb2fw.SBLCP_local_terminal.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;

public class OmicronComm {
	double omicron_phaseAVoltage;
	double omicron_phaseAVoltagePhase;
	double omicron_phaseACurrent;
	double omicron_phaseACurrentPhase;
	double omicron_phaseBVoltage;
	double omicron_phaseBVoltagePhase;
	double omicron_phaseBCurrent;
	double omicron_phaseBCurrentPhase;

	double[] omicron_values = {
		0.0,	// 0 => omicron_phaseAVoltage
		0.0,	// 1 => omicron_phaseAVoltagePhase
		0.0,	// 2 => omicron_phaseACurrent
		0.0,	// 3 => omicron_phaseACurrentPhase
		0.0,	// 4 => omicron_phaseBVoltage
		0.0,	// 5 => omicron_phaseBVoltagePhase
		0.0,	// 6 => omicron_phaseBCurrent
		0.0		// 7 => omicron_phaseBCurrentPhase
	};

	boolean omicronIsOn = false;
	Process omicronProcess;
	
	ArrayList<String> omicron_parameters = new ArrayList<>();
	
	/**
	 * A constructor that will initialize the omicron_parameters array list.
	 */
	public OmicronComm() {
		omicron_parameters.add("omicron_phaseAVoltage");			// 0		
		omicron_parameters.add("omicron_phaseAVoltagePhase");		// 1		
		omicron_parameters.add("omicron_phaseACurrent");			// 2		
		omicron_parameters.add("omicron_phaseACurrentPhase");		// 3		
		omicron_parameters.add("omicron_phaseBVoltage");			// 4		
		omicron_parameters.add("omicron_phaseBVoltagePhase");		// 5		
		omicron_parameters.add("omicron_phaseBCurrent");			// 6		
		omicron_parameters.add("omicron_phaseBCurrentPhase");		// 7
	}
	
	/**
	 * A constructor that will, on top of initializing the omicron_parameters array list, will turn on the Omicron
	 * with the values from the arguments using the method setOmicron_AllphaseAphaseB(). 
	 */
	public OmicronComm(	double phaseAVoltage,
						double phaseAVoltagePhase,
						double phaseACurrent,
						double phaseACurrentPhase,
						double phaseBVoltage,
						double phaseBVoltagePhase,
						double phaseBCurrent,
						double phaseBCurrentPhase) {
		this();
		
		// Set the state
		omicron_phaseAVoltage = phaseAVoltage;
		omicron_phaseAVoltagePhase = phaseAVoltagePhase;
		omicron_phaseACurrent = phaseACurrent;
		omicron_phaseACurrentPhase = phaseACurrentPhase;
		omicron_phaseBVoltage = phaseBVoltage;
		omicron_phaseBVoltagePhase = phaseBVoltagePhase;
		omicron_phaseBCurrent = phaseBCurrent;
		omicron_phaseBCurrentPhase = phaseBCurrentPhase;

		omicronProcess = setOmicron_AllphaseAphaseB(phaseAVoltage,
											  phaseAVoltagePhase,
											  phaseACurrent,
											  phaseACurrentPhase,
											  phaseBVoltage,
											  phaseBVoltagePhase,
											  phaseBCurrent,
											  phaseBCurrentPhase);
		
		omicronIsOn = true;
	}
	
	/**
	 * While the Omicron is running, if you want to, for example, change phase A's current from 10A to 5A, 
	 * this is the method you will call.
	 */
	public void modifyOmicronValues(double phaseAVoltage,
									double phaseAVoltagePhase,
									double phaseACurrent,
									double phaseACurrentPhase,
									double phaseBVoltage,
									double phaseBVoltagePhase,
									double phaseBCurrent,
									double phaseBCurrentPhase) {
		
		// Obtain the output stream to send keyboard input to the Python program
		OutputStream outputStream = omicronProcess.getOutputStream();
		PrintWriter writer = new PrintWriter(outputStream);

		String toSet = "";
		toSet += String.valueOf(phaseAVoltage);
		toSet += ",";
		toSet += String.valueOf(phaseAVoltagePhase);
		toSet += ",";
		toSet += String.valueOf(phaseACurrent);
		toSet += ",";
		toSet += String.valueOf(phaseACurrentPhase);
		toSet += ",";
		toSet += String.valueOf(phaseBVoltage);
		toSet += ",";
		toSet += String.valueOf(phaseBVoltagePhase);
		toSet += ",";
		toSet += String.valueOf(phaseBCurrent);
		toSet += ",";
		toSet += String.valueOf(phaseBCurrentPhase);
		
		// Send the input to the Python program
		writer.println(toSet);
		writer.flush();
	}
	
	/**
	 * If you want to stop the Omicron, this is the function you will call.
	 */
	public void turnOffOmicron() {
		if (omicronIsOn) {
			try {
				pythonAppStopper(omicronProcess);
				omicronIsOn = false;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * This is the function that, using the arguments, turn on the Omicron by sending the values of the argument to
	 * the python file setOmicron_turnOnAndStayOn.py to start running the Omicron.
	 * 
	 * @return A process of the opened python file.
	 */
	private Process setOmicron_AllphaseAphaseB(double phaseAVoltage,
										 double phaseAVoltagePhase,
										 double phaseACurrent,
										 double phaseACurrentPhase,
										 double phaseBVoltage,
										 double phaseBVoltagePhase,
										 double phaseBCurrent,
										 double phaseBCurrentPhase) {
		
		// Set the state
		omicron_phaseAVoltage = phaseAVoltage;
		omicron_phaseAVoltagePhase = phaseAVoltagePhase;
		omicron_phaseACurrent = phaseACurrent;
		omicron_phaseACurrentPhase = phaseACurrentPhase;
		omicron_phaseBVoltage = phaseBVoltage;
		omicron_phaseBVoltagePhase = phaseBVoltagePhase;
		omicron_phaseBCurrent = phaseBCurrent;
		omicron_phaseBCurrentPhase = phaseBCurrentPhase;
		
		String toSet = "";
		toSet += String.valueOf(phaseAVoltage);
		toSet += ",";
		toSet += String.valueOf(phaseAVoltagePhase);
		toSet += ",";
		toSet += String.valueOf(phaseACurrent);
		toSet += ",";
		toSet += String.valueOf(phaseACurrentPhase);
		toSet += ",";
		toSet += String.valueOf(phaseBVoltage);
		toSet += ",";
		toSet += String.valueOf(phaseBVoltagePhase);
		toSet += ",";
		toSet += String.valueOf(phaseBCurrent);
		toSet += ",";
		toSet += String.valueOf(phaseBCurrentPhase);
		
		return pythonAppTalker("python/setOmicron_turnOnAndStayOn.py", toSet);
	}
	
	// Pretty useless imo, like when would we want to have, for example, only phase 1's current on and everything else off?
	private void setOmicronIndividual(String parameterToSet, double valueToSet) {
		if (omicron_parameters.contains(parameterToSet)) {
			int indexIs = omicron_parameters.indexOf(parameterToSet);
			
			omicron_values[indexIs] = valueToSet;
			
			// TODO Set to Omicron
		}
	}
	
	/**
	 * Given a python file's path, open the Python file and pass in the argument arg1
	 * @param pythonFileLocation Path of the Python file.
	 * @param arg1 Argument to pass into the Python file when starting the Python file.
	 * @return A process of the opened python file.
	 */
	protected Process pythonAppTalker(String pythonFileLocation, String arg1) {
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
	
	/**
	 * Given a python file's path, open the Python file and pass in the argument arg1, arg2, param1, param2;
	 * @param pythonFileLocation Path of the Python file.
	 * @return A process of the opened python file.
	 */
	protected Process pythonAppTalker(String pythonFileLocation, String arg1, String arg2, double param1, double param2) {
        try {
            // Build the command to execute the Python script
        	ProcessBuilder pb = new ProcessBuilder("python", pythonFileLocation,
                    arg1,
                    arg2,
                    String.valueOf(param1),
                    String.valueOf(param2));
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

	/**
	 * Because the Omicron python files listen to keyboard inputs, this function's job is to send those
	 * inputs.
	 * 
	 * @param process A process of the opened python file.
	 * @throws IOException
	 */
	protected void pythonAppStopper(Process process) throws IOException {
        // Obtain the output stream to send keyboard input to the Python program
		OutputStream outputStream = process.getOutputStream();
		PrintWriter writer = new PrintWriter(outputStream);

		// Send the input to the Python program
		writer.println("shutdown");
		writer.flush();
    }


}
