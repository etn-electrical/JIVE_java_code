import sys
import pyvisa

rm = pyvisa.ResourceManager()
print(rm.list_resources())
meter = rm.open_resource(rm.list_resources()[0], timeout = 2000)
meter.read_termination = '\3'
meter.write_termination = '\3'

print(meter.query("*IDN?"))

headers = ['URMS', 'IRMS', 'P', 'S', 'Q', 'LAMBDA', 'PHI', 'FU', 'WH', 'WHP', 'WHM', 'WS', 'WQ']

# print(meter.query(":STATUS?"))

meter.write(":NUMERIC:NORMAL:NUMBER 26")
# print(meter.query(":NUMERIC:NORMAL:NUMBER?"))
    
count = 1
for i in [1,2]:
    for j in headers:
        meter.write(":NUMERIC:NORMAL:ITEM{} {},{}".format(count, j, i))
        print("Phase {} : {} => ".format(i, j), end="")
        print(meter.query(":NUMERIC:NORMAL:Value? {}".format(count)), end="")
        count += 1

print("End\n")