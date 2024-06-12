
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
    print("[ERROR] setOmicron_AllPh0Ph1.py: Couldn't get ID for device")
    sys.exit()

engineApp.DevLock(devID)
print(f"[INFO] setOmicron_AllPh0Ph1.py: CMC with Sno. {serialNo} connected.")

frequency = 60 # [Hz]


def setOmicronParameters():
    try:
        # Grab user input, expected format:
        #       0v,0vp,0i,0ip,1v,1vp,1i,1ip
        #       explaination: phase 0 voltage, phase 0 voltage phase, etc
        theWholeString = str(sys.argv[1])
        
        strli = theWholeString.split(",")

        if (len(strli) != 8):
            print("[ERROR] setOmicron_AllPh0Ph1.py: Incorrect number of parameters.")
            return 0
    except IndexError:
        print("[ERROR] setOmicron_AllPh0Ph1.py: Incorrect number of parameters.")
        return 0
    else:
        print(f"[INFO] setOmicron_AllPh0Ph1.py: Parameters all good. Sending them to Omicron...")

    # Set freq of all phases (1, 2, and 3, aka 1:1, 1:2, and 1:3)
    engineApp.Exec(devID, f"out:v(1):f({frequency})")
    engineApp.Exec(devID, f"out:i(1):f({frequency})")

    # Setting values according to input string format
    engineApp.Exec(devID, f"out:v(1:1):a({strli[0]})")
    engineApp.Exec(devID, f"out:v(1:1):p({strli[1]})")
    engineApp.Exec(devID, f"out:i(1:1):a({strli[2]})")
    engineApp.Exec(devID, f"out:i(1:1):p({strli[3]})")
    engineApp.Exec(devID, f"out:v(1:2):a({strli[4]})")
    engineApp.Exec(devID, f"out:v(1:2):p({strli[5]})")
    engineApp.Exec(devID, f"out:i(1:2):a({strli[6]})")
    engineApp.Exec(devID, f"out:i(1:2):p({strli[7]})")

    return 1

stat = setOmicronParameters()

TIMEOUT_DURATION = 30 # in sec

if (stat == 1):
    engineApp.Exec(devID, "out:on")
    print("[INFO] setOmicron_AllPh0Ph1.py: Press press 's' + Enter to continue...")
    
    start_time = time.time()
    while time.time() - start_time < TIMEOUT_DURATION:
        try:
            input_data = input()
            if input_data:
                break
        except KeyboardInterrupt:
            break
    else:
        print("[WARNING] setOmicron_AllPh0Ph1.py: Timeout reached. Turning off...")
        engineApp.Exec(devID, "out:off")

    engineApp.Exec(devID, "out:off")
    print("[INFO] setOmicron_AllPh0Ph1.py: Omicron Off.")
else:
    print("[ERROR] setOmicron_AllPh0Ph1.py: Parameters set fail, restart program to retry.")

engineApp.DevUnlock(devID)