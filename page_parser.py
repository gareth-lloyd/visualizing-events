#!/usr/bin/env python

import sys
from xml.sax import handler, make_parser
from xml.sax.saxutils import escape

class WikipediaPage(object):
    def __init__(self):
        self.title = ''
        self.id = ''
        self.text = ''
        self.coords = ''

    def __str__(self):
        return 'ID %s TITLE %s' % (self.id, self.title)

class PageParser(handler.ContentHandler):
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
                print self.currentPage
            elif (name == 'revision'):
                # we've finished the revision section
                self.ignoreIdTags = False
            self.currentTag = ''

        def characters(self, content):
            if (self.currentTag == 'id' and not self.ignoreIdTags):
                self.currentPage.id += content
            elif (self.currentTag == 'title'):
                self.currentPage.title += content
            elif (self.currentTag == 'text'):
                self.currentPage.text += content

if __name__ == '__main__':
    parser = make_parser()
    parser.setContentHandler(PageParser())
    parser.parse(sys.argv[1])
