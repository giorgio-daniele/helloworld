// Jenkinsfile configuration for a pipeline

pipeline {
    agent any

    tools {
        maven "Maven"
    }

    stages {

        // Checkout the code
        stage("Checkout") {
            steps {
                checkout scm
            }
        }

        // Clean and verify the project
        stage("Clean") {
            steps {
                sh "mvn clean verify"
            }
        }

        // Build the project
        stage("Build") {
            steps {
                sh "mvn compile"
            }
        }

        // Test the project
        stage("Test") {
            steps {
                sh "mvn test"
            }
            post {
                always {
                    junit "target/surefire-reports/*.xml"
                }
            }
        }

        // Static Application Security Testing (SAST) using SonarScanner
        stage("SonarScanner") {
            steps {
                // Inject SonarQube configuration (URL and token)
                // "SonarQubeServer" is the name of the configured server in:
                // "Manage Jenkins" -> "Configure System" -> "SonarQube servers"
                withSonarQubeEnv("SonarQubeServer") {
                    sh "mvn sonar:sonar"
                }
            }
        }

        // Quality Gate check from SonarQube server
        stage("SonarQube Quality Gate") {
            steps {
                timeout(time: 5, unit: 'MINUTES') {
                    script {
                        def qg = waitForQualityGate()
                        echo "SonarQube Quality Gate status: ${qg}"
                        if (qg.status != "OK") {
                            error "Pipeline aborted due to quality gate failure"
                        }
                    }
                }
            }
        }
    }
}
