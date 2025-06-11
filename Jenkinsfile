pipeline {
    agent any

    environment {
        PROJECT_NAME = 'route33-service'
        IMAGE_NAME = "${PROJECT_NAME}"

        ECR_REPO = "${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com/${IMAGE_NAME}"
        MANIFEST_REPO = 'git@github.com:Goorm-Team3/route33-argocd.git'
        MANIFEST_FILE_PATH = 'apps/service/deployment.yaml'

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

        stage('Set Image Tag') {
            steps {
                script {
                    def tag = new Date().format("yyyyMMdd-HHmmss", TimeZone.getTimeZone('Asia/Seoul'))
                    env.IMAGE_TAG = tag
                }
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
                    docker tag ${IMAGE_NAME}:${IMAGE_TAG} ${ECR_REPO}:${IMAGE_TAG}

                    echo "[INFO] Pushing Docker image to ECR..."
                    docker push ${ECR_REPO}:${IMAGE_TAG}
                '''
            }
        }

        stage('Update Manifest Repository') {
            steps {
                script {
                    def tempDir = "/tmp/manifest-repo-${UUID.randomUUID().toString()}"
                    sh """
                        echo "[INFO] Cloning manifest repository to ${tempDir}..."
                        git clone ${MANIFEST_REPO} ${tempDir}
                    """

                    try {
                        dir(tempDir) {
                            sh """
                                echo "[INFO] Checking out 'dev' branch..."
                                git checkout dev

                                echo "[INFO] Updating image tag in manifest..."
                                sed -i 's|image: ${ECR_REPO}:.*|image: ${ECR_REPO}:${IMAGE_TAG}|' ${MANIFEST_FILE_PATH}

                                git config user.name "jenkins-bot"
                                git config user.email "jenkins-bot@your-org.com"
                                git add ${MANIFEST_FILE_PATH}
                                git commit -m "Update image tag to ${IMAGE_TAG}"
                                git push origin dev
                            """
                        }
                    } finally {
                        sh "rm -rf ${tempDir}"
                        echo "[INFO] Cleaned up temporary manifest repository directory."
                    }
                }
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