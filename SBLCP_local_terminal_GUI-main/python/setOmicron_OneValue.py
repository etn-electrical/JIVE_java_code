
import sys
import win32com.client # pip install pywin32
import time
import pyvisa
import keyboard

#This section is how the progroma reachs the CMC API
engineApp = win32com.client.Dispatch("OMICRON.CMEngAL")

# get IDs of associated CMCs - first in the list is used
engineApp.DevScanForNew()
devList = engineApp.DevGetList(0)
try:
    devID = int(str.split(str.split(devList, ';')[0], ',')[0])
    serialNo = str.split(str.split(devList, ';')[0], ',')[1]
except Exception:
    print("[ERROR] setOmicron_OneValue.py: Couldn't get ID for device")
    sys.exit()

engineApp.DevLock(devID)
print(f"[INFO] setOmicron_OneValue.py: CMC with Sno. {serialNo} connected.")

frequency = 60 # [Hz]

def setOmicronParameters():
    try:    # Input args format: <Phase> <Param> <Value> <Param Phase>
        # Grab user input
        phaseToSet = str(sys.argv[1])           # format: phase 0: ph0; phase 1: ph1
        parametersToSet = str(sys.argv[2])      # format: URMS (in volts), IRMS (in A), P (phase, in degree)
        valuesToSet = float(sys.argv[3])        # format: double: eg, 120.0, 10.0, 180.0, etc.
        paramPhaseToSet = float(sys.argv[4])    # format: double: eg, 120.0, 10.0, 180.0, etc.
        print(f"[INFO] setOmicron_OneValue.py: Phase: {phaseToSet}\n[INFO] setOmicron_OneValue.py: Param: {parametersToSet}\n[INFO] setOmicron_OneValue.py: Value: {valuesToSet}\n[INFO] setOmicron_OneValue.py: Param Phase: {paramPhaseToSet}")
    except IndexError:
        print("[ERROR] setOmicron_OneValue.py: Incorrect number of parameters. Need 4 total.")
        return 0
    except NameError:
        print("[ERROR] setOmicron_OneValue.py: Incorrect number of parameters. Need 4 total.")
        return 0
    else:
        print(f"[INFO] setOmicron_OneValue.py: Parameters all good. Sending them to Omicron...")

    # Set freq of all phases (1, 2, and 3, aka 1:1, 1:2, and 1:3)
    engineApp.Exec(devID, f"out:v(1):f({frequency})")
    engineApp.Exec(devID, f"out:i(1):f({frequency})")

    # Define phase Number
    phaseNumber = -1
    if (phaseToSet == "ph0"):
        phaseNumber = 0 + 1
    elif (phaseToSet == "ph1"):
        phaseNumber = 1 + 1

    if (phaseNumber == -1):
        return 0

    if (parametersToSet == "URMS"):
        # We want to set voltage
        if (not((valuesToSet > 120.0) or (valuesToSet < -120.0))):   # We do this to prevent setting voltage too high
            engineApp.Exec(devID, f"out:v(1:{phaseNumber}):a({valuesToSet})")
            # Set the phase of the voltage
            engineApp.Exec(devID, f"out:v(1:{phaseNumber}):p({paramPhaseToSet})")
            return 1    # Success
    elif (parametersToSet == "IRMS"):
        # We want to set current
        if (not((valuesToSet > 60.0) or (valuesToSet < -60.0))):    # We do this to prevent setting voltage too high
            engineApp.Exec(devID, f"out:i(1:{phaseNumber}):a({valuesToSet})")
            # Set the phase of the current
            engineApp.Exec(devID, f"out:i(1:{phaseNumber}):p({paramPhaseToSet})")
            return 1    # Success
    else:
        print("[ERROR] setOmicron_OneValue.py: Invalid parameter.")
        return 0

stat = setOmicronParameters()

TIMEOUT_DURATION = 30 # in sec

if (stat == 1):
    engineApp.Exec(devID, "out:on")
    print("[INFO] setOmicron_OneValue.py: Press press 's' + Enter to continue...")
    
    start_time = time.time()
    while time.time() - start_time < TIMEOUT_DURATION:
        try:
            input_data = input()
            if input_data:
                break
        except KeyboardInterrupt:
            break
    else:
        print("[WARNING] setOmicron_OneValue.py: Timeout reached. Turning off...")
        engineApp.Exec(devID, "out:off")

    engineApp.Exec(devID, "out:off")
    print("[INFO] setOmicron_OneValue.py: Omicron Off.")
else:
    print("[ERROR] setOmicron_OneValue.py: Parameters set fail, restart program to retry.")

engineApp.DevUnlock(devID)