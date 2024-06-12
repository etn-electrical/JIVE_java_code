/******************************************************************************************************
 * 
 * 		Connected Solutions @ Eaton (Moon Township, PA)
 * 	
 * 		@author Jonathan Tan (JonathanTan@eaton.com)
 * 		@date 6/30/2023
 * 
 * ****************************************************************************************************
 * 
 * 		SblcpSerialCom.java
 * 
 * 		Purpose	:	This Class is used to open and close a serial com port connection.
 * 
 * 		For more info, checkout this project's confluence page: https://confluence-prod.tcc.etn.com/pages/viewpage.action?pageId=299547479
 * 
 * *****************************************************************************************************/


package eaton.cs.sb2fw.SBLCP_local_terminal.util;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortIOException;

import eaton.cs.sb2fw.SBLCP_local_terminal.GUI.MainGUIcontroller;

import java.io.InputStream;
import java.io.OutputStream;

public class SblcpSerialCom {
	private String COMport = "";
	private int baudRate = 115200;	// Default value
	private int timeOut_ms = 1000;	// Default value
	private SerialPort[] availableSerialPorts;
	private SerialPort serialPortWeWant;
	
	private boolean serialPortIsOpen = false;
	
	MainGUIcontroller mainGUIcontrollerInstance;
	
	public SblcpSerialCom(String givenCOMport, int givenBaudRate, int givenTimeOut_ms) {
		mainGUIcontrollerInstance = new MainGUIcontroller();
		
		COMport = givenCOMport;
		baudRate = givenBaudRate;
		timeOut_ms = givenTimeOut_ms;
		
		// Get the list of available serial ports
        SerialPort[] availableSerialPorts = SerialPort.getCommPorts();
        
        // Find the desired serial port by name or index
        serialPortWeWant = null;
        for (SerialPort p : availableSerialPorts) {
            if (p.getDescriptivePortName().contains(COMport)) {
            	// If port is found, assign to state and exit
            	serialPortWeWant = p;
                break;
            }
        }
        
        if (serialPortWeWant == null) {
        	System.out.println("Serial port not found.");
        	return;
        }
        
        // Open the serial port
        if (!serialPortWeWant.openPort()) {
            System.out.println("Failed to open the serial port.");
            return;
        }
        
        // Set the serial port parameters
        serialPortWeWant.setComPortParameters(baudRate, 8, 1, SerialPort.NO_PARITY);

        // Set the read timeout
        serialPortWeWant.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, timeOut_ms, 0);
        
        // Set Status
        serialPortIsOpen = true;
	}
	
	public char readInputStream() throws SerialPortIOException {
		// Get the input stream from the serial port
        InputStream inputStream = serialPortWeWant.getInputStream();

        // Read data from the serial port
        try {
            int data = inputStream.read();
            return (char) data;
        } catch (Exception e) {
            e.printStackTrace();
            return '\0';
        }
	}
	
	public String sendData(String dataToSend) {
        // Get the output stream from the serial port
        OutputStream outputStream = serialPortWeWant.getOutputStream();
        
        // Send data through the serial port
        try {
            outputStream.write(dataToSend.getBytes());
            outputStream.flush();
            return "SblcpSerialCom.java: Data sent successfully." + dataToSend;
        } catch (Exception e) {
            return "SblcpSerialCom.java: sendData() Error: " + e.toString();
        }
    }
	
	public String closeSerialPort() {
		serialPortWeWant.closePort();
		serialPortIsOpen = false;
		return "SblcpSerialCom.java: Serial Port closed.";
	}
	
	public boolean getSerialPortOpenStatus() {
		return serialPortIsOpen;
	}
}
