/**
 * @author Jonathan Tan (JonathanTan@eaton.com)
 *
 */
module sb2fw_SBLCP_local_terminal {
    exports eaton.cs.sb2fw.SBLCP_local_terminal.GUI;

	requires javafx.controls;
    requires javafx.fxml;
	requires javafx.base;
	requires javafx.graphics;
	requires com.fazecast.jSerialComm;
	requires com.google.gson;
    
    opens eaton.cs.sb2fw.SBLCP_local_terminal.GUI to javafx.fxml;
}