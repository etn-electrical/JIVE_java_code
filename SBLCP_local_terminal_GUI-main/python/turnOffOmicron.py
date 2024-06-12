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
    print("[ERROR] Couldn't get ID for device")
    sys.exit()

engineApp.DevLock(devID)

engineApp.Exec(devID, "out:off")
engineApp.DevUnlock(devID)