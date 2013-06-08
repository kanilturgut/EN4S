import os
import json
import base64
import geopy
import geopy.distance
from datetime import datetime
from functools import wraps

from flask import Flask, request, session
from flask import abort, render_template
from flask.ext import restful

from settings import db
import settings
import pymongo
from bson import ObjectId

app = Flask(__name__)
api = restful.Api(app)


def basic_authentication():
    return session.get('logged_in')


def authenticate(func):
    @wraps(func)
    def wrapper(*args, **kwargs):
        if not getattr(func, 'authenticated', True):
            return func(*args, **kwargs)

        acct = basic_authentication()
        if acct:
            return func(*args, **kwargs)

        restful.abort(401)
    return wrapper


@app.route('/apidocs')
def Docs():
    return render_template('users.html')


@app.route('/apidocs/users')
def DocsUsers():
    return render_template('users.html')


@app.route('/apidocs/complaints')
def DocsComplaints():
    return render_template('complaints.html')


class Login(restful.Resource):
    def post(self):
        data_dict = json.loads(request.data)
        user = db.users.find_one(
            {"username": str(data_dict['username'])}
        )

        if not user:
            return {'error': 'user not found'}, 404
        if data_dict['password'] != str(data_dict["password"]):
            return {'error': 'password is invalid'}, 404
        else:
            session['user'] = user
            session['logged_in'] = True
            print "SESSION LOGIN logged_in value: " + \
                str(session.get('logged_in'))
            return {'success': 'logged in'}, 200


class Register(restful.Resource):
    def post(self):
        data_dict = json.loads(request.data)

        username = str(data_dict['username'])
        password = str(data_dict['password'])

        if not (username or password):
            return {'error': 'username or password not given'}, 404
        else:
            try:
                db.users.insert(
                    {"username": username, "password": password}
                )
                return {'success': "registered successfuly"}, 201
            except:
                return {'error': "can't register"}, 404


class Home(restful.Resource):
    method_decorators = [authenticate]

    def get(self):
        if not session.get('logged_in'):
            return {'error': 'authentication failed'}
        else:
            return {'success': 'auth ok'}


class ComplaintRecent(restful.Resource):
    def get(self):
        l = []
        category = request.args.get('category', '')

        if category is "":
            category = "all"

        if category is not 'all':
            items = db.complaint.find({"category": category})
            items = items.sort("date", pymongo.DESCENDING)
        else:
            items = db.complaint.find().sort("date", pymongo.DESCENDING)

        for item in items:
            item["_id"] = str(item["_id"])
            item["date"] = str(item["date"])
            l.append(item)

        return (l, 200, {"Cache-Control": "no-cache"})


class ComplaintTop(restful.Resource):
    def get(self):
        l = []
        category = request.args.get('category', '')

        if category is "":
            category = "all"

        if category is not 'all':
            items = db.complaint.find({"category": category})
            items = items.sort("upvote_count", pymongo.DESCENDING)
        else:
            items = db.complaint.find().sort("upvote_count",
                                             pymongo.DESCENDING)

        for item in items:
            item["_id"] = str(item["_id"])
            item["date"] = str(item["date"])
            l.append(item)

        return (l, 200, {"Cache-Control": "no-cache"})


class ComplaintNear(restful.Resource):
    def get(self):
        l = []

        lati = request.args.get('latitude', '')
        longi = request.args.get('longitude', '')
        category = request.args.get('category', '')
        print "latitude: " + str(lati)
        print "longitude: " + str(longi)

        if category is "":
            category = "all"

        loc = [float(lati), float(longi)]
        if category is not 'all':
            items = db.complaint.find({"category": category,
                                       "location": {"$near": loc}})
        else:
            items = db.complaint.find({"location": {"$near": loc}})

        for item in items:
            item["_id"] = str(item["_id"])
            item["date"] = str(item["date"])
            l.append(item)

        return (l, 200, {"Cache-Control": "no-cache"})


class Complaint(restful.Resource):
    # todo new_complaint olusturmaya gerek yok
    # direk request.data'ya olmayan verileri ekle
    # ve database'e ekle

    method_decorators = [authenticate]

    def get(self):
        l = []
        for item in db.complaint.find():
            item["_id"] = str(item["_id"])
            item["date"] = str(item["date"])
            l.append(item)

        return (l, 200, {"Cache-Control": "no-cache"})

    def post(self):
        if not session.get("logged_in"):
            return {'error': 'you need to be logged in'}, 404
        else:
            user = session["user"]
            data_dict = json.loads(request.data)

            username = str(user["username"])
            category = str(data_dict['category'])
            location = data_dict['location']
            address = str(data_dict['address'])
            city = str(data_dict['city'])
            title = data_dict['title']

            new_complaint = {
                "title": title,
                "user": username,
                "pics": [],
                "category": category,
                "upvoters": [user["username"]],
                "upvote_count": 1,
                "downvote_count": 0,
                "location": location,
                "address": address,
                "city": city,
                "date": datetime.now()
            }

            db.complaint.insert(new_complaint)
            new_complaint["_id"] = str(new_complaint["_id"])
            new_complaint["date"] = str(new_complaint["date"])

            return new_complaint, 201


class ComplaintPicturePut(restful.Resource):
    # todo base64 olarak degil
    # file objesi olarak almak daha verimli olacak.

    def put(self, obj_id):
        if not session.get("logged_in"):
            return {'error': 'you need to be logged in'}, 404
        else:
            data_dict = json.loads(request.data)
            obj_id = ObjectId(str(obj_id))
            obj = db.complaint.find_one({"_id": obj_id})
            if not obj:
                return abort(404)
            city = obj["city"]

            arr = data_dict["pic"]  # base 64 encoded
            h = data_dict["hash"]  # hash of the pic

            filename = byte_array_to_file(arr, city, h)

            db.complaint.update(
                {"_id": obj_id}, {"$addToSet": {"pics": filename}}
            )

            return {'success': 'update is complete'}


class ComplaintUpvote(restful.Resource):
    method_decorators = [authenticate]

    def put(self, obj_id):
        data_dict = json.loads(request.data)
        obj_id = ObjectId(str(obj_id))
        obj = db.complaint.find_one({"_id": obj_id})
        if not obj:
            return abort(404)

        upvoters = obj["upvoters"]
        if session["user"]["username"] in upvoters:
            return {"error": "user already upvoted"}, 406

        comp_lati = obj["location"][0]
        comp_longi = obj["location"][1]

        user_lati = data_dict["location"][0]
        user_longi = data_dict["location"][1]

        pt_comp = geopy.Point(comp_lati, comp_longi)
        pt_user = geopy.Point(user_lati, user_longi)

        distance = geopy.distance.distance(pt_comp, pt_user).km
        distance = float(distance)

        if distance > 1:
            return {"error": "user is not close"}, 406
        else:
            db.complaint.update(
                {"_id": obj_id},
                {"$addToSet": {"upvoters": session["user"]["username"]}}
            )
            db.complaint.update(
                {"_id": obj_id}, {"$inc": {"upvote_count": 1}}
            )
            return {"success": "upvote accepted"}, 202


class ComplaintSingle(restful.Resource):
    # implement this
    def get(self, obj_id):
        obj_id = ObjectId(str(obj_id))
        obj = db.complaint.find_one({"_id": obj_id})
        if not obj:
            return abort(404)
        return obj


def byte_array_to_file(array, city, h):
    IMAGEFOLDER = "/srv/flask/en4s/uploads/pics/"
    URL = "/pics/"

    try:
        os.makedirs(IMAGEFOLDER + city + "/")
    except:
        pass

    # new_filename = IMAGEFOLDER + city + "/" +\
    #            hashlib.sha256(str(array)).hexdigest() + ".jpg"

    new_filename = IMAGEFOLDER + city + "/" + h + ".jpg"
    new_url = URL + city + "/" + h + ".jpg"

    array = base64.b64decode(array)

    file = open(new_filename, "wb")
    file.write(array)
    file.close()
    return new_url


api.add_resource(Home, '/')
api.add_resource(Login, '/login')
api.add_resource(Register, '/register')
api.add_resource(Complaint, '/complaint')
api.add_resource(ComplaintSingle, '/complaint/<string:obj_id>')
api.add_resource(ComplaintUpvote, '/complaint/<string:obj_id>/upvote')
api.add_resource(ComplaintRecent, '/complaint/recent')
api.add_resource(ComplaintTop, '/complaint/top')
api.add_resource(ComplaintNear, '/complaint/near')
api.add_resource(ComplaintPicturePut, '/upload/<string:obj_id>')


if __name__ == '__main__':
    app.debug = settings.DEBUG
    app.secret_key = settings.SECRET
    app.run(host=settings.HOST, port=settings.PORT)
