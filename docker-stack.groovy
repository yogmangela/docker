node{
    def buildNumber=BUILD_NUMBER
    stage("clone git repo"){
        git credentialsId: 'GIT_CREDENTIALS', url: 'https://github.com/yogmangela/spring-boot-mongo-docker.git'
    }
    stage("Maven Clean package"){
        def mavenHome= tool name: "Maven_3.6.3", type: "maven"
        def mavenCMD="${mavenHome}/bin/mvn"
        sh "${mavenCMD} clean package"
    }
    stage("Build Docker Image"){
        sh "docker build -t yogmicroservices/mongo-docker:${buildNumber} ."
    }  
    /*
    stage("Push docker image to DockerHub"){
         withCredentials([string(credentialsId: 'DOCKER_HUB_PWD', variable: 'DOCKER_HUB_PWD')]){
            sh "docker login -u yogmicroservices -p ${DOCKER_HUB_PWD}"
        }
        sh "docker push  yogmicroservices/mongo-docker:${buildNumber}"
    }  
    */
    
    stage("Deploy to docker swarm cluster"){
        sshagent(['DOCKER_DEV_SSH']) {
		    sh 'scp -o StrictHostKeyChecking=no  docker-compose.yml ubuntu@172.31.13.194:'
            sh 'ssh -o StrictHostKeyChecking=no ubuntu@172.31.13.194 docker stack deploy --prune --compose-file docker-compose.yml mongo-docker-srv'
        }
    }  
    // Code review using SonarQube
    stage("SonarQubeAnalysis"){
        withSonarQubeEnv(credentialsId: 'SONARQUBE_TOKEN') {
           sh 'mvn org.sonarsource.scanner.maven:sonar-maven-plugin:3.6.0.1398:sonar'
        }    
    }
}






