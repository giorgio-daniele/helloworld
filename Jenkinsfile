pipeline {
    agent any

    tools {
        maven "Maven"
    }

    environment {
        API_URL      = "http://dtrack-backend:8080/api/v1/bom"
        API_KEY      = credentials("dtrack-backend-token")
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

        /*
        stage("Test Report") {
            steps {
                junit "target/surefire-reports/*.xml"
            }
        }*/

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
                        sh "mvn org.cyclonedx:cyclonedx-maven-plugin:makeAggregateBom"
                        sh 'ls -lh target/bom.xml || echo "BOM file missing!"'
                        withEnv([
                            "URL=${env.API_URL}",
                            "KEY=${env.API_KEY}",
                            "BOM=${env.SBOM_PATH}",
                            "UID=${env.PROJECT_UUID}"
                        ]) {
                            sh """
                                curl -s -X POST    "\$URL"                 \\
                                    -H "X-Api-Key:  \$KEY"                 \\
                                    -H "Content-Type: multipart/form-data" \\
                                    -F "project=\$UID"                     \\
                                    -F "autocreate=true"                   \\
                                    -F "bom=@\$BOM" > http.body
                                echo \$? > http.code
                            """
                        }

                        def code = readFile('http.code').trim().toInteger()
                        if (code != 0) {
                            error "Dependency Track POST failed with code: ${code}"
                        }

                        def body = readFile('http.body').trim()
                        echo "Response body: ${body}"

                        //def body       = readFile('http.body').trim()
                        //def parsedBody = readJSON(text: body)
                        //def token      = parsedBody["token"]
                        //echo "Dependency Track Token: ${token}"

                    } catch (err) {
                        error "SBOM upload failed: ${err.getMessage()}"
                    }
                }
            }
        }
    }
}
