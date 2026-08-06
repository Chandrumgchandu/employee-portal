pipeline {

    agent any

    tools {
        jdk 'JDK21'
        maven 'Maven'
    }

    environment {
        SCANNER_HOME = tool 'SonarScanner'
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Compile') {
            steps {
                sh 'mvn clean compile'
            }
        }

        stage('Unit Test') {
            steps {
                sh 'mvn test'
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('SonarQube') {
                    sh 'mvn clean verify sonar:sonar'
                }
            }
        }
        stage('Quality Gate') {

            steps {

                timeout(time: 2, unit: 'MINUTES') {

                    waitForQualityGate abortPipeline: true

                }

            }

        }

      stage('Package') {
    steps {
        sh 'mvn clean package -DskipTests'
    }
}

stage('Upload to Nexus') {
    steps {
        withCredentials([usernamePassword(
            credentialsId: 'nexus-creds',
            usernameVariable: 'NEXUS_USER',
            passwordVariable: 'NEXUS_PASS'
        )]) {

            sh '''
            cat > settings.xml <<EOF
                <settings>
                <servers>
                    <server>
                    <id>nexus-releases</id>
                    <username>$NEXUS_USER</username>
                    <password>$NEXUS_PASS</password>
                    </server>

                    <server>
                    <id>nexus-snapshots</id>
                    <username>$NEXUS_USER</username>
                    <password>$NEXUS_PASS</password>
                    </server>
                </servers>
                </settings>
                EOF

            mvn deploy -DskipTests -s settings.xml
            '''
        }
    }
}

    }

    post {

        success {

            echo 'Pipeline Success'

        }

        failure {

            echo 'Pipeline Failed'

        }

    }

}