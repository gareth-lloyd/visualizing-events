import sys
import urllib
from pymongo import ASCENDING, DESCENDING
from events_processor import DataSource
d = DataSource()

JSON_FMT = '{"title" : "%s", "invalid_coord": %s}'
CSV_FMT = '"%s","%s"'
fmt = CSV_FMT


if fmt == CSV_FMT:
    print '"page","invalid string"'

# output location-linked events. Prefer less-linked pages
for page in d.invalidCoordCollection.find().sort("_id", ASCENDING):
    title = "http://en.wikipedia.org/wiki/%s" % urllib.quote(page['_id'].encode('utf_8'))
    invalid = page['coord_string'].encode('utf_8').replace('"', '')
    print fmt % (title, invalid)
