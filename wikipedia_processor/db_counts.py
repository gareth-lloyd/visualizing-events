from events_processor import DataSource

d = DataSource()

print "counts:"
print "pages: ", d.pageCollection.count()
print "events: ", d.eventCollection.count()
print "invalid coordinates: ", d.invalidCoordCollection.count()