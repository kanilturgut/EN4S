from pymongo import MongoClient

# configuration
HOST = "0.0.0.0"
PORT = 5000
DEBUG = True
SECRET = "$61$h6adL9$$.1dEc3hGy23JOD8LscGTt."
CLIENT = MongoClient('localhost', 27017)
db = CLIENT.en4s
