import sys
import pyvisa

rm = pyvisa.ResourceManager()
print(rm.list_resources())
meter = rm.open_resource(rm.list_resources()[0], timeout = 2000)
meter.read_termination = '\3'
meter.write_termination = '\3'

hrInput = int(sys.argv[1])
minInput = int(sys.argv[2])
secInput = int(sys.argv[3])

meter.write(":INTEGrate:TIMer{} {},{},{}".format(1, hrInput, minInput, secInput))