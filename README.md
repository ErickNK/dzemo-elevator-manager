# Elevator Manager Tech Challenge

This service is designed to mimic the operation fo elevators in a building.
Elevators and floors are configurable via /src/main/resources/application.properties/application.properties files.

```properties
elevator-configs.floor-count=10
elevator-configs.elevators-count=5
```
## Prerequisites
1. Docker and Docker Compose installed on machine.
2. Java 17 installed

## Setup
1. Docker Compose issued to create services that the whole application requires. 
   - To start up services, navigate to root directory of project.
   - ```shell
     $ cd docker && sudo docker-compose -f docker-compose.dev.yml up -d mysql rabbitmq
     ```
2. Find SQL schema scripts to setup database on root directory
   - execute scripts on mysql docker container created by docker-compose under localhost:3306

3. Run java application. Application will run at port http://localhost:8081


## Frontend
This service has a webpage at http://localhost:8081. The webpage allows viewing of elevator statuses in realtime.

## Concept
The service allows a configurable amount of elevators to move to a floor they are called.
The service relies on Rabbitmq to provide a First In First Out (FIFO) queue system. Each time a call
is made for elevator it checks whether the call is viable then places a task to move the elevator 
to that floor on the queue. This allows multiple calls from different floors without disrupting ongoing 
call. 
Calls on same floor are denied as the elevator is already on the same floor.

Elevators move at set seconds per floor and open doors at set seconds with below configs.
```properties
elevator-configs.elevators-time-per-floor=5
elevator-configs.doors-time-per-action=2
```

Internally movement is simulated via Thread.sleep(1000). Each movement is asynchronous and happens on
its own thread.
On every second current position and statuses of elevator are saved on db and published to 
webpage via **Pusherjs**.