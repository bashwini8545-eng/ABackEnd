pipeline {
    agent any

    environment {
        EC2_HOST      = '3.16.163.235'
        EC2_USER      = 'ec2-user'
        JAR_NAME      = 'backend-0.0.1-SNAPSHOT.jar'
        DEPLOY_DIR    = '/home/ec2-user/apps/backend'
        JAVA_HOME_EC2 = '/usr/lib/jvm/java-17-amazon-corretto'
    }

    stages {

        stage('Checkout') {
            steps {
                echo '📥 Checking out source code...'
                git branch: 'main',
                    credentialsId: 'github-creds',
                    url: 'https://github.com/bashwini8545-eng/ABackEnd'
                echo '✅ Checkout complete.'
            }
        }

        stage('Build & Test') {
            steps {
                echo '🔨 Building and running tests...'
                bat 'mvn clean verify --batch-mode'
            }
            post {
                always {
                    junit allowEmptyResults: true,
                          testResults: '**/target/surefire-reports/*.xml'
                }
            }
        }

        stage('Package') {
            steps {
                echo '📦 Packaging fat JAR...'
                bat 'mvn package -DskipTests --batch-mode'
                archiveArtifacts artifacts: "target/${JAR_NAME}", fingerprint: true
            }
        }

        stage('Verify EC2 Connection') {
            steps {
                echo '🔗 Verifying SSH connectivity to EC2...'
                withCredentials([file(credentialsId: 'ec2-ssh-key', variable: 'KEY_FILE')]) {
                    bat """
                        copy "%KEY_FILE%" "%WORKSPACE%\\temp-key.pem"
                        icacls "%WORKSPACE%\\temp-key.pem" /reset
                        icacls "%WORKSPACE%\\temp-key.pem" /inheritance:r
                        icacls "%WORKSPACE%\\temp-key.pem" /remove "Everyone"
                        icacls "%WORKSPACE%\\temp-key.pem" /remove "BUILTIN\\Users"
                        icacls "%WORKSPACE%\\temp-key.pem" /grant:r "NT AUTHORITY\\SYSTEM:(R)"
                        ssh -o StrictHostKeyChecking=no -i "%WORKSPACE%\\temp-key.pem" ^
                            %EC2_USER%@%EC2_HOST% "echo EC2 connection OK"
                        del "%WORKSPACE%\\temp-key.pem"
                    """
                }
            }
        }

        stage('Deploy to EC2') {
            steps {
                echo '🚀 Deploying JAR to EC2...'
                withCredentials([file(credentialsId: 'ec2-ssh-key', variable: 'KEY_FILE')]) {
                    bat """
                        copy "%KEY_FILE%" "%WORKSPACE%\\temp-key.pem"
                        icacls "%WORKSPACE%\\temp-key.pem" /reset
                        icacls "%WORKSPACE%\\temp-key.pem" /inheritance:r
                        icacls "%WORKSPACE%\\temp-key.pem" /remove "Everyone"
                        icacls "%WORKSPACE%\\temp-key.pem" /remove "BUILTIN\\Users"
                        icacls "%WORKSPACE%\\temp-key.pem" /grant:r "NT AUTHORITY\\SYSTEM:(R)"

                        ssh -o StrictHostKeyChecking=no -i "%WORKSPACE%\\temp-key.pem" ^
                            %EC2_USER%@%EC2_HOST% "mkdir -p %DEPLOY_DIR%"

                        scp -o StrictHostKeyChecking=no -i "%WORKSPACE%\\temp-key.pem" ^
                            "%WORKSPACE%\\target\\%JAR_NAME%" ^
                            %EC2_USER%@%EC2_HOST%:%DEPLOY_DIR%/%JAR_NAME%

                        ssh -o StrictHostKeyChecking=no -i "%WORKSPACE%\\temp-key.pem" ^
                            %EC2_USER%@%EC2_HOST% ^
                            "pkill -f '%JAR_NAME%' || true ; nohup %JAVA_HOME_EC2%/bin/java -jar %DEPLOY_DIR%/%JAR_NAME% > %DEPLOY_DIR%/app.log 2>&1 &"

                        del "%WORKSPACE%\\temp-key.pem"
                    """
                }
            }
        }

        stage('Smoke Test') {
            steps {
                echo '🧪 Waiting for app to start...'
                bat """
                    timeout /t 30 /nobreak
                    curl -f http://%EC2_HOST%:8080/actuator/health ^
                         || (echo Smoke test failed & exit 1)
                """
            }
        }
    }

    post {
        success {
            echo '✅ Pipeline complete – application deployed successfully!'
        }
        failure {
            echo '❌ Pipeline failed – check the console output above.'
        }
        always {
            cleanWs()
        }
    }
}
