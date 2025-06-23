def token = null

def postSBOM(api, key, uid, bomPath) {
    def res = sh(
        script: """
            curl -s                                     \\
                -w '%{http_code}\\n'                    \\
                -X POST "$api"                          \\
                -H "X-Api-Key: $key"                    \\
                -H "Content-Type: multipart/form-data"  \\
                -F "project=$uid"                       \\
                -F "autocreate=true"                    \\
                -F "bom=@$bomPath"
            """, 
            returnStdout: true).trim()

    // Seperate code and body
    def httpCode   = res[-3..-1]
    def body       = res[0..-4]
    def parsedBody = readJSON(text: body)
    return [httpCode, parsedBody]
}

def getFindings(api, key, uid) {
    def res = sh(
        script: """
            curl -s                                 \\
                -w '%{http_code}\\n'                \\
                -X GET "$api/$uid"                  \\
                -H "X-Api-Key: $key"                \\
                -H "accept: application/json"
            """, 
            returnStdout: true).trim()

    // Seperate code and body
    def httpCode   = res[-3..-1]
    def body       = res[0..-4]
    def parsedBody = readJSON(text: body)
    return [httpCode, parsedBody]
}


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
                    // POST the SBOM
                    withCredentials([string(credentialsId: "dtrack-backend-token", variable: "KEY")]) {
                        withEnv(["API=http://dtrack-backend:8080/api/v1/bom", "UID=e4368795-5409-4b60-bb9d-d448732becb0", "BOM=target/bom.xml"]) {
                            def (httpCode, parsedBody) = postSBOM(env.API, env.KEY, env.UID, env.BOM)
                            echo "HTTP Code: ${httpCode}"
                            echo "Token: ${parsedBody.token}"
                            token = parsedBody.token
                        }
                    }
                }
            }
        }

        stage("SBOM Findings") {
            steps {
                script {
                    // GET the findings
                    withCredentials([string(credentialsId: "dtrack-backend-token", variable: "KEY")]) {
                        withEnv(["API=http://dtrack-backend:8080/api/v1/findings", "UID=e4368795-5409-4b60-bb9d-d448732becb0"]) {
                            def (httpCode, parsedBody) = getFindings(env.API, env.KEY, env.UID)
                            /*echo "HTTP Code: ${httpCode}"
                            echo "Token: ${parsedBody.token}"
                            token = parsedBody.token*/
                        }
                    }
                }
            }
        }

        /*
        stage("SBOM Findings") {
            steps {
                script {
                    try {
                        // GET findings associated to uploaded SBOM
                        withCredentials([string(credentialsId: "dtrack-backend-token", variable: "KEY")]) {
                            withEnv([
                                "API=http://dtrack-backend:8080/api/v1/finding/project",
                                "UID=e4368795-5409-4b60-bb9d-d448732becb0",
                            ]) {

                                // Run HTTP
                                def res = sh(
                                    script: '''
                                        curl                                    \
                                        -s -w '%{http_code}\n'                  \
                                        -X  GET "$API/$UID"                     \
                                        -H "X-Api-Key: $KEY"                    \
                                        -H "accept: application/json"
                                    ''', returnStdout: true).trim()

                                // Separate body and HTTP code
                                def httpCode   = res[-3..-1]
                                def body       = res[0..-4]
                                def parsedBody = readJSON(text: body)
                            }
                        }
                    } catch (err) {
                        error "SBOM findings failed: ${err.getMessage()}"
                    }
                }
            }
        }*/

    }
}
