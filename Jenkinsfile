pipeline {
    agent any

    tools {
        maven "Maven"
    }

    environment {
        API_URL      = "http://dtrack-backend:8080/api/v1/bom"
        SBOM_PATH    = "target/bom.xml"
        PROJECT_UUID = "e4368795-5409-4b60-bb9d-d448732becb0"
    }

    stages {
        stage("Checkout") {
            steps {
                checkout scm
            }
        }

        stage("Build and Verify") {
            steps {
                sh "mvn clean verify"
            }
        }

        // Abilita questo stage se vuoi includere i test report
        /*
        stage("Test Report") {
            steps {
                junit "target/surefire-reports/*.xml"
            }
        }
        */

        // Se vuoi usare SonarQube:
        /*
        stage("Static Analysis - SonarQube") {
            steps {
                withSonarQubeEnv("SonarQubeServer") {
                    sh "mvn sonar:sonar"
                }
            }
        }

        stage("Quality Gate") {
            steps {
                timeout(time: 5, unit: 'MINUTES') {
                    script {
                        def qg = waitForQualityGate()
                        if (qg.status != 'OK') {
                            error "Quality Gate failed: ${qg.status}"
                        }
                    }
                }
            }
        }
        */

        stage("SBOM Upload") {
            steps {
                script {
                    try {
                        // Generate the SBOM
                        sh "mvn org.cyclonedx:cyclonedx-maven-plugin:makeAggregateBom"

                        // Post the SBOM
                        withCredentials([string(credentialsId: "dtrack-backend-token", variable: "API_KEY")]) {
                            def res = sh(
                                script: '''
                                    echo "API_KEY length: ${API_KEY.length()}"
                                    curl -s -w '%{http_code}\n' -X POST "$API_URL" \
                                    -H "X-Api-Key:   $API_KEY"                     \
                                    -H "Content-Type: multipart/form-data"         \
                                    -F "project=$PROJECT_UUID"                     \
                                    -F "autocreate=true"                           \
                                    -F "bom=@$SBOM_PATH"
                                ''',
                                returnStdout: true,
                                environment:  [
                                    API_URL:      "http://dtrack-backend:8080/api/v1/bom",
                                    PROJECT_UUID: "e4368795-5409-4b60-bb9d-d448732becb0",
                                    SBOM_PATH:    "target/bom.xml"
                                ]).trim()

                            // Parse the reply
                            echo "${res}"
                        }
                    } catch (err) {
                        error "SBOM upload failed: ${err.getMessage()}"
                    }
                }
            }
        }
    }
}
