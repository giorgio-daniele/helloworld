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
                        
                        // Use proper Groovy string interpolation instead of shell variables
                        sh """
                            curl -s -X POST    "${env.API_URL}"        \\
                                -H "X-Api-Key:  ${env.API_KEY}"        \\
                                -H "Content-Type: multipart/form-data" \\
                                -F "project=${env.PROJECT_UUID}"       \\
                                -F "autocreate=true"                   \\
                                -F "bom=@${env.SBOM_PATH}"             \\
                                -w "%{http_code}"                      \\
                                -o http.body > http.code
                        """
                        
                        // Read the HTTP status code properly
                        def httpCode = readFile('http.code').trim().toInteger()
                        if (httpCode < 200 || httpCode >= 300) {
                            def body = readFile('http.body').trim()
                            error "Dependency Track POST failed with HTTP code: ${httpCode}. Response: ${body}"
                        }
                        
                        def body = readFile('http.body').trim()
                        echo "Response body: ${body}"
                        
                        // Optional: Parse JSON response if needed
                        /*
                        if (body && body.startsWith('{')) {
                            def parsedBody = readJSON(text: body)
                            def token = parsedBody["token"]
                            echo "Dependency Track Token: ${token}"
                        }
                        */
                        
                    } catch (Exception err) {
                        error "SBOM upload failed: ${err.getMessage()}"
                    } finally {
                        // Clean up temporary files
                        sh 'rm -f http.code http.body || true'
                    }
                }
            }
        }
    }
}