/******************************************************************************************************
 * 
 * 		Connected Solutions @ Eaton (Moon Township, PA)
 * 	
 * 		@author Jonathan Tan (JonathanTan@eaton.com)
 * 		@date 6/30/2023
 * 
 * ****************************************************************************************************
 * 
 * 		SblcpSocket.java
 * 
 * 		Purpose	:	Unused.
 * 
 * 		For more info, checkout this project's confluence page: https://confluence-prod.tcc.etn.com/pages/viewpage.action?pageId=299547479
 * 
 * *****************************************************************************************************/


package eaton.cs.sb2fw.SBLCP_local_terminal.util;

import eaton.cs.sb2fw.SBLCP_local_terminal.GUI.*;

import java.io.IOException;
import java.net.DatagramSocket; // Apparently java.net.Socket is for TCP not UDP
import java.net.DatagramPacket;
import java.net.InetAddress;

public class SblcpSocket {
	private String ipAddress;
	private int port;
	private boolean socket_is_open = false;
	private InetAddress address;
	private DatagramSocket socket;
	
	/**
	 * Constructor
	 * @param givenIpAddress
	 * @param givenPort
	 */
	public SblcpSocket(String givenIpAddress, int givenPort) {
		ipAddress = givenIpAddress;
		port = givenPort;
		
		try {
			address = InetAddress.getByName(ipAddress);
            socket = new DatagramSocket();	// Creates a socket on local machine's IP and a random port
            socket_is_open = true;
            
            System.out.println("SclcpSocket: Socket create successfully");
		} catch (IOException e) {
            e.printStackTrace();
            
            System.err.println("SclcpSocket: Error occurred when creating socket: "/* + e.printStackTrace()*/);
        }
	}
	
	// example method
	public void sblcpSend(byte[] data) {
		try {
			DatagramPacket packet = new DatagramPacket(data, data.length, address, port);
			socket.send(packet);
		} catch (IOException e) {
	        System.err.println("SclcpSocket: An error occurred while sending data: " + e.getMessage());
	        e.printStackTrace();
	    }
	}
	
	// example method
	public byte[] sblcpReceive(int bufferSize) {
		try {
			byte[] buffer = new byte[bufferSize];
			DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
			socket.receive(packet);	// This is a blocking function
			return packet.getData();
		} catch (IOException e) {
	        System.err.println("SclcpSocket: An error occurred while receiving data: " + e.getMessage());
	        e.printStackTrace();
	        return new byte[0]; // Return an empty byte array or any other default value
	    }
    }

	
	public void closeSocket() {
		socket_is_open = false;
		socket.close();
	}
	
	public String getSocketIpAddress() {
		return ipAddress;
	}
	
	public int getPort() {
		return port;
	}
	
	public boolean getIsSocketOpen() {
		return socket_is_open;
	}
}
