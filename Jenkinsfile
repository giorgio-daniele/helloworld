// Definizione della pipeline. Tutto il flusso di 
// build, test e analisi avverrà qui dentro.
pipeline {

    /* 
    * L'agente definisce su quale nodo Jenkins
    * eseguire la pipeline. Possiamo userare
    * la parola chiave "any" per indicare che
    * qualsiasi maccchina (incluso il controller)
    * può svolgere questa attività; al contrario,
    * attraverso un sistema di etichettatura,
    * è possibile generare un meccanismo di
    * selezione simile a quello esistente in
    * ambiente Kubernetes 
    */
 
    // agent { label 'debian' }
    agent any


    /* 
    * Jenkins è pensato per supportare qualsiasi
    * toolchain di compilazione (per i linguaggi
    * che sono compilati). Questo significa che
    * gli strumenti per compilazione, il testing
    * e la pacchettizzazione possono cambiare da
    * contesto a contesto. Per questo motivo,
    * nella sezione che segue, di norma, si indicano
    * gli strumenti impegnati per tutta la pipeline.
    *
    * In questo caso, trattandosi di una pipeline
    * Java, il tool necessario alla compilazione,
    * al testing ed infine alla produzione di un
    * artefatto è "maven"
    */
 
    tools {
        maven "MavenTooling"
        // jdk "JDK11"
    }

    /* Variabili d'ambiente accessibili ad ogni stadio */
    environment {
        SONARQUBE_ENV = 'SonarQube'
    }


    stages {

        // Preleva il codice dal sistema di versionamento
        stage('Checkout') {
            steps {
                checkout scm
                echo 'Checkout completed'
            }
        }

        // Pulisce i file di compilazioni precedenti
        stage('Clean') {
            steps {
                sh   'mvn clean'
                echo 'Clean done'
            }
        }

        // Compila il codice Java sorgente
        stage('Build') {
            steps {
                sh   'mvn compile'
                echo 'Build completed'
            }
        }

        /*
         * Con il comando mvn test, si chiede a
         * Maven di eseguire sia la compilazione
         * che i test.
         *
         * L'esito dei test può essere positivo
         * o negativo (come qualsiasi stage della
         * pipeline).
         *
         * Se i test falliscono, non è possibile
         * transitare allo stadio successivo nel
         * ciclo di vita del software - rilascio
         */


        stage('Test') {
            steps {
                sh 'mvn test'
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                }
            }
        }

        stage('Package') {
            steps {
                sh   'mvn package -DskipTests'
                echo 'Packaging done'
            }
        }

        /*
        * Con l'installazione del SonarScannar,
        * Jenkins ha a disposizione un plugin che
        * gli consente di effettuare analisi
        * statica del codice.
        * 
        * Lo scanner agisce sul nodo di agent 
        * sul quale è in esecuzione. Il server 
        * SonarQube serve solo ad interpretare 
        * i risultati ottenuti dallo scanner, 
        * strutturandoli in formato che sia 
        * comprensibile per un team di 
        * sviluppatori. Inoltre, in base alla
        * configurazione utilizzata su SonarQube
        * il risultato ottenuto dallo scanner
        * può far fallire gli stages successivi
        *
        */

        stage('SonarQube') {
            steps {
                withSonarQubeEnv(SONARQUBE_ENV) {
                    sh 'mvn sonar:sonar'
                }
                echo 'SonarQube analysis completed'
            }
        }
    }

    
    post {
        success {
            echo 'Build succeeded!'
        }
        failure {
            echo 'Build failed!'
        }

        // Sempre eseguito, utile per cleanup
        // always {
        //     cleanWs() // Pulisce workspace
        // }
    }
}
