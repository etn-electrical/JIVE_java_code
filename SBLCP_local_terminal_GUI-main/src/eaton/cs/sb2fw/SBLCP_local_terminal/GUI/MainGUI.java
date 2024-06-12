/******************************************************************************************************
 * 
 * 		Connected Solutions @ Eaton (Moon Township, PA)
 * 	
 * 		@author Jonathan Tan (JonathanTan@eaton.com)
 * 		@date 5/31/2023
 * 
 * ****************************************************************************************************
 * 
 * 		MainGUI.java
 * 
 * 		JavaFX 101: In JavaFX, there is a "main function" that has to extend Application, have to override
 * 				  	the start() method, and have to have this function with the exact declaration:
 * 						public static void main(String[] args) {
 *       					launch(args);
 *   					}
 * 				 	And interesting enough, that "main function" happens to be this Class! Woohoo!
 * 				  	
 * 					On top of having that "main function" class, a javaFX program will have a "controller"
 * 					Class. While it is possible to have the controller and the "main function" in the same
 * 					file, I thought it would be less of a shit show to have them separated. Hence, we have
 * 					this class (MainGUI.java) and the controller (MainGUIcontroller.java).
 * 
 * 		Purpose	  : Now that we have a better idea of how JavaFX works, this Class is mainly used to 
 * 					handle the opening and closing of the GUI window, and also some properties of the
 * 					window, like how big it is, the icon, the title name, etc.
 * 
 * 		For more info, checkout this project's confluence page: https://confluence-prod.tcc.etn.com/pages/viewpage.action?pageId=299547479
 * 
 * *****************************************************************************************************/

package eaton.cs.sb2fw.SBLCP_local_terminal.GUI;

import javafx.application.*;
import javafx.fxml.*;
import javafx.scene.*;
import javafx.scene.image.Image;
import javafx.stage.*;

public class MainGUI extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("mainGUI.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root, 1250, 790); // width height

        // Get the controller instance
        MainGUIcontroller mainGUIcontroller = loader.getController();

        primaryStage.setOnCloseRequest(event -> {
            // Call cleanup code here
            mainGUIcontroller.stopListening();
            // Close the serial port if it's open
            if (mainGUIcontroller.serialCom != null) {
                mainGUIcontroller.serialCom.closeSerialPort();
            }
            
            try {
            	// Close the log txt writer
            	mainGUIcontroller.closeLogTxtWritterBuffer();
            } catch (NullPointerException e) {
            	// Do nothing
            	// Very bad coding practice indeed
            	// I did this so that if you open a window and attempt to close the window without listening to serial,
            	// you wont get stuck not able to close the window because of the NullPointerException
            }
            
            // Close omicron object
        	mainGUIcontroller.handleSbOmicYokoBlockTurnOffOmicButton(null);
        	
        	// Stop yc integration
//        	if (mainGUIcontroller.yc != null) {
//        		mainGUIcontroller.yc.stopIntegration();
//        		mainGUIcontroller.yc.resetIntegration();
//        	}
            
            // Exit the JavaFX application
            Platform.exit();
        });

        // primaryStage settings
        primaryStage.setScene(scene);
        primaryStage.setTitle("SBLCP Local Client");
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("eatonlogo.png")));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
