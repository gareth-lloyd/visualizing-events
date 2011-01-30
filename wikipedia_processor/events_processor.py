"""
Works with page_parser to save two kinds of data to a mongo datastore:
descriptions of events, and links to pages with coordinates
"""
import sys
import pymongo
import re
import page_parser

YEARS_PROCESSED = 0
isYearPattern = re.compile(r"^\d{1,4}( BC)?$")
coordsPattern = re.compile(r"\{\{[Cc]oord\|(.*?)\}\}")

dataSource = None

class Coords(object):
    """
    class to parse and hold coord data
    """
    delim = re.compile(r"\s*\|\s*")
    valid = re.compile(r"^[0-9\.\-+NSEW]+$")
    granularities = [1, 0.0166666, 0.000166666]
    points = {"N": 1, "S": -1, "E": 1, "W": -1}

    def __init__(self, coordStr):
        coordStr = coordStr.strip()
        pieces = [x for x in Coords.delim.split(coordStr) if Coords.valid.match(x)]
        if not pieces:
            raise ValueError()

        numPieces = len(pieces)
        if numPieces % 2 != 0:
            pieces = self._padPieces(pieces, numPieces)
            numPieces = len(pieces)

        gran, self.lat = 0, 0
        for piece in pieces[:(numPieces / 2)]:
            self.lat = self._process(self.lat, piece, gran)
            gran += 1

        gran, self.long = 0, 0
        for i, piece in enumerate(pieces[(numPieces/2):]):
            self.long = self._process(self.long, piece, i)

    def _process(self, input, piece, gran):
        if Coords.points.has_key(piece):
            input *= Coords.points[piece]
        elif piece.find('.') != -1:
            input += float(piece) * Coords.granularities[gran]
        else:
            input += int(piece) * Coords.granularities[gran]
        return input

    def _padPieces(self, pieces, numPieces):
        divider = 0
        for piece in pieces:
            if not Coords.points.has_key(piece):
                divider += 1
            else:
                break
        if divider == numPieces:
            raise ValueError()
        latPieces = pieces[:divider + 1]
        longPieces = pieces[divider + 1:]
        while len(latPieces) < 4:
            latPieces.insert(-1, "0")
        while len(longPieces) < 4:
            longPieces.insert(-1, "0")
        latPieces.extend(longPieces)
        return latPieces

    def __str__(self):
        return "%f|%f" % (self.lat, self.long)
    def __unicode__(self):
        return "%f|%f" % (self.lat, self.long)

class Event(object):
    linkDelimiters = re.compile(r"(\[\[|\]\]|\||<ref|/ref>|<!--|-->)")
    months = {
        "january" : 1, "jan" : 1, "January" : 1,
        "february" : 2, "Feb" : 2, "feb" : 2, "February" : 2,
        "march" : 3, "Mar" : 3, "mar" : 3, "March" : 3,
        "april" : 4, "Apr" : 4, "apr" : 4, "April" : 4,
        "may" : 5, "May" : 5,
        "june" : 6, "jun" : 6, "Jun" : 6, "June" : 6,
        "july" : 7, "jul" : 7, "jul" : 7, "July" : 7,
        "august" : 8, "Aug" : 8, "aug" : 8, "August" : 8,
        "september" : 9, "Sep" : 9, "Sept" : 9, "sep" : 9, "sept": 9, "September" : 9,
        "october" : 10, "Oct" : 10, "oct" : 10, "October" : 10,
        "november" : 11, "Nov" : 11, "nov" : 11, "November" : 11,
        "december" : 12, "Dec" : 12, "dec" : 12, "December" : 12,
    }
    # states for link processing
    TEXT, ALT_TEXT, LINK, IGNORE = (0, 1, 2, 3)

    # stored date for events in nested bullet form
    storedMonth = None
    storedDay = None

    def __init__(self, eventText, year, month=None, day=None):
        self.year, self.month, self.day = year, month, day
        self.links = []
        eventTextPieces = []
        pieces = Event.linkDelimiters.split(eventText)
        # this is a little state machine creating a clean sentence plus links
        state = Event.TEXT
        for piece in pieces:
            if piece == '[[':
                state = Event.LINK
            elif piece == ']]' or piece == "/ref>" or piece == "-->":
                state = Event.TEXT
            elif piece == '|':
                if state == Event.LINK:
                    state = Event.ALT_TEXT
            elif piece == "<ref" or piece == "<!--":
                state = Event.IGNORE
            elif state == Event.LINK:
                self.links.append(piece)
                eventTextPieces.append(piece)
            elif state == Event.TEXT:
                if not piece.startswith('*'):
                    eventTextPieces.append(piece)
                else:
                    eventTextPieces.append(piece.lstrip('* '))
            elif state == Event.ALT_TEXT:
                eventTextPieces.pop()
                eventTextPieces.append(piece)

        self.eventText = ''.join(eventTextPieces)
        if self.links and self.month is None:
            parts = self.links[0].split()
            if Event.months.has_key(parts[0]):
                self.month = Event.months[parts[0]]
                if len(parts) == 2:
                    try:
                        self.day = int(parts[1])
                    except ValueError:
                        pass
    def __str__(self):
        return "%d %s" % (self.year, self.eventText.encode("utf_8"))
    def __unicode__(self):
        return "%d %s" % (self.year, self.eventText)

class DataSource(object):
    INVALID_COORD_PAGES = 0
    COORD_PAGES = 0
    EVENTS_SAVED = 0

    def __init__(self, mock=False):
        self.mock = mock
        if not mock:
            connection = pymongo.Connection()
            db = connection.time_place
            self.eventCollection = db.events
            self.eventCollection.ensure_index("year", direction=pymongo.ASCENDING)
            self.pageCollection = db.pages
            self.invalidCoordCollection = db.invalid_coords

    def saveInvalidCoordPage(self, page, coordStr):
        DataSource.INVALID_COORD_PAGES += 1
        if self.mock:
            print page
        else:
            self.invalidCoordCollection.save({
                "_id" : page.title,
                "coord_string" : s
            })

    def savePage(self, page):
        DataSource.COORD_PAGES += 1
        if self.mock:
            print page
        else:
            coords = []
            for coord in page.coords:
                coords.append({"latitude": coord.lat, "longitude": coord.long})
            self.pageCollection.save({
                "coords": coords,
                "article_length": len(page.text),
                "_id": page.title
            })

    def saveEvent(self, event):
        DataSource.EVENTS_SAVED += 1
        if self.mock:
            print event
            return
        else:
            self.eventCollection.save({
                "year": event.year,
                "links": event.links,
                "month" : event.month,
                "day" : event.day,
                "event_text" : event.eventText
            })

def processYear(page):
    """
    Take a WikiPage representing a year and try to parse out individual events
    recorded there. Add these to a list on the page object.
    """
    errors = 0
    page.events = []
    try:
        year = int(page.title)
    except ValueError:
        # year may be "X BC"
        try:
            endIndex = page.title.find(' ')
            year = -int(page.title[0:endIndex])
        except:
            return False

    # Grab just the events section onwards
    startIndex = page.text.find('=Events')
    startIndex = startIndex if startIndex != -1 else 0
    lastEvent = None
    for line in page.text[startIndex:].splitlines():
        if len(line) < 4:
            pass    # skip empty lines
        elif line.startswith('='):
            if (line.find("Births") != -1 or line.find("Deaths") != -1):
                break
        elif line.startswith('**') and lastEvent is not None:
            newEvent = Event(line, year, lastEvent.month, lastEvent.day)
            page.events.append(newEvent)
        elif line.startswith('*'):
            try:
                lastEvent = Event(line, year)
                page.events.append(lastEvent)
            except ValueError:
                pass

def processPageForCoords(page):
    page.coords = []
    for s in coordsPattern.findall(page.text):
        try:
            page.coords.append(Coords(s))
        except (ValueError):
            dataSource.saveInvalidCoordPage(page, s)

def processPage(page):
    """
    We're interested in pages representing years with event descriptions,
    and those which mention any sort of geographic coordinates.
    """
    if isYearPattern.match(page.title):
        processYear(page)
        for event in page.events:
            dataSource.saveEvent(event)
    else:
        processPageForCoords(page)
        if page.coords:
            dataSource.savePage(page)

if __name__ == "__main__":
    dataSource = DataSource(mock=True)
    page_parser.parseWithCallback(sys.argv[1], processPage)
    print
    print "Done parsing: ", sys.argv[1]
    outputs = (YEARS_PROCESSED, DataSource.EVENTS_SAVED, DataSource.COORD_PAGES, DataSource.INVALID_COORD_PAGES)
    print "Years: %d; Events: %d; Pages with coords: %d; Invalid coords: %d" % outputs