# graviton


## Getting started

### Start Postgres DB

Graviton requires PostgreSQL 14 or higher.
It can be installed locally or run as a container.

#### Local DB

If you prefer to run PostgreSQL locally:

```shell script
sudo dnf install postgresql-server postgresql postgresql-jdbc
sudo postgresql-setup --initdb --unit postgresql
```

Configure to trust local connections.
Open `/var/lib/pgsql/data/pg_hba.conf` and change locall connection methods to `trust`:

```
  # TYPE  DATABASE    USER        CIDR-ADDRESS          METHOD
  local   all         all                               trust
  host    all         all         127.0.0.1/32          trust
  host    all         all         ::1/128               trust
```

Then enable and start the postgresql service:

```shell script
sudo systemctl enable postgresql.service
sudo systemctl start postgresql.service
```

Create the graviton user and DB:

```shell script
sudo su - postgres -c 'createuser -dls graviton'
createdb --user graviton graviton
```

#### Containerized DB

If you prefer to run PostgreSQL in a container:

Install podman and podman-compose:

```shell script
sudo dnf install podman podman-compose
```

Use podman-compose to start the DB:

```shell script
podman-compose up
```

### Initialize DB

Tables need inserted into the graviton DB:

```
psql -U graviton graviton -h localhost -p 5432 < grav_db.sql
```
(password: graviton)

### Start graviton

You can run your application in dev mode that enables live coding using:

```shell script
./gradlew quarkusDev
```

> **_NOTE:_**  Quarkus now ships with a Dev UI, which is available in dev mode only at http://localhost:8080/q/dev/.

### Load products and subscriptions into graviton

By default, graviton will look in the `config/subscription_data` directory for the "source" product and subscription data.
You will need to ask the product maintainers for this.
Once the subscription data directory is ready, sync the product and subscription data:

```shell script
curl -k -X POST 'http://localhost:8080/poc/sync/products'
./refresher.sh
```

The product sync takes less than a minute.
Subscriptions sync can take 10 minutes.

### Test an org

With the DB configured, and the data loaded, try grabbing products for an org:

```shell script
curl -kv 'http://localhost:8080/candlepin/owners/711497/products'
```

You should see json output.

### Further configuration

You can customize your application by adding/editing the .env file at the base of the project.
To point to different "source" directories, add these lines to .env file:

```
GRAVITON_DATASOURCE_FS_SUBSCRIPTIONS=/my/custom/subscription_data/subscriptions
GRAVITON_DATASOURCE_FS_PRODUCTS=/my/custom/subscription_data/product_data
GRAVITON_DATASOURCE_FS_RAW=/my/custom/subscription_data/raw
```


## Packaging and running the application

The application can be packaged using:
```shell script
./gradlew build
```
It produces the `quarkus-run.jar` file in the `build/quarkus-app/` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `build/quarkus-app/lib/` directory.

The application is now runnable using `java -jar build/quarkus-app/quarkus-run.jar`.

If you want to build an _über-jar_, execute the following command:
```shell script
./gradlew build -Dquarkus.package.type=uber-jar
```

The application, packaged as an _über-jar_, is now runnable using `java -jar build/*-runner.jar`.

## Creating a native executable

You can create a native executable using: 
```shell script
./gradlew build -Dquarkus.package.type=native
```

Or, if you don't have GraalVM installed, you can run the native executable build in a container using: 
```shell script
./gradlew build -Dquarkus.package.type=native -Dquarkus.native.container-build=true
```

You can then execute your native executable with: `./build/graviton-1.0.0-SNAPSHOT-runner`

If you want to learn more about building native executables, please consult https://quarkus.io/guides/gradle-tooling.

## Provided Code

### RESTEasy Reactive

Easily start your Reactive RESTful Web Services

[Related guide section...](https://quarkus.io/guides/getting-started-reactive#reactive-jax-rs-resources)
