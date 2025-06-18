pipeline {
    agent any

    environment {
        SONARQUBE_ENV = 'SonarScanner' // Sostituisci con il nome configurato in Jenkins
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Clean') {
            steps {
                sh 'mvn clean'
            }
        }
        
        stage('Package') {
            steps {
                sh 'mvn package'
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv("${SONARQUBE_ENV}") {
                    sh 'mvn sonar:sonar'
                }
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
