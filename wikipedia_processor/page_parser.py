#!/usr/bin/env python

import sys
from xml.sax import handler, make_parser
from xml.sax.handler import feature_namespaces
from xml.sax.saxutils import escape, XMLFilterBase
from unicodedata import normalize
import pymongo
from pymongo import Connection
import re

## globals
YEARS_PROCESSED = 0
COORD_PAGES = 0
EVENTS_SAVED = 0
COORD_ERRORS = 0

connection = Connection()
db = connection.time_place
eventCollection = db.events
eventCollection.ensure_index("year", direction=pymongo.ASCENDING)
pageCollection = db.pages


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
    def __init__(self):
        self.state = Coords.PROCESSING_LAT
        self.granularity = Coords.DEG
        self.lat = 0.0
        self.long = 0.0
        self.addedData = False

    def addPiece(self, piece):
        if (self.state == Coords.DONE):
            return
        if (piece.find('.') != -1):
            # we're dealing with a floating point measurement
            if (self.state == Coords.PROCESSING_LAT):
                self.lat = float(piece)
                self.addedData = True
                self.state = Coords.PROCESSING_LONG
            elif (self.state == Coords.PROCESSING_LONG):
                self.long = float(piece)
                self.state = Coords.DONE
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
                self.addedData = True
                self.lat += self.processPieceAtCorrectGranularity(piece)
            elif (self.state == Coords.PROCESSING_LONG):
                self.long += self.processPieceAtCorrectGranularity(piece)

    def hasData(self):
        return self.addedData

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
            return intPiece * 0.00166666

    def __str__(self):
        return "%f|%f" % (self.lat, self.long)
    def __unicode__(self):
        return "%f|%f" % (self.lat, self.long)

class WikipediaPage(object):
    """
    holds data and methods related to one <page> element
    parsed from the dump
    """
    isYearPattern = re.compile(r"^\d{1,4}( BC)?$")
    coordsPattern = re.compile(r"\{\{[Cc]oord\|(.*?)\}\}")

    def __init__(self):
        self.title = u''
        self.id = u''
        self.text = u''
        self.coords = []

    def processForCoords(self):
        """
        Try to skip early if it's not relevant (e.g. it's a redirect)
        otherwise detect all Coordinates and return True if some found
        """
        coordStrings = self.coordsPattern.findall(self.text)
        if coordStrings:
            for s in coordStrings:
                coord = self.coordFromStr(s)
                if (coord and coord.hasData()):
                    self.coords.append(coord)
                else:
                    print "invalid coord %s" % s
        if (self.coords):
            return True
        return False

    def coordFromStr(self, coordStr):
        c = Coords()
        for piece in coordStr.split('|'):
            try:
                c.addPiece(piece)
            except:
                global COORD_ERRORS
                COORD_ERRORS += 1
                print "COORD ERROR: str: %s piece: %s" % (coordStr, piece)
        return c

    def isYear(self):
        """
        Is this a year page?
        """
        return self.isYearPattern.match(self.title)

    def __str__(self):
        return 'ID %s TITLE %s' % (self.id.encode('utf_8'), self.title.encode('utf_8'))

    def __unicode__(self):
        return 'ID %s TITLE %s' % (self.id, self.title)

class Event(object):
    linkPattern = re.compile(r"\[\[(.*?)\]\]")
    """
    A single event line from a year page
    """
    def __init__(self, line, year):
        """
        Extract links from the line
        """
        self.year = year
        self.eventText = line
        self.links = self.linkPattern.findall(line)
        if self.links:
            self.isValidEvent = True
        else:
            self.isValidEvent = False

def processAndSaveEvents(page):
    """
    Take a wikipedia representing a year and extract
    the events recorded there. Save each to the datastore.
    """
    global YEARS_PROCESSED
    YEARS_PROCESSED += 1
    year = titleToYear(page.title)
    if not year:
        return

    # Grab just the events section onwards
    startIndex = page.text.find('Events')
    if (startIndex == -1):
        return
    eventsOnwards = page.text[startIndex:]

    # process from here line by line
    for line in eventsOnwards.splitlines():
        if line.startswith('='):
            if (line.find("Births") != -1 or line.find("Deaths") != -1):
                break
        elif line.startswith('*'):
            saveEvent(Event(line, year))

def titleToYear(title):
    try:
        return int(title)
    except:
        try:
            # year is BC
            endIndex = title.find(' ')
            return -1 * int(title[0:endIndex])
        except:
            return False



class text_normalize_filter(XMLFilterBase):
    """
    SAX filter to ensure that contiguous texts nodes are merged into one
    That hopefully speeds up the parsing process a lot, specially when reading
    revisions with long text
    Receip by Uche Ogbuji, James Kew and Peter Cogolo
    Retrieved from "Python Cookbook, 2nd ed., by Alex Martelli, Anna Martelli
    Ravenscroft, and David Ascher (O'Reillly Media, 2005) 0-596-00797-3"
    """

    def __init__(self, upstream, downstream):
        XMLFilterBase.__init__(self, upstream)
        self._downstream=downstream
        self._accumulator=[]
    def _complete_text_node(self):
        if self._accumulator:
            self._downstream.characters(''.join(self._accumulator))
            self._accumulator=[]
    def characters(self, text):
        self._accumulator.append(text)
    def ignorableWhiteSpace(self, ws):
        self._accumulator.append(text)
def _wrap_complete(method_name):
    def method(self, *a, **k):
        self._complete_text_node()
        getattr(self._downstream, method_name)(*a, **k)
    method.__name__= method_name
    setattr(text_normalize_filter, method_name, method)
for n in '''startElement endElement endDocument'''.split():
    _wrap_complete(n)

class WikipediaHandler(handler.ContentHandler):
    def __init__(self, out=sys.stdout):
        handler.ContentHandler.__init__(self)
        self._out = out
        self.currentPage = None
        self.currentTag = ''
        self.ignoreIdTags = False

    def startElement(self, name, attrs):
        self.currentTag = name
        if (name == 'page'):
            # add a page
            self.currentPage = WikipediaPage()
        elif (name == 'revision'):
            # when we're in revision, ignore ids
            self.ignoreIdTags = True

    def endElement(self, name):
        if (name == 'page'):
            self.analysePage()
        elif (name == 'revision'):
            # we've finished the revision section
            self.ignoreIdTags = False
        self.currentTag = ''

    def characters(self, content):
        if (self.currentTag == 'id' and not self.ignoreIdTags):
            self.currentPage.id = content
        elif (self.currentTag == 'title'):
            self.currentPage.title = content
        elif self.currentTag == 'text':
            self.currentPage.text = content

    def analysePage(self):
        if (self.currentPage.isYear()):
            processAndSaveEvents(self.currentPage)
        elif (self.currentPage.processForCoords()):
            savePage(self.currentPage)

def savePage(page):
    """
    Take a Wikipedia page and add it to the
    Pages collection
    """
    global COORD_PAGES
    COORD_PAGES += 1

    coords = page.coords[0]
    pageDict = {
        "latitude": coords.lat,
        "longitude": coords.long,
        "article_length": len(page.text),
        "_id": page.title
    }
    global pageCollection
    pageCollection.insert(pageDict)

def saveEvent(event):
    if not event.isValidEvent:
        return
    global EVENTS_SAVED
    EVENTS_SAVED += 1
    global eventCollection
    eventDict = {
        "year": event.year,
        "links": event.links
    }
    eventCollection.insert(eventDict)

if __name__ == '__main__':
    parser = make_parser()
    # Tell the parser we are not interested in XML namespaces
    parser.setFeature(feature_namespaces, 0)

    wh = WikipediaHandler()

    # apply the text_normalize_filter
    filter_handler = text_normalize_filter(parser, wh)
    filter_handler.parse(open(sys.argv[1]))

    print "coord pages saved: %d" % COORD_PAGES
    print "year pages processed: %d" % YEARS_PROCESSED
    print "events saved: %d" % EVENTS_SAVED
    print "coord errors: %d" % COORD_ERRORS