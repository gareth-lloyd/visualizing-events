#!/usr/bin/env python

import sys
from xml.sax import handler, make_parser
from xml.sax.handler import feature_namespaces
from xml.sax.saxutils import escape, XMLFilterBase
from unicodedata import normalize

class Coords(object):
    def __init__(self, lat=None, long=None):
        self.lat = lat
        self.long = long

class WikipediaPage(object):
    def __init__(self):
        self.title = u''
        self.id = u''
        self.text = u''
        self.coords = []

    def containsCoords(self):
        return False

    def isYear(self):
        return False

    def __str__(self):
        return 'ID %s TITLE %s' % (self.id.encode('utf_8'), self.title.encode('utf_8'))

    def __unicode(self):
        return 'ID %s TITLE %s' % (self.id, self.title)

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

def processYear(text):
    pass

def processPage(text):
    pass

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
            self.currentPage.text += content

    def analysePage(self):
        if (containsCoords(self.currentPage.text)):
            processPage(self.currentPage)
        if (isYear(self.currentPage.text)):
            processYear(self.currentPage)

if __name__ == '__main__':
    parser = make_parser()
    # Tell the parser we are not interested in XML namespaces
    parser.setFeature(feature_namespaces, 0)

    wh = WikipediaHandler()

    # apply the text_normalize_filter
    filter_handler = text_normalize_filter(parser, wh)
    filter_handler.parse(open(sys.argv[1]))
