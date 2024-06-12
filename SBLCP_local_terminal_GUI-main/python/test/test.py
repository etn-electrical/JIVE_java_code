import sys
import time
import pyvisa

rm = pyvisa.ResourceManager()
print(rm.list_resources())
meter = rm.open_resource(rm.list_resources()[0], timeout = 2000)
meter.read_termination = '\3'
meter.write_termination = '\3'

print(meter.query("*IDN?"))

headers = ['URMS', 'IRMS', 'P', 'S', 'Q', 'LAMBDA', 'PHI', 'FU', 'WH', 'WHP', 'WHM', 'WS', 'WQ']

meter.write(":NUMERIC:NORMAL:NUMBER 26")

# meter.write(":INTEGrate:STARt")
meter.write(":INTEGrate:STOP")
