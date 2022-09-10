# Elevator Manager Tech Challenge

This service is designed to mimic the operation fo elevators in a building.
Elevators and floors are configurable via /src/main/resources/application.properties/application.properties files.

```properties
elevator-configs.floor-count=10
elevator-configs.elevators-count=5
```
## Prerequisites
1. Docker and Docker Compose installed on machine.


## Setup
1. Docker Compose issued to create services that the whole application requires. 
   - To start up services, navigate to root directory of project.
   - ```shell
     $ cd docker && docker-compose sudo docker-compose -f docker-compose.dev.yml up -d mysql rabbitmq
     ```
2. Find SQL schema scripts to setup database on root directory
   - execute scripts on mysql docker container created by docker-compose under localhost:3306

3. Run java application. Application will run at port http://localhost:8081


## Frontend
This service has a webpage at http://localhost:8081. The webpage allow viewing of elevator statuses in realtime.