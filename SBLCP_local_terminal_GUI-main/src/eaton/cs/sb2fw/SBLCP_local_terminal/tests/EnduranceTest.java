/**
 * EnduranceTest.java
 * Purpose: Create a thread to run the endurance test, update status to 
 * 			mainGUIcontroller label.
 * 
 * @author Jonathan Tan (JonathanTan@eaton.com)
 * 
 * For more info, checkout this project's confluence page:
 * https://confluence-prod.tcc.etn.com/pages/viewpage.action?pageId=299547479
 */

package eaton.cs.sb2fw.SBLCP_local_terminal.tests;

import eaton.cs.sb2fw.SBLCP_local_terminal.GUI.MainGUIcontroller;
import eaton.cs.sb2fw.SBLCP_local_terminal.util.CallSBLCPCommand;
import eaton.cs.sb2fw.SBLCP_local_terminal.util.SB2;
import eaton.cs.sb2fw.SBLCP_local_terminal.util.SblcpSerialCom;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class EnduranceTest {
    private BufferedWriter bufferedWriter;
    private static final String FILE_PATH = "enduranceTest_output/";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MMdd_HHmm");
    private String fileName;

    // Constructor to create and initialize the file
    public EnduranceTest() {
        try {
            String timestamp = DATE_FORMAT.format(new Date());
            fileName = FILE_PATH + "endurance_test_" + timestamp + ".csv";

            FileWriter fileWriter = new FileWriter(fileName, true);
            bufferedWriter = new BufferedWriter(fileWriter);

            // Check if the file is empty, if so, write the header
            bufferedWriter.write(getCSVHeader());
            bufferedWriter.newLine();
        } catch (IOException e) {
            e.printStackTrace();
            // Handle any potential exceptions here if needed
        }
    }
	
	private boolean enduranceTestIsRunning = false;
	private Thread enduranceTestThread;
	
	private int readingCounter = 0;
	
	private final int MAX_TIME_WE_WANT_TO_WAIT_FOR_RESPONSE_MS = 3000;
	private final int TIME_SINCE_CYCLE_START_TO_WAIT_TO_GET_DEV_STAT_MS = 1000;

	public void startEnduranceTest(MainGUIcontroller mgc, SB2 sb, SblcpSerialCom sc, int cycleTime_ms, int timeAfterCycleStartToGetDevStat_ms) {
		enduranceTestThread = new Thread(() -> {
			mgc.printToSerialPortOutputBox = false;		// Disable output prints
			
			while (!Thread.currentThread().isInterrupted()) {
				mgc.modifyEnduranceTestProgressIndicatorAndLabel(true, "Initializing test...");
				
				CallSBLCPCommand.call_NonCustom_SBLCPcommand_blocking(sc, CallSBLCPCommand.SET_NEXT_EXPECTED_UDP_SEQUENCE_NUMBER, Thread.currentThread(), 3000);
				CallSBLCPCommand.call_NonCustom_SBLCPcommand_blocking(sc, CallSBLCPCommand.GET_NEXT_EXPECTED_UDP_SEQUENCE_NUMBER, Thread.currentThread(), 3000);
				
				
				while (!Thread.currentThread().isInterrupted()) {
					++readingCounter;
					mgc.modifyEnduranceTestProgressIndicatorAndLabel(true, "This is reading #" + readingCounter + ".");
					
					// Wait to get device status
					try {
						Thread.sleep(TIME_SINCE_CYCLE_START_TO_WAIT_TO_GET_DEV_STAT_MS);
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					}
					
					
					if (Thread.currentThread().isInterrupted()) break;	// Interrupt checker
					
					
					// Get device status
					int timeToGetDeviceStatus = CallSBLCPCommand.call_NonCustom_SBLCPcommand_blocking(sc, CallSBLCPCommand.GET_DEVICE_STATUS, Thread.currentThread(), 3000);
					
					
					if (Thread.currentThread().isInterrupted()) break;	// Interrupt checker
					
					
					int timeToSetUDP = -1;
					int timeToGetUDP = -1;
					if (timeToGetDeviceStatus == -1) {
						mgc.modifyEnduranceTestProgressIndicatorAndLabel(true, "Warning: No response from GET_DEVICE_STATUS. Reconnecting to breaker...");
						// No response from get device status is received
						// "Reset connection"
						// Set UDP
						mgc.modifyEnduranceTestProgressIndicatorAndLabel(true, "Resetting UDP (1st time)...");
						timeToSetUDP = CallSBLCPCommand.call_NonCustom_SBLCPcommand_blocking(sc, CallSBLCPCommand.SET_NEXT_EXPECTED_UDP_SEQUENCE_NUMBER, Thread.currentThread(), 1000);
						
						
						if (Thread.currentThread().isInterrupted()) break;	// Interrupt checker
						
						
						if (timeToSetUDP == -1) {
							mgc.modifyEnduranceTestProgressIndicatorAndLabel(true, "Resetting UDP (2nd time)...");
							timeToSetUDP = CallSBLCPCommand.call_NonCustom_SBLCPcommand_blocking(sc, CallSBLCPCommand.SET_NEXT_EXPECTED_UDP_SEQUENCE_NUMBER, Thread.currentThread(), 1300);
							
							
							if (Thread.currentThread().isInterrupted()) break;	// Interrupt checker
							
							
							if (timeToSetUDP != -1) timeToSetUDP += 1000 + 1300;
						}
						// Get UDP
						mgc.modifyEnduranceTestProgressIndicatorAndLabel(true, "Regetting UDP (1st time)...");
						timeToGetUDP = CallSBLCPCommand.call_NonCustom_SBLCPcommand_blocking(sc, CallSBLCPCommand.GET_NEXT_EXPECTED_UDP_SEQUENCE_NUMBER, Thread.currentThread(), 1000);
						
						
						if (Thread.currentThread().isInterrupted()) break;	// Interrupt checker
						
						
						if (timeToGetUDP == -1) {
							try {
								mgc.modifyEnduranceTestProgressIndicatorAndLabel(true, "Letting GET_NEXT_UDP cooldown expire (2s)...");
								Thread.sleep(2000);
							} catch (InterruptedException e) {
								Thread.currentThread().interrupt();
							}
							mgc.modifyEnduranceTestProgressIndicatorAndLabel(true, "Regetting UDP (2nd time)...");
							timeToGetUDP = CallSBLCPCommand.call_NonCustom_SBLCPcommand_blocking(sc, CallSBLCPCommand.GET_NEXT_EXPECTED_UDP_SEQUENCE_NUMBER, Thread.currentThread(), 1300);
							
							
							if (Thread.currentThread().isInterrupted()) break;	// Interrupt checker
							
							
							if (timeToGetUDP != -1) timeToGetUDP += 1000 + 2000 + 1300;
						}
						
						// At this point I assume both retries succeeded, if they fail again, dont care.
						// And at this point I believe we are out of time too so skip re-get_dev_stat
						if (timeToGetUDP == -1 && timeToSetUDP == -1) {
							mgc.modifyEnduranceTestProgressIndicatorAndLabel(true, "Failed to reconnecting to breaker, proceeding and hopefully it fixes itself...");
						}
					}
					
					
					if (Thread.currentThread().isInterrupted()) break;	// Interrupt checker
					
					
					if (timeToGetDeviceStatus != -1) {
						// Grab data and write to csv file
						int numOfAttempts = Integer.parseInt(sb.getIndividualState("phase_A_Q1_act_energy").getValue());
						int numOfFailedOpens = Integer.parseInt(sb.getIndividualState("phase_A_Q2_act_energy").getValue());
						int numOfFailedClose = Integer.parseInt(sb.getIndividualState("phase_A_Q3_act_energy").getValue());
						double phase_A_rms_voltage= Double.parseDouble(sb.getIndividualState("phase_A_rms_voltage").getValue());
						double phase_A_rms_current = Double.parseDouble(sb.getIndividualState("phase_A_rms_current").getValue());
						double phase_B_rms_voltage= Double.parseDouble(sb.getIndividualState("phase_B_rms_voltage").getValue());
						double phase_B_rms_current = Double.parseDouble(sb.getIndividualState("phase_B_rms_current").getValue());
						writeToCSV(readingCounter, numOfAttempts, numOfFailedOpens, numOfFailedClose, 
								   phase_A_rms_voltage, phase_A_rms_current, phase_B_rms_voltage, phase_B_rms_current);
						
						mgc.modifyEnduranceTestNumberLables(numOfAttempts, numOfFailedOpens, numOfFailedClose);
					}
					
					
					if (Thread.currentThread().isInterrupted()) break;	// Interrupt checker
					
					
					// Wait the remaining time
					try {
						int timeToWait = 0;
						if (timeToSetUDP == -1 && timeToGetUDP == -1) {
							timeToWait = cycleTime_ms - TIME_SINCE_CYCLE_START_TO_WAIT_TO_GET_DEV_STAT_MS - timeToGetDeviceStatus;
						} else {
							timeToWait = cycleTime_ms - TIME_SINCE_CYCLE_START_TO_WAIT_TO_GET_DEV_STAT_MS - timeToGetDeviceStatus - timeToSetUDP - timeToGetUDP;
						}
						mgc.modifyEnduranceTestProgressIndicatorAndLabel(true, "Waiting for time to pass... :)");
						Thread.sleep(timeToWait>0?timeToWait:0);
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					}
				} // End of inner while loop
				if (Thread.currentThread().isInterrupted()) break;	// Interrupt checker
			} // End of outter while loop
		});
		
		enduranceTestThread.start();
	}
	
	/**
	 * 
	 * @param mgc
	 * @return 1 = Thread killed, -1 = Thread killed failed
	 */
	public int stopEnduranceTest(MainGUIcontroller mgc) {
		closeCSV();
		mgc.printToSerialPortOutputBox = true;
		
		if (enduranceTestThread != null && enduranceTestThread.isAlive()) {
			enduranceTestThread.interrupt();
			try {
				enduranceTestThread.join();
			} catch (InterruptedException e) {
				// hi
			}
		}
		
		int ms_waited = 0;
		final int MAX_WAIT_MS = 3000;
		while (enduranceTestThread.isAlive()) {
			// Wait for the Thread to die
			
			if (ms_waited >= MAX_WAIT_MS) return -1;	// Time out
			
			try {
				Thread.sleep(1);
				++ms_waited;
			} catch (InterruptedException e) {
				// Do nothing hehe, very bad coding practice I know
			}
		}
		
		enduranceTestThread = null;
		
		return 1;
	}
	
	// Method to write data to CSV file
    public void writeToCSV(int readingCounter, int attemptsCounter, int failedOpensCounter,
                           int failedClosesCounter, double phaseAVoltage, double phaseACurrent,
                           double phaseBVoltage, double phaseBCurrent) {
        try {
            // Get the current timestamp for the log entry
            String timestamp = DATE_FORMAT.format(new Date());

            // Create the data line
            String dataLine = String.format("%s,%d,%d,%d,%d,%.2f,%.2f,%.2f,%.2f",
                    timestamp, readingCounter, attemptsCounter, failedOpensCounter, failedClosesCounter,
                    phaseAVoltage, phaseACurrent, phaseBVoltage, phaseBCurrent);

            // Write the data line to the CSV file
            bufferedWriter.write(dataLine);
            bufferedWriter.newLine();
            bufferedWriter.flush(); // Ensure data is written to disk immediately

        } catch (IOException e) {
            e.printStackTrace();
            // Handle any potential exceptions here if needed
        }
    }

    // Method to get the CSV header
    private String getCSVHeader() {
        return "Timestamp,ReadingCounter,AttemptsCounter,FailedOpensCounter,FailedClosesCounter,"
                + "PhaseAVoltage,PhaseACurrent,PhaseBVoltage,PhaseBCurrent";
    }
    
    // Method to close the file when done
    public void closeCSV() {
        try {
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
            // Handle any potential exceptions here if needed
        }
    }
}
