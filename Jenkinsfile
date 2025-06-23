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
        /* stage("Test") {
            steps {
                sh "mvn test"
            }
            post {
                always {
                    junit "target/surefire-reports/*.xml"
                }
            }
        }*/

        // Static Application Security Testing (SAST) using SonarScanner
        /*stage("SonarScanner") {
            steps {
                // Inject SonarQube configuration (URL and token)
                // "SonarQubeServer" is the name of the configured server in:
                // "Manage Jenkins" -> "Configure System" -> "SonarQube servers"
                withSonarQubeEnv("SonarQubeServer") {
                    sh "mvn sonar:sonar"
                }
            }
        }*/

        // Quality Gate check from SonarQube server
        /*stage("SonarQube Quality Gate") {
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
        }*/

        stage("Depedency-Track") {
            steps {

                script {

                    def apiURL   = "http://dtrack-backend:8080/api/v1"
                    def apiKey   = "odt_jnSed9yc_yLqy3n2NdVmBdAIIeMPFPAeerZWotCms"
                    def sbomPath = "target/bom.xml"
                    def projUUID = "e4368795-5409-4b60-bb9d-d448732becb0"

                    // Generate the SBOM
                    sh "mvn org.cyclonedx:cyclonedx-maven-plugin:makeAggregateBom"

                    try {
                        // POST it to Dependency track
                        withEnv(["URL=${apiURL}/bom", "KEY=${apiKey}", "BOM=${sbomPath}", "UID=${projUUID}"]) {
                            sh """
                                curl -s -X POST     "\$URL"                     \\
                                    -H "X-Api-Key:   \$KEY"                     \\
                                    -H "Content-Type: multipart/form-data"      \\
                                    -F "project=\$UID"                          \\
                                    -F "autocreate=true"                        \\
                                    -F "bom=@\$BOM" > http.body
                                echo \$? > http.code
                            """
                        }
                    } catch (Exception e) {
                        error "Something bad happened on posting!"
                    }

                    // Inspect the temporary files
                    def code = readFile('http.code').trim().toInteger()
                    echo "Code = ${code}"

                    // Read the body (JSON)
                    def body        = readFile('http.body').trim()
                    def parsedBody  = readJSON(text: body)
                    def token       = parsedBody["token"]
                    echo "Token = ${token}"

                    try {
                        // GET the list of findings
                        withEnv(["URL=${apiURL}finding/project/${projUUID}", "KEY=${apiKey}"]) {
                            sh """
                                curl -s -X GET      "\$URL"                     \\
                                    -H "X-Api-Key:   \$KEY"                     \\
                                    -H "accept: application/json" > http.body
                                echo \$? > http.code
                            """
                        }
                    } catch (Exception e) {
                        error "Something bad happened on getting findings!"
                    }


                }
            }
        }
    }
}
