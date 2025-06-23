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

        /*
        * Attendiamo risposta dal server SonarQube
        * sull'esito dell'analisi. Dal momento che
        * il server Jenkins non sa nulla del server
        * SonarQube (di fatto è una macchina remota),
        * il server Jenkins dovrà esporre un webhook
        *
        * Il weebhook è genere un endpoint sul quale
        * un process remoto esegue una POST. Quando
        * si riceve la POST, si riceva la notifica e
        * quindi il server che la riceve può sincro
        * nizzarsi con il processo remoto.
        */

        stage("Quality Gate") {
            steps {
                timeout(time: 10, unit: 'MINUTES') {
                    script {
                        def qg = waitForQualityGate()
                        if (qg.status != 'OK') {
                            error "Pipeline failed due to Quality Gate failure: ${qg.status}"
                        } else {
                            echo "Quality Gate passed: ${qg.status}"
                        }
                    }
                }
            }
        }

        /*
        * Se il quality gate è attraversato con successo,
        * allora il codice può continuare la pipeline. Per
        * esempio, si può generare lo SBOM (Software Bill
        * of Materials), ossia la lista delle dipendenze
        * associate al codice sorgente che possono essere
        * obsolete, possono contenere delle vulnerabilità
        * note
        * 
        */

        stage('BOM generation') {
            steps {
                sh   'mvn org.cyclonedx:cyclonedx-maven-plugin:makeAggregateBom'
                echo 'BOM generated'
            }
        }


        /* A questo  */

        // stage('Package') {
        //    steps {
        //        sh   'mvn package -DskipTests'
        //        echo 'Packaging done'
        //    }
        // }

        stage('Upload SBOM') {
            steps {
                withCredentials([string(credentialsId: 'dtrack-token', variable: 'DTRACK_API_KEY')]) {
                    script {
                        def projectUUID = '3bcf7641-d64e-4a64-84b3-5bea0cdebe13'
                        def dtrackUrl   = 'http://dtrack-apiserver:8080/api/v1/bom'
                        def sbomPath    = 'target/bom.xml'

                        if (!fileExists(sbomPath)) {
                            error "SBOM file not found at ${sbomPath}"
                        }

                        sh """
                            curl -X POST "${dtrackUrl}" \\
                                -H "X-Api-Key: ${DTRACK_API_KEY}" \\
                                -H "Content-Type: multipart/form-data" \\
                                -F "project=${projectUUID}" \\
                                -F "bom=@${sbomPath}" 
                        """
                    }
                }
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
