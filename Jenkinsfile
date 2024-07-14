pipeline {
    agent {
        dockerfile {
            filename 'agent/Dockerfile'
            args '-v /root/.m2:/root/.m2 -v /var/run/docker.sock:/var/run/docker.sock'
        }
    }

    environment {
        DOCKERHUB_AUTH = credentials('DockerHubCredentials')
        MYSQL_AUTH = credentials('MYSQL_AUTH')
        SSH_AUTH_SERVER = credentials('SSH_AUTH_SERVER')
        HOSTNAME_DEPLOY_PROD = "34.197.213.138"
        HOSTNAME_DEPLOY_STAGING = "34.233.177.253"
        IMAGE_NAME = 'paymybuddy'
        IMAGE_TAG = 'latest'
    }

    stages {

        stage('Test') {
            steps {
                sh 'mvn clean test'
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                }
            }
        }

        stage('SonarCloud analysis') {
            steps {
                withSonarQubeEnv('SonarCloudServer') {
                    sh 'mvn sonar:sonar -s .m2/settings.xml'
                }
            }
        }

        stage('Quality Gate') {
            steps {
                timeout(time: 60, unit: 'SECONDS') {
                    waitForQualityGate abortPipeline: false
                }
            }
        }

        stage('Package') {
            steps {
                sh 'mvn clean package -DskipTests'
            }
        }

        stage('Build and push IMAGE to docker registry') {
            steps {
                sh """
                    docker build -t ${DOCKERHUB_AUTH_USR}/${IMAGE_NAME}:${IMAGE_TAG} .
                    echo ${DOCKERHUB_AUTH_PSW} | docker login -u ${DOCKERHUB_AUTH_USR} --password-stdin
                    docker push ${DOCKERHUB_AUTH_USR}/${IMAGE_NAME}:${IMAGE_TAG}
                """
            }
        }

        stage ('IAC Staging  on aws') { 
            when {
                expression { GIT_BRANCH == 'origin/deployment' }
            }
            agent { 
                docker { 
                    image 'jenkins/jnlp-agent-terraform' 
                } 
            }     
            steps {
              withCredentials([aws(credentialsId: 'AwsCredentials', accessKeyVariable: 'AWS_ACCESS_KEY_ID', secretKeyVariable: 'AWS_SECRET_ACCESS_KEY')]) {
                dir('terraform/staging') {
                    sh '''
                        terraform init
                        terraform destroy --auto-approve
                        terraform apply --auto-approve
                        sleep 60
                    '''
                }
              }
            }
        }


        stage ('Deploy in staging') {
            when {
                expression { GIT_BRANCH == 'origin/deployment' }
            }
            agent { 
                dockerfile {
                    filename 'agent/Dockerfile.ansible'
                    args '-u root -v /var/run/docker.sock:/var/run/docker.sock'
                }
            }
            steps {
                dir('ansible') {
                    sh '''
                        ansible-galaxy collection install community.docker
                        ansible-playbook -i hosts.yml playbook.yml --extra-vars deploy_host="staging"
                        sleep 60
                    '''
                }
            }
        }

        stage('Test Staging') {
            when {
                expression { GIT_BRANCH == 'origin/deployment' }
            }
            steps {
                sh '''
                    apk add --no-cache curl
                    curl ${HOSTNAME_DEPLOY_STAGING}:8080
                '''
            }
        }

        stage('Destroy staging') {  
            when {
                expression { GIT_BRANCH == 'origin/deployment' }
            }        
            agent { 
                docker { 
                    image 'jenkins/jnlp-agent-terraform'  
                } 
            }
            steps {
                timeout(time: 3, unit: "MINUTES") {
                    // input message: "Confirmer vous la suppression de l'environnement staging dans AWS ?", ok: 'Yes'
                } 
                withCredentials([aws(credentialsId: 'AwsCredentials', accessKeyVariable: 'AWS_ACCESS_KEY_ID', secretKeyVariable: 'AWS_SECRET_ACCESS_KEY')]) {
                    dir('terraform/staging') {
                        sh '''
                            terraform init
                            terraform destroy --auto-approve
                        '''
                    }
                }
            }
        }

        stage ('IAC Prod on AWS'){
            when {
                expression { GIT_BRANCH == 'origin/deployment' }
            }
             
            agent { 
                docker { 
                    image 'jenkins/jnlp-agent-terraform' 
                } 
            }
             steps {
              withCredentials([aws(credentialsId: 'AwsCredentials', accessKeyVariable: 'AWS_ACCESS_KEY_ID', secretKeyVariable: 'AWS_SECRET_ACCESS_KEY')]) {
                dir ('terraform/prod') {
                    sh '''
                        terraform init
                        terraform destroy --auto-approve
                        terraform apply --auto-approve 
                        sleep 60
                    '''
                }
              }
        
            }
        } 

        stage ('Deploy in prod') {
            when {
                expression { GIT_BRANCH == 'origin/deployment' }
            }
            agent { 
                dockerfile {
                    filename 'agent/Dockerfile.ansible'
                    args '-u root -v /var/run/docker.sock:/var/run/docker.sock'
                }
            }
            steps {
                dir('ansible') {
                    sh '''
                        ansible-galaxy collection install community.docker
                        ansible-playbook -i hosts.yml playbook.yml --extra-vars deploy_host="prod"
                        sleep 60
                    '''
                }
            }
        }

        stage('Test Prod') {
            when {
                expression { GIT_BRANCH == 'origin/deployment' }
            }
            steps {
                sh '''
                    apk add --no-cache curl
                    curl ${HOSTNAME_DEPLOY_PROD}:8080
                '''
            }
        }

        stage('Destroy prod') {  
            when {
                expression { GIT_BRANCH == 'origin/deployment' }
            }        
            agent { 
                docker { 
                    image 'jenkins/jnlp-agent-terraform'  
                } 
            }
            steps {
                timeout(time: 3, unit: "MINUTES") {
                    // input message: "Confirmer vous la suppression de l'environnement staging dans AWS ?", ok: 'Yes'
                } 
                withCredentials([aws(credentialsId: 'AwsCredentials', accessKeyVariable: 'AWS_ACCESS_KEY_ID', secretKeyVariable: 'AWS_SECRET_ACCESS_KEY')]) {
                    dir('terraform/prod') {
                        sh '''
                            terraform init
                            terraform destroy --auto-approve
                        '''
                    }
                }
            }
        }
    }

}
