pipeline {
    agent any

    tools {
        jdk 'JDK17'
        maven 'Maven3'
    }

    options {
        timestamps()
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '10'))
    }

    parameters {
        choice(name: 'DEPLOY_ENV', choices: ['dev', 'staging'], description: 'Target deployment environment')
        string(name: 'TOMCAT_URL', defaultValue: 'http://localhost:8081', description: 'Tomcat 10 server base URL')
        string(name: 'APP_CONTEXT', defaultValue: 'construction-dashboard', description: 'Base application context name')
        booleanParam(name: 'RUN_TESTS', defaultValue: true, description: 'Run unit test suite')
        booleanParam(name: 'RUN_SELENIUM', defaultValue: true, description: 'Run Selenium E2E test suite as deployment quality gate')
    }

    environment {
        WAR_FILE = 'target/dashboard.war'
        TARGET_PATH = "/${params.APP_CONTEXT}-${params.DEPLOY_ENV}"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
                script {
                    def branch = env.BRANCH_NAME ?: env.GIT_BRANCH ?: 'develop'
                    def commit = env.GIT_COMMIT ?: 'HEAD'
                    echo "=========================================="
                    echo "Pipeline Checkout: ${branch}"
                    echo "Commit: ${commit}"
                    echo "Target Environment: ${params.DEPLOY_ENV}"
                    echo "Target Context Path: ${env.TARGET_PATH}"
                    echo "Tomcat Server URL: ${params.TOMCAT_URL}"
                    echo "Run Unit Tests: ${params.RUN_TESTS}"
                    echo "Run Selenium Quality Gate: ${params.RUN_SELENIUM}"
                    echo "=========================================="
                }
            }
        }

        stage('Build') {
            steps {
                script {
                    echo "Compiling application sources..."
                    runCmd('mvn -B clean compile')
                }
            }
        }

        stage('Unit Tests') {
            when {
                expression { params.RUN_TESTS == true }
            }
            steps {
                script {
                    echo "Executing Unit and Integration Test Suite..."
                    runCmd('mvn -B test')
                }
            }
        }

        stage('Package') {
            steps {
                script {
                    echo "Packaging WAR archive (skipping tests during package stage)..."
                    runCmd('mvn -B package -DskipTests')
                }
                archiveArtifacts artifacts: 'target/*.war', fingerprint: true, allowEmptyArchive: false
            }
        }

        stage('Selenium Tests') {
            steps {
                script {
                    if (params.RUN_SELENIUM) {
                        echo "==========================================================="
                        echo "Executing Selenium E2E Tests Quality Gate (Headless Chrome)..."
                        echo "==========================================================="
                        runCmd('mvn -B -Pselenium test -Dheadless=true')
                    } else {
                        echo "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"
                        echo "WARNING: Selenium Quality Gate is explicitly DISABLED!"
                        echo "Proceeding with deployment without end-to-end verification."
                        echo "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"
                    }
                }
            }
        }

        stage('Deploy') {
            steps {
                script {
                    echo "Deploying ${env.WAR_FILE} to Tomcat 10 at ${params.TOMCAT_URL}${env.TARGET_PATH}..."
                    withCredentials([usernamePassword(credentialsId: 'tomcat-manager', usernameVariable: 'TC_USER', passwordVariable: 'TC_PASS')]) {
                        def deployUrl = "${params.TOMCAT_URL}/manager/text/deploy?path=${env.TARGET_PATH}&update=true"
                        if (isUnix()) {
                            sh 'curl --fail -s -S -u "$TC_USER:$TC_PASS" -T "' + env.WAR_FILE + '" "' + deployUrl + '"'
                        } else {
                            bat 'curl.exe --fail -s -S -u "%TC_USER%:%TC_PASS%" -T "' + env.WAR_FILE + '" "' + deployUrl + '"'
                        }
                    }
                }
            }
        }

        stage('Verify') {
            steps {
                script {
                    def healthUrl = "${params.TOMCAT_URL}${env.TARGET_PATH}/health"
                    echo "Verifying application health at: ${healthUrl}"
                    def maxRetries = 12
                    def delaySeconds = 5
                    def isUp = false

                    for (int i = 1; i <= maxRetries; i++) {
                        echo "Health verification poll attempt ${i}/${maxRetries}..."
                        try {
                            def response = ""
                            if (isUnix()) {
                                response = sh(script: 'curl -s -m 5 "' + healthUrl + '" || true', returnStdout: true).trim()
                            } else {
                                response = bat(script: '@curl.exe -s -m 5 "' + healthUrl + '"', returnStdout: true).trim()
                            }
                            echo "Health response: ${response}"
                            if (response.contains('"status":"UP"') || response.contains('"UP"') || response.contains('UP')) {
                                isUp = true
                                echo "Application health check PASSED (Status: UP)!"
                                break
                            }
                        } catch (Exception e) {
                            echo "Health check poll exception on attempt ${i}: ${e.message}"
                        }
                        sleep(delaySeconds)
                    }

                    if (!isUp) {
                        error("Application health verification failed after ${maxRetries} attempts at ${healthUrl}")
                    }
                }
            }
        }
    }

    post {
        success {
            echo "==========================================================="
            echo "SUCCESS: Application deployed and healthy at:"
            echo "${params.TOMCAT_URL}${env.TARGET_PATH}/"
            echo "==========================================================="
        }
        failure {
            echo "==========================================================="
            echo "FAILURE: Pipeline execution failed."
            echo "Deployment skipped because tests failed or deployment error occurred."
            echo "Verify Jenkins credentials ('tomcat-manager'), Tomcat status on ${params.TOMCAT_URL},"
            echo "and application runtime logs in catalina.out."
            echo "==========================================================="
        }
        always {
            cleanWs()
        }
    }
}

def runCmd(String cmd) {
    if (isUnix()) {
        sh cmd
    } else {
        bat cmd
    }
}
