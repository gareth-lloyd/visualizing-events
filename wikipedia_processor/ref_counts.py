from pymongo import ASCENDING, DESCENDING
from events_processor import DataSource
d = DataSource()

# drop and recreate the reference counts
#d.db.drop_collection("ref_counts")
refCountCollection = d.db.ref_counts

# count references to pages
#for event in d.eventCollection.find():
#    for link in event['links']:
#        p = d.pageCollection.find_one({"_id" : link})
#        if p:
#            ref = refCountCollection.find_one({"_id" : p["_id"]})
#            if not ref:
#                refCountCollection.insert({"_id" : p["_id"], "refs": 1})
#            else:
#                refCountCollection.update({'_id': p["_id"]}, {"$inc" : { "refs": 1 }})

for ref in refCountCollection.find().sort("refs", DESCENDING):
    print ref["refs"], ref["_id"]
