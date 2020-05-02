node{
    def buildNumber=BUILD_NUMBER
    stage("clone git repo"){
        git credentialsId: 'GIT_CREDENTIALS', url: 'https://github.com/yogmangela/java-web-app-docker.git'
    }
    stage("Maven Clean package"){
        def mavenHome= tool name: "Maven_3.6.3", type: "maven"
        def mavenCMD="${mavenHome}/bin/mvn"
        sh "${mavenCMD} clean package"
    }
    stage("Build Docker Image"){
        sh "docker build -t yogmicroservices/java-web:${buildNumber} ."
    }
    
    stage("Push docker image to DockerHub"){
         withCredentials([string(credentialsId: 'DOCKER_HUB_PWD', variable: 'DOCKER_HUB_PWD')]){
            sh "docker login -u yogmicroservices -p ${DOCKER_HUB_PWD}"
        }
        sh "docker push  yogmicroservices/java-web:${buildNumber}"
    }
    stage("Deploy socker Apps in Docker Swarm Cluster"){
        sshagent(['DOCKER_DEV_SSH']) {
            sh "ssh -o strictHostKeyChecking=no ubuntu@172.31.13.194 docker service rm java-web-srv || true"    
            sh "ssh -o strictHostKeyChecking=no ubuntu@172.31.13.194 docker service create --name java-web-srv -p 8080:8080 --replicas 2 yogmicroservices/java-web:${buildNumber}"
        }    
    }
}