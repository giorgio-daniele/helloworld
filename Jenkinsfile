/* Jenkins file configurationg for a pipeline */

pipeline{
    agent any
    stages{
        stage("Checkout"){
            steps {
                checkout scm
            }
        }
        stage("Clean"){
            steps {
                withMaven {
                    sh "mvn clean verify"
                }
            }
        }
        stage("Build"){
            steps {
                withMaven {
                    sh "mvn compile"
                }
            }
        }
        stage("Test"){
            steps {
                withMaven {
                    sh "mvn test"
                }
            }
            post {
                always {
                    junit "target/surefire-reports/*.xml"
                }
        
            }
        }
    }
}