pipeline{
  agent any
  environment {
  DOCKER_IMAGE_NAME="nananainfra1/infarbootcamp-demo"
  DOCKER_USERNAME="nananainfra1"
  DOCKER_PASSWORD=credentials("DOCKER_SECRET")
  }
  stages{
    stage("Test"){
      steps{
        sh '''
        echo "running the tests......"
        mvn clean test
        '''

   }
    }
   stage("Build"){
     steps{
       sh '''
         echo "building the docker image......"
         docker build -t "${DOCKER_IMAGE_NAME}"  -f ./Dockerfile-multi
       '''
       }
  }
  stage("Push"){
    steps{
      sh '''
        echo "pushing docker image...."
        docker login -u "${DOCKER_USERNAME}" -p "${DOCKER_PASSWORD}"
        docker tag "${DOCKER_IMAGE_NAME}" "${DOCKER_IMAGE_NAME}":"$BUILD_NUMBER"
        docker push "${DOCKER_IMAGE_NAME}":"$BUILD_NUMBER"
        docker push "${DOCKER_IMAGE_NAME}":latest
        echo "cleaning up local images......"
        docker rmi "${DOCKER_IMAGE_NAME}":"$BUILD_NUMBER"
        docker rmi "${DOCKER_IMAGE_NAME}":latest
      '''}
  }

  stage("Deploy"){
    steps{
      sh '''
       echo" deploying the application......"
       docker rm -f metadata-service ||true
       docker run -d -p 8082:8080 --name metadata-jen "${DOCKER_IMAGE_NAME}":latest
      '''
    }
}
}
}
