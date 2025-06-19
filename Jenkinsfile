pipeline {
    agent any

    environment {
        SONARQUBE_ENV = 'SonarQube'
    }

    options {
        // Attiva il supporto ANSI color nella console output
        ansiColor('xterm')
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
                echo '\u001B[34mCheckout completed\u001B[0m'  // testo blu
            }
        }

        stage('Clean') {
            steps {
                sh 'mvn clean'
                echo '\u001B[33mClean done\u001B[0m' // testo giallo
            }
        }

        stage('Test') {
            steps {
                sh 'mvn test'
                echo '\u001B[32mTests passed successfully\u001B[0m'  // testo verde
            }
        }

        stage('Package') {
            steps {
                sh 'mvn package'
                echo '\u001B[36mPackaging done\u001B[0m' // testo ciano
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv("${SONARQUBE_ENV}") {
                    sh 'mvn sonar:sonar'
                }
                echo '\u001B[35mSonarQube analysis completed\u001B[0m' // testo magenta
            }
        }
    }

    post {
        success {
            echo '\u001B[32mBuild is fine!\u001B[0m' // verde
        }
        failure {
            echo '\u001B[31mBuild is bad!\u001B[0m' // rosso
        }
    }
}
