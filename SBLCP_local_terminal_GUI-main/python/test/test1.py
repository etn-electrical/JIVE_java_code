"""
PickUpTest.py
This example shows how the CMC Sequence Memory and internal counters are used to perform real time signal ramps
and measure the starting current of an Overcurrent protection relay.

author: LukGas
date: 05.10.2022
version: 1.0
"""

import sys
import win32com.client # pip install pywin32
import time
import pyvisa 

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


# values for the ramp
voltage = 35 # [V]
#startCurrent = 1.5 # [A]
#endCurrent = 2 # [A]
#stepCurrent = 0.01 # [A]
frequency = 60 # [Hz]
#stepTime = 0.1 # [s]



# static values
engineApp.Exec(devID, f"out:v(1):f({frequency})")
engineApp.Exec(devID, "out:v(1:1):p(0)")
#engineApp.Exec(devID, "out:v(1:2):p(-120)")
#engineApp.Exec(devID, "out:v(1:3):p(120)")
#engineApp.Exec(devID, f"out:i(1):f({frequency})")
#engineApp.Exec(devID, "out:i(1:1):p(0)")
#engineApp.Exec(devID, "out:i(1:2):p(-120)")
#engineApp.Exec(devID, "out:i(1:3):p(120)")

## calculate number of steps
#count = int((endCurrent - startCurrent) / stepCurrent + 1)

## prepare sequence
#engineApp.Exec(devID, "seq:begin")
engineApp.Exec(devID, f"out:v(1):a({voltage})")
#engineApp.Exec(devID, f"out:i(1):a({startCurrent})")
#engineApp.Exec(devID, f"out:i(1):a({stepCurrent},step)")
#engineApp.Exec(devID, f"seq:set(count(1),{count})")
engineApp.Exec(devID, "out:on")
#engineApp.Exec(devID, f"seq:wait(bin(2),3,{stepTime},2)")
#engineApp.Exec(devID, "out:ana:step")
#engineApp.Exec(devID, f"seq:wait(bin(2),2,{stepTime},1)")
#engineApp.Exec(devID, "seq:add(count(1),-1)")
#engineApp.Exec(devID, "seq:wait(count(1)=0,1,0,-1)")
print("before timer")
time.sleep(10)
engineApp.Exec(devID, "out:off")
#engineApp.Exec(devID, "seq:end")


## now start this sequence in the CMC
#engineApp.Exec(devID, "seq:exec")
#print("Test started.")

## wait until the sequence is finished
#while True:
#    result = engineApp.Exec(devID, "seq:status?(step)")
#    step = int(str.split(str.split(result, ',')[1], ';')[0])
#    if step == 0:
#        break

## read out counter
#countNow = int(str.split(str.split(engineApp.Exec(devID, "seq:status?(count(1))"), ',')[1], ';')[0])

## calculate current at pickup
#pickupCurrent = startCurrent + (count - countNow) * stepCurrent

##if pickupCurrent > endCurrent:
#    print("Test finished. No Start")
#    sys.exit()
#else:
#    print(f"Test finished. Starting current is: {pickupCurrent} amps")

engineApp.DevUnlock(devID)

