"""

"""

import sys
import pymongo
import re
import page_parser

YEARS_PROCESSED = 0
COORD_PAGES = 0
EVENTS_SAVED = 0
COORD_ERRORS = 0
MULTI_COORDS = 0

isYearPattern = re.compile(r"^\d{1,4}( BC)?$")
coordsPattern = re.compile(r"\{\{[Cc]oord\|(.*?)\}\}")

#connection = pymongo.Connection()
#db = connection.time_place
#eventCollection = db.events
#eventCollection.ensure_index("year", direction=pymongo.ASCENDING)
#pageCollection = db.pages

class Coords(object):
    PROCESSING_LAT = 0
    PROCESSING_LONG = 1
    DONE = 2
    DEG = 4
    MIN = 5
    SEC = 6
    """
    class to hold coord data and help with parsing
    by allowing
    """
    def __init__(self, coordStr):
        self.state = Coords.PROCESSING_LAT
        self.granularity = Coords.DEG
        self.lat = 0.0
        self.long = 0.0
        self.addedLat = False
        self.addedLong = False
        for piece in coordStr.split('|'):
            self.addPiece(piece)

    def addPiece(self, piece):
        if (self.state == Coords.DONE):
            return
        if (piece.find('.') != -1):
            # we're dealing with a floating point measurement
            if (self.state == Coords.PROCESSING_LAT and not self.addedLat):
                self.lat = float(piece)
                self.addedLat = True
                self.state = Coords.PROCESSING_LONG
            elif (self.state == Coords.PROCESSING_LONG and not self.addedLong):
                self.long = float(piece)
                self.addedLong = True
        elif (not piece.isdigit()):
            if (piece == 'N' or piece == 'S'):
                # get ready for latitude
                self.state = Coords.PROCESSING_LONG
                self.granularity = Coords.DEG
                if (piece == 'S'):
                    self.lat *= -1
                return
            elif (piece == 'E' or piece == 'W'):
                if (piece == 'W'):
                    self.long *= -1
                self.state = Coords.DONE
                return
        else:
            if (self.state == Coords.PROCESSING_LAT):
                self.lat += self.processPieceAtCorrectGranularity(piece)
                self.addedLat = True
            elif (self.state == Coords.PROCESSING_LONG):
                self.long += self.processPieceAtCorrectGranularity(piece)
                self.addedLong = True

    def hasData(self):
        return self.addedLat and self.addedLong

    def processPieceAtCorrectGranularity(self, piece):
        intPiece = int(piece)
        if self.granularity == Coords.DEG:
            self.granularity = Coords.MIN
            return intPiece
        elif self.granularity == Coords.MIN:
            self.granularity = Coords.SEC
            return intPiece * 0.0166666
        elif self.granularity == Coords.SEC:
            self.granularity = Coords.DEG
            return intPiece * 0.000166666

    def __str__(self):
        return "%f|%f" % (self.lat, self.long)
    def __unicode__(self):
        return "%f|%f" % (self.lat, self.long)

class Event(object):
    linkDelimiters = re.compile(r"(\[\[|\]\]|\|)")
    months = {
        "january" : 1, "Jan" : 1, "jan" : 1, "January" : 1,
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
    TEXT, ALT_TEXT, LINK = (0, 1, 2)

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
            elif piece == ']]':
                state = Event.TEXT
            elif piece == '|':
                state = Event.ALT_TEXT
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
            if Event.months.has_key(parts[0]) and len(parts) == 2:
                try:
                    self.day = int(parts[1])
                    self.month = Event.months[parts[0]]
                except ValueError:
                    pass

def savePage(page):
    global COORD_PAGES
    COORD_PAGES += 1
    global pageCollection
    pageCollection.insert({
        "latitude": page.coords[0].lat,
        "longitude": page.coords[0].long,
        "article_length": len(page.text),
        "_id": page.title
    })

def saveEvents(page):
    global EVENTS_SAVED
    for event in page.events:
        EVENTS_SAVED += 1
        global eventCollection
        eventCollection.insert({
            "year": event.year,
            "links": event.links
        })

def processYear(page):
    """
    Take a WikiPage representing a year and try to parse out individual events
    recorded there. Add these to a list on the page object.
    """
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
    startIndex = page.text.find('Events')
    startIndex = startIndex if startIndex != -1 else 0
    lastEventSaved = None
    for line in page.text[startIndex:].splitlines():
        if line.startswith('='):
            if (line.find("Births") != -1 or line.find("Deaths") != -1):
                break
        elif line.startswith('**') and lastEventSaved is not None:
            page.events.append(Event(line, year, lastEventSaved.month, lastEventSaved.day))
        else:
            try:
                lastEventSaved = Event(line, year)
                page.events.append(lastEventSaved)
            except ValueError:
                pass # not fatal

def processPageForCoords(page):
    page.coords = []
    for s in coordsPattern.findall(page.text):
        try:
            page.coords.append(Coords(s))
        except (ValueError):
            print "invalid coord %s" % s.encode('utf_8')

def processPage(page):
    """
    We're interested in pages representing years with event descriptions,
    and those which mention any sort of geographic coordinates.
    """
    if isYearPattern.match(page.title):
        processYear(page)
        if page.events:
            pass#saveEvents(page)
    else:
        processPageForCoords(page)
        if page.coords:
            pass#savePage(page)


if __name__ == "__main__":
    """
    When called as script, argv[1] is assumed to be a filename and we
    simply print pages found.
    """
    page_parser.parseWithCallback(sys.argv[1], processPage)