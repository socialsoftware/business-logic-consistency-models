# Microservices Simulator

The artifact supports the test of business logic of a microservices application designed on the concept of Domain-Driven Design Aggregate and using Transactional Causal Consistency to handle transactional behavior.

The system allows testing the interleaving of functionalities execution in a deterministic context, such that it is possible to evaluate the resulting behavior.

## Run Using Docker

* Build the application
```
docker-compose build
```

* Running the application
```
docker-compose up backend
```

* Running Spock Tests
```
docker-compose up unit-tests
```

* Some test cases:
  * [Tournament Merge Tests](backend/src/test/groovy/pt/ulisboa/tecnico/socialsoftware/blcm/aggregate/tournament/MergeUnitTest.groovy)
  * [Tournament Functionality Tests](backend/src/test/groovy/pt/ulisboa/tecnico/socialsoftware/blcm/functionality/TournamentFunctionalityTest.groovy)


## Run Using Maven

### Technology Requirements

- [Maven 3.6.3](https://archive.apache.org/dist/maven/maven-3/3.6.3/)

- [Java 17+](https://openjdk.org/projects/jdk/17/)

- [PSQL 14](https://www.postgresql.org/download/)

- [JMeter 5.5](https://jmeter.apache.org/download_jmeter.cgi)

### Setting up the database
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


### Running the application

```
cd backend
mvn clean spring-boot:run
```
### Running Spock tests

```
cd backend
mvn clean -Ptest test
```

* Some test cases:
  * [Tournament Merge Tests](backend/src/test/groovy/pt/ulisboa/tecnico/socialsoftware/blcm/aggregate/tournament/MergeUnitTest.groovy)
  * [Tournament Functionality Tests](backend/src/test/groovy/pt/ulisboa/tecnico/socialsoftware/blcm/functionality/TournamentFunctionalityTest.groovy)

## Running JMeter tests

* After starting application, either using Docker or Maven, and installing JMeter

```
cd backend/jmeter/tournament/thesis-cases/
jmeter -n -t TEST.jmx
```

* Some test cases:
  * [5a-updateStudentName-addParticipant-processUpdateNameEvent.jmx](backend/jmeter/tournament/thesis-cases/5a-updateStudentName-addParticipant-processUpdateNameEvent.jmx)
  * [5b-addParticipant-updateStudentName-processUpdateNameEvent.jmx](backend/jmeter/tournament/thesis-cases/5b-addParticipant-updateStudentName-processUpdateNameEvent.jmx)
  * [5c-updateStudentName1-addParticipant-updateStudentName2-processUpdateNameEvent.jmx](backend/jmeter/tournament/thesis-cases/5c-updateStudentName1-addParticipant-updateStudentName2-processUpdateNameEvent.jmx)
  * [5d-addParticipant1-updateStudentName-processUpdateNameEvent1-addParticipant2-processUpdateNameEvent2.jmx](backend/jmeter/tournament/thesis-cases/5d-addParticipant1-updateStudentName-processUpdateNameEvent1-addParticipant2-processUpdateNameEvent2.jmx)
  * [8-5-update-tournament-concurrent-intention-pass.jmx](backend/jmeter/tournament/thesis-cases/8-5-update-tournament-concurrent-intention-pass.jmx)
  * [8-6-add-participant-concurrent-update-execution-student-name-processing-ends-first.jmx](backend/jmeter/tournament/thesis-cases/8-6-add-participant-concurrent-update-execution-student-name-processing-ends-first.jmx)
  * [8-7-add-participant-concurrent-anonymize-event-processing-processing-ends-last.jmx](backend/jmeter/tournament/thesis-cases/8-7-add-participant-concurrent-anonymize-event-processing-processing-ends-last.jmx)
  * [8-8-update-execution-student-add-participant-process-event-add-participant.jmx](backend/jmeter/tournament/thesis-cases/8-8-update-execution-student-add-participant-process-event-add-participant.jmx) 
  * [8-9-add-participant-concurrent-anonymize-event-processing-processing-ends-first.jmx](backend/jmeter/tournament/thesis-cases/8-9-add-participant-concurrent-anonymize-event-processing-processing-ends-first.jmx)
  * [8-10-concurrent-delete-tournament-add-participant.jmx](backend/jmeter/tournament/thesis-cases/8-10-concurrent-delete-tournament-add-participant.jmx)
### Viewing JMeter tests structure

```
cd backend/jmeter/tournament/thesis-cases/
jmeter
```
* The command launches JMeter GUI. By clicking `File > Open` and selecting a test file it is possible to observe the test structure.
* Tests can also be run using the GUI, by clicking on the `Start` button.

##  Spock Tests in DAIS2023 paper - 23nd International Conference on Distributed Applications and Interoperable Systems

To reproduce the paper results follow the steps:

* Analyze a figure in the paper, fig3a-d and fig4;
* Read the test case code for the figure, including the final assertions that define the expected behavior (see below);
* Run the test case (see below);
* Read the logger INFO messages, they use UPPERCASE. They identify when a functionality and event processing starts and ends and what its version number is. 
  * For instance, in test-fig4 both functionalities start with the same version number (they are concurrent), but addParticipant finishes with a higher number, because it finishes after updateName. It can be observed in the log that an exception was thrown, due to the invariant break.


### Figure 3(a)
* [Test code](https://github.com/socialsoftware/business-logic-consistency-models/blob/8dcfbc6ce824ae5e506521bde4c63322f47c6e00/backend/src/test/groovy/pt/ulisboa/tecnico/socialsoftware/blcm/functionality/TournamentFunctionalityTest.groovy#L142-L157) 
* Run:
```
docker-compose up test-fig3a
```

### Figure 3(b)
* [Test code](https://github.com/socialsoftware/business-logic-consistency-models/blob/8dcfbc6ce824ae5e506521bde4c63322f47c6e00/backend/src/test/groovy/pt/ulisboa/tecnico/socialsoftware/blcm/functionality/TournamentFunctionalityTest.groovy#L159-L176)
* Run: 
```
docker-compose up test-fig3b
```

### Figure 3(c)
* [Test code](https://github.com/socialsoftware/business-logic-consistency-models/blob/8dcfbc6ce824ae5e506521bde4c63322f47c6e00/backend/src/test/groovy/pt/ulisboa/tecnico/socialsoftware/blcm/functionality/TournamentFunctionalityTest.groovy#L178-L197)
* Run: 
```
docker-compose up test-fig3c
```

### Figure 3(d)
* [Test code](https://github.com/socialsoftware/business-logic-consistency-models/blob/8dcfbc6ce824ae5e506521bde4c63322f47c6e00/backend/src/test/groovy/pt/ulisboa/tecnico/socialsoftware/blcm/functionality/TournamentFunctionalityTest.groovy#L199-L220)
* Run: 
```
docker-compose up test-fig3d
```

### Figure 4
* [Test code](https://github.com/socialsoftware/business-logic-consistency-models/blob/8dcfbc6ce824ae5e506521bde4c63322f47c6e00/backend/src/test/groovy/pt/ulisboa/tecnico/socialsoftware/blcm/functionality/TournamentFunctionalityTest.groovy#L302-L332)
* Run: 
```
docker-compose up test-fig4
```
