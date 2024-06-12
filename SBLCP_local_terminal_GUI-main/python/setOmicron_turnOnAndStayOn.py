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
    print("[ERROR] setOmicron_turnOnAndStayOn.py: Couldn't get ID for device")
    sys.exit()

engineApp.DevLock(devID)
print(f"[INFO] setOmicron_turnOnAndStayOn.py: CMC with Sno. {serialNo} connected.")

######################################################################################################
# vvvvvvvvvvvvvvvvvvvvvvvvv If you need to change the frequency, change here vvvvvvvvvvvvvvvvvvvvvvvvv
frequency = 60 # [Hz]
# ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
######################################################################################################
print(f"[INFO] setOmicron_turnOnAndStayOn.py: Frequency is hardcoded to {frequency}Hz, to modify, do it in the code.")

def setOmicronParameters():
    try:
        # Grab user input, expected format:
        #       0v,0vp,0i,0ip,1v,1vp,1i,1ip
        #       explaination: phase 0 voltage, phase 0 voltage phase, etc
        theWholeString = str(sys.argv[1])
        
        strli = theWholeString.split(",")

        if (len(strli) != 8):
            print("[ERROR] setOmicron_turnOnAndStayOn.py: Incorrect number of parameters.")
            return 0
    except IndexError:
        print("[ERROR] setOmicron_turnOnAndStayOn.py: Incorrect number of parameters.")
        return 0
    else:
        print(f"[INFO] setOmicron_turnOnAndStayOn.py: Parameters all good. Sending them to Omicron...")

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

TIMEOUT_DURATION = 1800 # in sec (1800s = 30min)

if (stat == 1):
    engineApp.Exec(devID, "out:on")
    print("[INFO] setOmicron_turnOnAndStayOn.py: Type \"shutdown\" + Enter to shutdown, or re-enter arguments to change output values. Note that dispite you HAVE to input voltage arguments, they are unchangable.")
    print("Listening to new argument...")
    
    start_time = time.time()
    while time.time() - start_time < TIMEOUT_DURATION:
        try:
            input_data = input()
            if input_data == "shutdown":
                break
            else:
                # Might be crap might be new arguments, split to find out
                newStrli = input_data.split(",")

                print(f"[DEBUG] setOmicron_turnOnAndStayOn.py: newStrli: {newStrli}")

                if len(newStrli) == 8:
                    # Not crap
                    # Setting values according to input string format
                    # engineApp.Exec(devID, f"out:v(1:1):a({newStrli[0]})")
                    # engineApp.Exec(devID, f"out:v(1:1):p({newStrli[1]})")
                    engineApp.Exec(devID, f"out:i(1:1):off")
                    engineApp.Exec(devID, f"out:i(1:1):a({newStrli[2]})")
                    engineApp.Exec(devID, f"out:i(1:1):p({newStrli[3]})")
                    engineApp.Exec(devID, f"out:i(1:1):on")
                    # engineApp.Exec(devID, f"out:v(1:2):a({newStrli[4]})")
                    # engineApp.Exec(devID, f"out:v(1:2):p({newStrli[5]})")
                    engineApp.Exec(devID, f"out:i(1:2):off")
                    engineApp.Exec(devID, f"out:i(1:2):a({newStrli[6]})")
                    engineApp.Exec(devID, f"out:i(1:2):p({newStrli[7]})")
                    engineApp.Exec(devID, f"out:i(1:2):on")

                    continue    # We can change values infinitely
                else:
                    # Is crap
                    print("[ERROR] setOmicron_turnOnAndStayOn.py: Error in new argument. Please re-enter correct number of argument (8). Listening to new argument...")
                    continue
        except KeyboardInterrupt:
            break
    else:
        print("[WARNING] setOmicron_turnOnAndStayOn.py: Timeout reached. Turning off...")
        engineApp.Exec(devID, "out:off")

    engineApp.Exec(devID, "out:off")
    print("[INFO] setOmicron_turnOnAndStayOn.py: Omicron Off.")
else:
    print("[ERROR] setOmicron_turnOnAndStayOn.py: Parameters set fail, restart program to retry.")

engineApp.DevUnlock(devID)