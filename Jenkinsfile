def postSBOM(api, key, uid, bomPath) {
    def res = sh(
        script: 
            """
            #!/bin/bash
            curl -s -w '%{http_code}\\n' -X POST "$api" \\
                -H "X-Api-Key: $key"                    \\
                -H "Content-Type: multipart/form-data"  \\
                -F "project=$uid"                       \\
                -F "autocreate=true"                    \\
                -F "bom=@$bomPath"
            """, 
            returnStdout: true).trim()

    // Seperate code and body
    def code       = res[-3..-1]
    def body       = res[0..-4].trim()
    return [code, body]
}

def get(api, key) {
    def res = sh(script: 
            """
            #!/bin/bash
            curl -s -w '%{http_code}\\n' -X GET "$api"      \\
            -H "X-Api-Key: $key"                            \\
            -H "Accept: application/json"
            """, 
            returnStdout: true).trim()

    // Seperate code and body
    def code       = res[-3..-1]
    def body       = res[0..-4].trim()
    return [code, body]
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

        /*
        stage("Test Report") {
            steps {
                junit "target/surefire-reports/*.xml"
            }
        }


        stage("Static Analysis - SonarQube") {
            steps {
                withSonarQubeEnv("SonarQubeServer") {
                    sh "mvn sonar:sonar"
                }
            }
        }

        stage("Quality Gate - SonarQube" ) {
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
        }*/

        stage("SBOM Creation") {
            steps {
                // Generate the SBOM
                sh "mvn org.cyclonedx:cyclonedx-maven-plugin:makeAggregateBom"
            }
        }

        stage("SBOM Upload") {
            steps {
                script {
                    def BASE_API = "http://dtrack-backend:8080/api/v1"

                    withCredentials([string(credentialsId: "dtrack-backend-token", variable: "KEY")]) {
                        withEnv(["UID=e4368795-5409-4b60-bb9d-d448732becb0", "BOM=target/bom.xml"]) {                           
                            try {
                                def (code, bodyStr) = postSBOM("${BASE_API}/bom", env.KEY, env.UID, env.BOM)
                                def parsedBody   = readJSON(text: bodyStr)
                                writeFile(file: "token.data", text: parsedBody.token)
                            } catch (Exception  e) {
                                error "${e}"
                            }
                        }
                    }
                }
            }
        }

        stage("SBOM Findings") {
            steps {
                script {
                    def BASE_API = "http://dtrack-backend:8080/api/v1"

                    withCredentials([string(credentialsId: "dtrack-backend-token", variable: "KEY")]) {
                        withEnv(["UID=e4368795-5409-4b60-bb9d-d448732becb0"]) {
  
                            try {
                                def proc = true;
                                while(proc) {
                                    def (code, bodyStr) = get("${BASE_API}/event/token/${env.UID}", env.KEY)
                                    def body = readJSON(text: bodyStr)
                                    proc = body["proc"]
                                    if (proc) {
                                        sleep(time: 5, unit: "SECONDS")
                                    }
                                }

                                // Get the findings
                                def (code, body) = get("${BASE_API}/finding/project/${env.UID}", env.KEY)
                                echo "${body}"
                            } catch (Exception  e) {
                                error "${e}"
                            }
                        }
                    }
                }
            }
        }

    }
}
