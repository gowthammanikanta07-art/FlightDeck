pipeline {
    agent any

    tools {
        maven 'Maven3'
        jdk 'JDK21'
    }

    options {
        timeout(time: 30, unit: 'MINUTES')
    }

    environment {
        MAVEN_OPTS = '-Dmaven.repo.local=C:\\Users\\HP\\.m2\\repository'
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
                    bat 'mvn clean install -DskipTests -o -Dmaven.repo.local=C:\\Users\\HP\\.m2\\repository'
                }
            }
        }

        stage('Unit Tests - Flight Info Service') {
            steps {
                dir('flight-info-service') {
                    bat 'mvn test -Dtest=FlightServiceTest -o -Dmaven.repo.local=C:\\Users\\HP\\.m2\\repository'
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
                    bat 'mvn test -Dtest=FlightControllerIntegrationTest -o -Dmaven.repo.local=C:\\Users\\HP\\.m2\\repository'
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
                    bat 'mvn jacoco:report -o -Dmaven.repo.local=C:\\Users\\HP\\.m2\\repository'
                }
            }
            post {
    			always {
			        recordCoverage(
			            tools: [[parser: 'JACOCO']],
			            id: 'jacoco-flight',
			            name: 'Flight Info Coverage',
			            sourceCodeRetention: 'EVERY_BUILD',
			            sourceDirectories: [[path: 'flight-info-service/src/main/java']]
			        )
    			}
			}
        }

        stage('SonarQube Analysis - Flight Info Service') {
            steps {
                dir('flight-info-service') {
                    withSonarQubeEnv('SonarQube') {
                        bat 'mvn sonar:sonar -o -Dmaven.repo.local=C:\\Users\\HP\\.m2\\repository'
                    }
                }
            }
        }

        stage('Quality Gate - Flight Info Service') {
            steps {
                timeout(time: 2, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }
    }

    post {
        success {
            echo 'Pipeline passed! '
        }
        failure {
            echo 'Pipeline failed! '
        }
    }
}