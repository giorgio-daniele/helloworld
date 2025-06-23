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
        stage("Test") {
            steps {
                sh "mvn test"
            }
            post {
                always {
                    junit "target/surefire-reports/*.xml"
                }
            }
        }

        // Static Application Security Testing (SAST) using SonarScanner
        stage("SonarScanner") {
            steps {
                // Inject SonarQube configuration (URL and token)
                // "SonarQubeServer" is the name of the configured server in:
                // "Manage Jenkins" -> "Configure System" -> "SonarQube servers"
                withSonarQubeEnv("SonarQubeServer") {
                    sh "mvn sonar:sonar"
                }
            }
        }

        // Quality Gate check from SonarQube server
        stage("SonarQube Quality Gate") {
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
        }

        stage("Depedency-Track") {
            steps {
                sh "mvn org.cyclonedx:cyclonedx-maven-plugin:makeAggregateBom"
                script {

                    /* Define the parameters to use in the CURL operation */

                    def apiURL   = "http://dtrack-backend:8080/api/v1/bom"
                    def apiKey   = "odt_jnSed9yc_yLqy3n2NdVmBdAIIeMPFPAeerZWotCms"
                    def sbomPath = "target/bom.xml"
                    def projName = "helloworld"
                    def projUUID = "e4368795-5409-4b60-bb9d-d448732becb0"
                    def projVers = "1.0"

                    /* Define the context for the shell execution */

                    withEnv([
                        "API_URL=${apiURL}",
                        "API_KEY=${apiKey}",
                        "SBOM_PATH=${sbomPath}",
                        "PROJ_UUID=${projUUID}"
                    ]) {
                        sh '''
                            curl -s -X POST "$API_URL"                         \
                                -H "X-Api-Key: $API_KEY"                       \
                                -H "Content-Type: multipart/form-data"         \
                                -F "project=$PROJ_UUID"                        \
                                -F "autocreate=true"                           \
                                -F "bom=@$SBOM_PATH" > http.body 2>&1
                            echo $? > http.code
                        '''
                    }

                    def jsonSlurper = new JsonSlurper()
                    def body        = readFile('http.body').trim()
                    def parsedBody  = jsonSlurper.parseText(body)
                    def token       = parsedBody.token
                    def code        = readFile('http.code').trim().toInteger()

                    echo "Token: \n${token}"
                    echo "Code:  \n${code}"
                }
            }
        }
    }
}
