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

        stage("SBOM Upload") {
            steps {
                script {
                    try {
                        // Generate the SBOM
                        sh "mvn org.cyclonedx:cyclonedx-maven-plugin:makeAggregateBom"

                        // Post the SBOM
                        withCredentials([string(credentialsId: "dtrack-backend-token", variable: "KEY")]) {
                            withEnv([
                                "API=http://dtrack-backend:8080/api/v1/bom",
                                "UID=e4368795-5409-4b60-bb9d-d448732becb0",
                                "BOM=target/bom.xml"
                            ]) {
                                def res = sh(
                                    script: '''
                                        curl                                    \
                                        -s -w '%{http_code}\n'                  \
                                        -X  POST      "$API"                    \
                                        -H "X-Api-Key: $KEY"                    \
                                        -H "Content-Type: multipart/form-data"  \
                                        -F "project=$UID"                       \
                                        -F "autocreate=true"                    \
                                        -F "bom=@$SBOM_PATH"
                                    ''', returnStdout: true).trim()
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
