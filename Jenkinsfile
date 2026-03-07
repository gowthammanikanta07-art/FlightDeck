pipeline {
    agent any

    tools {
        maven 'Maven3'
        jdk 'JDK21'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build - Flight Info Service') {
            steps {
                dir('flight-info-service') {
                    bat 'mvn clean install -DskipTests -o'
                }
            }
        }

        stage('Unit Tests - Flight Info Service') {
            steps {
                dir('flight-info-service') {
                    bat 'mvn test -Dtest=FlightServiceTest -o'
                }
            }
            post {
                always {
                    junit 'flight-info-service/target/surefire-reports/*.xml'
                }
            }
        }

        stage('Integration Tests - Flight Info Service') {
            steps {
                dir('flight-info-service') {
                    bat 'mvn test -Dtest=FlightControllerIntegrationTest -o'
                }
            }
            post {
                always {
                    junit 'flight-info-service/target/surefire-reports/*.xml'
                }
            }
        }

        stage('Code Coverage - Flight Info Service') {
            steps {
                dir('flight-info-service') {
                    bat 'mvn jacoco:report -o'
                }
            }
            post {
                always {
                    recordCoverage(
                        tools: [[parser: 'JACOCO']],
                        id: 'jacoco',
                        name: 'JaCoCo Coverage'
                    )
                }
            }
        }

        stage('SonarQube Analysis - Flight Info Service') {
            steps {
                dir('flight-info-service') {
                    withSonarQubeEnv('SonarQube') {
                        bat 'mvn sonar:sonar -o'
                    }
                }
            }
        }

        stage('Quality Gate') {
            steps {
                timeout(time: 2, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }
    }

    post {
        success {
            echo 'Pipeline passed!'
        }
        failure {
            echo 'Pipeline failed!'
        }
    }
}