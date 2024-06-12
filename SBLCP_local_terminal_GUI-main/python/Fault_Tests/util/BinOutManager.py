# ###################################################################################################################
# *******************************************************************************************************************
# 
#   Connected Solutions @ Eaton (Moon Township, PA)
#
#   Author  : Jonathan Tan (jonathantan@eaton.com)
#   Date    : 8/3/2023
#
# *******************************************************************************************************************
#
#   Purpose : This class is dumb and unnecessary. IGNORE THIS
# 
# *******************************************************************************************************************
# ###################################################################################################################

# -------------------------------------------------------------------------------------------------------------------
# ----- Libraries import 
# -------------------------------------------------------------------------------------------------------------------
import sys
import win32com.client # pip install pywin32
import time


# -------------------------------------------------------------------------------------------------------------------
# ----- Local variable
# -------------------------------------------------------------------------------------------------------------------
FILE_NAME = "BinOutManager.py"
FUNCTION_NAME = "BinOutManager"
PRINT_TAG = FILE_NAME + ":" + FUNCTION_NAME + ": "
LOG_TYPE = "DEBUG"
LOG_HEADER = "[" + LOG_TYPE + "] "


class BinOutManager:
	def __init__(self, omicronEngineApp, omicronDevID):
		self.binOut_1 = 0
		self.binOut_2 = 0
		self.binOut_3 = 0
		self.binOut_4 = 0
		
		self.omicronEngineApp = omicronEngineApp
		self.omicronDevID = omicronDevID

	def setBinOut(self, binOutNum=-1):
		if (binOutNum == 1):
			self.binOut_1 = 1
		elif (binOutNum == 2):
			self.binOut_2 = 1
		elif (binOutNum == 3):
			self.binOut_3 = 1
		elif (binOutNum == 4):
			self.binOut_4 = 1
		else:
			print(LOG_HEADER + "Please enter a bin output port number!")

	def clearBinOut(self, binOutNum=-1):
		if (binOutNum == 1):
			self.binOut_1 = 0
		elif (binOutNum == 2):
			self.binOut_2 = 0
		elif (binOutNum == 3):
			self.binOut_3 = 0
		elif (binOutNum == 4):
			self.binOut_4 = 0
		else:
			print(LOG_HEADER + "Please enter a bin output port number!")

	def turnOnBinOut(self):
		if (self.binOut_1 == 0 and self.binOut_2 == 0 and self.binOut_3 == 0 and self.binOut_4 == 0):
			print(LOG_HEADER + "Please use setBinOut(int) to tell me which bin output to turn on!")
			return
		
		strOfPortsToTurnOn = ""
		if (self.binOut_1 == 1):
			strOfPortsToTurnOn += "1"
		if (self.binOut_2 == 1):
			if (strOfPortsToTurnOn == ""):
				strOfPortsToTurnOn += "2"
			else:
				strOfPortsToTurnOn += ",2"
		if (self.binOut_3 == 1):
			if (strOfPortsToTurnOn == ""):
				strOfPortsToTurnOn += "3"
			else:
				strOfPortsToTurnOn += ",3"
		if (self.binOut_4 == 1):
			if (strOfPortsToTurnOn == ""):
				strOfPortsToTurnOn += "4"
			else:
				strOfPortsToTurnOn += ",4"

		# Turn on bin out
		self.omicronEngineApp.Exec(self.omicronDevID, f"out:bin(1):off({strOfPortsToTurnOn})")