from events_processor import DataSource
d = DataSource()

JSON_FMT = '{"year" : %s, "title" : "%s", "latitude": %s, "longitude": %s, "month": %d, "day": %d, "eventText": %s}'
CSV_FMT = '"%s","%s %s","%s","%s","%s","%s"'
fmt = JSON_FMT

MIN_LAT = -90
MAX_LAT = 90
MIN_LONG = -180
MAX_LONG = 180

# drop and recreate the reference counts
d.db.drop_collection("ref_counts")
refCountCollection = d.db.ref_counts

# count references to pages
for event in eventCollection.find().sort([("year", pymongo.ASCENDING), ("month", pymongo.ASCENDING), ("day", pymongo.ASCENDING)]):
    for link in event['links']:
        p = pageCollection.find_one({"_id" : link})
        if p:
            ref = refCountCollection.find_one({"_id" : p["_id"]})
            if not ref:
                refCountCollection.insert({"_id" : p["_id"], "refs": 1})
            else:
                refCountCollection.update({'_id': p["_id"]}, {"$inc" : { "refs": 1 }})

# output location-linked events. Prefer less-linked pages
for event in eventCollection.find().sort([("year", pymongo.ASCENDING), ("month", pymongo.ASCENDING), ("day", pymongo.ASCENDING)]):
    pageToLink = None
    leastRefs = 1000000
    for link in event['links']:
        p = pageCollection.find_one({"_id" : link})
        if p:
            ref = refCountCollection.find_one({"_id" : p["_id"]})
            if ref["refs"] < leastRefs:
                pageToLink = p
    if pageToLink:
        eventText = event["event_text"].replace(',', '').encode('utf-8')
        title = pageToLink["_id"].replace(',', '').encode('utf-8')
        coord = pageToLink["coords"][0]
        print fmt % (title, coord["latitude"], coord["longitude"], event["year"], event["month"], event["day"], eventText)