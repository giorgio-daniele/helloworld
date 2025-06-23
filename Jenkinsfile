/* Jenkins file configurationg for a pipeline */

pipeline{
    agent any
    tools {
        maven "Maven"
    }
    stages{
        stage("Checkout"){
            steps {
                checkout scm
            }
        }
        stage("Clean"){
            steps {
                sh "mvn clean verify"
            }
        }
        stage("Build"){
            steps {
                sh "mvn compile"
            }
        }
        stage("Test"){
            steps {
                sh "mvn test"
            }
            post {
                always {
                    junit "target/surefire-reports/*.xml"
                }
            }
        }
    }
}