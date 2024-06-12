import sys
import pyvisa

rm = pyvisa.ResourceManager()
print(rm.list_resources())
meter = rm.open_resource(rm.list_resources()[0], timeout = 2000)
meter.read_termination = '\3'
meter.write_termination = '\3'

# Arguments format: <phase, "0" for ph0 or "1" for ph1>, <item, eg "URMS">

phaseWeWantToRead = int(sys.argv[1])
itemWeWantToRead = str(sys.argv[2])


# Item options: ['URMS', 'IRMS', 'P', 'S', 'Q', 'LAMBDA', 'PHI', 'FU', 'WH', 'WHP', 'WHM', 'WS', 'WQ']

meter.write(":NUMERIC:NORMAL:NUMBER 1")
# print(meter.query(":NUMERIC:NORMAL:NUMBER?"))

meter.write(":NUMERIC:NORMAL:ITEM{} {},{}".format(1, itemWeWantToRead, phaseWeWantToRead + 1))
print("Phase {}: {} => ".format(phaseWeWantToRead, itemWeWantToRead), end = "")
print(meter.query(":NUMERIC:NORMAL:Value? {}".format(1)))