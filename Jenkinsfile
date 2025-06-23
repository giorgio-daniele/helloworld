def token  = null
def status = false

def postSBOM(api, key, uid, bomPath) {
    def res = sh(
        script: """
            curl -s -w '%{http_code}\\n' -X POST "$api" \\
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

def getFindings(api, key) {
    def res = sh(
        script: """
            curl -s -w '%{http_code}\\n' -X GET "$api"      \\
            -H "X-Api-Key: $key"                            \\
            -H "accept: application/json"
            """, returnStdout: true).trim()

    // Seperate code and body
    def httpCode   = res[-3..-1]
    def body       = res[0..-4]
    def parsedBody = readJSON(text: body)
    return [httpCode, parsedBody]
}

def getStatus(api, key) {
    def res = sh(
        script: """
            curl -s -w '%{http_code}\\n' -X GET "$api"      \\
            -H "X-Api-Key: $key"                            \\
            -H "accept: application/json"
            """, returnStdout: true).trim()

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
                    // POST the SBOM
                    withCredentials([string(credentialsId: "dtrack-backend-token", variable: "KEY")]) {
                        withEnv(["UID=e4368795-5409-4b60-bb9d-d448732becb0", "BOM=target/bom.xml"]) {
                            def (httpCode, parsedBody) = postSBOM("${BASE_API}/bom", env.KEY, env.UID, env.BOM)
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
                    def BASE_API = "http://dtrack-backend:8080/api/v1"
                    // GET the findings
                    withCredentials([string(credentialsId: "dtrack-backend-token", variable: "KEY")]) {
                        withEnv(["UID=e4368795-5409-4b60-bb9d-d448732becb0"]) {

                            // Await the report to be ready
                            while(status == false) {
                                def (httpCode, parsedBody) = getStatus("${BASE_API}/event/token/${env.UID}", env.KEY)
                                sleep(1000)
                                echo "${parsedBody}"
                            }

                            //def (httpCode, parsedBody) = getFindings("${BASE_API}/finding/project/${env.UID}", env.KEY)
                            //echo "${httpCode} ${parsedBody}"
                        }
                    }
                }
            }
        }

    }
}
