from events_processor import DataSource

d = DataSource()

print "BEFORE:"
print "pages: ", d.pageCollection.count()
print "events: ", d.eventCollection.count()
print "invalid coordinates: ", d.invalidCoordCollection.count()

#d.db.drop_collection("events")
#d.db.drop_collection("invalid_coords")
#d.db.drop_collection("pages")

print
print "AFTER:"
print "pages: ", d.pageCollection.count()
print "events: ", d.eventCollection.count()
print "invalid coordinates: ", d.invalidCoordCollection.count()
