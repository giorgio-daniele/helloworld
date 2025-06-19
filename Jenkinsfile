pipeline {
    agent any

    environment {
        SONARQUBE_ENV = 'SonarQube'
    }

    stages {

        // Checkout the source code from SCM
        stage('Checkout') {
            steps {
                checkout scm
                echo 'Checkout completed'
            }
        }

        // Clean the Maven project (delete target directory)
        stage('Clean') {
            steps {
                sh 'mvn clean'
                echo 'Clean done'
            }
        }

        // Compile the Java source code
        stage('Build') {
            steps {
                sh 'mvn compile'
                echo 'Build completed'
            }
        }

        // Run unit tests
        stage('Test') {
            steps {
                sh 'mvn test'
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                }
            }
        }

        // Package the project, skipping tests because they are already run
        stage('Package') {
            steps {
                sh 'mvn package -DskipTests'
                echo 'Packaging done'
            }
        }

        // Perform SonarQube static code analysis
        /* stage('SonarQube') {
            steps {
                withSonarQubeEnv(SONARQUBE_ENV) {
                    sh 'mvn sonar:sonar'
                }
                echo 'SonarQube analysis completed'
            }
        } */
    }

    post {
        success {
            echo 'Build succeeded!'
        }
        failure {
            echo 'Build failed!'
        }
    }
}
