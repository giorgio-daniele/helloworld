def httpCode   = null
def body       = null
def parsedBody = null
def token      = null

pipeline {
    agent any

    tools {
        maven "Maven"
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

        stage("SBOM") {
            steps {
                // Generate the SBOM
                sh "mvn org.cyclonedx:cyclonedx-maven-plugin:makeAggregateBom"
            }
        }

        stage("SBOM Upload") {
            steps {
                script {
                    try {
                        // Post the SBOM
                        withCredentials([string(credentialsId: "dtrack-backend-token", variable: "KEY")]) {
                            withEnv([
                                "API=http://dtrack-backend:8080/api/v1/bom",
                                "UID=e4368795-5409-4b60-bb9d-d448732becb0",
                                "BOM=target/bom.xml"
                            ]) {

                                // Run HTTP
                                def res = sh(
                                    script: '''
                                        curl                                    \
                                        -s -w '%{http_code}\n'                  \
                                        -X  POST "$API"                         \
                                        -H "X-Api-Key: $KEY"                    \
                                        -H "Content-Type: multipart/form-data"  \
                                        -F "project=$UID"                       \
                                        -F "autocreate=true"                    \
                                        -F "bom=@$BOM"
                                    ''', returnStdout: true).trim()

                                // Separate body and HTTP code
                                httpCode   = res[-3..-1]
                                body       = res[0..-4]
                                parsedBody = readJSON(text: body)
                                token      = parsedBody["token"]

                                echo "HTTP Code: ${httpCode}"
                                echo "Token: ${token}"
                            }
                        }
                    } catch (err) {
                        error "SBOM upload failed: ${err.getMessage()}"
                    }
                }
            }
        }

        stage("SBOM Findings") {
            steps {
                script {
                    try {
                        // GET findings associated to uploaded SBOM
                        withCredentials([string(credentialsId: "dtrack-backend-token", variable: "KEY")]) {
                            withEnv([
                                "API=http://dtrack-backend:8080/api/v1/finding/project/",
                                "UID=e4368795-5409-4b60-bb9d-d448732becb0",
                            ]) {

                                // Run HTTP
                                def res = sh(
                                    script: '''
                                        curl                                    \
                                        -s -w '%{http_code}\n'                  \
                                        -X  POST "$API/$UID"                    \
                                        -H "X-Api-Key: $KEY"                    \
                                        -H "accept: application/json"
                                    ''', returnStdout: true).trim()

                                // Separate body and HTTP code
                                echo "${res}"
                            }
                        }
                    } catch (err) {
                        error "SBOM upload failed: ${err.getMessage()}"
                    }
                }
            }
        }

    }
}
