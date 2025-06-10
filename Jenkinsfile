pipeline {
    agent any

    environment {
        PROJECT_NAME = 'route33-service'

        IMAGE_NAME = "${PROJECT_NAME}"
        IMAGE_TAG = '1.0'

        ECR_REPO = "${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com/${IMAGE_NAME}"
        ECR_IMAGE_TAG = "${IMAGE_TAG}"

        AWS_ACCOUNT_ID = '982081072642'
        AWS_REGION = 'ap-northeast-2'
        SECRET_NAME = 'spring/backend/application-properties'

        JAVA_HOME = '/usr/lib/jvm/java-17-openjdk-amd64'
        MAVEN_HOME = '/usr/share/maven'
        PATH = "${JAVA_HOME}/bin:${MAVEN_HOME}/bin:${env.PATH}"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Check Branch') {
            when {
                expression {
                    return env.BRANCH_NAME == 'dev'
                }
            }
            steps {
                echo "Building dev branch..."
            }
        }

        stage('Fetch application.properties from AWS Secrets Manager') {
          steps {
            script {
              def resourceDir = "src/main/resources"
              def secretPath = "${resourceDir}/application.properties"

              sh """
                echo "[INFO] Creating resources directory if it doesn't exist..."
                mkdir -p ${resourceDir}

                echo "[INFO] Fetching secret from AWS Secrets Manager..."
                aws secretsmanager get-secret-value \
                  --region ${AWS_REGION} \
                  --secret-id ${SECRET_NAME} \
                  --query SecretString \
                  --output text > ${secretPath}

                echo "[INFO] application.properties written to ${secretPath}"
              """
            }
          }
        }

        stage('Build') {
            steps {
                sh "mvn clean install -DskipTests"
            }
        }

        stage('Build Docker Image') {
            steps {
                script {
                    def jarName = sh(script: "ls target/*.jar | grep -v 'original' | head -n 1", returnStdout: true).trim()
                    sh """
                        docker build -t ${IMAGE_NAME}:${IMAGE_TAG} \
                            --build-arg JAR_FILE=${jarName} .
                    """
                }
            }
        }

        stage('Push Docker Image to ECR') {
            steps {
                sh '''
                    echo "[INFO] Logging into AWS ECR..."
                    aws ecr get-login-password --region $AWS_REGION | docker login --username AWS --password-stdin $ECR_REPO

                    echo "[INFO] Tagging Docker image..."
                    docker tag ${IMAGE_NAME}:${IMAGE_TAG} ${ECR_REPO}:${ECR_IMAGE_TAG}

                    echo "[INFO] Pushing Docker image to ECR..."
                    docker push ${ECR_REPO}:${ECR_IMAGE_TAG}
                '''
            }
        }
    }

    post {
        always {
            echo '[INFO] Cleaning up workspace...'
            cleanWs()
        }
    }
}