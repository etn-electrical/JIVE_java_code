package eaton.cs.sb2fw.SBLCP_local_terminal.util;

import java.io.IOException;
import java.util.Arrays;

import eaton.cs.sb2fw.SBLCP_local_terminal.tests.AutoCalibrationTest;
import eaton.cs.sb2fw.SBLCP_local_terminal.tests.EnduranceTest;
import eaton.cs.sb2fw.SBLCP_local_terminal.tests.EnergyAccumTest;
import eaton.cs.sb2fw.SBLCP_local_terminal.tests.MeasurementTest;
import eaton.cs.sb2fw.SBLCP_local_terminal.GUI.*;

public class TestRememberToDeleteMe {
	public static void main(String[] args) {
		EnduranceTest et = new EnduranceTest();
		
		et.writeToCSV(1, 2, 3, 4, 5.5, 6.6, 7.7, 8.8);
		et.writeToCSV(2, 20, 30, 40, 50.5, 60.6, 70.7, 80.8);
		et.writeToCSV(3, 200, 300, 400, 500.5, 600.6, 700.7, 800.8);
		
		et.closeCSV();
		
		System.out.println("DOne");
	}
}
