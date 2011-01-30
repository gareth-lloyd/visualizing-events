import pymongo
from pymongo import Connection
connection = Connection()
db = connection.time_place
eventCollection = db.events
eventCollection.ensure_index([("year", pymongo.ASCENDING), ("month", pymongo.ASCENDING), ("day", pymongo.ASCENDING)])
pageCollection = db.pages

print ','.join(["title", "latitude", "longitude", "year", "month", "day", "event"])
for event in eventCollection.find().sort([("year", pymongo.ASCENDING), ("month", pymongo.ASCENDING), ("day", pymongo.ASCENDING)]):
    if event['month'] is None or event['day'] is None:
        continue
    for link in event['links']:
        p = pageCollection.find_one({"_id" : link})
        if p:
            eventText = event["event_text"].replace(',', '').encode('utf-8')
            title = p["_id"].replace(',', '').encode('utf-8')
            print '%s,%s %s,%s,%s,%s,%s' % (title, p["latitude"], p["longitude"], event["year"], event["month"], event["day"], eventText)
