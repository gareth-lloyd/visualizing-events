import pymongo
from pymongo import Connection
connection = Connection()
db = connection.time_place

#db.drop_collection("events")
#db.drop_collection("pages")
