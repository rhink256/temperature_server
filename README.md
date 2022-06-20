
## Overview
Backend for temperature monitoring project.

Java EE learning project which accepts temperature data from any number of Arduino environmental monitoring platforms via a REST API. Communicates with a Vue frontend via REST API and Websockets, allowing temperature updates to be pushed to the front end.

The Arduino code is in the "Arduino Sensor" project.

All sensor data is stored in an SQL database via Hibernate.

Build and deployment automation support exists, but requires Docker, Jenkins, and Nexus to be running locally. Nexus serves as a maven and a docker repository.

A Dockerfile is provided and depends on a modified wildfly container with postgres drivers added from the "Wildfly with Postgres Driver" project. A compose file exists in the "Docker Compose" project, and will start the database, backend (this project), and front end.
