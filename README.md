# docker swarm

- Pre-Req:
- - One Ubuntu20 for Master
- - Two Ubuntu20 for Workernodes

## Step1: Install docker on all three servers:

### to install docker 
```
sudo apt update
sudo apt install docker.io -y
sudo usermod -aG docker ubuntu
OR
sudo usermod -aG docker ${USER}
```

### to Add ubuntu user on docker group

## Step2: Open Ports:

- Docker Swarm listens on:
- - TCP 2377 for cluster mng communicaction
- - TCP nd UDP 7946 inter-comminucation between nodes/workers
- - UDP 4789 for overlay network type

***I have alloved all the port for demo purpose***


## Step3: Initiat and join docker swarm:

***by default when you install Docker , it alo install's Docker-Swarm***

- Initiat docker clcuster
```
docker swarm init
```
- output
```
ubuntu@ip-172-31-13-194:~$ docker swarm init
Swarm initialized: current node (lmcg76tzq1xdk38436nw7szpv) is now a manager.

To add a worker to this swarm, run the following command:

    docker swarm join --token SWMTKN-1-58xy1202fwk41ax38887kvezmnb6xdpoeuf1eyvgjmqji4p1jn-6g4ew1rso26sny4sekivasobz 172.31.13.194:2377

To add a manager to this swarm, run 'docker swarm join-token manager' and follow the instructions.

ubuntu@ip-172-31-13-194:~$
```

- Generate Copy docker token for worker to join cluster

- if you forget token run below command:

```
docker swarm join-token worker
```

- to add another Manager/Master node


```
docker swarm join-token manager
```

## Step4: Add worker-nodes to Cluster
- just execute token form Master node

## Step5: Get all Docker nodes
```
docker node ls
```

- see Leader node and worker 
```
ubuntu@ip-172-31-13-194:~$ docker node ls
ID                            HOSTNAME            STATUS     AVAILABILITY        MANAGER STATUS      ENGINE VERSION
5yu3usaa6t7xw9244q5in9uua     ip-172-31-0-252     Ready      Active                                  19.03.8
9ioteg21xur1f6vh9eul1in7c     ip-172-31-12-119    Ready      Active                                  19.03.8
lmcg76tzq1xdk38436nw7szpv *   ip-172-31-13-194    Ready      Active              Leader              19.03.8
ubuntu@ip-172-31-13-194:~$

```

## Step6: Deploy Docker application on Docker Swarm cluster

```
docker service create --name myWebAppServer --replicas 2 -p 80:80 httpd
```

- will create services

```
ubuntu@ip-172-31-13-194:~$ docker service create --name my-httpd-server --replicas 2 -p 80:80 httpd
4pl92i56cgrv5wgfb0yhcrnhm
overall progress: 2 out of 2 tasks
1/2: running
2/2: running
verify: Service converged
ubuntu@ip-172-31-13-194:~$

```
- list services created:

```
ubuntu@ip-172-31-13-194:~$ docker service ls
ID                  NAME                MODE                REPLICAS            IMAGE               PORTS
4pl92i56cgrv        my-httpd-server     replicated          2/2                 httpd:latest        *:80->80/tcp
ubuntu@ip-172-31-13-194:~$
```

- to get where exactly it had deployed ? RUN below command
:

```
docker service ps my-httpd-server
```
- see it has created 2 containers;
```
ubuntu@ip-172-31-13-194:~$ docker service ps my-httpd-server
ID                  NAME                IMAGE               NODE                DESIRED STATE       CURRENT STATE           ERROR               PORTS
mjr0q49o0sf4        my-httpd-server.1   httpd:latest        ip-172-31-13-194    Running             Running 4 minutes ago
mb0s0g24nlpt        my-httpd-server.2   httpd:latest        ip-172-31-0-252     Running             Running 4 minutes ago
ubuntu@ip-172-31-13-194:~$
```

## Step7: Access application using public-ip of either of the worker nodes;

- so ``34.240.233.69:80``

- If you have succesfully run all above comand you should get ##**it works!** displayed.