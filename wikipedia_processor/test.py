from page_parser import Coords


tests = """45.85833|-84.87083|region:US-MI_type:isle|name=St. Helena Island
45.49778|-86.77028|name=St. Martin Island (Lake Michigan)
45.79972|-86.75972|name=St. Vital Island
38.8929138|-77.0403734|type:landmark|name=OAS headquarters, Washington, D.C.|display=inline,title
"""


for line in tests.splitlines():
    c = Coords()
    for piece in line.split('|'):
        c.addPiece(piece)
    print "original: %s coord: %s" % (line, c)
