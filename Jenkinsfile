pipeline {
    agent {
        docker {
            image 'adoptopenjdk/openjdk11:jdk-11.0.8_10-alpine'
            args '-v /home/jenkins/.gradle:/root/.gradle'
        }
    }
    triggers { cron(env.BRANCH_NAME == "master" ? "H 3 * * *" : "") }
    stages {
        stage("Presteps") {
            steps {
                sh 'chmod +x gradlew'
                sh './gradlew clean'
                sh 'printenv'
            }
        }
        stage('Build') {
            steps {
                sh './gradlew shadowJar'
            }
        }
        stage('Test') {
            steps {
                sh './gradlew test'
            }
        }
        stage('Verify') {
            steps {
                sh './gradlew spotbugsMain'
                sh './gradlew dependencyCheckAnalyze'
            }
        }
    }
    post {
        always {
            archiveArtifacts artifacts: 'build/libs/**/*.jar,build/reports/**/*.html', fingerprint: true
            junit 'build/reports/**/*.xml'
        }
        cleanup {
            cleanWs()
        }
    }
}

