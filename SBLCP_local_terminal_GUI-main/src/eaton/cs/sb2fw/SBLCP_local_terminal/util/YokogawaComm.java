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
 * 					that controls the Yokogawa. It is separated into "normal" functions and "un-normal"
 * 					functions. 
 * 					 - Normal function are function that reads the Yokogawa's readings. 
 * 					 - Un-normal function are functions that deals with the Yokogawa's integration 
 * 					   functions. Like setting the timer, resetting, and starting and stopping.
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

public class YokogawaComm {
	final double BASICALLY_INF = 999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999.9;
	
    double[] yoko_readings = {
        0.0,    // 0 yoko_phAURMS
        0.0,    // 1 yoko_phAIRMS
        0.0,    // 2 yoko_phAP
        0.0,    // 3 yoko_phAS
        0.0,    // 4 yoko_phAQ
        0.0,    // 5 yoko_phALAMBDA
        0.0,    // 6 yoko_phAPHI
        0.0,    // 7 yoko_phAFU
        0.0,    // 8 yoko_phAWH
        0.0,    // 9 yoko_phAWHP
        0.0,    // 10 yoko_phAWHM
        0.0,    // 11 yoko_phAWS
        0.0,    // 12 yoko_phAWQ
        0.0,    // 13 yoko_phBURMS
        0.0,    // 14 yoko_phBIRMS
        0.0,    // 15 yoko_phBP
        0.0,    // 16 yoko_phBS
        0.0,    // 17 yoko_phBQ
        0.0,    // 18 yoko_phBLAMBDA
        0.0,    // 19 yoko_phBPHI
        0.0,    // 20 yoko_phBFU
        0.0,    // 21 yoko_phBWH
        0.0,    // 22 yoko_phBWHP
        0.0,    // 23 yoko_phBWHM
        0.0,    // 24 yoko_phBWS
        0.0     // 25 yoko_phBWQ
	};

    String[] yoko_headers = {
        "URMS", "IRMS", "P", "S", "Q", "LAMBDA", "PHI", "FU", "WH", "WHP", 
        "WHM", "WS", "WQ", "URMS", "IRMS", "P", "S", "Q", "LAMBDA", "PHI", 
        "FU", "WH", "WHP", "WHM", "WS", "WQ", 
    };
	
	
	public YokogawaComm() {
		
	}
	
	
	// ############################################################################
	// ----------------------------------------------------------------------------
	// ------ "Normal" get functions (Only get v and i, no integration) -----------
	// ----------------------------------------------------------------------------
	// ############################################################################
	public double getPh0Ph1IndividualVal(int phase, String header) {
		ArrayList<String> arrli = pythonAppTalkerAndReturnsString("python/yoko_getPh0Ph1IndividualVal.py", phase, header);
		
		String importantPart = arrli.get(1);
		
		String valueInString = importantPart.split(" ")[4];
		
		double value = 0.0;
		
		try {
        	value = Double.parseDouble(valueInString);
        } catch (NumberFormatException e) {
        	if (valueInString.equals("INF") || valueInString.equals("NAN")) {
        		value = BASICALLY_INF;
        	}
        }
		
		return value;
	}
	
	/**
	 * Important: Avoid using, slow.
	 * Send one command (run a Python file) that will return all 1 value at a time. This method have a loop that loops
	 * 26 times. Making it a pretty slow and useless function.
	 * @return A list of yoko readings. You can find the order of the values in the yoko_readings[] declaration above.
	 */
	public double[] getPh0Ph1AllVal() {
        for (int i = 0; i < yoko_headers.length; ++i) {
            ArrayList<String> arrli = pythonAppTalkerAndReturnsString("python/yoko_getPh0Ph1IndividualVal.py", 0, yoko_headers[i]);
            
            String importantPart = arrli.get(1);
            
            String valueInString = importantPart.split(" ")[4];
            
            try {
            	double value = Double.parseDouble(valueInString);
            	yoko_readings[i] = value;
            } catch (NumberFormatException e) {
            	if (valueInString.equals("INF") || valueInString.equals("NAN")) {
            		yoko_readings[i] = BASICALLY_INF;
            	}
            }
        }

        return yoko_readings;
	}
	
	/**
	 * Send one command (run a Python file) that will return all 26 values together.
	 * @return A list of yoko readings. You can find the order of the values in the yoko_readings[] declaration above.
	 */
	public double[] getPh0Ph1AllValAtOnce() {
		ArrayList<String> arrli = pythonAppTalkerAndReturnsString("python/yoko_getPh0Ph1AllVal.py");
		
		int count = 0;
		for (int i = 0; i < arrli.size(); ++i) {
			String[] strli = arrli.get(i).split(" ");	// Normal string should be of format: "Phase 1: URMS => 0.07071E+00"
			
			try {
				int phaseNumber = Integer.valueOf(strli[1]);
				
				if ((phaseNumber == 1 || phaseNumber == 2) && count >= 0) {
					// We are starting to see normal strings, this is the first encounter, start counting up
					double value;
					if (strli[5].equals("INF") || strli[5].equals("NAN")) {
		        		value = BASICALLY_INF;
		        	} else {
		        		value = Double.valueOf(strli[5]);
		        	}
					
					yoko_readings[count] = value;
					
					++count;
				}
			} catch (ArrayIndexOutOfBoundsException e) {
				// That means that we are not looking at a "normal" string
//				System.out.println("[DEBUG] Not looking at a normal string.");
				continue;
			} catch (NumberFormatException e) {
//				System.out.println("[DEBUG] NumberFormatException.");
			}
		}
		
		return yoko_readings;
	}
	
	public void overwriteValue(int indexNumber, double value) {
		yoko_readings[indexNumber] = value;
	}
	
	// ############################################################################
	// ----------------------------------------------------------------------------
	// --------- Un-"Normal" get functions (Start and Stop integration) -----------
	// ----------------------------------------------------------------------------
	// ############################################################################
	private boolean integrationIsRunning = false;
	public void startIntegration() {
		pythonAppTalkerAndReturnsString("python/yoko_startIntegration.py");
		integrationIsRunning = true;
	}
	public void stopIntegration() {
		pythonAppTalkerAndReturnsString("python/yoko_stopIntegration.py");
		integrationIsRunning = false;
	}
	public void resetIntegration() {
		if (integrationIsRunning) return;
		pythonAppTalkerAndReturnsString("python/yoko_resetIntegration.py");
	}
	public void setIntegrationTimer(int givenHr, int givenMin, int givenSec) {
		if (integrationIsRunning) return;
		pythonAppTalkerAndReturnsString("python/yoko_setIntegrationTimer.py", givenHr, givenMin, givenSec);
	}
	public boolean isIntegrationRunning() {
		return integrationIsRunning;
	}
	
	
	// ############################################################################
	// ############################################################################
	// ############################## Python Talkers ##############################
	// ############################################################################
	// ############################################################################ Note: for what these functions do, read the JavaDoc comments for the Python App Talker functions in OmicronComm.java
	protected ArrayList<String> pythonAppTalkerAndReturnsString(String pythonFileLocation, double...ds) {
		ArrayList<String> arrli = new ArrayList<>();
		
		try {
			// Convert the array of doubles to an array of strings
            String[] stringArgs = Arrays.stream(ds)
                    .mapToObj(String::valueOf)
                    .toArray(String[]::new);
            
            // Build the command to execute the Python script
            String[] command = new String[2 + stringArgs.length];
            command[0] = "python";
            command[1] = pythonFileLocation;
            System.arraycopy(stringArgs, 0, command, 2, stringArgs.length);
            
            // Build the command to execute the Python script
            ProcessBuilder pb = new ProcessBuilder(command);
            Process process = pb.start();

            // Read the output of the Python script
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                // Process the output line by line
            	arrli.add(line);
            }

            // Wait for the process to finish
            int exitCode = process.waitFor();
            System.out.println("Python script executed with exit code: " + exitCode);
            
            // Read the error output of the Python script
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String errorLine;
            while ((errorLine = errorReader.readLine()) != null) {
                // Process the error output line by line
                System.err.println("Python error: " + errorLine);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
		
		return arrli;
	}
	
	protected ArrayList<String> pythonAppTalkerAndReturnsString(String pythonFileLocation, int...ds) {
		ArrayList<String> arrli = new ArrayList<>();
		
		try {
			// Convert the array of doubles to an array of strings
            String[] stringArgs = Arrays.stream(ds)
                    .mapToObj(String::valueOf)
                    .toArray(String[]::new);
            
            // Build the command to execute the Python script
            String[] command = new String[2 + stringArgs.length];
            command[0] = "python";
            command[1] = pythonFileLocation;
            System.arraycopy(stringArgs, 0, command, 2, stringArgs.length);
            
            // Build the command to execute the Python script
            ProcessBuilder pb = new ProcessBuilder(command);
            Process process = pb.start();

            // Read the output of the Python script
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                // Process the output line by line
            	arrli.add(line);
            }

            // Wait for the process to finish
            int exitCode = process.waitFor();
            System.out.println("Python script executed with exit code: " + exitCode);
            
            // Read the error output of the Python script
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String errorLine;
            while ((errorLine = errorReader.readLine()) != null) {
                // Process the error output line by line
                System.err.println("Python error: " + errorLine);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
		
		return arrli;
	}
	
	protected ArrayList<String> pythonAppTalkerAndReturnsString(String pythonFileLocation, int phaseWeWantToRead, String itemWeWantToRead) {
		ArrayList<String> arrli = new ArrayList<>();
		
        try {
            // Build the command to execute the Python script
            ProcessBuilder pb = new ProcessBuilder("python", pythonFileLocation, String.valueOf(phaseWeWantToRead), itemWeWantToRead);
            Process process = pb.start();

            // Read the output of the Python script
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                // Process the output line by line and add them into array to be returned
            	arrli.add(line);
            }

            // Wait for the process to finish
            int exitCode = process.waitFor();
            System.out.println("Python script executed with exit code: " + exitCode);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        
        return arrli;
	}

	/**
	 * Not used anymore.
	 * 
	 * This method will open a Python file and create a Process. Which is for if you need
	 * to send follow up commands (if the Python file takes user input).
	 */
	protected Process pythonAppTalker(String pythonFileLocation) {
		try {
            // Build the command to execute the Python script
        	ProcessBuilder pb = new ProcessBuilder("python", pythonFileLocation);
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
	
	protected static void pythonAppTalker(String pythonFileLocation, int phaseWeWantToRead, String itemWeWantToRead) {
        try {
            // Build the command to execute the Python script
            ProcessBuilder pb = new ProcessBuilder("python", pythonFileLocation, String.valueOf(phaseWeWantToRead), itemWeWantToRead);
            Process process = pb.start();

            // Read the output of the Python script
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                // Process the output line by line
                System.out.println("Python output: " + line);
            }

            // Wait for the process to finish
            int exitCode = process.waitFor();
            System.out.println("Python script executed with exit code: " + exitCode);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
	}

	/**
	 * This one will send the input and return the arrli output
	 */
	protected ArrayList<String> pythonAppTalker(Process givenProcess, String givenInputForPythonApp) throws IOException {
		// Obtain the output stream to send keyboard input to the Python program
		OutputStream outputStream = givenProcess.getOutputStream();
		PrintWriter writer = new PrintWriter(outputStream);

		// Send the input to the Python program
		writer.println(givenInputForPythonApp);
		writer.flush();
		
		
		// Listens to the output
		ArrayList<String> arrli = new ArrayList<>();
		
		try {
            // Read the output of the Python script
            BufferedReader reader = new BufferedReader(new InputStreamReader(givenProcess.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                // Process the output line by line and add them into array to be returned
            	arrli.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
		
		return arrli;
	}
}
