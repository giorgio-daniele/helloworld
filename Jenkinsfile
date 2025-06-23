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

        stage("SBOM Upload") {
            steps {
                script {
                    try {
                        // Genera l'SBOM
                        sh "mvn org.cyclonedx:cyclonedx-maven-plugin:makeAggregateBom"

                        withCredentials([string(credentialsId: "dtrack-backend-token", variable: "API_KEY")]) {
                            // Esegue curl e cattura sia lo status code che il body
                            def response = sh(
                                script: """
                                    curl -s -w "\\n%{http_code}" -X POST "${API_URL}" \\
                                        -H "X-Api-Key: ${API_KEY}" \\
                                        -H "Content-Type: multipart/form-data" \\
                                        -F "project=${PROJECT_UUID}" \\
                                        -F "autocreate=true" \\
                                        -F "bom=@${SBOM_PATH}"
                                """,
                                returnStdout: true
                            ).trim()

                            // Estrae body e status code
                            def lines = response.readLines()
                            def httpCode = lines[-1] as Integer
                            def body = lines.init().join("\n")

                            if (httpCode < 200 || httpCode >= 300) {
                                error "SBOM upload failed. HTTP ${httpCode}: ${body}"
                            }

                            echo "SBOM uploaded successfully. Response body:\n${body}"

                            // (Facoltativo) Parsing JSON se body è strutturato
                            /*
                            if (body.startsWith("{")) {
                                def parsed = readJSON text: body
                                echo "Dependency Track token: ${parsed.token}"
                            }
                            */
                        }
                    } catch (err) {
                        error "SBOM upload failed: ${err.getMessage()}"
                    }
                }
            }
        }
    }
}
