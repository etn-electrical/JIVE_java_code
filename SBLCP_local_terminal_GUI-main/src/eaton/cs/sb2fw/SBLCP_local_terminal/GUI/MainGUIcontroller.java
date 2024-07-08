/******************************************************************************************************
 * 
 * 		Connected Solutions @ Eaton (Moon Township, PA)
 * 	
 * 		@author Jonathan Tan (JonathanTan@eaton.com)
 * 		@date 7/21/2023
 * 
 * ****************************************************************************************************
 * 
 * 		MainGUIcontroller.java
 * 
 * 		Purpose	: This class houses all states, objects, and functions that controls the GUI. 
 * 				  With ~>= 3000 lines of code, this class is nothing less than a shit show. However, 
 * 				  I tried my absolute best to keep it organized so it would be less painful for 
 * 				  non-Jonathan people to read it.
 * 
 * 		How to	: How to quickly understand this Class 101:
 * 				  1. Don't even try to read the code in attempt of understanding logically wtf is going on.
 * 				  2. EVERY button handler functions starts with handleXxxXxxXxx. For example, 
 * 					 handleMeasurementTestBlockForceStopButton() is a function than will be called
 * 					 when the "Force Stop" button in the "Measurement Test" block is pressed. Hence
 * 					 the name handle-measurement-test-block-force-stop-button.
 * 				  3. What is a "block"?
 * 					 Answer: In the GUI, there are "pages". Like the "Send Message" page and "Device
 * 							 Status" page. They are essentially BorderPane that is set to "visible" 
 * 							 when the user presses the corresponding button. Eg. if Joe press the 
 * 							 button "Factory Reset" button, the "factoryResetBlock" will be set to 
 * 							 visible and every other block to not visible. In the context of this program,
 * 							 "block"s, "page"s, and "BorderPane"s are essentially the same thing.
 * 				  4. Code organization: (Almost) everything is separated into sections with a section
 * 										comment block to tell you what that section does. And each section
 * 										will have the states/variables and functions that controls a block.
 * 				  5. Eclipse 101: 
 * 						i. 	Press ctrl+shift+'/' (Note it is the '/' on the numpad) to collapse all
 * 						   	code chucks.
 * 						ii.	For fast scrolling, press ctrl when scrolling.
 * 				  6. Now, collapse all code (ctrl+shift+'/') and scroll. You will find different code
 * 					 sections for different blocks. Read the method names. If the name doesn't tell
 * 					 you immediately what the method is for, there probably have a commend block on
 * 					 top of the method. If not, I am sorry, I'd failed you.
 * 				  7. Now that you kinda understand how the ~3000 lines of code work. Here is a deeper dive into
 * 					 the inner workings of the code:
 * 						i.	The "startListening()" function will run in a separate Thread. That thread
 * 							will only stop if the "stopListening()" function is called, which is called when
 * 							the handleStopListeningButton() function is called, which is called when the user
 * 							presses the "Stop Listening" button.
 * 						ii. :)
 * 
 * 		For more info, checkout this project's confluence page: https://confluence-prod.tcc.etn.com/pages/viewpage.action?pageId=299547479
 * 
 * *****************************************************************************************************/

package eaton.cs.sb2fw.SBLCP_local_terminal.GUI;

// Util classes
import eaton.cs.sb2fw.SBLCP_local_terminal.util.*;
import eaton.cs.sb2fw.SBLCP_local_terminal.tests.*;

// Java Libraries
import java.util.Enumeration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.io.InputStream;
import java.io.StringReader;
//import java.awt.Desktop.Action;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortIOException;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import java.util.Timer;
import java.util.TimerTask;


// JavaFX libraries
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.event.*;
import javafx.concurrent.Task;
import javafx.stage.Stage;
import javafx.util.Pair;
import javafx.scene.paint.Color;
import javafx.scene.robot.Robot;



public class MainGUIcontroller {
	SblcpMessage message_send;
	SblcpMessage message_receive;
//	SblcpSocket sock;	// They used "sock" in udp_client.c
	public static SB2 currentSB2;
	FileWriter fileWriter;
	BufferedWriter bufferedWriter;
	
	/**
	 * The initialize() method is automatically called by JavaFX when the 
	 * corresponding FXML file is loaded and the controller is instantiated. 
	 * It serves as an initialization point for the controller, allowing 
	 * you to perform any setup or initialization tasks for the UI elements 
	 * and their associated data. 
	 */
	@FXML
	public void initialize() {
		systemOutPrintlnAndPrintLog("SBLCP Local Terminal starting...", LogLevel.INFO);
		
		// Create two SblcpMessage instances to hold the rx and tx message
//		message_send = new SblcpMessage(null); // TODO change null to correct stuff
//		message_receive = new SblcpMessage(null); // TODO change null to correct stuff
		
		// Create a socket instance using SblcpSocket
//		sock = new SblcpSocket(null, 0); // TODO change null and 0 to correct stuff
		
		// Create a SB2 class instance to save all the variables
		currentSB2 = new SB2();
		
		systemOutPrintlnAndPrintLog("Populating Serial Ports Menu...", LogLevel.INFO);
		populateSerialPorts();
		
		systemOutPrintlnAndPrintLog("Populated Serial Ports Menu", LogLevel.INFO);
		
		populateTripCurveTable();
//		tripCodeCodeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getKey()));
		tripCodeTimeStampColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getKey()));
		tripCodeCloudReportColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getValue()));
//		tripCodeTable.getColumns().add(tripCodeCodeColumn);
		tripCodeTimeStampColumn.prefWidthProperty().bind(tripCodeTable.widthProperty().multiply(0.5));
		tripCodeCloudReportColumn.prefWidthProperty().bind(tripCodeTable.widthProperty().multiply(0.5));
		tripCodeTable.getColumns().add(tripCodeTimeStampColumn);
		tripCodeTable.getColumns().add(tripCodeCloudReportColumn);
	}
	
	public static MainGUIcontroller instanceOfMainGUIcontroller;
	public MainGUIcontroller() {
		instanceOfMainGUIcontroller = this;
	}
	
	ObservableList<Pair<String, String>> tripTableItems = FXCollections.observableArrayList();
	// ####################################################################################
	//-------------------------------------------------------------------------------------
	// 			Below methods/variables are shared by both GUI and Util 
	//-------------------------------------------------------------------------------------
	// ####################################################################################
	private String inputtedIPAddress = "0.0.0.0";							// Default value
	private int inputtedPort = 32866;										// Default value
	private String inputtedUDPKey = "ba32b432c6e911edafa10242ac120002";		// Default value
	private int inputtedBaudRate = 115200;									// Default value
	private String inputtedSerialCOM = "COM7";								// Default value
	private String inputtedWifiSSID = "NETGEAR_11N";								// Default value
	private String inputtedWifiPassword = "sharedsecret";					// Default value
	private String commandChar = "Q";   										// for keeping track of which breaker we are connecting to
	private boolean deviceStatusBlockIsPopulated = false;					

	public boolean printToSerialPortOutputBox = true;
	
	
	// ####################################################################################
	//-------------------------------------------------------------------------------------
	// 			Below methods/variables are used to deal with SBLCP Util
	//-------------------------------------------------------------------------------------
	// ####################################################################################
	private boolean listening;
	
	/**
	 * 1. Start listening to serial com port object serialCom and outputting the message into 
	 *    the serial output box. 
	 * 2. Sets listening to true.
	 * 3. Catches JSON Strings that come from the esp32 dev board.
	 */
	public void startListening() {
		listening = true;
		systemOutPrintlnAndPrintLog("Started listening to " + inputtedSerialCOM + ".", LogLevel.INFO);
        
		Thread thread = new Thread(() -> {
        	ArrayList<Character> arrli = new ArrayList<>();
            while (listening) {
            	
            	char data = 0;
            	try {
            		data = serialCom.readInputStream();
            	} catch (SerialPortIOException e) {
            		displayAlertWindow(AlertType.ERROR, "Serial Port Error", "Serial Port Error", "Serial Port might be in used or something like that.");
            	}
                
                
                // If it is a \n (ascii = 10) that mean we are seeing the end of a line so we could print
                // out the whole line, else we didnt see the end of a line so keep collecting characters 
                // until we do see a \n
                if (data == 10) {	// 10 in ascii is \n
                    // Printing the characters as a string
                    StringBuilder stringBuilder = new StringBuilder();
                    for (int i = 0; i < arrli.size() - 1; ++i) {
                        stringBuilder.append(arrli.get(i));
                    }
                    
                    String outputString = stringBuilder.toString();
                    
                    
                    // Grab and update the response_counter value
                    int foundResponseCounter = findResponseCounterInResponse(outputString);
                    if (foundResponseCounter != -1) {
                    	currentSB2.updateResponseCounter(foundResponseCounter);
                    } else {/*We didnt get a response*/}

                    
                    if (printToSerialPortOutputBox) {
                    	systemOutPrintAndPrintInputStreamInSerialPortOutputBox(outputString);
                    }
                    
                    // Parse the string to JSON for IP list
                    String ipListJSONstring = findBreakersIPListJSONstring(outputString);
                    if (ipListJSONstring != "") {
                    	parseBreakersIPList(ipListJSONstring);
                    	populateBreakersOnTheNetworkSection();
                    }
                    
                    // Parse the string to JSON for device status
                    String deviceStatusJSONstring = findDeviceStatusJSONstring(outputString);
                    if (deviceStatusJSONstring != "") {
                    	parsedDeviceStatus = parseDeviceStatusJSONstring(deviceStatusJSONstring);
                    	updateDeviceStatusTable();
                    }
                    
                    // Parse the string to JSON for EOL Calib Param
                    String calibParamJSONstring = findCalibParamJSONstring(outputString);
                    if (calibParamJSONstring != "") {
                    	parsedCalibParam = parseCalibParamJSONstring(calibParamJSONstring);
                    	updateCalibParamTable();
                    }
                    
                    // Parse the string to JSON for EOL Calib Param
                    String nextUDPSeqJSONstring = findNextUDPSeqJSONstring(outputString);
                    if (nextUDPSeqJSONstring != "") {
                    	parsedNextUDPSeq = parseCalibParamJSONstring(nextUDPSeqJSONstring);
                    	updateNextUDPSeqTable();
                    }
                    
                    // Parse the string to int for handle position
                    int handle_position_temp = findHANDLE_POSITIONstate(outputString);
                    if (handle_position_temp != -1) {
                    	currentSB2.setHandlePositionState(handle_position_temp);
                    	updateHandlePositionLabel();
                    }
                    
                    
                    // Parse the string to JSON for device status
                    String deviceTripCodeJSONstring = findTripParamJSONstring(outputString);
                    if (deviceTripCodeJSONstring != "") {
                    	
                    	parsedTripCode = parseTripCodeJSONstring(deviceTripCodeJSONstring);
                    	
                    	updateTripTable();
                    }
                    
                    String deviceMacAddressJSONstring = findMacAddressJSONstring(outputString);
                    if (deviceMacAddressJSONstring != "") {
                    	parsedMacAddress = parseMacAddressJSONstring(deviceMacAddressJSONstring);
                    	System.out.println(parsedMacAddress.get(0).getValue());
                    	Platform.runLater(() -> {
                    		if (commandChar == "Q")
                    		{
                			ipAddressLabelForNum1.setText(parsedMacAddress.get(0).getValue().equals("") ? "-" : parsedMacAddress.get(0).getValue());}
                    		else if(commandChar == "W")
                    		{ipAddressLabelForNum2.setText(parsedMacAddress.get(0).getValue().equals("") ? "-" : parsedMacAddress.get(0).getValue());}
                    		else if(commandChar == "E")
                    		{ipAddressLabelForNum3.setText(parsedMacAddress.get(0).getValue().equals("") ? "-" : parsedMacAddress.get(0).getValue());}
                    		else if(commandChar == "R")
                    		{ipAddressLabelForNum4.setText(parsedMacAddress.get(0).getValue().equals("") ? "-" : parsedMacAddress.get(0).getValue());}
                    		else if(commandChar == "T")
                    		{ipAddressLabelForNum5.setText(parsedMacAddress.get(0).getValue().equals("") ? "-" : parsedMacAddress.get(0).getValue());}
                    		else if(commandChar == "Y")
                    		{ipAddressLabelForNum6.setText(parsedMacAddress.get(0).getValue().equals("") ? "-" : parsedMacAddress.get(0).getValue());}
                    		else if(commandChar == "U")
                    		{ipAddressLabelForNum7.setText(parsedMacAddress.get(0).getValue().equals("") ? "-" : parsedMacAddress.get(0).getValue());}
                    		else if(commandChar == "I")
                    		{ipAddressLabelForNum8.setText(parsedMacAddress.get(0).getValue().equals("") ? "-" : parsedMacAddress.get(0).getValue());}
                    		else if(commandChar == "O")
                    		{ipAddressLabelForNum9.setText(parsedMacAddress.get(0).getValue().equals("") ? "-" : parsedMacAddress.get(0).getValue());}
                    		else if(commandChar == "P")
                    		{ipAddressLabelForNum10.setText(parsedMacAddress.get(0).getValue().equals("") ? "-" : parsedMacAddress.get(0).getValue());}
                    		});
                    }
                    
                    
                    // Parse the string to JSON for Wifi Error JSON
                    String nextWifiErrorJSON = findNextWifiErrorJSONstring(outputString);
                    if (nextWifiErrorJSON != "") {
                    	parsedWifiErrorJSON = parseNextWifiErrorJSONstring(nextWifiErrorJSON);
                    	
                    	// Find for error, the JSON have several fields, each field, if == 1, means there is an error
                    	// eg, if, in the C code, function "sendWifiErrorJSON(1, 1, 0, 0)" is called, that means there
                    	// 	   is an error and this for loop and if-else will catch it, on the other hand, if 
                    	//	   "sendWifiErrorJSON(0, 0, 0, 0)" is called, it means no error and the for loop and if-else
                    	//     should maintain foundError to be false.
                    	boolean foundError = false;
                    	boolean inErrorState = false;	// To make sure that we only send new wifi stuff once
                    	for (int i = 0; i < parsedWifiErrorJSON.size(); ++i) {
                    		if ("1".compareTo(parsedWifiErrorJSON.get(i).getValue()) == 0) {
                    			// parsedWifiErrorJSON.get(i).getValue() is "1" aka error is found
                    			foundError = true;
                    			System.out.println("[DEBUG] mainGUIcontroller:startListening(): Error Found, attempting to resend wifi credentials.");
                    		}
                    	}
                    	
                    	if (foundError && !inErrorState) {
                    		try {
                    			sendWifiCredentialsToEsp32();
                    			inErrorState = true;
                    		} catch (IllegalStateException e) {
                    			// Very bad coding practice indeed
                    		}
                    	} else {
                    		// No error!
                    		inErrorState = false;
                    		hideProgressIndicatorAndLabel();
                    	}
                    }
                    
                    // Clears the array list
                    arrli = new ArrayList<>();
                    
                    lastCustomMessageNumber = -2;
                } else {
                	arrli.add(data);
                }
            }
        });
        
        thread.start();
    }
	
	/**
	 * Sets listening to false. Which will kill the thread and stop listening to the serial port
	 * object serialCom.
	 */
	public void stopListening() {
		systemOutPrintlnAndPrintLog("Stopped listening to " + inputtedSerialCOM + ".", LogLevel.INFO);
        listening = false;
    }
	
	// ####################################################################################
	//-------------------------------------------------------------------------------------
	// 			Below methods/variables are used to control the ui elements
	//-------------------------------------------------------------------------------------
	// ####################################################################################
	@FXML
	private VBox 	logContainer, 
					logContent, 
					serialPortOutputBoxContainer, 
					serialPortOutputBoxContent,
					deviceStatusVBox;
	@FXML
	private ScrollPane 	logScrollPane,
						deviceStatusScrollPane;
	@FXML
	private MenuButton ipVersionMenuButton, serialPortMenuButton, breakerMenuButton;
	@FXML
	private MenuItem topMenuFileQuit;
	@FXML
	private Button	connectionOkButton,				// This is unused lol
					customMessageButton,			// This is a button bar button
					sendMessageButton,				// This is a button bar button
					deviceStatusButton,				// This is a button bar button
					EOLCalibParamButton,			// This is a button bar button
					nextUDPSequenceNumberButton,	// This is a button bar button
					handlePositionButton,			// This is a button bar button
					manufacturingModeButton,		// This is a button bar button
					setLEDButton,					// This is a button bar button
					sbOmicYokoButton,				// This is a button bar button
					energyAccumTestButton,			// This is a button bar button
					measurementTestButton,			// This is a button bar button
					enduranceTestButton,			// This is a button bar button
					factoryResetButton,				// This is a button bar button
					tripCodeButton;					// This is a button bar button
	@FXML 
	private Pane	customMessageBlock,
					sendMessageBlock,
					deviceStatusBlock,
					EOLCalibParamBlock,
					nextUDPSequenceNumberBlock,
					handlePositionBlock,
					manufacturingModeBlock,
					setLEDBlock,
					sbOmicYokoBlock,
					energyAccumTestBlock,
					measurementTestBlock,
					enduranceTestBlock,
					factoryResetBlock,
					tripCodeBlock;
	@FXML
    private TextField 	ipAddressInputField,
					    portInputField,
					    udpKeyInputField,
					    baudRateInputField,
					    serialCOMInputField,
					    wifiSSIDInputField,
					    wifiPasswordInputField,
					    Ade9000_APhCal0Field,
					    Ade9000_AiGainField,
					    Ade9000_AiRmsSOSField,
					    Ade9000_ApGainField,
					    Ade9000_AvGainField,
					    Ade9000_BPhCal0Field,
					    Ade9000_BiGainField,
					    Ade9000_BiRmsSOSField,
					    Ade9000_BpGainField,
					    Ade9000_BvGainField,
					    setNextUDPSequenceNumberInputField,
					    searchTripCode;
	@FXML
	private Circle	bottomRightStatusCircle;
	@FXML
	private TableView<Pair<String, String>> deviceStatusMiscTable,
											deviceStatusPhaseATable,
											deviceStatusPhaseBTable,
											/*deviceStatusPowerTable,*/
											calibParamTable,
											nextUDPSeqTable,
											tripCodeReferenceTable,
											tripCodeTable;
	@FXML
	private Label	handlePositionSecondaryLabel,
					handlePositionPrimaryLabel,
					startListeningConnectionStatusLabel;
	@FXML
	private ProgressIndicator startListeningProcessIndicator;
	
	@FXML
	private ChoiceBox codeChoice;
	ObservableList<String> codeSelectionList = FXCollections.observableArrayList("Dec", "Hex");
	
	// Creating columns for the various tables
	TableColumn<Pair<String, String>, String> miscItemColumn = new TableColumn<>("Item");
	TableColumn<Pair<String, String>, String> miscDataColumn = new TableColumn<>("Data");
	TableColumn<Pair<String, String>, String> phaseAItemColumn = new TableColumn<>("phase A Item");
	TableColumn<Pair<String, String>, String> phaseADataColumn = new TableColumn<>("Data");
	TableColumn<Pair<String, String>, String> phaseBItemColumn = new TableColumn<>("phase B Item");
	TableColumn<Pair<String, String>, String> phaseBDataColumn = new TableColumn<>("Data");
	TableColumn<Pair<String, String>, String> powerTableItemColumn = new TableColumn<>("Item");
	TableColumn<Pair<String, String>, String> powerTableDataColumn = new TableColumn<>("Data");
	TableColumn<Pair<String, String>, String> calibParamParamColumn = new TableColumn<>("Parameters");
	TableColumn<Pair<String, String>, String> calibParamParamValueColumn = new TableColumn<>("Value");
	TableColumn<Pair<String, String>, String> nextUDPSeqItemColumn = new TableColumn<>("Item");
	TableColumn<Pair<String, String>, String> nextUDPSeqValueColumn = new TableColumn<>("Value");
	TableColumn<Pair<String, String>, String> tripCodeReferenceCodeColumn = new TableColumn<>("Code");
	TableColumn<Pair<String, String>, String> tripCodeReferenceValueColumn = new TableColumn<>("Value");
//	TableColumn<Pair<String, String>, String> tripCodeCodeColumn = new TableColumn<>("Code");
	TableColumn<Pair<String, String>, String> tripCodeTimeStampColumn = new TableColumn<>("Timestamp");
	TableColumn<Pair<String, String>, String> tripCodeCloudReportColumn = new TableColumn<>("Code");
	
	// ####################################################################################
	// ######################### Printing and Logging functions ###########################
	// ####################################################################################
	/**
	 * This method takes a string parameter and output it into the GUI's log window. Kinda like System.out.println
	 * but into the log window.
	 * 
	 * @param logMessage The string to be logged.
	 */
	public void printLog(String logMessage) {
	    Text logText = new Text(logMessage);
	    logText.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 12px;");
	    Platform.runLater(() -> {
	    	logContent.getChildren().add(logText);
	    });
	    
	    ScrollPane scrollPane = (ScrollPane) logContainer.getChildren().get(0);
	    scrollPane.layout();
        scrollPane.setVvalue(1.0);	// Scroll to the most recent line.
	}
	
	/**
	 * As the name suggests, it System.out.println and printLog a message.
	 * @param logMessage The string to be printed and logged.
	 */
	public void systemOutPrintlnAndPrintLog(String logMessage, LogLevel givenLogLevel) {
		if (givenLogLevel == LogLevel.INFO) {
			System.out.println("[INFO]\t " + logMessage);
			printLog("[INFO]\t " + logMessage);
		} else if (givenLogLevel == LogLevel.WARNING) {
			System.out.println("[WARNING]\t " + logMessage);
			printLog("[WARNING] " + logMessage);
		} else if (givenLogLevel == LogLevel.ERROR) {
			System.out.println("[ERROR]\t " + logMessage);
			printLog("[ERROR]\t " + logMessage);
		} else if (givenLogLevel == LogLevel.DEBUG) {
			System.out.println("[DEBUG]\t " + logMessage);
			printLog("[DEBUG]\t " + logMessage);
		} else {
			System.out.println(logMessage);
			printLog(logMessage);
		}
	}
	
	/**
	 * Just read the name of the function ;)
	 * @param logMessage The string to be logged.
	 */
	public void printInputStreamInSerialPortOutputBox(String logMessage) {
    	Platform.runLater(() -> {
    		Text logText = new Text(logMessage);
    		logText.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 12px;");
    		serialPortOutputBoxContent.getChildren().add(logText);
    		ScrollPane scrollPane = (ScrollPane) serialPortOutputBoxContainer.getChildren().get(0);
    		scrollPane.layout();
    		scrollPane.setVvalue(1.0);	// Scroll to the most recent line.
    	});
    	writeLogToLogTxtFile(logMessage);
	}
    
	/**
	 * Just read the name of the function ;)
	 * @param stuffToPrint The string to be printed and logged.
	 */
    public void systemOutPrintAndPrintInputStreamInSerialPortOutputBox(String stuffToPrint) {
		System.out.println(stuffToPrint);
		printInputStreamInSerialPortOutputBox(stuffToPrint);
	}
    
    /**
     * The program will save the outputted log to a txt file. Which is done by this method.
     * @param logMessage The string to be written into the txt file.
     */
    public void writeLogToLogTxtFile(String logMessage) {
    	try {
    		if (bufferedWriter != null) {
    			bufferedWriter.write(logMessage);
    			bufferedWriter.newLine(); // Add a new line
    		}
		} catch (IOException e) {
			systemOutPrintlnAndPrintLog("Error occurred with writing to log txt file: " + e.getMessage(), LogLevel.ERROR);
		}
    }
    
    /**
     * Stop buffer and stops writing to txt file.
     */
    public void closeLogTxtWritterBuffer() {
    	// Close the resources
        try {
        	if (bufferedWriter != null) {
        		bufferedWriter.close();
    			fileWriter.close();
        	}
		} catch (IOException e) {
			systemOutPrintlnAndPrintLog("Error occurred with closing log txt file: " + e.getMessage(), LogLevel.ERROR);
		}
    }
    
    /**
     * Display Alert Window (a pop-up window) with parameters given.
     * @param givenAlertType Alert Type (Warning, Error, etc.). 
     * @param givenTitle Title of window.
     * @param givenHeader Header of window.
     * @param givenContent Content of window.
     */
    public void displayAlertWindow(AlertType givenAlertType, 
				   String givenTitle, 
				   String givenHeader, 
				   String givenContent) {
	Alert alert = new Alert(givenAlertType);
	alert.setTitle(givenTitle);
	alert.setHeaderText(givenHeader);
	alert.setContentText(givenContent);
	
	alert.showAndWait();
	}
	
    
    // ##########################################################################
 	// The following code(s) is to handle the "Connection" section
    // ##########################################################################
	SblcpSerialCom serialCom;
	@FXML
	public void handleStartListeningButton(ActionEvent event) {
		try {
			if (serialCOMInputField.getText() == null || serialCOMInputField.getText() == "") {
				displayAlertWindow(AlertType.ERROR, "Select Serial Port!", "Select Serial Port!", "");
				return;
			}
			if (serialCom.getSerialPortOpenStatus()) {
				displayAlertWindow(AlertType.ERROR, "You are already listening!", "You are already listening!", "If you want to restart serial port, click \"Stop Listening\" before clicking \"Start Listening\".");
				return;
			}
			
		} catch (NullPointerException e) {
			// Yes, yes very bad practice I know
		}
		
		// Create a txt file to output serial port logs to
		LocalDateTime currentTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
        String formattedTime = currentTime.format(formatter);
		String serialLogOutputFilePath = "logs/serial_port_log_" + formattedTime + ".txt";
		System.out.println(serialLogOutputFilePath);
		try {
            // Open the file for writing using FileWriter and BufferedWriter
            fileWriter = new FileWriter(serialLogOutputFilePath);
            bufferedWriter = new BufferedWriter(fileWriter);
        } catch (IOException e) {
            systemOutPrintlnAndPrintLog("Error occurred when creating log txt file: " + e.getMessage(), LogLevel.ERROR);
        }
		
		handleConnectionConnectButton();	// This was the "Connect" button but it is confusing so I will
											// "press" the button in the code so user only need to click on
											// the "Start Listening" button to connect and start listening
											// to the serial port
		
		// Set bottom right serial com status light color
		if (serialCom.getSerialPortOpenStatus()) {
			Color green = Color.web("#30eb49");
			bottomRightStatusCircle.setFill(green);
		}
		
		printToSerialPortOutputBox = true;
		
		startListening();
		
		showProgressIndicatorAndLabel();
		
		sendWifiCredentialsToEsp32();
		
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		ipAddressAnchorPaneForNum1.setDisable(false);
		ipAddressAnchorPaneForNum2.setDisable(false);
		ipAddressAnchorPaneForNum3.setDisable(false);
		ipAddressAnchorPaneForNum4.setDisable(false);
		ipAddressAnchorPaneForNum5.setDisable(false);
		ipAddressAnchorPaneForNum6.setDisable(false);
		ipAddressAnchorPaneForNum7.setDisable(false);
		ipAddressAnchorPaneForNum8.setDisable(false);
		ipAddressAnchorPaneForNum9.setDisable(false);
		ipAddressAnchorPaneForNum10.setDisable(false);
		Color green = Color.web("#30eb49");
		ipAddressCircleForNum1.setFill(green);
		
		// Send test signal, if receive response, we are good to talk with breaker
		// (ps: idk but sometimes, if we dont SEND_GET_NEXT_EXPECTED_UDP_SEQUENCE_NUMBER
		// it wont work, weird)
//		handleSendGetNextExpectedUDPSequenceNumberButton(null);
		CallSBLCPCommand.call_NonCustom_SBLCPcommand_blocking(serialCom, CallSBLCPCommand.GET_NEXT_EXPECTED_UDP_SEQUENCE_NUMBER, null, 0);
//		handleSendGetDeviceStatusButton(null);
		int responseTime_ms = CallSBLCPCommand.call_NonCustom_SBLCPcommand_blocking(serialCom, CallSBLCPCommand.GET_DEVICE_STATUS, null, 0);
		if (responseTime_ms >= 0) updateDeviceStatusTable();
	}
	@FXML
	public void handleStopListeningButton(ActionEvent event) {
		stopListening();
		
		// Close the serial port when done
		String returnedString = serialCom.closeSerialPort();
		systemOutPrintlnAndPrintLog(returnedString, LogLevel.INFO);
		
		// Set bottom right serial com status light color
		if (!serialCom.getSerialPortOpenStatus()) {
			Color red = Color.web("#f22424");
			bottomRightStatusCircle.setFill(red);
		}
		
		serialCom = null;
		Color white = Color.web("#ffffff");
		hideProgressIndicatorAndSayDisconnectedOnLabel();
		// turn off LEDs
		ipAddressCircleForNum1.setFill(white);
		ipAddressCircleForNum2.setFill(white);
		ipAddressCircleForNum3.setFill(white);
		ipAddressCircleForNum4.setFill(white);
		ipAddressCircleForNum5.setFill(white);
		ipAddressCircleForNum6.setFill(white);
		ipAddressCircleForNum7.setFill(white);
		ipAddressCircleForNum8.setFill(white);
		ipAddressCircleForNum9.setFill(white);
		ipAddressCircleForNum10.setFill(white);
		
		ipAddressAnchorPaneForNum1.setDisable(true);
		ipAddressAnchorPaneForNum2.setDisable(true);
		ipAddressAnchorPaneForNum3.setDisable(true);
		ipAddressAnchorPaneForNum4.setDisable(true);
		ipAddressAnchorPaneForNum5.setDisable(true);
		ipAddressAnchorPaneForNum6.setDisable(true);
		ipAddressAnchorPaneForNum7.setDisable(true);
		ipAddressAnchorPaneForNum8.setDisable(true);
		ipAddressAnchorPaneForNum9.setDisable(true);
		ipAddressAnchorPaneForNum10.setDisable(true);
		
		// Close the log txt writer
        closeLogTxtWritterBuffer();
	}
	@FXML
	public void handleRebootEsp32Button(ActionEvent event) {
		boolean serialComActive = checkSerialComActiveIfNotWarnAndReturnFalse();
		if (!serialComActive) return;

//		handleStopListeningButton(null);
//		try {
//			Thread.sleep(1000);
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
//		handleStartListeningButton(null);
		
		
		CallSBLCPCommand.call_NonCustom_SBLCPcommand_blocking(serialCom, CallSBLCPCommand.REBOOT_ESP32, null, 0);
		
		showProgressIndicatorAndLabel();
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		sendWifiCredentialsToEsp32();
	}
	
	// ####################################################################################
	// The following code(s) is to handle the "Send Messages" block
	// ####################################################################################
	private final int NORMAL_TIMEOUT_MS = 5000;
	@FXML
	public int handleSendGetNextExpectedUDPSequenceNumberButton(ActionEvent event) {
		boolean serialComActive = checkSerialComActiveIfNotWarnAndReturnFalse();
		if (!serialComActive) return -1;

		int temp = CallSBLCPCommand.call_NonCustom_SBLCPcommand_blocking(serialCom, CallSBLCPCommand.GET_NEXT_EXPECTED_UDP_SEQUENCE_NUMBER, null, 2000/*This command usually only takes 600ms, so if it doesn't respond within 2000ms, it is not responding*/);
//		if (temp < 0) displayNoResponseAlertWindow();
		return temp;
	}
	@FXML
	public int handleSendGetMacAddress(ActionEvent event) {
		boolean serialComActive = checkSerialComActiveIfNotWarnAndReturnFalse();
		if (!serialComActive) return -1;

		int temp = CallSBLCPCommand.call_NonCustom_SBLCPcommand_blocking(serialCom, CallSBLCPCommand.GET_MAC_ADDRESS, null, NORMAL_TIMEOUT_MS);
		if (temp < 0) displayNoResponseAlertWindow();
		return temp;
	}
	
	@FXML
	public int handleSendSetNextExpectedUDPSequenceNumberButton(ActionEvent event) {
		boolean serialComActive = checkSerialComActiveIfNotWarnAndReturnFalse();
		if (!serialComActive) return -1;

		int temp = CallSBLCPCommand.call_NonCustom_SBLCPcommand_blocking(serialCom, CallSBLCPCommand.SET_NEXT_EXPECTED_UDP_SEQUENCE_NUMBER, null, NORMAL_TIMEOUT_MS);
		if (temp < 0) displayNoResponseAlertWindow();
		return temp;
	}
	@FXML
	public int handleSendGetRemoteHandlePositionButton(ActionEvent event) {
		boolean serialComActive = checkSerialComActiveIfNotWarnAndReturnFalse();
		if (!serialComActive) return -1;

		int temp = CallSBLCPCommand.call_NonCustom_SBLCPcommand_blocking(serialCom, CallSBLCPCommand.GET_REMOTE_HANDLE_POSISTION, null, NORMAL_TIMEOUT_MS);
		if (temp < 0) displayNoResponseAlertWindow();
		return temp;
	}
	@FXML
	public int handleSendGetPrimaryHandleStatusButton(ActionEvent event) {
		boolean serialComActive = checkSerialComActiveIfNotWarnAndReturnFalse();
		if (!serialComActive) return -1;

		int temp = CallSBLCPCommand.call_NonCustom_SBLCPcommand_blocking(serialCom, CallSBLCPCommand.GET_PRIMARY_HANDLE_STATUS, null, NORMAL_TIMEOUT_MS);
		if (temp < 0) displayNoResponseAlertWindow();
		return temp;
	}
	@FXML
	public int handleSendSetRemoteHandlePositionButtonOpen(ActionEvent event) {
		boolean serialComActive = checkSerialComActiveIfNotWarnAndReturnFalse();
		if (!serialComActive) return -1;

		int temp = CallSBLCPCommand.call_NonCustom_SBLCPcommand_blocking(serialCom, CallSBLCPCommand.SET_REMOTE_HANDLE_POSISTION_open, null, NORMAL_TIMEOUT_MS);
		if (temp < 0) displayNoResponseAlertWindow();
		return temp;
	}
	@FXML
	public int handleSendSetRemoteHandlePositionButtonClose(ActionEvent event) {
		boolean serialComActive = checkSerialComActiveIfNotWarnAndReturnFalse();
		if (!serialComActive) return -1;

		int temp = CallSBLCPCommand.call_NonCustom_SBLCPcommand_blocking(serialCom, CallSBLCPCommand.SET_REMOTE_HANDLE_POSISTION_close, null, NORMAL_TIMEOUT_MS);
		if (temp < 0) displayNoResponseAlertWindow();
		return temp;
	}
	@FXML
	public int handleSendSetRemoteHandlePositionButtonToggle(ActionEvent event) {
		boolean serialComActive = checkSerialComActiveIfNotWarnAndReturnFalse();
		if (!serialComActive) return -1;

		int temp = CallSBLCPCommand.call_NonCustom_SBLCPcommand_blocking(serialCom, CallSBLCPCommand.SET_REMOTE_HANDLE_POSISTION_toggle, null, NORMAL_TIMEOUT_MS);
		if (temp < 0) displayNoResponseAlertWindow();
		return temp;
	}
	@FXML
	public int handleSendSetManufacturingModeOnButton(ActionEvent event) {
		boolean serialComActive = checkSerialComActiveIfNotWarnAndReturnFalse();
		if (!serialComActive) return -1;
		
//		return CallSBLCPCommand.call_NonCustom_SBLCPcommand_blocking(serialCom, CallSBLCPCommand.SET_MANUFACTURING_MODE_ON, null, NORMAL_TIMEOUT_MS);
		int temp = CallSBLCPCommand.call_NonCustom_SBLCPcommand_blocking(serialCom, CallSBLCPCommand.SET_MANUFACTURING_MODE_ON, null, NORMAL_TIMEOUT_MS);
		if (temp < 0) displayNoResponseAlertWindow();
		return temp;
	}
	@FXML
	public int handleSendSetManufacturingModeOffButton(ActionEvent event) {
		boolean serialComActive = checkSerialComActiveIfNotWarnAndReturnFalse();
		if (!serialComActive) return -1;

		int temp = CallSBLCPCommand.call_NonCustom_SBLCPcommand_blocking(serialCom, CallSBLCPCommand.SET_MANUFACTURING_MODE_OFF, null, NORMAL_TIMEOUT_MS);
		if (temp < 0) displayNoResponseAlertWindow();
		return temp;
	}
	@FXML
	public int handleSendGetDeviceStatusButton(ActionEvent event) {
		boolean serialComActive = checkSerialComActiveIfNotWarnAndReturnFalse();
		if (!serialComActive) return -1;
		
		int responseTime_ms = CallSBLCPCommand.call_NonCustom_SBLCPcommand_blocking(serialCom, CallSBLCPCommand.GET_DEVICE_STATUS, null, NORMAL_TIMEOUT_MS);
		if (responseTime_ms >= 0) updateDeviceStatusTable();
		else displayNoResponseAlertWindow();
		
		return responseTime_ms;
	}
	@FXML
	public int handleSendGetMeterTelemetryDataButton(ActionEvent event) {
		boolean serialComActive = checkSerialComActiveIfNotWarnAndReturnFalse();
		if (!serialComActive) return -1;
		
		int responseTime_ms = CallSBLCPCommand.call_NonCustom_SBLCPcommand_blocking(serialCom, CallSBLCPCommand.GET_METER_TELEMETRT_DATA, null, NORMAL_TIMEOUT_MS);
		if (responseTime_ms >= 0) updateDeviceStatusTable();
		else displayNoResponseAlertWindow();
		
		return responseTime_ms;
	}
	@FXML
	public int handleSendSetLEDToUserDefinedColourButton(ActionEvent event) {
		boolean serialComActive = checkSerialComActiveIfNotWarnAndReturnFalse();
		if (!serialComActive) return -1;

		int temp = CallSBLCPCommand.call_NonCustom_SBLCPcommand_blocking(serialCom, CallSBLCPCommand.SET_LED_TO_USER_DEFINED_COLOUR, null, NORMAL_TIMEOUT_MS);
		if (temp < 0) displayNoResponseAlertWindow();
		return temp;
	}
	@FXML
	public int handleSendGetBreakerEOLCalibParamButton(ActionEvent event) {
		boolean serialComActive = checkSerialComActiveIfNotWarnAndReturnFalse();
		if (!serialComActive) return -1;
		
		int responseTime_ms = CallSBLCPCommand.call_NonCustom_SBLCPcommand_blocking(serialCom, CallSBLCPCommand.GET_BREAKER_EOL_CALIB_PARAM, null, NORMAL_TIMEOUT_MS);
		if (responseTime_ms >= 0) updateCalibParamTable();
		else displayNoResponseAlertWindow();
		
		return responseTime_ms;
	}
	@FXML
	public int handleSendSetBreakerEOLCalibParamButton(ActionEvent event) {
		boolean serialComActive = checkSerialComActiveIfNotWarnAndReturnFalse();
		if (!serialComActive) return -1;

		int temp = CallSBLCPCommand.call_NonCustom_SBLCPcommand_blocking(serialCom, CallSBLCPCommand.SET_BREAKER_EOL_CALIB_PARAM, null, NORMAL_TIMEOUT_MS);
		if (temp < 0) displayNoResponseAlertWindow();
		return temp;
	}
	@FXML
	public int handleSendResetEnergyButton(ActionEvent event) {
		boolean serialComActive = checkSerialComActiveIfNotWarnAndReturnFalse();
		if (!serialComActive) return -1;

		int temp = CallSBLCPCommand.call_NonCustom_SBLCPcommand_blocking(serialCom, CallSBLCPCommand.RESET_ENERGY, null, NORMAL_TIMEOUT_MS);
		if (temp < 0) displayNoResponseAlertWindow();
		return temp;
	}
	// TODO: change the trip code command to match what is required
	@FXML
	public int handleSendTripCodeButton(ActionEvent event) {
		boolean serialComActive = checkSerialComActiveIfNotWarnAndReturnFalse();
		if (!serialComActive) return -1;
		
		int responseTime_ms = CallSBLCPCommand.call_NonCustom_SBLCPcommand_blocking(serialCom, CallSBLCPCommand.GET_TRIP_CODE, null, NORMAL_TIMEOUT_MS);
		if (responseTime_ms >= 0) updateTripTable();
		else displayNoResponseAlertWindow();
		
		return responseTime_ms;
	}
	
	
	// TEMP
	@FXML
	public void handleTESTsendButton(ActionEvent event) throws InterruptedException {
		boolean serialComActive = checkSerialComActiveIfNotWarnAndReturnFalse();
		if (!serialComActive) return;
		
		String debugStr0 = serialCom.sendData("z");
		systemOutPrintlnAndPrintLog(debugStr0, LogLevel.INFO);
		
		String toSend = "#,999,0x00000000,0x0084,123;321;456;654;789;987;007;700;505;626;115,0";
		toSend += "\n";	// Add a newline to the end for c code to check for string completeness
		
		int temp = CallSBLCPCommand.call_Custom_SBLCPcommand_blocking(serialCom, toSend, null, 5000);
		if (temp < 0) displayNoResponseAlertWindow();
	}
	@FXML
	public void handleTempSendWifiCredButton(ActionEvent event) {
		sendWifiCredentialsToEsp32();
	}
	
	
	// ####################################################################################
	// ############### Button handlers - most button handlers live here ###################
	// ####################################################################################
	/**
	 * This handles when user press "Quit" in top menu. (pretty useless)
	 */
	@FXML
    public void handleTopMenuFileQuit() {
		// ipAddressInputField is only used to grab the scene, it got nothing to do with closing the window
        Stage stage = (Stage) ipAddressInputField.getScene().getWindow();
        stage.close();
    }
	
	/**
	 * Unused method. But this handles the drop down menu for choosing ipv4 and ipv6.
	 * @param event
	 */
	@FXML
    public void handleIPVersionMenuItemAction(ActionEvent event) {
        MenuItem selectedItem = (MenuItem) event.getSource();
        String selectedText = selectedItem.getText();
        ipVersionMenuButton.setText(selectedText);
    }
	
	/**
	 * There used to be a button called "Connect" which is handled by this method. This method
	 * basically will create a serial port and set the state for the settings (baud rate, wifi
	 * ssid, etc). 
	 * 
	 * Note: No button controls this method anymore, it is called in handleStartListeningButton().
	 */
	@FXML
	public void handleConnectionConnectButton() {
		serialCom = null;
		if (udpKeyInputField.getText().length() != 0) {
			inputtedUDPKey = udpKeyInputField.getText();
		} // If nothing is inputted, use default value
		if (serialCOMInputField.getText().length() != 0) {
			inputtedSerialCOM = serialCOMInputField.getText();
		} // If nothing is inputted, use default value
		if (wifiSSIDInputField.getText().length() != 0) {
			inputtedWifiSSID = wifiSSIDInputField.getText();
		} // If nothing is inputted, use default value
		if (wifiPasswordInputField.getText().length() != 0) {
			inputtedWifiPassword = wifiPasswordInputField.getText();
		} // If nothing is inputted, use default value
		
		systemOutPrintlnAndPrintLog("Inputted info:\n\t  IP\t: " + inputtedIPAddress + 
							",\n\t  Port\t: " + inputtedPort + 
							",\n\t  UDP\t: " + inputtedUDPKey + 
							",\n\t  Baud\t: " + inputtedBaudRate + 
							",\n\t  Serial Port\t: " + inputtedSerialCOM +
							",\n\t  Wifi SSID\t: " + inputtedWifiSSID + 
							",\n\t  Wifi Password\t: " + inputtedWifiPassword + "\n"
							, LogLevel.INFO);
		
		// Create a instance of SblcpSerialCom class
		try {
			serialCom = new SblcpSerialCom(inputtedSerialCOM, inputtedBaudRate, 30000000);
			systemOutPrintlnAndPrintLog("SblcpSerialCom instance created successfully.", LogLevel.INFO);
		} catch (Exception e) {
			systemOutPrintlnAndPrintLog("SblcpSerialCom instance creation fail with error:\n" + e.toString(), LogLevel.WARNING);
		}
	}
	
	/**
	 * Button bar menu is controlled by this method. Controls which "blocks" aka "pages" is shown.
	 * @param event
	 */
	@SuppressWarnings("unchecked")
	@FXML
	public void handleButtonBarButtonsAction(ActionEvent event) {
		if (event.getSource() == customMessageButton) {
			customMessageBlock.setVisible(true);
			sendMessageBlock.setVisible(false);
			deviceStatusBlock.setVisible(false);
			EOLCalibParamBlock.setVisible(false);
			nextUDPSequenceNumberBlock.setVisible(false);
			handlePositionBlock.setVisible(false);
			manufacturingModeBlock.setVisible(false);
			setLEDBlock.setVisible(false);
			sbOmicYokoBlock.setVisible(false);
			energyAccumTestBlock.setVisible(false);
			measurementTestBlock.setVisible(false);
			enduranceTestBlock.setVisible(false);
			factoryResetBlock.setVisible(false);
			tripCodeBlock.setVisible(false);
    	} else if (event.getSource() == sendMessageButton) {
			customMessageBlock.setVisible(false);
    		sendMessageBlock.setVisible(true);
    		deviceStatusBlock.setVisible(false);
    		EOLCalibParamBlock.setVisible(false);
			nextUDPSequenceNumberBlock.setVisible(false);
			handlePositionBlock.setVisible(false);
			manufacturingModeBlock.setVisible(false);
			setLEDBlock.setVisible(false);
			sbOmicYokoBlock.setVisible(false);
			energyAccumTestBlock.setVisible(false);
			measurementTestBlock.setVisible(false);
			enduranceTestBlock.setVisible(false);
			factoryResetBlock.setVisible(false);
			tripCodeBlock.setVisible(false);
    	} else if (event.getSource() == deviceStatusButton) {
    		customMessageBlock.setVisible(false);
    		sendMessageBlock.setVisible(false);
            deviceStatusBlock.setVisible(true);
            EOLCalibParamBlock.setVisible(false);
			nextUDPSequenceNumberBlock.setVisible(false);
			handlePositionBlock.setVisible(false);
			manufacturingModeBlock.setVisible(false);
			setLEDBlock.setVisible(false);
			sbOmicYokoBlock.setVisible(false);
			energyAccumTestBlock.setVisible(false);
			measurementTestBlock.setVisible(false);
			enduranceTestBlock.setVisible(false);
			factoryResetBlock.setVisible(false);
			tripCodeBlock.setVisible(false);
			processDeviceStatusTable();
			deviceStatusBlockIsPopulated = true;
		} else if (event.getSource() == EOLCalibParamButton) {
			customMessageBlock.setVisible(false);
    		sendMessageBlock.setVisible(false);
    		deviceStatusBlock.setVisible(false);
    		EOLCalibParamBlock.setVisible(true);
			nextUDPSequenceNumberBlock.setVisible(false);
			handlePositionBlock.setVisible(false);
			manufacturingModeBlock.setVisible(false);
			setLEDBlock.setVisible(false);
			sbOmicYokoBlock.setVisible(false);
			energyAccumTestBlock.setVisible(false);
			measurementTestBlock.setVisible(false);
			enduranceTestBlock.setVisible(false);
			factoryResetBlock.setVisible(false);
			tripCodeBlock.setVisible(false);
			calibParamParamColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getKey()));
			calibParamParamValueColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getValue()));
			
			if (!calibParamTable.getColumns().contains(calibParamParamColumn)) calibParamTable.getColumns().add(calibParamParamColumn);
			if (!calibParamTable.getColumns().contains(calibParamParamValueColumn)) calibParamTable.getColumns().add(calibParamParamValueColumn);
			
			updateCalibParamTable();
    	} else if (event.getSource() == nextUDPSequenceNumberButton) {
			customMessageBlock.setVisible(false);
    		sendMessageBlock.setVisible(false);
    		deviceStatusBlock.setVisible(false);
    		EOLCalibParamBlock.setVisible(false);
			nextUDPSequenceNumberBlock.setVisible(true);
			handlePositionBlock.setVisible(false);
			manufacturingModeBlock.setVisible(false);
			setLEDBlock.setVisible(false);
			sbOmicYokoBlock.setVisible(false);
			energyAccumTestBlock.setVisible(false);
			measurementTestBlock.setVisible(false);
			enduranceTestBlock.setVisible(false);
			factoryResetBlock.setVisible(false);
			tripCodeBlock.setVisible(false);
			nextUDPSeqItemColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getKey()));
			nextUDPSeqValueColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getValue()));
			
			if (!nextUDPSeqTable.getColumns().contains(nextUDPSeqItemColumn)) nextUDPSeqTable.getColumns().add(nextUDPSeqItemColumn);
			if (!nextUDPSeqTable.getColumns().contains(nextUDPSeqValueColumn)) nextUDPSeqTable.getColumns().add(nextUDPSeqValueColumn);
			
			updateNextUDPSeqTable();
    	} else if (event.getSource() == handlePositionButton) {
			customMessageBlock.setVisible(false);
    		sendMessageBlock.setVisible(false);
    		deviceStatusBlock.setVisible(false);
    		EOLCalibParamBlock.setVisible(false);
			nextUDPSequenceNumberBlock.setVisible(false);
			handlePositionBlock.setVisible(true);
			manufacturingModeBlock.setVisible(false);
			setLEDBlock.setVisible(false);
			sbOmicYokoBlock.setVisible(false);
			energyAccumTestBlock.setVisible(false);
			measurementTestBlock.setVisible(false);
			enduranceTestBlock.setVisible(false);
			factoryResetBlock.setVisible(false);
			tripCodeBlock.setVisible(false);
    	} else if (event.getSource() == manufacturingModeButton) {
			customMessageBlock.setVisible(false);
    		sendMessageBlock.setVisible(false);
    		deviceStatusBlock.setVisible(false);
    		EOLCalibParamBlock.setVisible(false);
			nextUDPSequenceNumberBlock.setVisible(false);
			handlePositionBlock.setVisible(false);
			manufacturingModeBlock.setVisible(true);
			setLEDBlock.setVisible(false);
			sbOmicYokoBlock.setVisible(false);
			energyAccumTestBlock.setVisible(false);
			measurementTestBlock.setVisible(false);
			enduranceTestBlock.setVisible(false);
			factoryResetBlock.setVisible(false);
			tripCodeBlock.setVisible(false);
    	} else if (event.getSource() == setLEDButton) {
			customMessageBlock.setVisible(false);
    		sendMessageBlock.setVisible(false);
    		deviceStatusBlock.setVisible(false);
    		EOLCalibParamBlock.setVisible(false);
			nextUDPSequenceNumberBlock.setVisible(false);
			handlePositionBlock.setVisible(false);
			manufacturingModeBlock.setVisible(false);
			setLEDBlock.setVisible(true);
			sbOmicYokoBlock.setVisible(false);
			energyAccumTestBlock.setVisible(false);
			measurementTestBlock.setVisible(false);
			enduranceTestBlock.setVisible(false);
			factoryResetBlock.setVisible(false);
			tripCodeBlock.setVisible(false);
    	} else if (event.getSource() == sbOmicYokoButton) {
			customMessageBlock.setVisible(false);
    		sendMessageBlock.setVisible(false);
    		deviceStatusBlock.setVisible(false);
    		EOLCalibParamBlock.setVisible(false);
			nextUDPSequenceNumberBlock.setVisible(false);
			handlePositionBlock.setVisible(false);
			manufacturingModeBlock.setVisible(false);
			setLEDBlock.setVisible(false);
			sbOmicYokoBlock.setVisible(true);
			energyAccumTestBlock.setVisible(false);
			measurementTestBlock.setVisible(false);
			enduranceTestBlock.setVisible(false);
			factoryResetBlock.setVisible(false);
			tripCodeBlock.setVisible(false);
    	} else if (event.getSource() == energyAccumTestButton) {
			customMessageBlock.setVisible(false);
			sendMessageBlock.setVisible(false);
			deviceStatusBlock.setVisible(false);
			EOLCalibParamBlock.setVisible(false);
			nextUDPSequenceNumberBlock.setVisible(false);
			handlePositionBlock.setVisible(false);
			manufacturingModeBlock.setVisible(false);
			setLEDBlock.setVisible(false);
			sbOmicYokoBlock.setVisible(false);
			energyAccumTestBlock.setVisible(true);
			measurementTestBlock.setVisible(false);
			enduranceTestBlock.setVisible(false);
			factoryResetBlock.setVisible(false);
			tripCodeBlock.setVisible(false);
    	} else if (event.getSource() == measurementTestButton) {
			customMessageBlock.setVisible(false);
			sendMessageBlock.setVisible(false);
			deviceStatusBlock.setVisible(false);
			EOLCalibParamBlock.setVisible(false);
			nextUDPSequenceNumberBlock.setVisible(false);
			handlePositionBlock.setVisible(false);
			manufacturingModeBlock.setVisible(false);
			setLEDBlock.setVisible(false);
			sbOmicYokoBlock.setVisible(false);
			energyAccumTestBlock.setVisible(false);
			measurementTestBlock.setVisible(true);
			enduranceTestBlock.setVisible(false);
			factoryResetBlock.setVisible(false);
			tripCodeBlock.setVisible(false);
    	} else if (event.getSource() == enduranceTestButton) {
			customMessageBlock.setVisible(false);
			sendMessageBlock.setVisible(false);
			deviceStatusBlock.setVisible(false);
			EOLCalibParamBlock.setVisible(false);
			nextUDPSequenceNumberBlock.setVisible(false);
			handlePositionBlock.setVisible(false);
			manufacturingModeBlock.setVisible(false);
			setLEDBlock.setVisible(false);
			sbOmicYokoBlock.setVisible(false);
			energyAccumTestBlock.setVisible(false);
			measurementTestBlock.setVisible(false);
			enduranceTestBlock.setVisible(true);
			factoryResetBlock.setVisible(false);
			tripCodeBlock.setVisible(false);
    	} else if (event.getSource() == factoryResetButton) {
			customMessageBlock.setVisible(false);
			sendMessageBlock.setVisible(false);
			deviceStatusBlock.setVisible(false);
			EOLCalibParamBlock.setVisible(false);
			nextUDPSequenceNumberBlock.setVisible(false);
			handlePositionBlock.setVisible(false);
			manufacturingModeBlock.setVisible(false);
			setLEDBlock.setVisible(false);
			sbOmicYokoBlock.setVisible(false);
			energyAccumTestBlock.setVisible(false);
			measurementTestBlock.setVisible(false);
			enduranceTestBlock.setVisible(false);
			factoryResetBlock.setVisible(true);
			tripCodeBlock.setVisible(false);
    	} else if (event.getSource() == tripCodeButton) {
    		customMessageBlock.setVisible(false);
			sendMessageBlock.setVisible(false);
			deviceStatusBlock.setVisible(false);
			EOLCalibParamBlock.setVisible(false);
			nextUDPSequenceNumberBlock.setVisible(false);
			handlePositionBlock.setVisible(false);
			manufacturingModeBlock.setVisible(false);
			setLEDBlock.setVisible(false);
			sbOmicYokoBlock.setVisible(false);
			energyAccumTestBlock.setVisible(false);
			measurementTestBlock.setVisible(false);
			enduranceTestBlock.setVisible(false);
			factoryResetBlock.setVisible(false);
			tripCodeBlock.setVisible(true);
    	}
    	else {
    		// Unreachable, I just think it is a good idea to cover all cases (habit I got after writing too much VHDL)
			customMessageBlock.setVisible(false);
			sendMessageBlock.setVisible(false);
			deviceStatusBlock.setVisible(false);
			EOLCalibParamBlock.setVisible(false);
			nextUDPSequenceNumberBlock.setVisible(false);
			handlePositionBlock.setVisible(false);
			manufacturingModeBlock.setVisible(false);
			setLEDBlock.setVisible(false);
			sbOmicYokoBlock.setVisible(false);
			energyAccumTestBlock.setVisible(false);
			measurementTestBlock.setVisible(false);
			enduranceTestBlock.setVisible(false);
			factoryResetBlock.setVisible(false);
			tripCodeBlock.setVisible(false);
    	}
	}
	
	@FXML
    public void handleSerialPortMenuItemAction(ActionEvent event) {
        MenuItem selectedItem = (MenuItem) event.getSource();
        String selectedText = selectedItem.getText();
        serialCOMInputField.setText(selectedText);
    }
	
	@FXML
    public void handleBreakerSelectionMenuItemAction(ActionEvent event) {
		MenuItem selectedItem = (MenuItem) event.getSource();
        String selectedText = selectedItem.getText();
        breakerMenuButton.setText(selectedText);
	}
	
	@FXML
	public void handleCustomMessageBlockClearOutputButton(ActionEvent event) {
	    Platform.runLater(() -> {
	        serialPortOutputBoxContent.getChildren().clear();
	        System.gc(); // Suggest garbage collection to release memory
	    });
	}

	
	// ####################################################################################
	// ######### Progress Indicator Controllers for the "Start Listening" Button ##########
	// ####################################################################################
				 // vvv This is a rare case of non descriptive function names, and it is too late to change the names now  (because everything is so tight together), showProgressIndicatorAndLabel and hideProgressIndicatorAndLabel are to handle the process indicator next to the "Start Listening" button.
	private void showProgressIndicatorAndLabel() {
		startListeningProcessIndicator.setVisible(true);
		startListeningConnectionStatusLabel.setVisible(true);
		startListeningConnectionStatusLabel.setText("Connecting...");
	}
	private void hideProgressIndicatorAndLabel() {
		Platform.runLater(() -> {
			startListeningProcessIndicator.setVisible(false);
			startListeningConnectionStatusLabel.setVisible(true);
			startListeningConnectionStatusLabel.setText("Connected!");
		});
	}
	private void hideProgressIndicatorAndSayDisconnectedOnLabel() {
		Platform.runLater(() -> {
			startListeningProcessIndicator.setVisible(false);
			startListeningConnectionStatusLabel.setVisible(true);
			startListeningConnectionStatusLabel.setText("Disconnected");
		});
	}
	
	
	// ####################################################################################
	// ##### The following code(s) is to handle the "Breakers On the Network" section #####
	// ####################################################################################
	@FXML
	public Label 	ipAddressLabelForNum1,
					ipAddressLabelForNum2,
					ipAddressLabelForNum3,
					ipAddressLabelForNum4,
					ipAddressLabelForNum5,
					ipAddressLabelForNum6,
					ipAddressLabelForNum7,
					ipAddressLabelForNum8,
					ipAddressLabelForNum9,
					ipAddressLabelForNum10;
	@FXML
	public Circle	ipAddressCircleForNum1,
					ipAddressCircleForNum2,
					ipAddressCircleForNum3,
					ipAddressCircleForNum4,
					ipAddressCircleForNum5,
					ipAddressCircleForNum6,
					ipAddressCircleForNum7,
					ipAddressCircleForNum8,
					ipAddressCircleForNum9,
					ipAddressCircleForNum10;
	@FXML
	public AnchorPane 	ipAddressAnchorPaneForNum1,
						ipAddressAnchorPaneForNum2,
						ipAddressAnchorPaneForNum3,
						ipAddressAnchorPaneForNum4,
						ipAddressAnchorPaneForNum5,
						ipAddressAnchorPaneForNum6,
						ipAddressAnchorPaneForNum7,
						ipAddressAnchorPaneForNum8,
						ipAddressAnchorPaneForNum9,
						ipAddressAnchorPaneForNum10;
	
	@FXML
	public CheckBox checkbox1,
					checkbox2,
					checkbox3,
					checkbox4,
					checkbox5,
					checkbox6,
					checkbox7,
					checkbox8,
					checkbox9,
					checkbox10;
	
	public Boolean selectedBreakerList[] = {false, false, false, false, false, false, false, false, false, false}; 
	@FXML
	public void identifyBreakersOnTheNetworkConnectButtonNum1(ActionEvent event) {
		//curr_index = main_index 
		//temp_command_char = CommandChar;
		
		
		//select the breaker
		CallSBLCPCommand.call_NonCustom_SBLCPcommand_blocking(serialCom, "Q", null, 0); //select the 1st breaker
		CallSBLCPCommand.call_NonCustom_SBLCPcommand_blocking(serialCom, "1", null, 0); //send update seq number
		CallSBLCPCommand.call_NonCustom_SBLCPcommand_blocking(serialCom, "!", null, 0); // send identify me
		
		CallSBLCPCommand.call_NonCustom_SBLCPcommand_blocking(serialCom, commandChar, null, 0); // select the breaker again, to the current commandChar
		CallSBLCPCommand.call_NonCustom_SBLCPcommand_blocking(serialCom, "1", null, 0);
		//index = cu
		// bring back tar
		
	}
	@FXML
	public void identifyBreakersOnTheNetworkConnectButtonNum2(ActionEvent event) {
		//select the breaker
		CallSBLCPCommand.call_NonCustom_SBLCPcommand_blocking(serialCom, "W", null, 0); //select the breaker
		CallSBLCPCommand.call_NonCustom_SBLCPcommand_blocking(serialCom, "1", null, 0); //send update seq number
		CallSBLCPCommand.call_NonCustom_SBLCPcommand_blocking(serialCom, "!", null, 0); // send identify me
				
		CallSBLCPCommand.call_NonCustom_SBLCPcommand_blocking(serialCom, commandChar, null, 0); // select the breaker again, to the current commandChar
		CallSBLCPCommand.call_NonCustom_SBLCPcommand_blocking(serialCom, "1", null, 0);
	}
	@FXML
	public void identifyBreakersOnTheNetworkConnectButtonNum3(ActionEvent event) {
		CallSBLCPCommand.call_NonCustom_SBLCPcommand_blocking(serialCom, "E", null, 0); //select the breaker
		CallSBLCPCommand.call_NonCustom_SBLCPcommand_blocking(serialCom, "1", null, 0); //send update seq number
		CallSBLCPCommand.call_NonCustom_SBLCPcommand_blocking(serialCom, "!", null, 0); // send identify me
				
		CallSBLCPCommand.call_NonCustom_SBLCPcommand_blocking(serialCom, commandChar, null, 0); // select the breaker again, to the current commandChar
		CallSBLCPCommand.call_NonCustom_SBLCPcommand_blocking(serialCom, "1", null, 0);
	}
	@FXML
	public void identifyBreakersOnTheNetworkConnectButtonNum4(ActionEvent event) {
		CallSBLCPCommand.call_NonCustom_SBLCPcommand_blocking(serialCom, "R", null, 0);
		CallSBLCPCommand.call_NonCustom_SBLCPcommand_blocking(serialCom, "1", null, 0); //send update seq number
		CallSBLCPCommand.call_NonCustom_SBLCPcommand_blocking(serialCom, "!", null, 0); // send identify me
				
		CallSBLCPCommand.call_NonCustom_SBLCPcommand_blocking(serialCom, commandChar, null, 0); // select the breaker again, to the current commandChar
		CallSBLCPCommand.call_NonCustom_SBLCPcommand_blocking(serialCom, "1", null, 0);
	}
	@FXML
	public void identifyBreakersOnTheNetworkConnectButtonNum5(ActionEvent event) {
		CallSBLCPCommand.call_NonCustom_SBLCPcommand_blocking(serialCom, "T", null, 0);
		CallSBLCPCommand.call_NonCustom_SBLCPcommand_blocking(serialCom, "1", null, 0); //send update seq number
		CallSBLCPCommand.call_NonCustom_SBLCPcommand_blocking(serialCom, "!", null, 0); // send identify me
				
		CallSBLCPCommand.call_NonCustom_SBLCPcommand_blocking(serialCom, commandChar, null, 0); // select the breaker again, to the current commandChar
		CallSBLCPCommand.call_NonCustom_SBLCPcommand_blocking(serialCom, "1", null, 0);
	}
	@FXML
	public void identifyBreakersOnTheNetworkConnectButtonNum6(ActionEvent event) {
		CallSBLCPCommand.call_NonCustom_SBLCPcommand_blocking(serialCom, "Y", null, 0);
		CallSBLCPCommand.call_NonCustom_SBLCPcommand_blocking(serialCom, "1", null, 0); //send update seq number
		CallSBLCPCommand.call_NonCustom_SBLCPcommand_blocking(serialCom, "!", null, 0); // send identify me
				
		CallSBLCPCommand.call_NonCustom_SBLCPcommand_blocking(serialCom, commandChar, null, 0); // select the breaker again, to the current commandChar
		CallSBLCPCommand.call_NonCustom_SBLCPcommand_blocking(serialCom, "1", null, 0);
	}
	@FXML
	public void identifyBreakersOnTheNetworkConnectButtonNum7(ActionEvent event) {
		CallSBLCPCommand.call_NonCustom_SBLCPcommand_blocking(serialCom, "U", null, 0);
		CallSBLCPCommand.call_NonCustom_SBLCPcommand_blocking(serialCom, "1", null, 0); //send update seq number
		CallSBLCPCommand.call_NonCustom_SBLCPcommand_blocking(serialCom, "!", null, 0); // send identify me
				
		CallSBLCPCommand.call_NonCustom_SBLCPcommand_blocking(serialCom, commandChar, null, 0); // select the breaker again, to the current commandChar
		CallSBLCPCommand.call_NonCustom_SBLCPcommand_blocking(serialCom, "1", null, 0);
	}
	@FXML
	public void identifyBreakersOnTheNetworkConnectButtonNum8(ActionEvent event) {
		CallSBLCPCommand.call_NonCustom_SBLCPcommand_blocking(serialCom, "I", null, 0);
		CallSBLCPCommand.call_NonCustom_SBLCPcommand_blocking(serialCom, "1", null, 0); //send update seq number
		CallSBLCPCommand.call_NonCustom_SBLCPcommand_blocking(serialCom, "!", null, 0); // send identify me
				
		CallSBLCPCommand.call_NonCustom_SBLCPcommand_blocking(serialCom, commandChar, null, 0); // select the breaker again, to the current commandChar
		CallSBLCPCommand.call_NonCustom_SBLCPcommand_blocking(serialCom, "1", null, 0);
	}
	@FXML
	public void identifyBreakersOnTheNetworkConnectButtonNum9(ActionEvent event) {
		CallSBLCPCommand.call_NonCustom_SBLCPcommand_blocking(serialCom, "O", null, 0);
		CallSBLCPCommand.call_NonCustom_SBLCPcommand_blocking(serialCom, "1", null, 0); //send update seq number
		CallSBLCPCommand.call_NonCustom_SBLCPcommand_blocking(serialCom, "!", null, 0); // send identify me
				
		CallSBLCPCommand.call_NonCustom_SBLCPcommand_blocking(serialCom, commandChar, null, 0); // select the breaker again, to the current commandChar
		CallSBLCPCommand.call_NonCustom_SBLCPcommand_blocking(serialCom, "1", null, 0);
	}
	@FXML
	public void identifyBreakersOnTheNetworkConnectButtonNum10(ActionEvent event) {
		CallSBLCPCommand.call_NonCustom_SBLCPcommand_blocking(serialCom, "P", null, 0);	
		CallSBLCPCommand.call_NonCustom_SBLCPcommand_blocking(serialCom, "1", null, 0); //send update seq number
		CallSBLCPCommand.call_NonCustom_SBLCPcommand_blocking(serialCom, "!", null, 0); // send identify me
				
		CallSBLCPCommand.call_NonCustom_SBLCPcommand_blocking(serialCom, commandChar, null, 0); // select the breaker again, to the current commandChar
		CallSBLCPCommand.call_NonCustom_SBLCPcommand_blocking(serialCom, "1", null, 0);
	}
	@FXML
	public void handleBreakersOnTheNetworkConnectButtonNum1(ActionEvent event) {
		Color green = Color.web("#30eb49");
		Color white = Color.web("#ffffff");
		commandChar = "Q";
		// main_index = 
		CallSBLCPCommand.call_NonCustom_SBLCPcommand_blocking(serialCom, commandChar, null, 0);
		ipAddressCircleForNum1.setFill(green);
		ipAddressCircleForNum2.setFill(white);
		ipAddressCircleForNum3.setFill(white);
		ipAddressCircleForNum4.setFill(white);
		ipAddressCircleForNum5.setFill(white);
		ipAddressCircleForNum6.setFill(white);
		ipAddressCircleForNum7.setFill(white);
		ipAddressCircleForNum8.setFill(white);
		ipAddressCircleForNum9.setFill(white);
		ipAddressCircleForNum10.setFill(white);
		CallSBLCPCommand.call_NonCustom_SBLCPcommand_blocking(serialCom, "1", null, 0);
	}
	@FXML
	public void handleBreakersOnTheNetworkConnectButtonNum2(ActionEvent event) {
		System.out.println("Yess..it hit this NUM2\n");
		Color green = Color.web("#30eb49");
		Color white = Color.web("#ffffff");
		commandChar = "W";
		CallSBLCPCommand.call_NonCustom_SBLCPcommand_blocking(serialCom, commandChar, null, 0);
		ipAddressCircleForNum1.setFill(white);
		ipAddressCircleForNum2.setFill(green);
		ipAddressCircleForNum3.setFill(white);
		ipAddressCircleForNum4.setFill(white);
		ipAddressCircleForNum5.setFill(white);
		ipAddressCircleForNum6.setFill(white);
		ipAddressCircleForNum7.setFill(white);
		ipAddressCircleForNum8.setFill(white);
		ipAddressCircleForNum9.setFill(white);
		ipAddressCircleForNum10.setFill(white);
		CallSBLCPCommand.call_NonCustom_SBLCPcommand_blocking(serialCom, "1", null, 0);
	}
	@FXML
	public void handleBreakersOnTheNetworkConnectButtonNum3(ActionEvent event) {
		System.out.println("Yess..it hit this NUM3\n");
		Color green = Color.web("#30eb49");
		Color white = Color.web("#ffffff");
		commandChar = "E";
		CallSBLCPCommand.call_NonCustom_SBLCPcommand_blocking(serialCom, commandChar, null, 0);
		ipAddressCircleForNum1.setFill(white);
		ipAddressCircleForNum2.setFill(white);
		ipAddressCircleForNum3.setFill(green);
		ipAddressCircleForNum4.setFill(white);
		ipAddressCircleForNum5.setFill(white);
		ipAddressCircleForNum6.setFill(white);
		ipAddressCircleForNum7.setFill(white);
		ipAddressCircleForNum8.setFill(white);
		ipAddressCircleForNum9.setFill(white);
		ipAddressCircleForNum10.setFill(white);
		CallSBLCPCommand.call_NonCustom_SBLCPcommand_blocking(serialCom, "1", null, 0);
	}
	@FXML
	public void handleBreakersOnTheNetworkConnectButtonNum4(ActionEvent event) {
		Color green = Color.web("#30eb49");
		Color white = Color.web("#ffffff");
		commandChar = "R";
		CallSBLCPCommand.call_NonCustom_SBLCPcommand_blocking(serialCom, commandChar, null, 0);
		ipAddressCircleForNum1.setFill(white);
		ipAddressCircleForNum2.setFill(white);
		ipAddressCircleForNum3.setFill(white);
		ipAddressCircleForNum4.setFill(green);
		ipAddressCircleForNum5.setFill(white);
		ipAddressCircleForNum6.setFill(white);
		ipAddressCircleForNum7.setFill(white);
		ipAddressCircleForNum8.setFill(white);
		ipAddressCircleForNum9.setFill(white);
		ipAddressCircleForNum10.setFill(white);
		CallSBLCPCommand.call_NonCustom_SBLCPcommand_blocking(serialCom, "1", null, 0);
	}
	@FXML
	public void handleBreakersOnTheNetworkConnectButtonNum5(ActionEvent event) {
		Color green = Color.web("#30eb49");
		Color white = Color.web("#ffffff");
		commandChar = "T";
		CallSBLCPCommand.call_NonCustom_SBLCPcommand_blocking(serialCom, commandChar, null, 0);
		ipAddressCircleForNum1.setFill(white);
		ipAddressCircleForNum2.setFill(white);
		ipAddressCircleForNum3.setFill(white);
		ipAddressCircleForNum4.setFill(white);
		ipAddressCircleForNum5.setFill(green);
		ipAddressCircleForNum6.setFill(white);
		ipAddressCircleForNum7.setFill(white);
		ipAddressCircleForNum8.setFill(white);
		ipAddressCircleForNum9.setFill(white);
		ipAddressCircleForNum10.setFill(white);
		CallSBLCPCommand.call_NonCustom_SBLCPcommand_blocking(serialCom, "1", null, 0);
	}
	@FXML
	public void handleBreakersOnTheNetworkConnectButtonNum6(ActionEvent event) {
		Color green = Color.web("#30eb49");
		Color white = Color.web("#ffffff");
		commandChar = "Y";
		CallSBLCPCommand.call_NonCustom_SBLCPcommand_blocking(serialCom, commandChar, null, 0);
		ipAddressCircleForNum1.setFill(white);
		ipAddressCircleForNum2.setFill(white);
		ipAddressCircleForNum3.setFill(white);
		ipAddressCircleForNum4.setFill(white);
		ipAddressCircleForNum5.setFill(white);
		ipAddressCircleForNum6.setFill(green);
		ipAddressCircleForNum7.setFill(white);
		ipAddressCircleForNum8.setFill(white);
		ipAddressCircleForNum9.setFill(white);
		ipAddressCircleForNum10.setFill(white);
		CallSBLCPCommand.call_NonCustom_SBLCPcommand_blocking(serialCom, "1", null, 0);
	}
	@FXML
	public void handleBreakersOnTheNetworkConnectButtonNum7(ActionEvent event) {
		Color green = Color.web("#30eb49");
		Color white = Color.web("#ffffff");
		commandChar = "U";
		CallSBLCPCommand.call_NonCustom_SBLCPcommand_blocking(serialCom, commandChar, null, 0);
		ipAddressCircleForNum1.setFill(white);
		ipAddressCircleForNum2.setFill(white);
		ipAddressCircleForNum3.setFill(white);
		ipAddressCircleForNum4.setFill(white);
		ipAddressCircleForNum5.setFill(white);
		ipAddressCircleForNum6.setFill(white);
		ipAddressCircleForNum7.setFill(green);
		ipAddressCircleForNum8.setFill(white);
		ipAddressCircleForNum9.setFill(white);
		ipAddressCircleForNum10.setFill(white);
		CallSBLCPCommand.call_NonCustom_SBLCPcommand_blocking(serialCom, "1", null, 0);
	}
	@FXML
	public void handleBreakersOnTheNetworkConnectButtonNum8(ActionEvent event) {
		Color green = Color.web("#30eb49");
		Color white = Color.web("#ffffff");
		commandChar = "I";
		CallSBLCPCommand.call_NonCustom_SBLCPcommand_blocking(serialCom, commandChar, null, 0);
		ipAddressCircleForNum1.setFill(white);
		ipAddressCircleForNum2.setFill(white);
		ipAddressCircleForNum3.setFill(white);
		ipAddressCircleForNum4.setFill(white);
		ipAddressCircleForNum5.setFill(white);
		ipAddressCircleForNum6.setFill(white);
		ipAddressCircleForNum7.setFill(white);
		ipAddressCircleForNum8.setFill(green);
		ipAddressCircleForNum9.setFill(white);
		ipAddressCircleForNum10.setFill(white);
		CallSBLCPCommand.call_NonCustom_SBLCPcommand_blocking(serialCom, "1", null, 0);
	}
	@FXML
	public void handleBreakersOnTheNetworkConnectButtonNum9(ActionEvent event) {
		Color green = Color.web("#30eb49");
		Color white = Color.web("#ffffff");
		commandChar = "O";
		CallSBLCPCommand.call_NonCustom_SBLCPcommand_blocking(serialCom, commandChar, null, 0);
		ipAddressCircleForNum1.setFill(white);
		ipAddressCircleForNum2.setFill(white);
		ipAddressCircleForNum3.setFill(white);
		ipAddressCircleForNum4.setFill(white);
		ipAddressCircleForNum5.setFill(white);
		ipAddressCircleForNum6.setFill(white);
		ipAddressCircleForNum7.setFill(white);
		ipAddressCircleForNum8.setFill(white);
		ipAddressCircleForNum9.setFill(green);
		ipAddressCircleForNum10.setFill(white);
		CallSBLCPCommand.call_NonCustom_SBLCPcommand_blocking(serialCom, "1", null, 0);
	}
	@FXML
	public void handleBreakersOnTheNetworkConnectButtonNum10(ActionEvent event) {
		Color green = Color.web("#30eb49");
		Color white = Color.web("#ffffff");
		commandChar = "P";
		CallSBLCPCommand.call_NonCustom_SBLCPcommand_blocking(serialCom, commandChar, null, 0);
		ipAddressCircleForNum1.setFill(white);
		ipAddressCircleForNum2.setFill(white);
		ipAddressCircleForNum3.setFill(white);
		ipAddressCircleForNum4.setFill(white);
		ipAddressCircleForNum5.setFill(white);
		ipAddressCircleForNum6.setFill(white);
		ipAddressCircleForNum7.setFill(white);
		ipAddressCircleForNum8.setFill(white);
		ipAddressCircleForNum9.setFill(white);
		ipAddressCircleForNum10.setFill(green);
		CallSBLCPCommand.call_NonCustom_SBLCPcommand_blocking(serialCom, "1", null, 0);
	}
	
	@FXML
	public void handleCheckbox(ActionEvent event) {
		selectedBreakerList[0] = checkbox1.isSelected();
		selectedBreakerList[1] = checkbox2.isSelected();
		selectedBreakerList[2] = checkbox3.isSelected();
		selectedBreakerList[3] = checkbox4.isSelected();
		selectedBreakerList[4] = checkbox5.isSelected();
		selectedBreakerList[5] = checkbox6.isSelected();
		selectedBreakerList[6] = checkbox7.isSelected();
		selectedBreakerList[7] = checkbox8.isSelected();
		selectedBreakerList[8] = checkbox9.isSelected();
		selectedBreakerList[9] = checkbox10.isSelected();
	}
	
	@FXML
	public void handleMultipleBreakerConnect(ActionEvent event) {
		// setting status indicator color based on the connected breaker list
		Color green = Color.web("#30eb49");
		Color white = Color.web("#ffffff");
		// call the udp command now - Need this from Rahul
		CallSBLCPCommand.call_NonCustom_SBLCPcommand_blocking(serialCom, Arrays.toString(selectedBreakerList), null, 0);
		ipAddressCircleForNum1.setFill(selectedBreakerList[0] == true? green: white);
		ipAddressCircleForNum2.setFill(selectedBreakerList[1] == true? green: white);
		ipAddressCircleForNum3.setFill(selectedBreakerList[2] == true? green: white);
		ipAddressCircleForNum4.setFill(selectedBreakerList[3] == true? green: white);
		ipAddressCircleForNum5.setFill(selectedBreakerList[4] == true? green: white);
		ipAddressCircleForNum6.setFill(selectedBreakerList[5] == true? green: white);
		ipAddressCircleForNum7.setFill(selectedBreakerList[6] == true? green: white);
		ipAddressCircleForNum8.setFill(selectedBreakerList[7] == true? green: white);
		ipAddressCircleForNum9.setFill(selectedBreakerList[8] == true? green: white);
		ipAddressCircleForNum10.setFill(selectedBreakerList[9] == true? green: white);
		
		System.out.println(Arrays.toString(selectedBreakerList));
	}
	public String findBreakersIPListJSONstring(String givenString) {
		Pattern pattern = Pattern.compile("BREAKERS_IP_LIST_JSON_START(.*?)BREAKERS_IP_LIST_JSON_STOP", Pattern.DOTALL);
	    Matcher matcher = pattern.matcher(givenString);
	    String lastJSONString = ""; // Variable to store the last JSON string found
	    
	    // Find all occurrences of the pattern
	    while (matcher.find()) {
	        lastJSONString = matcher.group(1); // Update the last JSON string with each match
	    }
	    
	    return lastJSONString;
	}
	ArrayList<Pair<String, String>> parsedBreakersIPList;
	public void parseBreakersIPList(String rawJSON) {
		Gson gson = new Gson();

        // Deserialize the JSON string into a Map
        java.lang.reflect.Type type = new com.google.gson.reflect.TypeToken<java.util.Map<String, String>>(){}.getType();
        java.util.Map<String, String> map = gson.fromJson(rawJSON, type);

        // Convert the Map entries to Pair objects and store them in an ArrayList
        parsedBreakersIPList = new ArrayList<>();
        for (java.util.Map.Entry<String, String> entry : map.entrySet()) {
            Pair<String, String> pair = new Pair<>(entry.getKey(), entry.getValue());
            parsedBreakersIPList.add(pair);
        }
        //populateBreakersForMenu();
	}
	public void populateBreakersOnTheNetworkSection() {
		Platform.runLater(() -> {
			ipAddressLabelForNum1.setText(parsedBreakersIPList.get(0).getValue().equals("") ? "-" : parsedBreakersIPList.get(0).getValue());
			ipAddressLabelForNum2.setText(parsedBreakersIPList.get(1).getValue().equals("") ? "-" : parsedBreakersIPList.get(1).getValue());
			ipAddressLabelForNum3.setText(parsedBreakersIPList.get(2).getValue().equals("") ? "-" : parsedBreakersIPList.get(2).getValue());
			ipAddressLabelForNum4.setText(parsedBreakersIPList.get(3).getValue().equals("") ? "-" : parsedBreakersIPList.get(3).getValue());
			ipAddressLabelForNum5.setText(parsedBreakersIPList.get(4).getValue().equals("") ? "-" : parsedBreakersIPList.get(4).getValue());
			ipAddressLabelForNum6.setText(parsedBreakersIPList.get(5).getValue().equals("") ? "-" : parsedBreakersIPList.get(5).getValue());
			ipAddressLabelForNum7.setText(parsedBreakersIPList.get(6).getValue().equals("") ? "-" : parsedBreakersIPList.get(6).getValue());
			ipAddressLabelForNum8.setText(parsedBreakersIPList.get(7).getValue().equals("") ? "-" : parsedBreakersIPList.get(7).getValue());
			ipAddressLabelForNum9.setText(parsedBreakersIPList.get(8).getValue().equals("") ? "-" : parsedBreakersIPList.get(8).getValue());
			ipAddressLabelForNum10.setText(parsedBreakersIPList.get(9).getValue().equals("") ? "-" : parsedBreakersIPList.get(9).getValue());
		});
	}
	
	// ####################################################################################
	// The following code(s) is to handle the "device status" block
	// ####################################################################################
	/**
	 * Finds a JSON string that is between "DEVICE_STATUS_JSON_START" and "DEVICE_STATUS_JSON_END" given a string.
	 * @param givenString
	 * @return
	 */
	public String findDeviceStatusJSONstring(String givenString) {
	    Pattern pattern = Pattern.compile("DEVICE_STATUS_JSON_START(.*?)DEVICE_STATUS_JSON_END", Pattern.DOTALL);
	    Matcher matcher = pattern.matcher(givenString);
	    String lastJSONString = ""; // Variable to store the last JSON string found
	    
	    // Find all occurrences of the pattern
	    while (matcher.find()) {
	        lastJSONString = matcher.group(1); // Update the last JSON string with each match
	    }
	    
	    return lastJSONString;
	}
	ArrayList<Pair<String, String>> parsedDeviceStatus;
	public ArrayList<Pair<String, String>> parseDeviceStatusJSONstring(String rawDeviceStatusString) {
		Gson gson = new Gson();

        // Deserialize the JSON string into a Map
        java.lang.reflect.Type type = new com.google.gson.reflect.TypeToken<java.util.Map<String, String>>(){}.getType();
        java.util.Map<String, String> map = gson.fromJson(rawDeviceStatusString, type);

        // Convert the Map entries to Pair objects and store them in an ArrayList
        ArrayList<Pair<String, String>> parsedDeviceStatus = new ArrayList<>();
        for (java.util.Map.Entry<String, String> entry : map.entrySet()) {
            Pair<String, String> pair = new Pair<>(entry.getKey(), entry.getValue());
            parsedDeviceStatus.add(pair);
        }

        return parsedDeviceStatus;
	}
	private void updateDeviceStatusTable() {
	    ObservableList<Pair<String, String>> miscData = FXCollections.observableArrayList();
	    ObservableList<Pair<String, String>> phaseAData = FXCollections.observableArrayList();
	    ObservableList<Pair<String, String>> phaseBData = FXCollections.observableArrayList();

	    try {
	    	for (int i = 0; i < parsedDeviceStatus.size(); ++i) {
	    		Pair<String, String> pair = parsedDeviceStatus.get(i);
	    		String key = pair.getKey();
	    		
	    		if (key.startsWith("phase_A_")) {
	    			phaseAData.add(pair);
	    		} else if (key.startsWith("phase_B_")) {
	    			phaseBData.add(pair);
	    		} else {
	    			miscData.add(pair);
	    		}
	    		
	    		// Extract Data and saves it in currentSB2's state.
	    		currentSB2.updateIndividualState(pair);
	    	}
	    } catch (NullPointerException e) {
			return;
		}
	    
	    // Increment the counter (aka version) so that we can keep tract of which version of the device status message we are looking at.
	    currentSB2.incrementDeviceStatusMessageVersion();

	    deviceStatusMiscTable.setItems(miscData);
	    deviceStatusPhaseATable.setItems(phaseAData);
	    deviceStatusPhaseBTable.setItems(phaseBData);
	}
	private boolean realTimeDeviceStatusGETing = false;
	private Thread realTimeDeviceStatusGETThread;
	@FXML
	public void handleRealTimeDeviceStatusGETStartButton(ActionEvent event) {
		realTimeDeviceStatusGETing = true;	// Un-used
		printToSerialPortOutputBox = false;	// We want to grab data but not print to serial port output box.
		realTimeDeviceStatusGETThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
            	CallSBLCPCommand.call_NonCustom_SBLCPcommand_blocking(serialCom, CallSBLCPCommand.GET_DEVICE_STATUS, null, 3000);
            	
            	try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
            }
        });
		realTimeDeviceStatusGETThread.start();
	}
	@FXML
	public void handleRealTimeDeviceStatusGETStopButton(ActionEvent event) {
		if (realTimeDeviceStatusGETThread != null && realTimeDeviceStatusGETThread.isAlive()) {
	        realTimeDeviceStatusGETThread.interrupt();
	        try {
	            realTimeDeviceStatusGETThread.join(); // Wait for the thread to complete
	        } catch (InterruptedException e) {
	            // Handle the interruption if needed
	        }
	        systemOutPrintlnAndPrintLog("Measurement Test Thread should be closed (" + realTimeDeviceStatusGETThread.toString() + ")\n", LogLevel.DEBUG);
	        realTimeDeviceStatusGETThread = null;
	    }
		printToSerialPortOutputBox = true;
	}
	@FXML
	public void handleDeviceStatusBlockRefreshTableButton(ActionEvent event) {
		clearDeviceStatusTable();
	}
	@FXML
	public void handleDeviceStatusBlockReadAvgButton(ActionEvent event) {
		handleSendSetManufacturingModeOnButton(null);
		int timeToTurnOnManu = CallSBLCPCommand.call_NonCustom_SBLCPcommand_blocking(serialCom, CallSBLCPCommand.SET_MANUFACTURING_MODE_ON, null, 3000);
		handleSendGetMeterTelemetryDataButton(null);
		int timeToGetMeterTelem = CallSBLCPCommand.call_NonCustom_SBLCPcommand_blocking(serialCom, CallSBLCPCommand.GET_METER_TELEMETRT_DATA, null, 3000);
		
		if (timeToTurnOnManu == -1 || timeToGetMeterTelem == -1) {
			displayNoResponseAlertWindow();
			return;
		}
		
		updateDeviceStatusTable();
	}
	
	public void searchCodeFromTripReferenceTable(ActionEvent event) {
		String txtValue = searchTripCode.getText().replaceAll("[\\s|\\u00A0]+", "");
		if (codeChoice.getValue() == "Hex") {
			try {
				int decimal = Integer.parseInt(txtValue,16);
				FilteredList<Pair<String, String>> filteredItems = new FilteredList<>(tripTableItems, i -> Integer.parseInt(i.getKey()) == decimal);
				tripCodeReferenceTable.setItems(filteredItems);
			} catch (Throwable ex) {
				displayAlertWindow(AlertType.ERROR, "Invalid input", "Invalid input", "Please make sure you are entering valid hex code!");
			}
			
		} else {
			if (!searchTripCode.getText().contains(".") && txtValue.matches("((-|\\+)?[0-9]+(\\.[0-9]+)?)+")) {
				FilteredList<Pair<String, String>> filteredItems = new FilteredList<>(tripTableItems, i -> Integer.parseInt(i.getKey()) == Integer.parseInt(txtValue));
				tripCodeReferenceTable.setItems(filteredItems);
			} else {
				displayAlertWindow(AlertType.ERROR, "Invalid input", "Invalid input", "Please make sure you are entering integers!");
			}
		}
		
		
	}
	
	public void clearTripCodeFromReferenceTable(ActionEvent event) {
		searchTripCode.clear();
		tripCodeReferenceTable.setItems(tripTableItems);
	}
	
	
	
	// ####################################################################################
	// The following code(s) is to handle the "EOL Calib Param" block
	// ####################################################################################
	// This is basically the same as findDeviceStatusJSONstring
	public String findCalibParamJSONstring(String givenString) {
		Pattern pattern = Pattern.compile("CALIB_PARAM_JSON_START(.*?)CALIB_PARAM_JSON_END", Pattern.DOTALL);
	    Matcher matcher = pattern.matcher(givenString);
	    String lastJSONString = ""; // Variable to store the last JSON string found
	    
	    // Find all occurrences of the pattern
	    while (matcher.find()) {
	        lastJSONString = matcher.group(1); // Update the last JSON string with each match
	    }
	    
	    return lastJSONString;
	}
	ArrayList<Pair<String, String>> parsedCalibParam;
	// This is basically the same as parseDeviceStatusJSONstring
	public ArrayList<Pair<String, String>> parseCalibParamJSONstring(String rawCalibParamString) {
		Gson gson = new Gson();

        // Deserialize the JSON string into a Map
        java.lang.reflect.Type type = new com.google.gson.reflect.TypeToken<java.util.Map<String, String>>(){}.getType();
        System.out.println("KNOCK PARAM");
        System.out.println(type);
        System.out.println(rawCalibParamString);
        java.util.Map<String, String> map = gson.fromJson(rawCalibParamString, type);
        System.out.println("KNOCK 2");
        System.out.println(map);
        // Convert the Map entries to Pair objects and store them in an ArrayList
        ArrayList<Pair<String, String>> parsedCalibParam = new ArrayList<>();
        for (java.util.Map.Entry<String, String> entry : map.entrySet()) {
            Pair<String, String> pair = new Pair<>(entry.getKey(), entry.getValue());
            parsedCalibParam.add(pair);
        }

        return parsedCalibParam;
	}
	private void updateCalibParamTable() {
		ObservableList<Pair<String, String>> calibParamStuff = FXCollections.observableArrayList();
		try {
			for (int i = 0; i < parsedCalibParam.size(); ++i) {
				Pair<String, String> pair = parsedCalibParam.get(i);
				
				calibParamStuff.add(pair);
				
				// Extract Data and saves it in currentSB2's state.
//	        currentSB2.updateIndividualState(pair); TODO
			}
		} catch (NullPointerException e) {
			return;	// Yes, yes, very bad coding practice indeed.
		}

	    calibParamTable.setItems(calibParamStuff);
	}
	private void populateTripCurveTable() {
		tripTableItems.add(new Pair<String, String>("0", "0 - NO_TRIP"));
		tripTableItems.add(new Pair<String, String>("1", "1 - NORMAL_STARTUP"));
		tripTableItems.add(new Pair<String, String>("2", "2 - SELF_TEST_SUCCESSFUL_TRIP"));
		tripTableItems.add(new Pair<String, String>("3", "3 - OUTPUT_STATUS_LOG_VIA_SPI"));
		tripTableItems.add(new Pair<String, String>("4", "4 - END_OF_FACTORY_ANCHOR"));
		tripTableItems.add(new Pair<String, String>("5", "5 - RECALL_RESET_ANCHOR"));
		for (int j = 8; j < 32; j++) {
			tripTableItems.add(new Pair<String, String>(Integer.toString(j), Integer.toHexString(j) + " - RESERVED"));
		}
		tripTableItems.add(new Pair<String, String>("32", Integer.toHexString(32) + " - GENERAL_ARC_DETECTION_LOW_CURRENT_TRIP"));
		tripTableItems.add(new Pair<String, String>("33", Integer.toHexString(33) + " - GENERAL_ARC_DETECTION_HIGH_CURRENT_TRIP"));
		tripTableItems.add(new Pair<String, String>("34", Integer.toHexString(34) + " - DIMMER_ARC_DETECTION_LOW_CURRENT_TRIP"));
		tripTableItems.add(new Pair<String, String>("35", Integer.toHexString(35) + " - DIMMER_ARC_DETECTION_HIGH_CURRENT_TRIP"));
		tripTableItems.add(new Pair<String, String>("36", Integer.toHexString(36) + " - GROUND_FAULT_OVERCURRENT_TRIP"));
		tripTableItems.add(new Pair<String, String>("37", Integer.toHexString(37) + " - PARALLEL_ARC_FAULT_TRIP"));
		tripTableItems.add(new Pair<String, String>("38", Integer.toHexString(38) + " - SHORT_DELAY_FAULT_OVERCURRENT_TRIP"));
		tripTableItems.add(new Pair<String, String>("39", Integer.toHexString(39) + " - OVERVOLTAGE_TRIP"));
		tripTableItems.add(new Pair<String, String>("40", Integer.toHexString(40) + " - HRGF_COLDSTART_TRIP"));
		tripTableItems.add(new Pair<String, String>("41", Integer.toHexString(41) + " - HRGF_RUNNING_TRIP"));
		tripTableItems.add(new Pair<String, String>("42", Integer.toHexString(42) + " - GN_TRIP"));
		tripTableItems.add(new Pair<String, String>("43", Integer.toHexString(43) + " - OVERLOAD_TRIP"));
		tripTableItems.add(new Pair<String, String>("44", Integer.toHexString(44) + " - HIGH_GN_TRIP"));
		tripTableItems.add(new Pair<String, String>("63", Integer.toHexString(63) + " - PRIMARY_HAL_TRIP_OR_OFF"));
		tripTableItems.add(new Pair<String, String>("64", Integer.toHexString(64) + " - FAILED_STARTUP_RAM_TEST"));
		tripTableItems.add(new Pair<String, String>("65", Integer.toHexString(65) + " - FAILED_STARTUP_ROM_TEST"));
		tripTableItems.add(new Pair<String, String>("66", Integer.toHexString(66) + " - FAILED_STARTUP_PROCESSOR_TEST"));
		tripTableItems.add(new Pair<String, String>("67", Integer.toHexString(67) + " - DATA_OVERRUN_TRIP"));
		tripTableItems.add(new Pair<String, String>("68", Integer.toHexString(68) + " - INVALID_DATA_SEQUENCE_TRIP"));
		tripTableItems.add(new Pair<String, String>("69", Integer.toHexString(69) + " - INCORRECT_INTERRUPT_COUNT_TRIP"));
		tripTableItems.add(new Pair<String, String>("70", Integer.toHexString(70) + " - HF_SENSE_FAULT_TRIP"));
		tripTableItems.add(new Pair<String, String>("71", Integer.toHexString(71) + " - LINE_CURRENT_BIAS_ERROR_TRIP"));
		tripTableItems.add(new Pair<String, String>("72", Integer.toHexString(72) + " - GROUND_FAULT_CURRENT_BIAS_ERROR_TRIP"));
		tripTableItems.add(new Pair<String, String>("73", Integer.toHexString(73) + " - LOG_HF_MIN_DETECTOR_STUCK_ERROR_TRIP"));
		tripTableItems.add(new Pair<String, String>("74", Integer.toHexString(74) + " - FAILED_CONTINUOUS_RAM_TEST_TRIP"));
		tripTableItems.add(new Pair<String, String>("75", Integer.toHexString(75) + " - FAILED_CONTINUOUS_ROM_TEST_TRIP"));
		tripTableItems.add(new Pair<String, String>("76", Integer.toHexString(76) + " - FAILED_CONTINUOUS_PROCESSOR_TEST_TRIP"));
		tripTableItems.add(new Pair<String, String>("77", Integer.toHexString(77) + " - NONHANDLED_INTERRUPT_TRIP"));
		tripTableItems.add(new Pair<String, String>("78", Integer.toHexString(78) + " - FAILED_TRIP_ATTEMPT"));
		tripTableItems.add(new Pair<String, String>("79", Integer.toHexString(79) + " - FAILED_SELF_CHECK_GF_INPUT"));
		tripTableItems.add(new Pair<String, String>("80", Integer.toHexString(80) + " - FAILED_SELF_CHECK_OUTPUT"));
		tripTableItems.add(new Pair<String, String>("81", Integer.toHexString(81) + " - FAILED_SELF_CHECK_CT_DIRECTION"));
		tripTableItems.add(new Pair<String, String>("82", Integer.toHexString(82) + " - COMPLETE_LOSS_OF_ZCD_TRIP"));
		tripTableItems.add(new Pair<String, String>("96", Integer.toHexString(96) + " - SECONDARY_SWITCH_FAULT"));
		tripTableItems.add(new Pair<String, String>("97", Integer.toHexString(97) + " - SECONDARY_SWITCH_STUCK_OPEN"));
		tripTableItems.add(new Pair<String, String>("98", Integer.toHexString(98) + " - SECONDARY_SWITCH_STUCK_CLOSED"));
		tripTableItems.add(new Pair<String, String>("99", Integer.toHexString(99) + " - SECONDARY_SWITCH_UNEXPECTED_OPEN"));
		tripTableItems.add(new Pair<String, String>("100", Integer.toHexString(100) + " - SECONDARY_SWITCH_UNEXPECTED_CLOSED"));
		tripTableItems.add(new Pair<String, String>("101", Integer.toHexString(101) + " - SECONDARY_SWITCH_UNEXPECTED_OVERLOAD"));
		tripTableItems.add(new Pair<String, String>("102", Integer.toHexString(102) + " - SECONDARY_SWITCH_DEFAULT_TO_CLOSED"));
		tripTableItems.add(new Pair<String, String>("103", Integer.toHexString(103) + " - SECONDARY_SWITCH_BACKUP_TRIGGERED"));
		tripTableItems.add(new Pair<String, String>("112", Integer.toHexString(112) + " - LOG_BREAKER_TURN_ON"));
		tripTableItems.add(new Pair<String, String>("113", Integer.toHexString(113) + " - LOG_BREAKER_TURN_OFF"));
		tripTableItems.add(new Pair<String, String>("114", Integer.toHexString(114) + " - LOG_BREAKER_PROVISIONING"));
		tripTableItems.add(new Pair<String, String>("115", Integer.toHexString(115) + " - LOG_BREAKER_RE_PROVISIONING"));
		tripTableItems.add(new Pair<String, String>("116", Integer.toHexString(116) + " - LOG_BREAKER_TIMER_BASED_LOG"));
		tripTableItems.add(new Pair<String, String>("117", Integer.toHexString(117) + " - LOG_SPI_COMM_FAIL"));
		tripTableItems.add(new Pair<String, String>("118", Integer.toHexString(118) + " - LOG_I2C_COMM_FAIL"));
		tripTableItems.add(new Pair<String, String>("119", Integer.toHexString(119) + " - LOG_SPI_COMM_ERR"));
		tripTableItems.add(new Pair<String, String>("120", Integer.toHexString(120) + " - LOG_I2C_COMM_ERR"));
		
		for (int i = 128; i < 256; i++) {
			tripTableItems.add(new Pair<String, String>(Integer.toString(i), Integer.toHexString(i) + " - USER_INITIATED_HW_FAULT_CODES"));
		}
		

		tripCodeReferenceCodeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getKey()));
		tripCodeReferenceValueColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getValue()));
		tripCodeReferenceValueColumn.prefWidthProperty().bind(tripCodeReferenceTable.widthProperty().multiply(0.75));
		tripCodeReferenceTable.getColumns().add(tripCodeReferenceCodeColumn);
		tripCodeReferenceTable.getColumns().add(tripCodeReferenceValueColumn);		
		codeChoice.setItems(codeSelectionList);
		codeChoice.setValue("Dec");
		tripCodeReferenceTable.setItems(tripTableItems);
		
	}
	
	private void updateTripTable() {
		ObservableList<Pair<String, String>> tripParam = FXCollections.observableArrayList();
		
		try {
			// trip code call sends 100 messages at all points
			for (int i = 0; i < parsedTripCode.size(); ++i) {
				Pair<String, String> pair = parsedTripCode.get(i);
				tripParam.add(pair);
			}
		} catch (NullPointerException e) {
			return;	// Yes, yes, very bad coding practice indeed.
		}

	    tripCodeTable.setItems(tripParam);
	}
	
	@FXML
	public void handleTripCodeClearTableButton(ActionEvent event) {
		tripCodeTable.getItems().clear();
		parsedTripCode = null;
	}
	
//	private void updateTripCodeTable() {
//	    ObservableList<Pair<String, String>> miscData = FXCollections.observableArrayList();
//	    ObservableList<Pair<String, String>> phaseAData = FXCollections.observableArrayList();
//	    ObservableList<Pair<String, String>> phaseBData = FXCollections.observableArrayList();
//
//	    try {
//	    	for (int i = 0; i < parsedDeviceStatus.size(); ++i) {
//	    		Pair<String, String> pair = parsedDeviceStatus.get(i);
//	    		String key = pair.getKey();
//	    		
//	    		if (key.startsWith("phase_A_")) {
//	    			phaseAData.add(pair);
//	    		} else if (key.startsWith("phase_B_")) {
//	    			phaseBData.add(pair);
//	    		} else {
//	    			miscData.add(pair);
//	    		}
//	    		
//	    		// Extract Data and saves it in currentSB2's state.
//	    		currentSB2.updateIndividualState(pair);
//	    	}
//	    } catch (NullPointerException e) {
//			return;
//		}
//	    
//	    // Increment the counter (aka version) so that we can keep tract of which version of the device status message we are looking at.
//	    currentSB2.incrementDeviceStatusMessageVersion();
//
//	    deviceStatusMiscTable.setItems(miscData);
//	    deviceStatusPhaseATable.setItems(phaseAData);
//	    deviceStatusPhaseBTable.setItems(phaseBData);
//	}
	
	
	@FXML
	public void handleEOLCalibParamBlockClearTableButton(ActionEvent event) {
		calibParamTable.getItems().clear();
		parsedCalibParam = null;
	}
	@FXML
	public void handleEOLCalibParamBlockSetButton(ActionEvent event) {
		try {
			// Retrieve the values from the text boxes and send it.
			currentSB2.setEOLParamState("Ade9000_APhCal0Field", Integer.parseInt(Ade9000_APhCal0Field.getText()));
			currentSB2.setEOLParamState("Ade9000_AiGainField", Integer.parseInt(Ade9000_AiGainField.getText()));
			currentSB2.setEOLParamState("Ade9000_AiRmsSOSField", Integer.parseInt(Ade9000_AiRmsSOSField.getText()));
			currentSB2.setEOLParamState("Ade9000_ApGainField", Integer.parseInt(Ade9000_ApGainField.getText()));
			currentSB2.setEOLParamState("Ade9000_AvGainField", Integer.parseInt(Ade9000_AvGainField.getText()));
			currentSB2.setEOLParamState("Ade9000_BPhCal0Field", Integer.parseInt(Ade9000_BPhCal0Field.getText()));
			currentSB2.setEOLParamState("Ade9000_BiGainField", Integer.parseInt(Ade9000_BiGainField.getText()));
			currentSB2.setEOLParamState("Ade9000_BiRmsSOSField", Integer.parseInt(Ade9000_BiRmsSOSField.getText()));
			currentSB2.setEOLParamState("Ade9000_BpGainField", Integer.parseInt(Ade9000_BpGainField.getText()));
			currentSB2.setEOLParamState("Ade9000_BvGainField", Integer.parseInt(Ade9000_BvGainField.getText()));

			// Send the message
			CallSBLCPCommand.call_Custom_SBLCPcommand_blocking(serialCom, currentSB2.createSEND_SET_BREAKER_EOL_CALIB_PARAMcustomMessage(), null, 3000);
			// Send twice as this is a workaround for corrupted message
//			sendCustomMessage(currentSB2.createSEND_SET_BREAKER_EOL_CALIB_PARAMcustomMessage());
		} catch (NumberFormatException e) {
			systemOutPrintlnAndPrintLog("Something you entered isn't an integer!", LogLevel.ERROR);
		} catch (NoSuchFieldException e) {
			systemOutPrintlnAndPrintLog("NoSuchFieldException encountered " + e.getMessage(), LogLevel.ERROR);
		} catch (IllegalAccessException e) {
			systemOutPrintlnAndPrintLog("IllegalAccessException encountered " + e.getMessage(), LogLevel.ERROR);
		}
	}

	
	// ####################################################################################
	// The following code(s) is to handle the "Next UDP Seq #" block
	// ####################################################################################
	@FXML
	public void handleNextUDPSeqNumBlockSetNextUDPSequeneNumberButton(ActionEvent event) {
		String nextSeqNumberToSend = setNextUDPSequenceNumberInputField.getText();
		
		String toSend = "#,888,0x00000000,0x0080,0x";
		toSend += nextSeqNumberToSend;
		toSend += ",hiIamAHashWuHaha";
		toSend += "\n";
		
		sendCustomMessage(toSend);
	}
	ArrayList<Pair<String, String>> parsedNextUDPSeq;
	
	public String findNextUDPSeqJSONstring(String givenString) {
		Pattern pattern = Pattern.compile("GET_NEXT_UDP_JSON_START(.*?)GET_NEXT_UDP_JSON_END", Pattern.DOTALL);
	    Matcher matcher = pattern.matcher(givenString);
	    String lastJSONString = ""; // Variable to store the last JSON string found
	    
	    // Find all occurrences of the pattern
	    while (matcher.find()) {
	        lastJSONString = matcher.group(1); // Update the last JSON string with each match
	    }
	    
	    return lastJSONString;
	}
	public ArrayList<Pair<String, String>> parseNextUDPSeqJSONstring(String rawNextUDPSeqString) {
		Gson gson = new Gson();

        // Deserialize the JSON string into a Map
        java.lang.reflect.Type type = new com.google.gson.reflect.TypeToken<java.util.Map<String, String>>(){}.getType();
        java.util.Map<String, String> map = gson.fromJson(rawNextUDPSeqString, type);

        // Convert the Map entries to Pair objects and store them in an ArrayList
        ArrayList<Pair<String, String>> parsedNextUDPSeq = new ArrayList<>();
        for (java.util.Map.Entry<String, String> entry : map.entrySet()) {
            Pair<String, String> pair = new Pair<>(entry.getKey(), entry.getValue());
            parsedNextUDPSeq.add(pair);
        }

        return parsedNextUDPSeq;
	}
	
	public String findTripParamJSONstring(String givenString) {
		Pattern pattern = Pattern.compile("TRIP_PARAM_JSON_START(.*?)TRIP_PARAM_JSON_END", Pattern.DOTALL);
	    Matcher matcher = pattern.matcher(givenString);
	    String lastJSONString = ""; // Variable to store the last JSON string found
	    
	    // Find all occurrences of the pattern
	    while (matcher.find()) {
	    	System.out.println("PATTERN");
	        lastJSONString = matcher.group(1); // Update the last JSON string with each match
	    }
	    
	    return lastJSONString;
	}
	ArrayList<Pair<String, String>> parsedTripCode;
	public ArrayList<Pair<String, String>> parseTripCodeJSONstring(String rawTripCodeString) {
		Gson gson = new Gson();

        // Deserialize the JSON string into a Map
        java.lang.reflect.Type type = new com.google.gson.reflect.TypeToken<java.util.Map<String, String>>(){}.getType();
        java.util.Map<String, String> map;
        map = gson.fromJson(rawTripCodeString, type);
        /*
        try {
        	 map = gson.fromJson(rawTripCodeString, type);
        } catch (JsonSyntaxException e) {
        	String start = "{";
        	for (int i = 0; i <= 100; i++) {
        		Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT")); 
            	c.set(Calendar.YEAR, 2024);
            	c.set(Calendar.MONTH, 6);
            	c.set(Calendar.DAY_OF_MONTH, 6);
            	c.set(Calendar.HOUR_OF_DAY, 6);
            	c.set(Calendar.MINUTE, 6);
            	c.set(Calendar.SECOND, 6);
            	c.set(Calendar.MILLISECOND, i);
            	long timestamp = c.getTimeInMillis();
            	String content = ":0";
            	String end;
            	if (i == 100) {
            		end = "}";
            	} else {
            		end = ", ";
            	}
            	start = start + timestamp + content + end;
        	}
        	
        	*/
//        	rawTripCodeString = "{\"1343892452\":\"21\", \"1343892453\":\"23\", \"1343892454\":\"23\"}";
        	//map = gson.fromJson(start, type);
        //}
        // Convert the Map entries to Pair objects and store them in an ArrayList
        ArrayList<Pair<String, String>> parsedTripCode = new ArrayList<>();
        for (java.util.Map.Entry<String, String> entry : map.entrySet()) {
            Pair<String, String> pair = new Pair<>(entry.getKey(), entry.getValue());
            parsedTripCode.add(pair);
        }

        return parsedTripCode;
	}
	public String findMacAddressJSONstring(String givenString) {
		Pattern pattern = Pattern.compile("MAC_ADDRESS_JSON_START(.*?)MAC_ADDRESS_JSON_END", Pattern.DOTALL);
	    Matcher matcher = pattern.matcher(givenString);
	    String lastJSONString = ""; // Variable to store the last JSON string found
	    
	    // Find all occurrences of the pattern
	    while (matcher.find()) {
	        lastJSONString = matcher.group(1); // Update the last JSON string with each match
	    }
	    
	    return lastJSONString;
	}
	ArrayList<Pair<String, String>> parsedMacAddress;
	public ArrayList<Pair<String, String>> parseMacAddressJSONstring(String rawMacAddressString) {
		Gson gson = new Gson();
        // Deserialize the JSON string into a Map
        java.lang.reflect.Type type = new com.google.gson.reflect.TypeToken<java.util.Map<String, String>>(){}.getType();
        System.out.println("KNOCK1");
        String extractedMacAddress = rawMacAddressString.replace(": ", ": \"");
        String modifiedRawString = extractedMacAddress.replace("}", "\"}");
       
        
        java.util.Map<String, String> map = gson.fromJson(modifiedRawString.toString(), type);
        // Convert the Map entries to Pair objects and store them in an ArrayList
        ArrayList<Pair<String, String>> parsedMacAddress = new ArrayList<>();
        for (java.util.Map.Entry<String, String> entry : map.entrySet()) {
            Pair<String, String> pair = new Pair<>(entry.getKey(), entry.getValue());
            parsedMacAddress.add(pair);
        }

        return parsedMacAddress;
	}
	
	
	
	
	private void updateNextUDPSeqTable() {
		ObservableList<Pair<String, String>> nextUDPSeqStuff = FXCollections.observableArrayList();
		System.out.println(parsedNextUDPSeq);
		try {
			for (int i = 0; i < parsedNextUDPSeq.size(); ++i) {
				Pair<String, String> pair = parsedNextUDPSeq.get(i);
				
				nextUDPSeqStuff.add(pair);
				
				// Extract Data and saves it in currentSB2's state.
//	        currentSB2.updateIndividualState(pair); TODO
			}
		} catch (NullPointerException e) {
			return;	// Yes, yes, very bad coding practice indeed.
		}

	    nextUDPSeqTable.setItems(nextUDPSeqStuff);
	}
	@FXML
	public void handleNextUDPseqBlockClearTableButton(ActionEvent event) {
		nextUDPSeqTable.getItems().clear();
		parsedNextUDPSeq = null;
	}
	
//	handleTripClearTableButton
	@FXML
	public void handleTripClearTableButton(ActionEvent event) {
		tripCodeTable.getItems().clear();
	}
	
	// ####################################################################################
	// The following code(s) is to listen to Wifi Error JSON
	// ####################################################################################
	@FXML
	ArrayList<Pair<String, String>> parsedWifiErrorJSON;
	public String findNextWifiErrorJSONstring(String givenString) {
		Pattern pattern = Pattern.compile("WIFI_ERROR_JSON_START(.*?)WIFI_ERROR_JSON_END", Pattern.DOTALL);
	    Matcher matcher = pattern.matcher(givenString);
	    String lastJSONString = ""; // Variable to store the last JSON string found
	    
	    // Find all occurrences of the pattern
	    while (matcher.find()) {
	        lastJSONString = matcher.group(1); // Update the last JSON string with each match
	    }
	    
	    return lastJSONString;
	}
	public ArrayList<Pair<String, String>> parseNextWifiErrorJSONstring(String rawJSONString) {
		Gson gson = new Gson();

        // Deserialize the JSON string into a Map
        java.lang.reflect.Type type = new com.google.gson.reflect.TypeToken<java.util.Map<String, String>>(){}.getType();
        java.util.Map<String, String> map = gson.fromJson(rawJSONString, type);

        // Convert the Map entries to Pair objects and store them in an ArrayList
        ArrayList<Pair<String, String>> parsedJSONString = new ArrayList<>();
        for (java.util.Map.Entry<String, String> entry : map.entrySet()) {
            Pair<String, String> pair = new Pair<>(entry.getKey(), entry.getValue());
            parsedJSONString.add(pair);
        }

        return parsedJSONString;
	}
	
	
	// ####################################################################################
	// ####################################################################################
	// ########## The following code(s) is to handle the "SmartOmiYoko" block #############
	// ####################################################################################
	// ####################################################################################
	public double sbOmicYokoOmicSettingsPh0Voltage = 120.0;
	public double sbOmicYokoOmicSettingsPh0VoltagePhase = 0.0;
	public double sbOmicYokoOmicSettingsPh0Current = 10.0;
	public double sbOmicYokoOmicSettingsPh0CurrentPhase = 0.0;
	public double sbOmicYokoOmicSettingsPh1Voltage = 120.0;
	public double sbOmicYokoOmicSettingsPh1VoltagePhase = 180.0;
	public double sbOmicYokoOmicSettingsPh1Current = 10.0;
	public double sbOmicYokoOmicSettingsPh1CurrentPhase = 180.0;
	public double sbOmicYokoOmicSettingsPh0VoltageSB = 0.0;
	public double sbOmicYokoOmicSettingsPh0CurrentSB = 0.0;
	public double sbOmicYokoOmicSettingsPh1VoltageSB = 0.0;
	double sbOmicYokoOmicSettingsPh1CurrentSB = 0.0;
	@FXML
	TextField 	sbOmicYokoOmicSettingsPh0VoltageInputField,
				sbOmicYokoOmicSettingsPh0VoltagePhaseInputField,
				sbOmicYokoOmicSettingsPh0CurrentInputField,
				sbOmicYokoOmicSettingsPh0CurrentPhaseInputField,
				sbOmicYokoOmicSettingsPh1VoltageInputField,
				sbOmicYokoOmicSettingsPh1VoltagePhaseInputField,
				sbOmicYokoOmicSettingsPh1CurrentInputField,
				sbOmicYokoOmicSettingsPh1CurrentPhaseInputField;
	@FXML
	Circle omicronStatusCircle;
	@FXML
	Label 	sbOmicYokoOmicSettingsPhAVoltageYokoLabel,
			sbOmicYokoOmicSettingsPhACurrentYokoLabel,
			sbOmicYokoOmicSettingsPhARealPowerYokoLabel,
			sbOmicYokoOmicSettingsPhAReactivePowerYokoLabel,
			sbOmicYokoOmicSettingsPhBVoltageYokoLabel,
			sbOmicYokoOmicSettingsPhBCurrentYokoLabel,
			sbOmicYokoOmicSettingsPhBRealPowerYokoLabel,
			sbOmicYokoOmicSettingsPhBReactivePowerYokoLabel,
			// --------------------------------------------------------------
			sbOmicYokoOmicSettingsPhAVoltageSBLabel,
			sbOmicYokoOmicSettingsPhAVoltagePercentSBLabel,
			sbOmicYokoOmicSettingsPhACurrentSBLabel,
			sbOmicYokoOmicSettingsPhACurrentPercentSBLabel,
			sbOmicYokoOmicSettingsPhARealPowerSBLabel,
			sbOmicYokoOmicSettingsPhARealPowerPercentSBLabel,
			sbOmicYokoOmicSettingsPhAReactivePowerSBLabel,
			sbOmicYokoOmicSettingsPhAReactivePowerPercentSBLabel,
			sbOmicYokoOmicSettingsPhBVoltageSBLabel,
			sbOmicYokoOmicSettingsPhBVoltagePercentSBLabel,
			sbOmicYokoOmicSettingsPhBCurrentSBLabel,
			sbOmicYokoOmicSettingsPhBCurrentPercentSBLabel,
			sbOmicYokoOmicSettingsPhBRealPowerSBLabel,
			sbOmicYokoOmicSettingsPhBRealPowerPercentSBLabel,
			sbOmicYokoOmicSettingsPhBReactivePowerSBLabel,
			sbOmicYokoOmicSettingsPhBReactivePowerPercentSBLabel;
	@SuppressWarnings("exports")
	@FXML
	public ProgressIndicator autoCalibProcessIndicator;
	@SuppressWarnings("exports")
	@FXML
	public Label autoCalibStatusLabel;
	OmicronComm oc;
	YokogawaComm yc;
	AutoCalibrationTest act;
	boolean weAlreadyTurnedOnOmicron = false;
	@FXML
	public void handleSbOmicYokoBlockTurnOnOmicButton(ActionEvent event) {
		if (weAlreadyTurnedOnOmicron) {
			displayAlertWindow(AlertType.WARNING, 
								"You already started the Omicron!", 
								"You already started the Omicron!", 
								"If it doesn't feel like it is on, make sure USB is plugged in, make sure Python files are not missing, or make sure Omicron is on.");
			return;
		}
		
		Color green = Color.web("#30eb49");
		omicronStatusCircle.setFill(green);
		
		grabSbOmicYokoBlockOmicSettingsInputtedValue();
		
		oc = new OmicronComm(sbOmicYokoOmicSettingsPh0Voltage, sbOmicYokoOmicSettingsPh0VoltagePhase, sbOmicYokoOmicSettingsPh0Current, sbOmicYokoOmicSettingsPh0CurrentPhase, 
		sbOmicYokoOmicSettingsPh1Voltage, sbOmicYokoOmicSettingsPh1VoltagePhase, sbOmicYokoOmicSettingsPh1Current, sbOmicYokoOmicSettingsPh1CurrentPhase);
		
		systemOutPrintlnAndPrintLog("handleSbOmicYokoBlockTurnOnOmicButton(): Omicron Turned On", LogLevel.INFO);
		
		weAlreadyTurnedOnOmicron = true;
	}
	private void grabSbOmicYokoBlockOmicSettingsInputtedValue() {
		boolean alreadyLogged = false;
		try {
			sbOmicYokoOmicSettingsPh0Voltage = Double.valueOf(sbOmicYokoOmicSettingsPh0VoltageInputField.getText());
		} catch (NumberFormatException e) {
			if (!alreadyLogged) {
				systemOutPrintlnAndPrintLog("grabSbOmicYokoBlockOmicSettingsInputtedValue(): Error grabbing inputted Ph0Voltage value. Either you user inputted something not a number or nothing is inputted. Using default values...", LogLevel.WARNING);
				alreadyLogged = true;
			} else {
				// Dont log, we dont want to bombard user with shit ton of logs that basically says the same thing.
			}
		} catch (NullPointerException e) {
			// Do nothing, very bad practice I know
		}
		try {
			sbOmicYokoOmicSettingsPh0VoltagePhase = Double.valueOf(sbOmicYokoOmicSettingsPh0VoltagePhaseInputField.getText());
		} catch (NumberFormatException e) {
			if (!alreadyLogged) {
				systemOutPrintlnAndPrintLog("grabSbOmicYokoBlockOmicSettingsInputtedValue(): Error grabbing inputted Ph0VoltagePhase value. Either you user inputted something not a number or nothing is inputted. Using default values...", LogLevel.WARNING);
				alreadyLogged = true;
			} else {
				// Dont log, we dont want to bombard user with shit ton of logs that basically says the same thing.
			}
		} catch (NullPointerException e) {
			// Do nothing, very bad practice I know
		}
		try {
			sbOmicYokoOmicSettingsPh0Current = Double.valueOf(sbOmicYokoOmicSettingsPh0CurrentInputField.getText());
		} catch (NumberFormatException e) {
			if (!alreadyLogged) {
				systemOutPrintlnAndPrintLog("grabSbOmicYokoBlockOmicSettingsInputtedValue(): Error grabbing inputted Ph0Current value. Either you user inputted something not a number or nothing is inputted. Using default values...", LogLevel.WARNING);
				alreadyLogged = true;
			} else {
				// Dont log, we dont want to bombard user with shit ton of logs that basically says the same thing.
			}
		} catch (NullPointerException e) {
			// Do nothing, very bad practice I know
		}
		try {
			sbOmicYokoOmicSettingsPh0CurrentPhase = Double.valueOf(sbOmicYokoOmicSettingsPh0CurrentPhaseInputField.getText());
		} catch (NumberFormatException e) {
			if (!alreadyLogged) {
				systemOutPrintlnAndPrintLog("grabSbOmicYokoBlockOmicSettingsInputtedValue(): Error grabbing inputted Ph0CurrentPhase value. Either you user inputted something not a number or nothing is inputted. Using default values...", LogLevel.WARNING);
				alreadyLogged = true;
			} else {
				// Dont log, we dont want to bombard user with shit ton of logs that basically says the same thing.
			}
		} catch (NullPointerException e) {
			// Do nothing, very bad practice I know
		}
		try {
			sbOmicYokoOmicSettingsPh1Voltage = Double.valueOf(sbOmicYokoOmicSettingsPh1VoltageInputField.getText());
		} catch (NumberFormatException e) {
			if (!alreadyLogged) {
				systemOutPrintlnAndPrintLog("grabSbOmicYokoBlockOmicSettingsInputtedValue(): Error grabbing inputted Ph1Voltage value. Either you user inputted something not a number or nothing is inputted. Using default values...", LogLevel.WARNING);
				alreadyLogged = true;
			} else {
				// Dont log, we dont want to bombard user with shit ton of logs that basically says the same thing.
			}
		} catch (NullPointerException e) {
			// Do nothing, very bad practice I know
		}
		try {
			sbOmicYokoOmicSettingsPh1VoltagePhase = Double.valueOf(sbOmicYokoOmicSettingsPh1VoltagePhaseInputField.getText());
		} catch (NumberFormatException e) {
			if (!alreadyLogged) {
				systemOutPrintlnAndPrintLog("grabSbOmicYokoBlockOmicSettingsInputtedValue(): Error grabbing inputted Ph1VoltagePhase value. Either you user inputted something not a number or nothing is inputted. Using default values...", LogLevel.WARNING);
				alreadyLogged = true;
			} else {
				// Dont log, we dont want to bombard user with shit ton of logs that basically says the same thing.
			}
		} catch (NullPointerException e) {
			// Do nothing, very bad practice I know
		}
		try {
			sbOmicYokoOmicSettingsPh1Current = Double.valueOf(sbOmicYokoOmicSettingsPh1CurrentInputField.getText());
		} catch (NumberFormatException e) {
			if (!alreadyLogged) {
				systemOutPrintlnAndPrintLog("grabSbOmicYokoBlockOmicSettingsInputtedValue(): Error grabbing inputted Ph1Current value. Either you user inputted something not a number or nothing is inputted. Using default values...", LogLevel.WARNING);
				alreadyLogged = true;
			} else {
				// Dont log, we dont want to bombard user with shit ton of logs that basically says the same thing.
			}
		} catch (NullPointerException e) {
			// Do nothing, very bad practice I know
		}
		try {
			sbOmicYokoOmicSettingsPh1CurrentPhase = Double.valueOf(sbOmicYokoOmicSettingsPh1CurrentPhaseInputField.getText());
		} catch (NumberFormatException e) {
			if (!alreadyLogged) {
				systemOutPrintlnAndPrintLog("grabSbOmicYokoBlockOmicSettingsInputtedValue(): Error grabbing inputted Ph1CurrentPhase value. Either you user inputted something not a number or nothing is inputted. Using default values...", LogLevel.WARNING);
				alreadyLogged = true;
			} else {
				// Dont log, we dont want to bombard user with shit ton of logs that basically says the same thing.
			}
		} catch (NullPointerException e) {
			// Do nothing, very bad practice I know
		}
	}
	@FXML
	public void handleSbOmicYokoBlockTurnOffOmicButton(ActionEvent event) {
		if (weAlreadyTurnedOnOmicron) {
			oc.turnOffOmicron();
			
			Color red = Color.web("#f22424");
			omicronStatusCircle.setFill(red);
			
			weAlreadyTurnedOnOmicron = false;
		}
	}
	@FXML
	public void handleSbOmicYokoBlockYokoRefreshButton(ActionEvent event) {
		if (yc == null) yc = new YokogawaComm();
		
		double[] yokoReadings = yc.getPh0Ph1AllValAtOnce();
		
		sbOmicYokoOmicSettingsPhAVoltageYokoLabel.setText(String.valueOf(yokoReadings[0]));
		sbOmicYokoOmicSettingsPhACurrentYokoLabel.setText(String.valueOf(yokoReadings[1]));
		sbOmicYokoOmicSettingsPhARealPowerYokoLabel.setText(String.valueOf(yokoReadings[2]));
		sbOmicYokoOmicSettingsPhAReactivePowerYokoLabel.setText(String.valueOf(yokoReadings[4]));
		sbOmicYokoOmicSettingsPhBVoltageYokoLabel.setText(String.valueOf(yokoReadings[13]));
		sbOmicYokoOmicSettingsPhBCurrentYokoLabel.setText(String.valueOf(yokoReadings[14]));
		sbOmicYokoOmicSettingsPhBRealPowerYokoLabel.setText(String.valueOf(yokoReadings[15]));
		sbOmicYokoOmicSettingsPhBReactivePowerYokoLabel.setText(String.valueOf(yokoReadings[17]));
	}
	/**
	 * Handles the Auto Calibrate Button. Creates an auto calibrate test object and modify label
	 * to indicate user that calibration is completed.
	 * @param event
	 */
	@FXML
	public void handleSbOmicYokoBlockAutoCalibrateButton(ActionEvent event) {
		systemOutPrintlnAndPrintLog("mainGUIcontroller:handleSbOmicYokoBlockAutoCalibrateButton(): Auto calibration was triggered", LogLevel.DEBUG);
		autoCalibProcessIndicator.setVisible(true);
		autoCalibStatusLabel.setText("Running calibration...");
		
		act = new AutoCalibrationTest();
		
		if (yc == null) yc = new YokogawaComm();
		act.runAutoCalibration(this, yc, oc, serialCom);
	}
	@FXML
	public void handleSbOmicYokoBlockAutoCalibrateForceStopButton(ActionEvent event) {
		int success = act.forceStop();
		
		Platform.runLater(() -> {
			if (success == 1) {
				systemOutPrintlnAndPrintLog("mainGUIcontroller:handleSbOmicYokoBlockAutoCalibrateButton(): Auto calibration forced stop.", LogLevel.DEBUG);
				autoCalibProcessIndicator.setVisible(false);
				autoCalibStatusLabel.setText("Forced Stop.");
			} else if (success == -1) {
				systemOutPrintlnAndPrintLog("mainGUIcontroller:handleSbOmicYokoBlockAutoCalibrateButton(): Failed to force stop.", LogLevel.DEBUG);
				autoCalibProcessIndicator.setVisible(true);
				autoCalibStatusLabel.setText("Failed to force stop. (Force Stop Timed Out)");
			}
		});
	}
	@FXML
 	public void handleSBOmicYokoBlockDeviceStatusRereadButton(ActionEvent event) {
		CallSBLCPCommand.call_NonCustom_SBLCPcommand_blocking(serialCom, CallSBLCPCommand.SET_MANUFACTURING_MODE_ON, null, 3000);
		CallSBLCPCommand.call_NonCustom_SBLCPcommand_blocking(serialCom, CallSBLCPCommand.GET_METER_TELEMETRT_DATA, null, 3000);
		
		sbOmicYokoOmicSettingsPhAVoltageSBLabel.setText(String.format("%.4f", Double.valueOf(currentSB2.getIndividualState("phase_A_rms_voltage").getValue())));
		sbOmicYokoOmicSettingsPhACurrentSBLabel.setText(String.format("%.4f", Double.valueOf(currentSB2.getIndividualState("phase_A_rms_current").getValue())));
		sbOmicYokoOmicSettingsPhARealPowerSBLabel.setText(String.format("%.4f", Double.valueOf(currentSB2.getIndividualState("phase_A_active_power").getValue())));
		sbOmicYokoOmicSettingsPhAReactivePowerSBLabel.setText(String.format("%.4f", Double.valueOf(currentSB2.getIndividualState("phase_A_reactive_power").getValue())));
		sbOmicYokoOmicSettingsPhBVoltageSBLabel.setText(String.format("%.4f", Double.valueOf(currentSB2.getIndividualState("phase_B_rms_voltage").getValue())));
		sbOmicYokoOmicSettingsPhBCurrentSBLabel.setText(String.format("%.4f", Double.valueOf(currentSB2.getIndividualState("phase_B_rms_current").getValue())));
		sbOmicYokoOmicSettingsPhBRealPowerSBLabel.setText(String.format("%.4f", Double.valueOf(currentSB2.getIndividualState("phase_B_active_power").getValue())));
		sbOmicYokoOmicSettingsPhBReactivePowerSBLabel.setText(String.format("%.4f", Double.valueOf(currentSB2.getIndividualState("phase_B_reactive_power").getValue())));
		
		handleSBOmicYokoBlockDeviceStatusSeePercentDiffButton(null);
	}
	@FXML
	public void handleSbOmicYokoBlockReSetOmicButton(ActionEvent event) {
		grabSbOmicYokoBlockOmicSettingsInputtedValue();
		
		oc.modifyOmicronValues(sbOmicYokoOmicSettingsPh0Voltage, 
							   sbOmicYokoOmicSettingsPh0VoltagePhase, 
							   sbOmicYokoOmicSettingsPh0Current, 
							   sbOmicYokoOmicSettingsPh0CurrentPhase, 
							   sbOmicYokoOmicSettingsPh1Voltage, 
							   sbOmicYokoOmicSettingsPh1VoltagePhase, 
							   sbOmicYokoOmicSettingsPh1Current, 
							   sbOmicYokoOmicSettingsPh1CurrentPhase);
	}
	public void modifyAutoCalibProgressIndicatorAndLabel(boolean indicatorVisible, String labelToSet) {
		Platform.runLater(() -> {
			autoCalibProcessIndicator.setVisible(indicatorVisible);
			autoCalibStatusLabel.setText(labelToSet);
		});
	}
	@FXML
	public void handleSBOmicYokoBlockDeviceStatusSeePercentDiffButton(ActionEvent event) {
		Color green = Color.web("#30eb49");
		Color red = Color.web("#fa1505");
		
		final double GAIN_ACCEPTABLE_PERCENT = 0.02/*0.5*/;
		final double RPower_ACCEPTABLE_PERCENT = 0.05/*0.5*/;

		if (!sbOmicYokoOmicSettingsPhAVoltageYokoLabel.getText().equals("-") && !sbOmicYokoOmicSettingsPhAVoltageSBLabel.getText().equals("-")) {
			double percentDiff = calculatePercentDiffFromTwoStrings(sbOmicYokoOmicSettingsPhAVoltageYokoLabel.getText(), sbOmicYokoOmicSettingsPhAVoltageSBLabel.getText());
			sbOmicYokoOmicSettingsPhAVoltagePercentSBLabel.setText(String.format("%.3f", percentDiff));
			if (Math.abs(percentDiff) > GAIN_ACCEPTABLE_PERCENT) {
				sbOmicYokoOmicSettingsPhAVoltagePercentSBLabel.setTextFill(red);
			} else {
				sbOmicYokoOmicSettingsPhAVoltagePercentSBLabel.setTextFill(green);
			}
			
			percentDiff = calculatePercentDiffFromTwoStrings(sbOmicYokoOmicSettingsPhACurrentYokoLabel.getText(), sbOmicYokoOmicSettingsPhACurrentSBLabel.getText());
			sbOmicYokoOmicSettingsPhACurrentPercentSBLabel.setText(String.format("%.3f", percentDiff));
			if (Math.abs(percentDiff) > GAIN_ACCEPTABLE_PERCENT) {
				sbOmicYokoOmicSettingsPhACurrentPercentSBLabel.setTextFill(red);
			} else {
				sbOmicYokoOmicSettingsPhACurrentPercentSBLabel.setTextFill(green);
			}
			
			percentDiff = calculatePercentDiffFromTwoStrings(sbOmicYokoOmicSettingsPhARealPowerYokoLabel.getText(), sbOmicYokoOmicSettingsPhARealPowerSBLabel.getText());
			sbOmicYokoOmicSettingsPhARealPowerPercentSBLabel.setText(String.format("%.3f", percentDiff));
			if (Math.abs(percentDiff) > RPower_ACCEPTABLE_PERCENT) {
				sbOmicYokoOmicSettingsPhARealPowerPercentSBLabel.setTextFill(red);
			} else {
				sbOmicYokoOmicSettingsPhARealPowerPercentSBLabel.setTextFill(green);
			}
			
			percentDiff = calculatePercentDiffFromTwoStrings(sbOmicYokoOmicSettingsPhAReactivePowerYokoLabel.getText(), sbOmicYokoOmicSettingsPhAReactivePowerSBLabel.getText());
			sbOmicYokoOmicSettingsPhAReactivePowerPercentSBLabel.setText(String.format("%.3f", percentDiff));
			if (Math.abs(percentDiff) > RPower_ACCEPTABLE_PERCENT) {
				sbOmicYokoOmicSettingsPhAReactivePowerPercentSBLabel.setTextFill(red);
			} else {
				sbOmicYokoOmicSettingsPhAReactivePowerPercentSBLabel.setTextFill(green);
			}
			
			percentDiff = calculatePercentDiffFromTwoStrings(sbOmicYokoOmicSettingsPhBVoltageYokoLabel.getText(), sbOmicYokoOmicSettingsPhBVoltageSBLabel.getText());
			sbOmicYokoOmicSettingsPhBVoltagePercentSBLabel.setText(String.format("%.3f", percentDiff));
			if (Math.abs(percentDiff) > GAIN_ACCEPTABLE_PERCENT) {
				sbOmicYokoOmicSettingsPhBVoltagePercentSBLabel.setTextFill(red);
			} else {
				sbOmicYokoOmicSettingsPhBVoltagePercentSBLabel.setTextFill(green);
			}
			
			percentDiff = calculatePercentDiffFromTwoStrings(sbOmicYokoOmicSettingsPhBCurrentYokoLabel.getText(), sbOmicYokoOmicSettingsPhBCurrentSBLabel.getText());
			sbOmicYokoOmicSettingsPhBCurrentPercentSBLabel.setText(String.format("%.3f", percentDiff));
			if (Math.abs(percentDiff) > GAIN_ACCEPTABLE_PERCENT) {
				sbOmicYokoOmicSettingsPhBCurrentPercentSBLabel.setTextFill(red);
			} else {
				sbOmicYokoOmicSettingsPhBCurrentPercentSBLabel.setTextFill(green);
			}
			
			percentDiff = calculatePercentDiffFromTwoStrings(sbOmicYokoOmicSettingsPhBRealPowerYokoLabel.getText(), sbOmicYokoOmicSettingsPhBRealPowerSBLabel.getText());
			sbOmicYokoOmicSettingsPhBRealPowerPercentSBLabel.setText(String.format("%.3f", percentDiff));
			if (Math.abs(percentDiff) > RPower_ACCEPTABLE_PERCENT) {
				sbOmicYokoOmicSettingsPhBRealPowerPercentSBLabel.setTextFill(red);
			} else {
				sbOmicYokoOmicSettingsPhBRealPowerPercentSBLabel.setTextFill(green);
			}
			
			percentDiff = calculatePercentDiffFromTwoStrings(sbOmicYokoOmicSettingsPhBReactivePowerYokoLabel.getText(), sbOmicYokoOmicSettingsPhBReactivePowerSBLabel.getText());
			sbOmicYokoOmicSettingsPhBReactivePowerPercentSBLabel.setText(String.format("%.3f", percentDiff));
			if (Math.abs(percentDiff) > RPower_ACCEPTABLE_PERCENT) {
				sbOmicYokoOmicSettingsPhBReactivePowerPercentSBLabel.setTextFill(red);
			} else {
				sbOmicYokoOmicSettingsPhBReactivePowerPercentSBLabel.setTextFill(green);
			}
		} else {
			if (sbOmicYokoOmicSettingsPhAVoltageYokoLabel.getText().equals("-")) {
				handleSbOmicYokoBlockYokoRefreshButton(null);
			} else if (sbOmicYokoOmicSettingsPhAVoltageSBLabel.getText().equals("-")) {
				handleSBOmicYokoBlockDeviceStatusRereadButton(null);
			}
		}
	}
	/**
	 * 
	 * @param stringOne	The reference
	 * @param stringTwo
	 * @return The percent difference between them.
	 */
	private double calculatePercentDiffFromTwoStrings(String stringOne, String stringTwo) {
		double doubleOne = 0.0;
		double doubleTwo = 0.0;
		try {
			doubleOne = Double.valueOf(stringOne);
			doubleTwo = Double.valueOf(stringTwo);
		} catch (NumberFormatException e) {
			displayAlertWindow(AlertType.ERROR, 
							   "Make sure Breaker and Yoko fields are not '-'!", 
							   "Make sure Breaker and Yoko fields are not '-'!", 
							   "Make sure Breaker and Yoko fields are not '-'!");
		}
		return (doubleTwo - doubleOne) / doubleOne * 100.0;
	}
	
	
	// ####################################################################################
	// ####################################################################################
	// ######### The following code(s) is to handle the "EnergyAccumTest" block ###########
	// ######### Note: For this test we will use the yc and oc object defined in the ######
	// ############### "SmartOmiYoko" block. ##############################################
	@FXML
	TextField 	energyAccumTestBlockHourInputField,
				energyAccumTestBlockMinInputField,
				energyAccumTestBlockSecInputField,
				energyAccumTestBlockIntervalInputField;
	@FXML
	ProgressIndicator energyAccumTestProcessIndicator;
	@FXML
	Label energyAccumTestTestStatusLabel;
	private int hourToRunEnergyAccumTest = 1;
	private int minuteToRunEnergyAccumTest = 0;
	private int secondToRunEnergyAccumTest = 0;
	private int intervalInMSToCollectDataForEnergyAccumTest = 10000;
	private boolean weStartedEnergyAccumTest = false;
	private EnergyAccumTest eat;
	@FXML
	public void handleEnergyAccumTestBlockTimeSetButton(ActionEvent event) {
		if (energyAccumTestBlockHourInputField.getText() == null || 
				energyAccumTestBlockMinInputField.getText() == null || 
				energyAccumTestBlockSecInputField.getText() == null) {
			displayAlertWindow(AlertType.ERROR, "Invalid input", "Invalid input", "Please make sure you are entering integers!");
			return;
		}
		
		try {
			hourToRunEnergyAccumTest = Integer.valueOf(energyAccumTestBlockHourInputField.getText());
		} catch (NumberFormatException e) {
			displayAlertWindow(AlertType.ERROR, "Invalid input", "Invalid input", "Please make sure you are entering integers!");
		}
		
		try {
			minuteToRunEnergyAccumTest = Integer.valueOf(energyAccumTestBlockMinInputField.getText());
		} catch (NumberFormatException e) {
			displayAlertWindow(AlertType.ERROR, "Invalid input", "Invalid input", "Please make sure you are entering integers!");
		}
		
		try {
			secondToRunEnergyAccumTest = Integer.valueOf(energyAccumTestBlockSecInputField.getText());
		} catch (NumberFormatException e) {
			displayAlertWindow(AlertType.ERROR, "Invalid input", "Invalid input", "Please make sure you are entering integers!");
		}
	}
	@FXML
	public void handleEnergyAccumTestBlockTimeSetIntervalButton(ActionEvent event) {
		try {
			intervalInMSToCollectDataForEnergyAccumTest = Integer.parseInt(energyAccumTestBlockIntervalInputField.getText());
		} catch (NumberFormatException e) {
			displayAlertWindow(AlertType.ERROR, 
					   "Number Format Error", 
					   "Number Format Error", 
					   "Make sure you entered an integer!");
		}
		
		if (intervalInMSToCollectDataForEnergyAccumTest < 3000) {
			// Reset back to default
			intervalInMSToCollectDataForEnergyAccumTest = 10000;
			// Throw error
			displayAlertWindow(AlertType.ERROR, 
					   "Invalid value", 
					   "Invalid value", 
					   "Please enter an integer GREATER OR EQUAL to 3000!");
		}
	}
	@FXML
	public void handleEnergyAccumTestBlockStartTestButton(ActionEvent event) {
//		if (weStartedEnergyAccumTest || eat != null) {
//			displayAlertWindow(AlertType.WARNING, 
//							   "Energy Accum Test already Running", 
//							   "Energy Accum Test already Running", 
//							   "Press \"Stop Int\" to stop before restarting.");
//			return;
//		}
		
		// Create yc object if it havent been created
		if (yc == null) yc = new YokogawaComm();
		if (oc == null) oc = new OmicronComm();

		// Create new energy accum test object
		eat = new EnergyAccumTest();
		eat.startEnergyAccumTest(this, currentSB2, oc, yc, hourToRunEnergyAccumTest, minuteToRunEnergyAccumTest, secondToRunEnergyAccumTest, intervalInMSToCollectDataForEnergyAccumTest);
		weStartedEnergyAccumTest = true;
		modifyEnergyAccumTestProgressIndicatorAndLabel(true, "Test running...");
	}
	@FXML
	public void handleEnergyAccumTestBlockForceStopButton(ActionEvent event) {
//		if (!weStartedEnergyAccumTest || eat == null) {
//			displayAlertWindow(AlertType.WARNING, 
//							   "Energy Accum Test Not Running", 
//							   "Energy Accum Test Not Running", 
//							   "Press \"Start Int\" to start test.");
//			return;
//		}
		
		eat.stopEnergyAccumTest(this, yc);
		eat = null;
	}
	@FXML
	public void handleEnergyAccumTestBlockShowPlotButton(ActionEvent event) {
//		if (!weStartedEnergyAccumTest || eat == null) {
//			displayAlertWindow(AlertType.WARNING, 
//							   "Energy Accum Test not Running", 
//							   "Energy Accum Test not Running", 
//							   "Press \"Start Int\" to start test.");
//			return;
//		}
		eat.showPlot();
	}
	@FXML
	public void handleEnergyAccumTestBlockRefreshButton(ActionEvent event) {
		
	}
	public void modifyEnergyAccumTestProgressIndicatorAndLabel(boolean indicatorVisible, String labelToSet) {
		Platform.runLater(() -> {
			energyAccumTestProcessIndicator.setVisible(indicatorVisible);
			energyAccumTestTestStatusLabel.setText(labelToSet);
		});
	}
	

	// ####################################################################################
	// ####################################################################################
	// ######### The following code(s) is to handle the "Measurement Test" block ##########
	// ######### Note: For this test we will use the yc and oc object defined in the ######
	// ############### "SmartOmiYoko" block. ##############################################
	@FXML
	TextField 	measurementTestBlockCurrentsTextField,
				measurementTestBlockPhasesTextField;
	@FXML
	Button	measurementTestBlockSetSettingsButton,
			measurementTestBlockStartButton,
			measurementTestBlockForceStopButton,
			measurementTestBlockSeePlotButton;
	@FXML
	ProgressIndicator measurementTestProcessIndicator;
	@FXML
	Label measurementTestStatusLabel;
	@FXML
	MenuButton measurementTestBlockSelectTestButton;
	@FXML
	CheckBox measurementTestAverageBreakerReadingCheckBox;
	private ArrayList<Double> measurementTestBlockArrLiForCurrents = new ArrayList<>();
	private ArrayList<Double> measurementTestBlockArrLiForPhases = new ArrayList<>();
	@FXML
	public void handleMeasurementTestBlockSetSettingsButton(ActionEvent event) {
		averageBreakerReadingsForCurrentUnderOneAmp = measurementTestAverageBreakerReadingCheckBox.isSelected();
		
		// Write default values
		measurementTestBlockArrLiForCurrents.add(0.15);
		measurementTestBlockArrLiForCurrents.add(0.2);
		measurementTestBlockArrLiForCurrents.add(0.5);
		measurementTestBlockArrLiForCurrents.add(1.0);
		measurementTestBlockArrLiForCurrents.add(2.0);
		measurementTestBlockArrLiForCurrents.add(5.0);
		measurementTestBlockArrLiForCurrents.add(10.0);
		measurementTestBlockArrLiForCurrents.add(20.0);
		measurementTestBlockArrLiForCurrents.add(30.0);
		measurementTestBlockArrLiForPhases.add(0.0);
		measurementTestBlockArrLiForPhases.add(60.0);
		measurementTestBlockArrLiForPhases.add(-30.0);
		
		while (true) {	// This is not a loop for looping, it is here so that I can "return;" but go to the next try catch block below
			try {
				// Process "Currents" first
				String rawInput = measurementTestBlockCurrentsTextField.getText();
				String[] tempStringArr = rawInput.split(",");
				if (tempStringArr.length < 2) {
					displayAlertWindow(AlertType.WARNING, "You sure you have the right input value?", "You sure you have the right input value?", "Please make sure you inputted the correct values. Values must \nbe separated by commas and no spaces! \nFor example \"1,2,3\" is correct but \"1, 2, 3\" is not.");
					break;
				}
				measurementTestBlockArrLiForCurrents.removeAll(measurementTestBlockArrLiForCurrents);
				for (int i = 0; i < tempStringArr.length; ++i) {
					measurementTestBlockArrLiForCurrents.add(i, Double.valueOf(tempStringArr[i]));
				}
			} catch (NumberFormatException | NullPointerException e) {
				displayAlertWindow(AlertType.ERROR, "Error in inputted parameters", 
						"Error in inputted parameters", 
						"Make sure you entered numbers separated by commas (no space). For example \"1,2,3\" is \ncorrect but \"1, 2, 3\" is not.");
			}
			break;
		}
		
		try {
			// Process "Phases" first
			String rawInput = measurementTestBlockPhasesTextField.getText();
			String[] tempStringArr = rawInput.split(",");
			if (tempStringArr.length < 2) {
				displayAlertWindow(AlertType.WARNING, "You sure you have the right input value?", "You sure you have the right input value?", "Please make sure you inputted the correct values. Values must \nbe separated by commas and no spaces! \nFor example \"1,2,3\" is correct but \"1, 2, 3\" is not.");
				return;
			}
			measurementTestBlockArrLiForPhases.removeAll(measurementTestBlockArrLiForPhases);
			for (int i = 0; i < tempStringArr.length; ++i) {
				measurementTestBlockArrLiForPhases.add(i, Double.valueOf(tempStringArr[i]));
			}
		} catch (NumberFormatException | NullPointerException e) {
			displayAlertWindow(AlertType.ERROR, "Error in inputted parameters", 
												"Error in inputted parameters", 
												"Make sure you entered numbers separated by commas (no space). For example \"1,2,3\" is \ncorrect but \"1, 2, 3\" is not.");
		}
	}
	public void modifyMeasurementTestProgressIndicatorAndLabel(boolean indicatorVisible, String labelToSet) {
		Platform.runLater(() -> {
			measurementTestProcessIndicator.setVisible(indicatorVisible);
			measurementTestStatusLabel.setText(labelToSet);
		});
	}
	private MeasurementTest mt;
	private boolean averageBreakerReadingsForCurrentUnderOneAmp = true;
	private boolean weStartedMeasurementest = false;
	@FXML
	public void handleMeasurementTestBlockStartButton(ActionEvent event) {
		// Write default values if "Set Settings" isnt pressed
		if (measurementTestBlockArrLiForCurrents.size() == 0 || measurementTestBlockArrLiForPhases.size() == 0) {
			measurementTestBlockArrLiForCurrents.add(0.15);
			measurementTestBlockArrLiForCurrents.add(0.2);
			measurementTestBlockArrLiForCurrents.add(0.5);
			measurementTestBlockArrLiForCurrents.add(1.0);
			measurementTestBlockArrLiForCurrents.add(2.0);
			measurementTestBlockArrLiForCurrents.add(5.0);
			measurementTestBlockArrLiForCurrents.add(10.0);
			measurementTestBlockArrLiForCurrents.add(20.0);
			measurementTestBlockArrLiForCurrents.add(30.0);
			measurementTestBlockArrLiForPhases.add(0.0);
			measurementTestBlockArrLiForPhases.add(60.0);
			measurementTestBlockArrLiForPhases.add(-30.0);
		}
		
		// Create yc object if it havent been created
		if (yc == null) yc = new YokogawaComm();
		if (oc == null) oc = new OmicronComm();

		// Create new test object
		mt = new MeasurementTest();
		mt.startMeasurementTest(this, 
								serialCom,
								currentSB2, 
								oc, 
								yc, 
								measurementTestBlockArrLiForCurrents, 
								measurementTestBlockArrLiForPhases, 
								averageBreakerReadingsForCurrentUnderOneAmp);
		weStartedMeasurementest = true;
		modifyMeasurementTestProgressIndicatorAndLabel(true, "Test running...");
	}
	@FXML
	public void handleMeasurementTestBlockForceStopButton(ActionEvent event) {
		mt.stopMeasurementTest(this);
	}
	@FXML
	public void handleMeasurementTestBlockSeePlotButton(ActionEvent event) {
		mt.showPlot();
	}
	
	
	// ####################################################################################
	// ####################################################################################
	// ######### The following code(s) is to handle the "Endurance Test" block ############
	// ####################################################################################
	// ####################################################################################
	@FXML
	public Label enduranceTestBlockNumOfAttemptsLabel,
				  enduranceTestBlockNumOfFailedOpensLabel,
				  enduranceTestBlockNumOfFailedClosesLabel,
				  enduranceTestBlockTestRunningStatusLabel;
	@FXML
	private ProgressIndicator enduranceTestBlockTestRunningProgressIndicator;
	@FXML
	private TextField enduranceTestBlockCycleTImeInputField, enduranceTestBlockTimeSinceCycleInputField;
	private boolean enduranceTestIsRunning = false;
	private EnduranceTest et;
	private int inputtedCycleTime = 5000;
	private int inputtedTimeAfterCycleStartToGetDevStat = 1000;
	private boolean enduranceTestMoveCursor = false;
	private Thread enduranceTestCursorMovingThread;
	@FXML
	public void handleStartMikeEnduranceTestStartButton(ActionEvent event) {
		boolean serialComActive = checkSerialComActiveIfNotWarnAndReturnFalse();
		if (!serialComActive) return;

		int startCommandResponseTime = CallSBLCPCommand.call_NonCustom_SBLCPcommand_blocking(serialCom, CallSBLCPCommand.MIKE_ENDURANCE_TEST_START, null, 3000);
		
		if (startCommandResponseTime != -1) {
			final int MOVE_CURSOR_EVERY_T_MS = 5/*minutes*/ * 60 * 1000;
			enduranceTestMoveCursor = true;
			enduranceTestCursorMovingThread = new Thread(() -> {
	    		while (!Thread.currentThread().isInterrupted() && enduranceTestMoveCursor) {
	    			try {
	    				Thread.sleep(MOVE_CURSOR_EVERY_T_MS);
	    			} catch (InterruptedException e) {
	    				Thread.currentThread().interrupt();
	    				break;
	    			}
	    			
	    			enduranceTestMoveCursorInCircle();
	    		}
	    	});
			enduranceTestCursorMovingThread.start();
	    			
			// Start test thread
			et = new EnduranceTest();
			et.startEnduranceTest(this, currentSB2, serialCom, inputtedCycleTime, inputtedTimeAfterCycleStartToGetDevStat);
			enduranceTestIsRunning = true;
			modifyEnduranceTestProgressIndicatorAndLabel(true, "Starting...");
		} else {
			// Display error window
			displayNoResponseAlertWindow();
		}
	}
	@FXML
	public void handleStartMikeEnduranceTestStopButton(ActionEvent event) {
		if (!enduranceTestIsRunning) {
			displayAlertWindow(AlertType.WARNING, "Endurance Test is not running", "Endurance Test is not running", "You can only stop after starting");
			return;
		}
		
		boolean serialComActive = checkSerialComActiveIfNotWarnAndReturnFalse();
		if (!serialComActive) return;

		int stopComandResponseTime = CallSBLCPCommand.call_NonCustom_SBLCPcommand_blocking(serialCom, CallSBLCPCommand.MIKE_ENDURANCE_TEST_STOP, null, 3000);
		
		int stopped = 0;
		if (stopComandResponseTime != -1) {
			// Stop test thread
			stopped = et.stopEnduranceTest(this);
		} else {
			// Display error window
			displayNoResponseAlertWindow();
		}
		
		if (stopped == 1) {
			modifyEnduranceTestProgressIndicatorAndLabel(false, "Test stopped");
		} else {
			modifyEnduranceTestProgressIndicatorAndLabel(false, "Test stop failed.");
		}
	}
	public void modifyEnduranceTestProgressIndicatorAndLabel(boolean indicatorVisible, String labelToSet) {
		Platform.runLater(() -> {
			enduranceTestBlockTestRunningProgressIndicator.setVisible(indicatorVisible);
			enduranceTestBlockTestRunningStatusLabel.setText(labelToSet);
		});
	}
	public void modifyEnduranceTestNumberLables(int a, int b, int c) {
		Platform.runLater(() -> {
			enduranceTestBlockNumOfAttemptsLabel.setText(String.valueOf(a));
			enduranceTestBlockNumOfFailedOpensLabel.setText(String.valueOf(b));
			enduranceTestBlockNumOfFailedClosesLabel.setText(String.valueOf(c));
		});
	}
	@FXML
	public void handleEnduranceTestBlockApplySettingsButton(ActionEvent event) {
		try {
			inputtedCycleTime = Integer.valueOf(enduranceTestBlockCycleTImeInputField.getText());
			inputtedTimeAfterCycleStartToGetDevStat = Integer.valueOf(enduranceTestBlockTimeSinceCycleInputField.getText());
		} catch (NumberFormatException e) {
			displayAlertWindow(AlertType.ERROR, "Input format error", "Input format error", "Make sure you entered integers");
		}/* catch (NullPointerException e) {
			displayAlertWindow(AlertType.ERROR, "Input format error", "Input format error", "Make sure you entered integers");
		}*/
		System.out.println();
	}
    public void enduranceTestMoveCursorInCircle() {
    	Platform.runLater(() -> {
    		final int CIRCLE_RADIUS = 150;
    		final double INCREMENT = Math.PI / 30.0;	// Adjust this value to control the speed of movement
    		
    		System.out.println("wee~");
    		
    		Robot robot = new Robot();
    		double centerX = robot.getMouseX();
    		double centerY = robot.getMouseY();
    		
    		double angle = 0.0;
    		while (angle < 2 * Math.PI) {
    			double x = centerX + CIRCLE_RADIUS * Math.cos(angle);
    			double y = centerY + CIRCLE_RADIUS * Math.sin(angle);
    			robot.mouseMove(x, y);
    			angle += INCREMENT;
    			try {
    				Thread.sleep(10); // Adjust this value if needed to control the smoothness of movement
    			} catch (InterruptedException e) {
    				e.printStackTrace();
    			}
    		}
    		
    		// Move back to center
    		robot.mouseMove(centerX, centerY);
    	});
    }

	
	
	// ####################################################################################
	//-------------------------------------------------------------------------------------
	// 			Below methods/variables are helper methods
	//-------------------------------------------------------------------------------------
	// ####################################################################################
	/**
	 * This method will grab all serial ports' name that is connected to machine
	 * and add them into the serial ports menu button as individual MenuItem item.
	 */
	private void populateSerialPorts() {
        SerialPort[] ports = SerialPort.getCommPorts();
        for (SerialPort port : ports) {
            systemOutPrintlnAndPrintLog("\t  Found: " + port.getSystemPortName(), null);
            MenuItem newMenuItem = new MenuItem(port.getSystemPortName());
            
            // Add the "onAction" for the menu item element
            newMenuItem.setOnAction(event -> handleSerialPortMenuItemAction(event));
            
            serialPortMenuButton.getItems().add(newMenuItem);
        }
	}
	
	private void populateBreakersForMenu() {
		
		for (int i = 0; i < parsedBreakersIPList.size(); ++i) {
			MenuItem newMenuItem = new MenuItem(parsedBreakersIPList.get(i).getValue());
			newMenuItem.setOnAction(event -> handleBreakerSelectionMenuItemAction(event));
			breakerMenuButton.getItems().add(newMenuItem);
		}
		
		
	}
	
	@SuppressWarnings("unchecked")
	private void processDeviceStatusTable() {
		miscItemColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getKey()));
		miscDataColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getValue()));

		phaseAItemColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getKey()));
		phaseADataColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getValue()));

		phaseBItemColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getKey()));
		phaseBDataColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getValue()));

		powerTableItemColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getKey()));
		powerTableDataColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getValue()));
		
		if (!deviceStatusMiscTable.getColumns().contains(miscItemColumn)) deviceStatusMiscTable.getColumns().add(miscItemColumn);
		if (!deviceStatusMiscTable.getColumns().contains(miscDataColumn)) deviceStatusMiscTable.getColumns().add(miscDataColumn);
		if (!deviceStatusPhaseATable.getColumns().contains(phaseAItemColumn)) deviceStatusPhaseATable.getColumns().add(phaseAItemColumn);
		if (!deviceStatusPhaseATable.getColumns().contains(phaseADataColumn)) deviceStatusPhaseATable.getColumns().add(phaseADataColumn);
		if (!deviceStatusPhaseBTable.getColumns().contains(phaseBItemColumn)) deviceStatusPhaseBTable.getColumns().add(phaseBItemColumn);
		if (!deviceStatusPhaseBTable.getColumns().contains(phaseBDataColumn)) deviceStatusPhaseBTable.getColumns().add(phaseBDataColumn);
//		if (!deviceStatusPowerTable.getColumns().contains(powerTableItemColumn)) deviceStatusPowerTable.getColumns().add(powerTableItemColumn);
//		if (!deviceStatusPowerTable.getColumns().contains(powerTableDataColumn)) deviceStatusPowerTable.getColumns().add(powerTableDataColumn);

		// Set the columns' properties
		phaseAItemColumn.prefWidthProperty().bind(deviceStatusPhaseATable.widthProperty().multiply(0.65));
		phaseAItemColumn.setSortable(false);
		phaseADataColumn.setSortable(false);
		phaseBItemColumn.prefWidthProperty().bind(deviceStatusPhaseBTable.widthProperty().multiply(0.65));
		phaseBItemColumn.setSortable(false);
		phaseBDataColumn.setSortable(false);
		
		if (parsedDeviceStatus != null) updateDeviceStatusTable();
	}
	
	private void clearDeviceStatusTable() {
		// Clears the table (remove all element)
		deviceStatusBlockIsPopulated = false;
	    deviceStatusMiscTable.getItems().clear();
	    deviceStatusPhaseATable.getItems().clear();
	    deviceStatusPhaseBTable.getItems().clear();
//	    deviceStatusPowerTable.getItems().clear();
	    parsedDeviceStatus = null;
	}
	
	/**
	 * This method sends custom messages by first sending a 'z' and then sending the given
	 * special message string xxx,xxx,xxx,... (This depends on the type of message)
	 * 
	 * @param fullMessageIncludingHeaderToSend
	 */
	public void sendCustomMessage(String fullMessageIncludingHeaderToSend) {
		boolean serialComActive = checkSerialComActiveIfNotWarnAndReturnFalse();
		if (!serialComActive) return;
		
		// Send z to indicate this is a custom message
		String debugStr0 = serialCom.sendData("z");
		systemOutPrintlnAndPrintLog(debugStr0, LogLevel.INFO);
		
		String toSend = fullMessageIncludingHeaderToSend;
		
		// For resending function if send/string extraction fails in the c-end
//		lastCustomMessage = toSend;
		
		String debugStr1 = serialCom.sendData(toSend);
		systemOutPrintlnAndPrintLog(debugStr1, LogLevel.INFO);
		
		// Send twice, I found that this is the easiest workaround for my corrupted string problem
		serialCom.sendData("z");
		String debugStr2 = serialCom.sendData(toSend);
		systemOutPrintlnAndPrintLog(debugStr2, LogLevel.INFO);
	}

	/**
	 * This method will check serial com activeness and if not active
	 * it will warn user using a warning box and also return false so that 
	 * methods calling this method can end "return;".
	 * 
	 * @return false if serial com is not active
	 */
	private boolean checkSerialComActiveIfNotWarnAndReturnFalse() {
		if (serialCom == null) {
			displayAlertWindow(
					AlertType.WARNING, 
					"Warning", 
					"Warning Message", 
					"SblcpSerialCom object not initialized. Press \"Connect\" to initialize!"
					);
			return false;
		}

		return true;
	}
	
	private void displayNoResponseAlertWindow() {
		displayAlertWindow(AlertType.WARNING, 
						   "No Response (Timed out)", 
						   "No Response (Timed out)", 
						   "Try GET_DEVICE_STATUS again. Also, some commands require \nmanufacturing mode to be on. If still can't receive response,\nstop listening and restart.");
	}

	private int findResponseCounterInResponse(String givenString) {
		Pattern pattern = Pattern.compile("#<(\\d+)>");
	    Matcher matcher = pattern.matcher(givenString);
	    int latestResponseCounter = -1;

	    // Find all occurrences of the pattern
	    while (matcher.find()) {
	        latestResponseCounter = Integer.valueOf(matcher.group(1));
	    }

	    return latestResponseCounter;
	}
	
	
	// Below are code to manage handle position stuff
	public int findHANDLE_POSITIONstate(String givenString) {
		int toReturn = -1;
		
	    Pattern pattern = Pattern.compile("HANDLE_POSITION:-\\[(.*?)\\]HANDLE_POSITION_END", Pattern.DOTALL);
	    Matcher matcher = pattern.matcher(givenString);
	    String lastJSONString = ""; // Variable to store the last JSON string found
	    
	    // Find all occurrences of the pattern
	    while (matcher.find()) {
	        lastJSONString = matcher.group(1); // Update the last JSON string with each match
	    }
	    
	    try {
	    	toReturn = Integer.parseInt(lastJSONString);
	    } catch (NumberFormatException e) {
	    	//huh
	    }
	    
	    return toReturn;
	}
	private void updateHandlePositionLabel() {
		Platform.runLater(() -> {
			int handleState = currentSB2.getHandlePositionState();
			
			if (handleState == 0) {
				handlePositionSecondaryLabel.setText("Open");
			} else if (handleState == 1) {
				handlePositionSecondaryLabel.setText("Close");
			} else {
				handlePositionSecondaryLabel.setText("N/A");
			}
		});
	}
	@FXML
	public void handleHandlePositionBlockGetBothHandlePositionButton(ActionEvent event) {
		handleSendGetPrimaryHandleStatusButton(null);
		handleSendGetRemoteHandlePositionButton(null);
	}

	private void sendWifiCredentialsToEsp32() {
//		String debugStr0 = serialCom.sendData("w");
//		systemOutPrintlnAndPrintLog(debugStr0, LogLevel.INFO);
		
		//																	    					   vvv this part is junk
		String toSend = "#,"+ inputtedWifiSSID + "," + inputtedWifiPassword + "," + inputtedUDPKey + ",jjj\n";
		
		String debugStr1 = serialCom.sendData(toSend);
		systemOutPrintlnAndPrintLog("MainGUIcontroller.java: Wifi credentials sent, debug: " + debugStr1, LogLevel.INFO);
	}
	
	
	// ############### Does not work ###############
	private int lastCustomMessageNumber = -1;
	/**
	 * This function will find the number in the string [SEFCMNS#{%d}], compare that with variable
	 * lastCustomMessageNumber, and if they are different, we probably got a new error message, then 
	 * return true.
	 * 
	 * @param givenString The string the serial port returns
	 * @return true if we found new string extraction failed message
	 */
	private boolean foundNewCustomMessageStringExtractionFailedMessage(String givenString) {
		Pattern pattern = Pattern.compile("\\[SEFCMNS#\\{(\\d+)\\}]");	// Match the int in the string [SEFCMNS#{%d}]
        Matcher matcher = pattern.matcher(givenString);
        
        String lastCustomMessageStringExtractionFailedMessage = "";
        // Find all occurrences of the pattern
	    while (matcher.find()) {
	    	lastCustomMessageStringExtractionFailedMessage = matcher.group(1);	// Use the last one seen
	    }
	    
	    int temp = 0;
	    try {
	    	temp = Integer.parseInt(lastCustomMessageStringExtractionFailedMessage);
	    } catch (NumberFormatException e) {
	    	//huh
	    }
	    
	    if (temp != lastCustomMessageNumber) {
	    	// If current is different than last custom message fail message number, we found a new custom message fail message
	    	return true;
	    } else {
	    	return false;
	    }
	}
	
	String lastCustomMessage = "";
	private void resendLastCustomMessage() {
		System.out.println("[DEBUG] Resending last custom message");
		
		// Send z to indicate this is a custom message
		String debugStr0 = serialCom.sendData("z");
		systemOutPrintlnAndPrintLog(debugStr0, LogLevel.INFO);
		
		String debugStr1 = serialCom.sendData(lastCustomMessage);
		systemOutPrintlnAndPrintLog(debugStr1, LogLevel.INFO);
	}
	// #############################################
}