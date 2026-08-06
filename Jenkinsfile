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
                echo "========== CHECKOUT =========="
                checkout scm
            }
        }

        stage('Compile') {
            steps {
                echo "========== COMPILE =========="
                sh 'mvn clean compile'
            }
        }

        stage('Unit Test') {
            steps {
                echo "========== UNIT TEST =========="
                sh 'mvn test'
            }
        }

        stage('SonarQube Analysis') {
            steps {
                echo "========== SONARQUBE ANALYSIS =========="
                withSonarQubeEnv('SonarQube') {
                    sh 'mvn clean verify sonar:sonar'
                }
            }
        }

        stage('Quality Gate') {
            steps {
                echo "========== QUALITY GATE =========="
                timeout(time: 2, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }

        stage('Package') {
            steps {
                echo "========== PACKAGE =========="
                sh 'mvn clean package -DskipTests'
            }
        }

        stage('Upload to Nexus') {
            steps {

                echo "========== UPLOAD TO NEXUS =========="

                withCredentials([
                    usernamePassword(
                        credentialsId: 'nexus-creds',
                        usernameVariable: 'NEXUS_USER',
                        passwordVariable: 'NEXUS_PASS'
                    )
                ]) {

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

echo "========== GENERATED SETTINGS.XML =========="
cat settings.xml

echo "========== DEPLOYING TO NEXUS =========="

mvn deploy -DskipTests -s settings.xml
'''
                }
            }
        }
    }

    post {

        success {
            echo "========== PIPELINE SUCCESS =========="
        }

        failure {
            echo "========== PIPELINE FAILED =========="
        }

        always {
            echo "========== PIPELINE FINISHED =========="
        }
    }
}