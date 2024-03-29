# The Madek API

The madek-api is a JSON / JSON-ROA API for Madek.


### TODOs Users and Groups

* DB schema refactoring users: zhdkid -> institutional_id

* resource `/users/`, `/users/:id` GET, POST, PATCH

* resource `/groups/:group_id/users/`  `/groups/:id/users/:user_id` GET, PUT, DELETE
* (???) resource `/users/:user_id/groups/`  `/groups/:id/users/:user_id` GET, PUT, DELETE



## Development

### Configuration

Create a config/settings.local.yml with content similar like:

    database:
      url: postgresql://localhost:5432/madek_development?pool=3

    services:
      api:
        http:
          port: 3100

This assumes that PUSER and PGPASSWORD environment variables are set. Values
can be submitted as in the following if this is not the case:

    database:
      url: postgresql://localhost:5432/madek_development?pool=3&user=PGUSER&password=PGPASSWORD

### Starting up the Server

Either `lein run` or with-in the REPL  `(-main)`.

### Running the Tests

When the server is running (!)

    bundle && ./bin/rspec

Note, there is no special environment as in rails.

## Copyright and license

Madek is (C) Zürcher Hochschule der Künste (Zurich University of the Arts).

Madek is Free Software under the GNU General Public License (GPL) v3, see the included LICENSE file for license details.

Visit our main website at http://www.zhdk.ch and the IT center
at http://itz.zhdk.ch