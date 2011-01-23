import pymongo
from pymongo import Connection
connection = Connection()
db = connection.time_place
eventCollection = db.events
pageCollection = db.pages

print "Num events: %d" % eventCollection.count()
print "Num pages: %d" % pageCollection.count()