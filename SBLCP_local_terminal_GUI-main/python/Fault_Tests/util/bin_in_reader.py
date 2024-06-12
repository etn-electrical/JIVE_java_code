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
#   Purpose : This code houses the function:
#                   pollBinInput(omicronEngineApp, omicronDevID, voltage_thresh=1.0, disablePrint=True)
#             Given the Omicron ID, voltage threshold, this function will scan the BINARY input ports on the 
#             omicron and return a list of boolean with "True" being on and "False being off".
#             So for example if you pluged in a 5Vrms 60Hz signal into binary input 1, pollBinInput() will return:
#                   [True, False, False, False, False, False, False, False, False, False]
# 
#   Note    : This code is hard coded to binary input group 1 (ie the command "inp:bin(1):get?"). For CMC356,
#             group 1 is the front panel's binary inputs and group 2 is the back panel's (i think). So, if you 
#             want to use this function for the back panel's bin input, you need to modify the code.
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
FILE_NAME = "bin_in_reader.py"
FUNCTION_NAME = "pollBinInput()"
PRINT_TAG = FILE_NAME + ":" + FUNCTION_NAME + ": "
LOG_TYPE = "DEBUG"
LOG_HEADER = "[" + LOG_TYPE + "] "


# -------------------------------------------------------------------------------------------------------------------
# ----- Binary input reader/pinger/poller whatever you want to call it
# ----- This function works by reading the omicron response 20 times and averaging it. If the averaged results
# ----- is greater than SHOULD_BE_GREATER_THAN_ME, it will think the binary input is high.
# ----- 
# ----- Important: SHOULD_BE_GREATER_THAN_ME is set to 0.0, which is pretty stupid, if we saw just a couple times
# -----            that the input is high, it will obviously be > 0.0. So SHOULD_BE_GREATER_THAN_ME's value should
# -----            be modified to suit application. As of now, I set SHOULD_BE_GREATER_THAN_ME == 0.0 so that it 
# -----            will, as long as the input reading is high even only once, the function will think that the input 
# -----            is high.
# -----
# ----- Ps       : When I say "input" I mean the 10 binary inputs on the front panel of the Omicron
# -----
# -------------------------------------------------------------------------------------------------------------------
def pollBinInput(omicronEngineApp, omicronDevID, voltage_thresh=1.0, disablePrint=True):
    SHOULD_BE_GREATER_THAN_ME = 0.0
    
    if (not disablePrint): print(LOG_HEADER + PRINT_TAG + "inp:bin:cfg?: " + omicronEngineApp.Exec(omicronDevID, "inp:bin:cfg?"))
    if (not disablePrint): print(LOG_HEADER + PRINT_TAG + "inp:bin(1:1):cfg?: " + omicronEngineApp.Exec(omicronDevID, "inp:bin(1:1):cfg?"))

    omicronEngineApp.Exec(omicronDevID, f"inp:bin(1:1):def({voltage_thresh})")
    omicronEngineApp.Exec(omicronDevID, f"inp:bin(1:2):def({voltage_thresh})")
    omicronEngineApp.Exec(omicronDevID, f"inp:bin(1:3):def({voltage_thresh})")
    omicronEngineApp.Exec(omicronDevID, f"inp:bin(1:4):def({voltage_thresh})")
    omicronEngineApp.Exec(omicronDevID, f"inp:bin(1:5):def({voltage_thresh})")
    omicronEngineApp.Exec(omicronDevID, f"inp:bin(1:6):def({voltage_thresh})")
    omicronEngineApp.Exec(omicronDevID, f"inp:bin(1:7):def({voltage_thresh})")
    omicronEngineApp.Exec(omicronDevID, f"inp:bin(1:8):def({voltage_thresh})")
    omicronEngineApp.Exec(omicronDevID, f"inp:bin(1:9):def({voltage_thresh})")
    omicronEngineApp.Exec(omicronDevID, f"inp:bin(1:10):def({voltage_thresh})")


    POLL_MAX_AMOUNT = 20
    BIN_CHANNEL_AMOUNT = 10
    sum_arr_buffer = []     # int array
    arr_to_return = []      # boolean array
    # initialize buffer with 20 0s
    for _ in range(0, BIN_CHANNEL_AMOUNT): 
        sum_arr_buffer.append(int(0))
        arr_to_return.append(False)

    count = 0
    for i in range(0, POLL_MAX_AMOUNT):
        if (not disablePrint): print(LOG_HEADER + PRINT_TAG + str(count) + ":\t", end = "")
        count += 1
        omiOut = omicronEngineApp.Exec(omicronDevID, "inp:bin(1):get?")
        # omiOut = omicronEngineApp.Exec(omicronDevID, "inp:ana:v(1):get?")
        if (not disablePrint): print(str(omiOut))
        hiOrLow_dec = int(omiOut.split(',')[1].split(";")[0])

        for j in reversed(range(0, BIN_CHANNEL_AMOUNT)):
            if (hiOrLow_dec & (2**j) == (2**j)):
                # We know that channel 10 is high
                sum_arr_buffer[j] += 1

        time.sleep(1/60/30)
        # time.sleep(0.25)
        
    # See if the specific channel is on
    for i in range(0, BIN_CHANNEL_AMOUNT):
        if (sum_arr_buffer[i] / (POLL_MAX_AMOUNT) > SHOULD_BE_GREATER_THAN_ME):
            # This means that the channel that correspond to this index is high
            arr_to_return[i] = True
    
    return arr_to_return


# For testing only
if __name__ == "__main__":
    # Initialize communication with Omicron 
    # This section is how the program reachs the CMC API
    engineApp = win32com.client.Dispatch("OMICRON.CMEngAL")

    # Get IDs of associated CMCs - first in the list is used
    engineApp.DevScanForNew()
    devList = engineApp.DevGetList(0)
    try:
        devID = int(str.split(str.split(devList, ';')[0], ',')[0])
        serialNo = str.split(str.split(devList, ';')[0], ',')[1]
    except Exception:
        print("Couldn't get ID for device")
        sys.exit()

    engineApp.DevLock(devID)
    print(f"CMC with Sno. {serialNo} connected.")

    print(pollBinInput(engineApp, devID, False))