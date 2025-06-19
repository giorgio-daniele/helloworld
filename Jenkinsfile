pipeline {
    agent any

    environment {
        SONARQUBE_ENV = 'SonarQube'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
                echo 'Checkout completed'
            }
        }

        stage('Clean') {
            steps {
                sh 'mvn clean'
                echo 'Clean done'
            }
        }

        stage('Test') {
            steps {
                sh 'mvn test'
                echo 'Tests passed successfully'
            }
        }

        stage('Package') {
            steps {
                sh 'mvn package'
                echo 'Packaging done'
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv("${SONARQUBE_ENV}") {
                    sh 'mvn sonar:sonar'
                }
                echo 'SonarQube analysis completed'
            }
        }
    }

    post {
        success {
            echo 'Build is fine!'
        }
        failure {
            echo 'Build is bad!'
        }
    }
}
