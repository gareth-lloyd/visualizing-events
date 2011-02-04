import sys
from pymongo import ASCENDING, DESCENDING
from events_processor import DataSource
d = DataSource()

JSON_FMT = '{"title" : "%s", "latitude": %s, "longitude": %s, "year" : %s, "month": %s, "day": %s, "eventText": "%s"}'
CSV_FMT = '"%s","%s %s","%s","%s","%s","%s"'
fmt = CSV_FMT

# apply a bounding box to lats and longs
MIN_LAT = -90
MAX_LAT = 90
MIN_LONG = -180
MAX_LONG = 180

# drop and recreate the reference counts
refCountCollection = d.db.ref_counts
if refCountCollection.count() == 0:
    print "ERROR: collection ref_counts has not been populated"
    sys.exit()

if fmt == CSV_FMT:
    print '"title","location","year","month","day","event"'

# output location-linked events. Prefer less-linked pages
for event in d.eventCollection.find().sort([("year", ASCENDING), ("month", ASCENDING), ("day", ASCENDING)]):
    pageToLink = None
    leastRefs = 1000000
    for link in event['links']:
        p = d.pageCollection.find_one({"_id" : link})
        if p:
            ref = refCountCollection.find_one({"_id" : p["_id"]})
            if ref["refs"] < leastRefs:
                pageToLink = p
    if pageToLink:
        eventText = event["event_text"].replace('"', '').encode('utf-8')
        title = pageToLink["_id"].replace(',', '').encode('utf-8')
        coord = pageToLink["coords"][0]
        latitude, longitude = coord["latitude"], coord["longitude"]
        month = event["month"] if event["month"] else 0
        day = event["day"] if event["day"] else 0
        if (MIN_LAT <= latitude <= MAX_LAT) and (MIN_LONG <= longitude <= MAX_LONG):
            print fmt % (title, latitude, longitude, event["year"], month, day, eventText)
