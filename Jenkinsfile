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
                    bat 'mvn test -Dtest=FlightServiceTest,BookingServiceTest -o -Dmaven.repo.local=C:\\Users\\HP\\.m2\\repository'
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
                    bat 'mvn test -Dtest=FlightControllerIntegrationTest,BookingControllerIntegrationTest -o -Dmaven.repo.local=C:\\Users\\HP\\.m2\\repository'
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
        }

        stage('Build - Coupon Service') {
            steps {
                dir('flight-coupon-service') {
                    bat 'mvn clean install -DskipTests -o -Dmaven.repo.local=C:\\Users\\HP\\.m2\\repository'
                }
            }
        }

        stage('Unit Tests - Coupon Service') {
            steps {
                dir('flight-coupon-service') {
                    bat 'mvn test -Dtest=CouponServiceTest -o -Dmaven.repo.local=C:\\Users\\HP\\.m2\\repository'
                }
            }
            post {
                always {
                    junit 'flight-coupon-service/target/surefire-reports/*.xml'
                }
            }
        }

        stage('Integration Tests - Coupon Service') {
            steps {
                dir('flight-coupon-service') {
                    bat 'mvn test -Dtest=CouponControllerIntegrationTests -o -Dmaven.repo.local=C:\\Users\\HP\\.m2\\repository'
                }
            }
            post {
                always {
                    junit 'flight-coupon-service/target/surefire-reports/*.xml'
                }
            }
        }

        stage('Code Coverage - Coupon Service') {
            steps {
                dir('flight-coupon-service') {
                    bat 'mvn jacoco:report -o -Dmaven.repo.local=C:\\Users\\HP\\.m2\\repository'
                }
            }
        }

        stage('Combined Coverage Report') {
            steps {
                echo 'Publishing combined FlightDeck coverage...'
            }
            post {
                always {
                    recordCoverage(
                        tools: [
                            [parser: 'JACOCO',
                             pattern: 'flight-info-service/target/site/jacoco/jacoco.xml'],
                            [parser: 'JACOCO',
                             pattern: 'flight-coupon-service/target/site/jacoco/jacoco.xml']
                        ],
                        id: 'jacoco-flightdeck',
                        name: 'FlightDeck Combined Coverage',
                        sourceCodeRetention: 'EVERY_BUILD',
                        sourceDirectories: [
                            [path: 'flight-info-service/src/main/java'],
                            [path: 'flight-coupon-service/src/main/java']
                        ]
                    )
                }
            }
        }

        stage('SonarQube Analysis - FlightDeck') {
            steps {
                withSonarQubeEnv('SonarQube') {
                    bat 'mvn sonar:sonar'
                }
            }
        }

        stage('Quality Gate - FlightDeck') {
            steps {
                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }

        stage('Docker Test') {
            steps {
                bat 'docker version'
            }
        }
        
        stage('Docker Build & Push') {
		    steps {
		        withCredentials([string(credentialsId: 'docker-hub-token', variable: 'DOCKER_TOKEN')]) {
		            bat """
		                echo %DOCKER_TOKEN% | docker login -u gowthamdocker878 --password-stdin
		
		                REM ── flight-info-service ───────────────────────────────
		                docker build ^
		                    -t gowthamdocker878/flight-info-service:%BUILD_NUMBER% ^
		                    -t gowthamdocker878/flight-info-service:latest ^
		                    .\\flight-info-service
		
		                docker push gowthamdocker878/flight-info-service:%BUILD_NUMBER%
		                docker push gowthamdocker878/flight-info-service:latest
		
		                REM ── flight-coupon-service ─────────────────────────────
		                docker build ^
		                    -t gowthamdocker878/flight-coupon-service:%BUILD_NUMBER% ^
		                    -t gowthamdocker878/flight-coupon-service:latest ^
		                    .\\flight-coupon-service
		
		                docker push gowthamdocker878/flight-coupon-service:%BUILD_NUMBER%
		                docker push gowthamdocker878/flight-coupon-service:latest
		
		                docker logout
		            """
		        }
		    }
		}

    } 

    post {
        success {
            echo 'FlightDeck pipeline passed!'
        }
        failure {
            echo 'FlightDeck pipeline failed!'
        }
    }

} 