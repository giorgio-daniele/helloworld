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
                        if (qg.status != "OK") {
                            error "Pipeline aborted due to quality gate failure"
                        }
                    }
                }
            }
        }

        stage("Depedency-Track") {
            steps {
                sh "mvn org.cyclonedx:cyclonedx-maven-plugin:makeAggregateBom"
                script {

                    /* Define the parameters to use in the CURL operation */

                    def apiURL   = "http://dtrack-backend:8080/api/v1/bom"
                    def apiKey   = "odt_jnSed9yc_yLqy3n2NdVmBdAIIeMPFPAeerZWotCms"
                    def sbomPath = "target/bom.xml"
                    def projName = "helloworld"
                    def projUUID = "e4368795-5409-4b60-bb9d-d448732becb0"
                    def projVers = "1.0"

                    /* Use HTTP to request the API server to process the SBOM */

                    def res = sh (
                        script: """
                            #!/bin/bash
                            curl -X POST ${apiURL}                         \\
                                    -H "X-Api-Key: ${apiKey}"              \\
                                    -H "Content-Type: multipart/form-data" \\
                                    -F "project=${projUUID}"               \\
                                    -F "autocreate=true"                   \\
                                    -F "bom=@${sbomPath}"
                            """,
                            returnStdout: true,
                            returnStatus: true,
                        )

                        /* Analyze the returing value and the body */
                        echo "Curl command exit status: ${res.exitStatus}"
                        echo "Curl command stdout: ${res.stdout}"
                }
            }
        }
    }
}
