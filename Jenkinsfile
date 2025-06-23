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
                // Inject SonarQube configuration - the URL and the token
                // "SonarQubeServer" is the name of the configuration you
                // find browsing at:
                // "Manage Jenkins" -> "Configure System" -> "SonarQube servers"
                withSonarQubeEnv("SonarQubeServer") {
                    sh "mvn sonar:sonar"
                }
            }
        }

        // Quality Gate - from SonarQube server
        stage("SonarQube"){
            steps {
                timeout(time: 5, unit: "MINUTES") {
                def qg = waitForQualityGate()
                echo qg
                if (qg.status != "OK") {
                    error "Pipeline aborted due to quality gate failure: ${qg.status}"
                }
            }
            }
        }
    }
}