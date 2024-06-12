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
#   Purpose : Code for GF testing.
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
# ----- Util functions import 
# -------------------------------------------------------------------------------------------------------------------
from util import bin_in_reader


# -------------------------------------------------------------------------------------------------------------------
# ----- Local variable
# -------------------------------------------------------------------------------------------------------------------
FILE_NAME = "gf_test_main.py"
FUNCTION_NAME = "main()"
PRINT_TAG = FILE_NAME + ":" + FUNCTION_NAME + ": "
LOG_TYPE = "INFO"
LOG_HEADER = "[" + LOG_TYPE + "] "


# -------------------------------------------------------------------------------------------------------------------
# ----- Here we go
# -------------------------------------------------------------------------------------------------------------------
if __name__ == "__main__":
    #######################################################
    # Initialize communication with Omicron 
    # This section is how the program reachs the CMC API
    #######################################################
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


    #######################################################
    # The good stuff
    #######################################################

    # Set values for the omi and turn it on
    frequency = 60  # [Hz]
    # --------------------------------------------------
    engineApp.Exec(devID, f"out:v(1):f({frequency})")
    engineApp.Exec(devID, f"out:i(1):f({frequency})")
    # engineApp.Exec(devID, f"out:v(2):f({frequency})")
    # engineApp.Exec(devID, "out:v(1:1):p(0)")                # output 1 power 0?
    ## Phase 1
    engineApp.Exec(devID, "out:v(1:1):p(0)")
    engineApp.Exec(devID, f"out:v(1:1):a({50})")         # the 1:1 means phase 1, 1:2 phase 2
    engineApp.Exec(devID, f"out:i(1:1):a({2})")          # the 1 in v(1) means 
    ## Phase 2
    engineApp.Exec(devID, "out:v(1:2):p(0)")
    engineApp.Exec(devID, f"out:v(1:2):a({50})")         # the 1:1 means phase 1, 1:2 phase 2
    engineApp.Exec(devID, f"out:i(1:2):a({2})")          # the 1 in v(1) means 
    engineApp.Exec(devID, "out:on")
    

    # Keep polling until one of the inputs in binInputsStateWeCareAbout change from on (or high or True) to off (or low or False)
    voltage_thresh = 1.0
    binInputsStateWeCareAbout = [1, 2, 10]
    while (True):   # Blocking function
        binInputsState = bin_in_reader.pollBinInput(engineApp, devID, voltage_thresh, True)
        print(LOG_HEADER + PRINT_TAG + f"Bin Inputs State: " + str(binInputsState))

        yes = True
        for i in range(0, len(binInputsStateWeCareAbout)):
            yes = binInputsState[i - 1]
        
        if not yes: break


    # Cut the current
    engineApp.Exec(devID, f"out:i(1:1):off")
    engineApp.Exec(devID, f"out:i(1:2):off")
    engineApp.Exec(devID, f"out:i(1:1):a({0})")
    engineApp.Exec(devID, f"out:i(1:2):a({0})")
    engineApp.Exec(devID, f"out:i(1:1):on")
    engineApp.Exec(devID, f"out:i(1:2):on")


    engineApp.Exec(devID, "out:bin(1):set(0b0001)") # Turn on bin out 1


    input("Press Enter to continue...")
    engineApp.Exec(devID, "out:bin(1):set(0b0000)")
    engineApp.Exec(devID, "out:off")

    print(LOG_HEADER + PRINT_TAG + "Omicron Off")

    engineApp.DevUnlock(devID)

# -------------------------------------------------------------------------------------------------------------------
# ----- EOF
# -------------------------------------------------------------------------------------------------------------------