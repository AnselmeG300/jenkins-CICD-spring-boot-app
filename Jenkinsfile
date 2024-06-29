pipeline {
    agent {
        dockerfile {
            filename 'agent/Dockerfile'
            args '-v /root/.m2:/root/.m2 -v /var/run/docker.sock:/var/run/docker.sock'
        }
    }

    environment {
        DOCKERHUB_AUTH = credentials('DockerHubCredentials')
        MYSQL_AUTH= credentials('MYSQL_AUTH')
        HOSTNAME_DEPLOY_PROD = "20.115.44.142"
        HOSTNAME_DEPLOY_STAGING = "20.115.42.32"
        IMAGE_NAME= 'paymybuddy'
        IMAGE_TAG= 'latest'
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

        stage ('Deploy in staging') {
            when {
                expression { GIT_BRANCH == 'origin/main' }
            }
            steps {
                sshagent(credentials: ['SSH_AUTH_SERVER']) { 
                    sh '''
                        [ -d ~/.ssh ] || mkdir ~/.ssh && chmod 0700 ~/.ssh
                        ssh-keyscan -t rsa,dsa ${HOSTNAME_DEPLOY_STAGING} >> ~/.ssh/known_hosts
                        scp -r deploy centos@${HOSTNAME_DEPLOY_STAGING}:/home/centos/
                        command1="cd deploy && echo ${DOCKERHUB_AUTH_PSW} | docker login -u ${DOCKERHUB_AUTH_USR} --password-stdin"
                        command2="echo 'IMAGE_VERSION=${DOCKERHUB_AUTH_USR}/${IMAGE_NAME}:${IMAGE_TAG}' > .env && echo ${MYSQL_AUTH_PSW} > secrets/db_password.txt && echo ${MYSQL_AUTH_USR} > secrets/db_user.txt"
                        command3="echo 'SPRING_DATASOURCE_URL=jdbc:mysql://paymybuddydb:3306/db_paymybuddy' > env/paymybuddy.env && echo 'SPRING_DATASOURCE_PASSWORD=${MYSQL_AUTH_PSW}' >> env/paymybuddy.env && echo 'SPRING_DATASOURCE_USERNAME=${MYSQL_AUTH_USR}' >> env/paymybuddy.env"
                        command4="docker compose down && docker pull ${DOCKERHUB_AUTH_USR}/${IMAGE_NAME}:${IMAGE_TAG}"
                        command5="docker compose up -d"
                        ssh -t centos@${HOSTNAME_DEPLOY_STAGING} \
                            -o SendEnv=IMAGE_NAME \
                            -o SendEnv=IMAGE_TAG \
                            -o SendEnv=DOCKERHUB_AUTH_USR \
                            -o SendEnv=DOCKERHUB_AUTH_PSW \
                            -o SendEnv=MYSQL_AUTH_USR \
                            -o SendEnv=MYSQL_AUTH_PSW \
                            -C "$command1 && $command2 && $command3 && $command4 && $command5"
                    '''
                }
            }
        }

        stage('Test Staging') {
            when {
                expression { GIT_BRANCH == 'origin/main' }
            }
            steps {
                sh '''
                    sleep 30
                    apk add --no-cache curl
                    curl ${HOSTNAME_DEPLOY_STAGING}:8080
                '''
            }
        }

        stage ('Deploy in prod') {
            when {
                expression { GIT_BRANCH == 'origin/main' }
            }
            steps {
                sshagent(credentials: ['SSH_AUTH_SERVER']) { 
                    sh '''
                        [ -d ~/.ssh ] || mkdir ~/.ssh && chmod 0700 ~/.ssh
                        ssh-keyscan -t rsa,dsa ${HOSTNAME_DEPLOY_PROD} >> ~/.ssh/known_hosts
                        scp -r deploy centos@${HOSTNAME_DEPLOY_PROD}:/home/centos/
                        command1="cd deploy && echo ${DOCKERHUB_AUTH_PSW} | docker login -u ${DOCKERHUB_AUTH_USR} --password-stdin"
                        command2="echo 'IMAGE_VERSION=${DOCKERHUB_AUTH_USR}/${IMAGE_NAME}:${IMAGE_TAG}' > .env && echo ${MYSQL_AUTH_PSW} > secrets/db_password.txt && echo ${MYSQL_AUTH_USR} > secrets/db_user.txt"
                        command3="echo 'SPRING_DATASOURCE_URL=jdbc:mysql://paymybuddydb:3306/db_paymybuddy' > env/paymybuddy.env && echo 'SPRING_DATASOURCE_PASSWORD=${MYSQL_AUTH_PSW}' >> env/paymybuddy.env && echo 'SPRING_DATASOURCE_USERNAME=${MYSQL_AUTH_USR}' >> env/paymybuddy.env"
                        command4="docker compose down && docker pull ${DOCKERHUB_AUTH_USR}/${IMAGE_NAME}:${IMAGE_TAG}"
                        command5="docker compose up -d"
                        ssh -t centos@${HOSTNAME_DEPLOY_PROD} \
                            -o SendEnv=IMAGE_NAME \
                            -o SendEnv=IMAGE_TAG \
                            -o SendEnv=DOCKERHUB_AUTH_USR \
                            -o SendEnv=DOCKERHUB_AUTH_PSW \
                            -o SendEnv=MYSQL_AUTH_USR \
                            -o SendEnv=MYSQL_AUTH_PSW \
                            -C "$command1 && $command2 && $command3 && $command4 && $command5"
                    '''
                }
            }
        }

        stage('Test Prod') {
            when {
                expression { GIT_BRANCH == 'origin/main' }
            }
            steps {
                sh '''
                    sleep 30
                    apk add --no-cache curl
                    curl ${HOSTNAME_DEPLOY_PROD}:8080
                '''
            }
        }
    }

}
