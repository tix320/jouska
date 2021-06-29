### Mongo admin creation guide
```
use admin;

db.createUser(
  {
    user: "admin",
    pwd: "foo"
    roles: [ { role: "userAdminAnyDatabase", db: "admin" }, "readWriteAnyDatabase" ]
  }
);
```