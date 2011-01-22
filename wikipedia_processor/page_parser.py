#!/usr/bin/env python

import sys
from xml.sax import handler, make_parser
from xml.sax.handler import feature_namespaces
from xml.sax.saxutils import escape, XMLFilterBase
from unicodedata import normalize
import re

YEARS_PROCESSED = 0
COORD_PAGES = 0
EVENTS_SAVED = 0


class Coords(object):
    def __init__(self, lat=None, long=None):
        self.lat = lat
        self.long = long

class WikipediaPage(object):
    isYearPattern = re.compile(r"^\d{1,4}( BC)?$")
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
        return False

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
        self.month = None
        self.day = None
        parts = self.linkPattern.findall(line)
        if parts:
            self.year = year
            self.successful = True
            if self.tryToParseDate(parts[0]) and len(parts) > 1:
                self.links = parts[1:]
        else:
            self.successful = False

    def tryToParseDate(self, text):
        segments = text.split()
        self.month = self.parseMonth(segments[0])
        if segments[1]:
            try:
                self.day = int(segments[1])
            except:
                pass
    def parseMonth(self, text):
        

def saveEvent(event):
    global EVENTS_SAVED
    EVENTS_SAVED += 1
    return

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
    for line in eventsOnwards.split():
        if line.startswith('='):
            if (line.find("Births") != -1 or line.find("Deaths") != -1):
                print line
                break
        if line.startswith('*'):
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

def savePage(page):
    """
    Take a Wikipedia page and add it to the
    Pages collection
    """
    global COORD_PAGES
    COORD_PAGES += 1

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