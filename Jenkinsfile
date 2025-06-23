/* Jenkins file configurationg for a pipeline */

pipeline{
    agent any
    tools {
        maven "Maven"
    }
    stages{
        
        // Checkout che code
        stage("Checkout"){
            steps {
                checkout scm
            }
        }

        // Clean the project
        stage("Clean"){
            steps {
                sh "mvn clean verify"
            }
        }

        // Build the project
        stage("Build"){
            steps {
                sh "mvn compile"
            }
        }

        // Test the project
        stage("Test"){
            steps {
                sh "mvn test"
            }
            post {
                always {
                    junit "target/surefire-reports/*.xml"
                }
            }
        }
        
        // Static Application Security Testing - SAST
        stage("SonarScanner"){
            steps {
                withSonarQubeEnv("SonarQubeServer") {
                    sh "mvn sonar:sonar"
                }
            }
        }

        // Quality Gate - from SonarQube server
        stage("SonarQube"){
            script {
                try {
                    waitForQualityGate abortPipeline: true
                } catch (err) {
                    error "EoP. Quality gate has failed"
                }
            }
        }
    }
}