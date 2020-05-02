## using Jenkins deploy application on Docker Swarm cluster

- check [docker-stack.groovy](/docker-stack.groovy) file which i have used for this applicaation building.

- install ``ssh agent`` >> manage plugins>> available tab>> search ssh agent.

- deploy service/application
last stage in pipeline script

- Under Pipeline script>>``Pipeline Syntax``>>``sshagent: SSH Agent``
- - Kind: SSH Username with private key
- - ID: Ubuntu
- - Usernae: ubuntu
- - tick Entire directly: copy content form pem file. (open pem file using text-editor and past content) >> Add
- - click generate Pipeline script

```
stage("Deploy socker Apps in Docker Swarm Cluster"){
    sshagent(['DOCKER_DEV_SSH']) {
        sh "ssh -o strictHostKeyChecking=no ubuntu@172.31.13.194 docker service rm java-web-srv || true"    
        sh "ssh -o strictHostKeyChecking=no ubuntu@172.31.13.194 docker service create --name java-web-srv -p 8080:8080 --replicas 2 yogmicroservices/java-web:${buildNumber}"
    }    
}
```
- explanation of above stage: 
- - ``DOCKER_DEV_SSH``: Using pem file to authenticate Docker-Master machine
- - ``strictHostKeyChecking=no`` : Say no to confirmation for fingerptint as you are doing CI
- - ``docker service rm java-web-srv || true`` : remove service if exist.
- - ``docker service create --name java-web-srv``: create a service called ``java-web-srv``
- - ``-p 8080:8080 --replicas 2``: use port 8080 for ingress & egress and create 2 replics / copy of service
- - ``yogmicroservices/java-web:${buildNumber}``: use this image/code to build services.


- once you have successfully RUN it you shoudl have service created:
- - RUN ``docker service ls `` on Docker Master node:
```
ubuntu@ip-172-31-13-194:~$ docker service ls
ID                  NAME                MODE                REPLICAS            IMAGE                          PORTS
bj7h27gt5f1t        java-web-srv        replicated          2/2                 yogmicroservices/java-web:12   *:8080->8080/tcp
ubuntu@ip-172-31-13-194:~$
```

- Access application using worknode-public-ip:8080/app-context

## Next create two services:

- Will use docker-compose.yml to create two services:
- - FE: and BE: Mongo DB -

- Create custome ``overlay network`` in Docker Master node. as this is required by docker-compose.yml. 
check [line: 39](
https://github.com/yogmangela/spring-boot-mongo-docker/blob/master/docker-compose.yml)
- it's external: customoverlay

```
ubuntu@ip-172-31-13-194:~$ docker network create -d overlay customoverlay
10bgae1muoym174o0r7a607tm
ubuntu@ip-172-31-13-194:~$
```

- Last stage is different than previous:

```
stage("Deploy to docker swarm cluster"){
    sshagent(['DOCKER_DEV_SSH']) {
        sh 'scp -o StrictHostKeyChecking=no  docker-compose.yml ubuntu@172.31.13.194:'
        sh 'ssh -o StrictHostKeyChecking=no ubuntu@172.31.13.194 docker stack deploy --prune --compose-file docker-compose.yml mongo-docker-srv'
    }
} 
```
- ``sh 'scp -o StrictHostKeyChecking=no  docker-compose.yml ubuntu@172.31.13.194:'``: copy docker-compose.yml file to docker master.
- ``sh 'ssh -o StrictHostKeyChecking=no ubuntu@172.31.13.194 docker stack deploy --prune --compose-file docker-compose.yml mongo-docker-srv``: deploying services using docker stack command.

- check services created
```
ubuntu@ip-172-31-13-194:~$ docker service ls
ID                  NAME                          MODE                REPLICAS            IMAGE                                    PORTS
bj7h27gt5f1t        java-web-srv                  replicated          2/2                 yogmicroservices/java-web:12             *:8080->8080/tcp
bu3sx42teo0v        mongo-docker-srv_mongo        replicated          1/1                 mongo:latest
ltf4785lokr8        mongo-docker-srv_springboot   replicated          2/2                 dockerhandson/spring-boot-mongo:latest   *:9090->8080/tcp
ubuntu@ip-172-31-13-194:~$
```

- check created stack:
```
ubuntu@ip-172-31-13-194:~$ docker stack ls
NAME                SERVICES            ORCHESTRATOR
mongo-docker-srv    2                   Swarm
ubuntu@ip-172-31-13-194:~$
```
## Added SonarQube as last stage:

 - make sure to change SonarQube-Public-ip under: Jenkins>>Manage Plugins>> Configure system >> SonarQube servers >> Server URL: add SonarQube-public-ip

 
