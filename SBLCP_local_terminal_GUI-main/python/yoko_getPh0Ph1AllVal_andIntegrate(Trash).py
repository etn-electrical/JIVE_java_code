##################################################################
# Author: Jonathan Tan (jonathantan@eaton.com)
##################################################################
# Very cool function, you can get yoko readings by typing "get", 
# start and stop integration by typing "startint" and "stopint", 
# reset integration by "resetint", and set timer value using 
# settimer:<hr>,<min>,<sec>.
# 
# However, Java program doesnt use this function.
##################################################################

import sys
import time
import pyvisa

LOGGING_TAG = " yoko_getPh0Ph1AllVal_andIntegrate.py: "

rm = pyvisa.ResourceManager()
print(rm.list_resources())
meter = rm.open_resource(rm.list_resources()[0], timeout = 2000)
meter.read_termination = '\3'
meter.write_termination = '\3'


##################################################################
######################### Helper Methods #########################
##################################################################
def readValues():
    count = 1
    for i in [1,2]:
        for j in headers:
            meter.write(":NUMERIC:NORMAL:ITEM{} {},{}".format(count, j, i))
            print("Phase {} : {} => ".format(i, j), end="")
            print(meter.query(":NUMERIC:NORMAL:Value? {}".format(count)), end="")
            count += 1
##################################################################
##################################################################


print(meter.query("*IDN?"))

headers = ['URMS', 'IRMS', 'P', 'S', 'Q', 'LAMBDA', 'PHI', 'FU', 'WH', 'WHP', 'WHM', 'WS', 'WQ']

meter.write(":NUMERIC:NORMAL:NUMBER 26")

TIMEOUT_DURATION = 1800 # in sec (1800s = 30min)

intRunning = False
start_time = time.time()
while time.time() - start_time < TIMEOUT_DURATION:
    try:
        input_data = input()
        if input_data == "shutdown":
            if (intRunning):
                meter.write(":INTEGrate:STOP")
            break
        elif input_data == "get":
            readValues()
        elif input_data == "startint":
            meter.write(":INTEGrate:STARt")
            intRunning = True
            print("IntegrationStarted")
        elif input_data == "stopint":
            meter.write(":INTEGrate:STOP")
            intRunning = False
            print("IntegrationStopped")
        elif input_data == "resetint":
            if (intRunning):
                print("[WARNING]" + LOGGING_TAG + "Integration already running, stop integrate before resetting!")
            else:
                meter.write(":INTEGrate:RESet")
        else:
            # Maybe set timer command (format: settimer:<hr,min,sec>)
            arr = input_data.split(":")
            if (len(arr) == 2) and (arr[0] == "settimer"):
                # It is a set timer command
                if (intRunning):
                    print("[WARNING]" + LOGGING_TAG + "Integration already running, stop integrate before changing timer values!")
                    continue
                timearr = arr[1].split(",")
                meter.write(":INTEGrate:TIMer{} {},{},{}".format(1, timearr[0], timearr[1], timearr[2]))
            else:
                # Not a set timer command
                print("[WARNING]" + LOGGING_TAG + "Command unrecognized!")

    except KeyboardInterrupt:
        break
else:
    print("[WARNING]" + LOGGING_TAG + "Timeout reached. Turning off...")


print("[INFO]" + LOGGING_TAG + "Yokogawa port closed.")