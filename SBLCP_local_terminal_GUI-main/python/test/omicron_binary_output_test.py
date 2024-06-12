import sys
import win32com.client # pip install pywin32
import time
import pyvisa

print("================================================== Python Start ==================================================")

# -------------------------------------------------------------------------------------------------------------------
# ----- Initialize communication with Omicron 
# -------------------------------------------------------------------------------------------------------------------

#This section is how the progroma reachs the CMC API
engineApp = win32com.client.Dispatch("OMICRON.CMEngAL")

# get IDs of associated CMCs - first in the list is used
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


# -------------------------------------------------------------------------------------------------------------------
# ----- My tests starts here 
# -------------------------------------------------------------------------------------------------------------------

print("-------------------------------------------------- Me Experiment --------------------------------------------------")

print(engineApp.Exec(devID, "out:bin:cfg?"))        # Get the number of modules
print(engineApp.Exec(devID, "out:bin(1):cfg?"))     # Get information about each modules
print(engineApp.Exec(devID, "out:bin(2):cfg?"))     # Get information about each modules

engineApp.Exec(devID, "out:bin(1):on(1,3)")

time.sleep(3)

engineApp.Exec(devID, "out:bin(1):off(1,2,3,4)")

time.sleep(3)

engineApp.Exec(devID, "out:bin(1):set(0b1011)")

time.sleep(3)

engineApp.Exec(devID, "out:bin(1):set(0b0000)")

# INPUT_CHANNEL = 1

# def isInputOn(input_channel):
#     BUFFER_SIZE = 20
#     buffer = []
#     bufferSum = 0
#     # initialize buffer with 20 0s
#     for _ in range(0, BUFFER_SIZE): buffer.append(int(0))

#     count = 0
#     for i in range(0, BUFFER_SIZE):
#         # print(str(count) + ":\t", end = "")
#         count += 1
#         omiOut = engineApp.Exec(devID, "inp:bin(1):get?")
#         # print(omiOut)
#         hiOrLow = int(omiOut.split(',')[1].split(";")[0])
#         buffer[i] = hiOrLow
#         bufferSum += hiOrLow

#         time.sleep(0.0001)
        
#     # print(bufferSum)
#     tof = bufferSum / (BUFFER_SIZE * 2**input_channel)
#     # print(tof)

#     if (tof > 0.0):
#         tof = True      # Yes I am really doing this, I know, it is gross
#     else:
#         tof = False     # Yes I am really doing this, I know, it is gross
    
#     return tof

# # print(f"So is the binary input {INPUT_CHANNEL} on receiving input? Answer: {isInputOn(INPUT_CHANNEL)}")


# # -------------------------------------------------------------------------------------------------------------------
# # ----- Good stuff starts here 
# # -------------------------------------------------------------------------------------------------------------------

# while (not isInputOn(INPUT_CHANNEL)):   # Blocking function
#     print(f"Waiting for input channel {INPUT_CHANNEL} to be on...")


# # values for the ramp
# voltage = 1     # [V]
# frequency = 60  # [Hz]

# # static values
# engineApp.Exec(devID, f"out:v(1):f({frequency})")
# engineApp.Exec(devID, f"out:i(1):f({frequency})")
# # engineApp.Exec(devID, f"out:v(2):f({frequency})")
# # engineApp.Exec(devID, "out:v(1:1):p(0)")                # output 1 power 0?

# ## Phase 1
# engineApp.Exec(devID, "out:v(1:1):p(5)")            # phase?
# engineApp.Exec(devID, f"out:v(1:1):a({30})")         # the 1:1 means phase 1, 1:2 phase 2
# engineApp.Exec(devID, f"out:i(1:1):a({1})")          # the 1 in v(1) means 

# ## Phase 2
# engineApp.Exec(devID, "out:v(1:2):p(20)")            # phase?
# engineApp.Exec(devID, f"out:v(1:2):a({50})")         # the 1:1 means phase 1, 1:2 phase 2
# engineApp.Exec(devID, f"out:i(1:2):a({2})")          # the 1 in v(1) means 

# engineApp.Exec(devID, "out:on")
# input("Press Enter to continue...")
# engineApp.Exec(devID, "out:off")

# print("Omicron Off")

engineApp.DevUnlock(devID)

print("================================================== Python End ==================================================")