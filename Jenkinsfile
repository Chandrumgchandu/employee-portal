pipeline {

    agent any

    tools {
        jdk 'JDK21'
        maven 'Maven'
    }

    environment {

        SCANNER_HOME = tool 'SonarScanner'

        IMAGE_NAME = "employee-portal"
        IMAGE_TAG  = "${BUILD_NUMBER}"

        AWS_REGION = "ap-south-1"
        AWS_ACCOUNT_ID = "849996548389"

        ECR_REPOSITORY = "employee-portal"

        ECR_URI = "${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com/${ECR_REPOSITORY}"
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
                echo "========== SONAR ANALYSIS =========="
                withSonarQubeEnv('SonarQube') {
                    sh 'mvn clean verify sonar:sonar'
                }
            }
        }

        stage('Quality Gate') {
            steps {
                echo "========== QUALITY GATE =========="
                timeout(time: 6, unit: 'MINUTES') {
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

mvn deploy -DskipTests -s settings.xml
'''
                }
            }
        }

        stage('Docker Build') {
            steps {

                echo "========== BUILD DOCKER IMAGE =========="

                sh """
                    docker build -t ${IMAGE_NAME}:${IMAGE_TAG} .
                    docker images
                """
            }
        }

        stage('Trivy Scan') {
            steps {

                echo "========== TRIVY IMAGE SCAN =========="

                sh """
                    trivy image \
                    --severity HIGH,CRITICAL \
                    --exit-code 0 \
                    ${IMAGE_NAME}:${IMAGE_TAG}
                """
            }
        }

        stage('Login to AWS ECR') {
            steps {

                echo "========== LOGIN TO ECR =========="

                withCredentials([
                    [$class: 'AmazonWebServicesCredentialsBinding',
                    credentialsId: 'aws-creds']
                ]) {

                    sh """
                    aws ecr get-login-password --region ${AWS_REGION} | \
                    docker login \
                    --username AWS \
                    --password-stdin \
                    ${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com
                    """
                }
            }
        }

        stage('Push Image to ECR') {
        steps {

            echo "========== PUSH IMAGE TO ECR =========="

            sh """
                docker tag ${IMAGE_NAME}:latest ${ECR_URI}:${IMAGE_TAG}

                docker push ${ECR_URI}:${IMAGE_TAG}
            """
        }
    }
    stage('Refresh ECR Secret') {
    steps {

        echo "========== REFRESH ECR SECRET =========="

        withCredentials([
            [$class: 'AmazonWebServicesCredentialsBinding',
            credentialsId: 'aws-creds']
        ]) {

            sh """
                kubectl delete secret ecr-secret \
                -n employee-portal --ignore-not-found

                kubectl create secret docker-registry ecr-secret \
                --docker-server=${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com \
                --docker-username=AWS \
                --docker-password=\$(aws ecr get-login-password --region ${AWS_REGION}) \
                -n employee-portal
            """
        }
    }
}

stage('Deploy to Kubernetes') {
    steps {

        echo "========== DEPLOY TO KUBERNETES =========="

        sh """
            kubectl set image deployment/employee-portal \
            employee-portal=${ECR_URI}:${IMAGE_TAG} \
            -n employee-portal

            kubectl rollout status deployment/employee-portal \
            -n employee-portal --timeout=180s

            echo "========== DEPLOYMENT =========="

            kubectl get deployment employee-portal -n employee-portal

            echo "========== PODS =========="

            kubectl get pods -n employee-portal -o wide

            echo "========== SERVICES =========="

            kubectl get svc -n employee-portal
        """
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

            echo "========== CLEANUP =========="

            sh '''
            docker image prune -f || true
            rm -f settings.xml || true
            '''

            cleanWs()

            echo "========== PIPELINE FINISHED =========="
        }
    }
}