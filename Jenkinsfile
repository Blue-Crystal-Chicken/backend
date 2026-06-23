pipeline {
    agent any

    options {
        timeout(time: 1, unit: 'HOURS')
        buildDiscarder(logRotator(numToKeepStr: '10'))
        disableConcurrentBuilds()
    }

    stages {
        stage('Inizializzazione') {
            steps {
                echo '=== Inizializzazione della Pipeline Backend ==='
                sh 'java -version'
            }
        }

        stage('Build') {
            steps {
                echo '=== Compilazione Backend API (Spring Boot) ==='
                sh 'chmod +x mvnw'
                sh './mvnw clean package -DskipTests'
            }
        }

        stage('Build Immagine Docker') {
            steps {
                echo '=== Creazione immagine Docker ==='
                sh 'docker build -t ghcr.io/blue-crystal-chicken/bcc-backend:latest .'
            }
        }

        stage('Push GHCR') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'github-bcc', usernameVariable: 'USER', passwordVariable: 'PASS')]) {
                    sh 'echo $PASS | docker login ghcr.io -u $USER --password-stdin'
                    sh 'docker push ghcr.io/blue-crystal-chicken/bcc-backend:latest'
                }
            }
        }
    }

    post {
        success { echo '=== Backend: pipeline completata con successo! ===' }
        failure { echo '=== Backend: errore durante la pipeline ===' }
    }
}