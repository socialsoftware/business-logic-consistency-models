# Business Logic Consistency Models

## Technology Requirements

- [Maven 3.6.3](https://archive.apache.org/dist/maven/maven-3/3.6.3/) 

- [Java 11+](https://www.oracle.com/java/technologies/downloads/#java11)

- [PSQL 12](https://www.postgresql.org/download/) 

- [JMeter 5.5](https://jmeter.apache.org/download_jmeter.cgi)


## Setting up the database
* Start db
```
sudo service postgresql start
sudo su -l postgres
dropdb blcmdb
createdb blcmdb
```
* Create user to access db
```
psql blcmdb
CREATE USER your-username WITH SUPERUSER LOGIN PASSWORD 'yourpassword';
\q
exit
```
* Rename `backend/src/main/resources/application-dev.properties.example` to `application-dev.properties` and fill the placeholder fields.


## Running the application

```
cd backend
mvn clean spring-boot:run
```

## Running JMeter tests

```
cd backend/jmeter/tournament/thesis-cases/
jmeter -n -t TEST.jmx
```

* **TEST** is replaced with actual name of a test