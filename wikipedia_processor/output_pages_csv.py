import sys
import urllib
from pymongo import ASCENDING, DESCENDING
from events_processor import DataSource
d = DataSource()

JSON_FMT = '{"title" : "%s", "latitude": %s, "longitude": %s}'
CSV_FMT = '"%s","%s %s"'
fmt = CSV_FMT

# output location-linked events. Prefer less-linked pages
for page in d.pageCollection.find().sort("_id", ASCENDING):
    title = "http://en.wikipedia.org/wiki/%s" % urllib.quote(page['_id'].encode('utf_8'))
    print fmt % (title, page['coords'][0]['latitude'], page['coords'][0]['longitude'])
