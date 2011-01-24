import pymongo
from pymongo import Connection
connection = Connection()
db = connection.time_place
eventCollection = db.events
pageCollection = db.pages

for event in eventCollection.find().sort("year"):
    for link in event['links']:
        p = pageCollection.find_one({"_id" : link})
        if p:
            print '{"year" : %s, "title" : "%s", "latitude": %s, "longitude": %s, "month": 0, "day": 0, "articleLength" : %s}' % (event["year"], p["_id"].encode('utf-8'), p["latitude"], p["longitude"], p["article_length"])


