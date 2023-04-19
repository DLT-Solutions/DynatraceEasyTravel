To create easyTravel-mongodb-db.tar.gz:   
- Start easyTravel in mongo, run content creator and stop easyTravel   
- Remove contents of ```diagnostic.data``` and ```journal directories```
- create tar.gz with following command 
```
~/userhome/.Dynatrace/easyTravel 2.0.0/easyTravel/database/mongodb/mongodb$ tar -czf ../easyTravel-mongodb-db.tar.gz mongodb
```