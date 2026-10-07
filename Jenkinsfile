pipeline {
    agent any

    environment {
        APP_NAME = 'hackathon-app'
        IMAGE_TAG = "${BUILD_NUMBER}"
        HOST_PORT = '8082'
        CONTAINER_PORT = '8082'
    }

    options {
        timestamps()
        disableConcurrentBuilds()
        timeout(time: 30, unit: 'MINUTES')
        buildDiscarder(logRotator(numToKeepStr: '10'))
    }

    stages {

        stage('Git Checkout') {
            steps {
                checkout scm
            }
        }

        stage('JUnit Test') {
            steps {
                sh 'mvn -B test'
            }
            post {
                always {
                    junit testResults: '**/target/surefire-reports/*.xml',
                          allowEmptyResults: false
                }
            }
        }

        stage('Maven Build') {
            steps {
                sh 'mvn -B package -DskipTests'
            }
        }

        stage('Gitleaks') {
            steps {
                sh '''
                    gitleaks git \
                      --redact \
                      --report-format json \
                      --report-path gitleaks-report.json \
                      .
                '''
            }
            post {
                always {
                    archiveArtifacts artifacts: 'gitleaks-report.json',
                                     allowEmptyArchive: true
                }
            }
        }

        stage('Docker Image Build') {
            steps {
                sh 'docker build -t ${APP_NAME}:${IMAGE_TAG} .'
            }
        }

        stage('Trivy Scan') {
            steps {
                sh '''
                    set -o pipefail
                    trivy image \
                      --severity HIGH,CRITICAL \
                      --ignore-unfixed \
                      --exit-code 1 \
                      --no-progress \
                      ${APP_NAME}:${IMAGE_TAG} \
                      2>&1 | tee trivy-report.txt
                '''
            }
            post {
                always {
                    archiveArtifacts artifacts: 'trivy-report.txt',
                                     allowEmptyArchive: true
                }
            }
        }

        stage('Docker Run') {
            steps {
                sh '''
                    docker rm -f ${APP_NAME}-pipeline 2>/dev/null || true

                    docker run -d \
                      --name ${APP_NAME}-pipeline \
                      --restart unless-stopped \
                      -p ${HOST_PORT}:${CONTAINER_PORT} \
                      ${APP_NAME}:${IMAGE_TAG}

                    for i in $(seq 1 20); do
                        if curl -fsS http://127.0.0.1:${HOST_PORT}/ > /dev/null; then
                            echo "Application is reachable."
                            exit 0
                        fi
                        sleep 2
                    done

                    docker logs ${APP_NAME}-pipeline
                    exit 1
                '''
            }
        }
    }

    post {
        always {
            sh 'docker ps -a --filter "name=hackathon-app-pipeline" || true'
        }

        success {
            echo 'All required pipeline stages completed successfully.'
        }

        failure {
            echo 'Pipeline failed. Deployment did not proceed past the failed gate.'
        }
    }
}
