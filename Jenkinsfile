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

    triggers {
        pollSCM('H/2 * * * *')
    }

    parameters {
        choice(name: 'REGISTRY_MODE', choices: ['dockerhub', 'local'], description: 'Docker registry target (dockerhub or local registry on localhost:5000)')
        choice(name: 'DEPLOY_TARGET', choices: ['ansible', 'docker', 'tomcat'], description: 'Deployment target runtime: ansible (remote target node), docker (local container), or tomcat (standalone)')
        string(name: 'TARGET_HOST', defaultValue: '127.0.0.1', description: 'Target node host address for Ansible deployment and verification')
        string(name: 'HOST_PORT', defaultValue: '8082', description: 'Host port for Docker container')
        string(name: 'CONTAINER_NAME', defaultValue: 'construction-dashboard', description: 'Docker container name')
        choice(name: 'DEPLOY_ENV', choices: ['dev', 'staging'], description: 'Target deployment environment')
        string(name: 'TOMCAT_URL', defaultValue: 'http://localhost:8081', description: 'Tomcat 10 server base URL')
        string(name: 'APP_CONTEXT', defaultValue: 'construction-dashboard', description: 'Base application context name')
        booleanParam(name: 'RUN_TESTS', defaultValue: true, description: 'Run unit test suite')
        booleanParam(name: 'RUN_SELENIUM', defaultValue: true, description: 'Run Selenium E2E test suite as deployment quality gate')
    }

    environment {
        WAR_FILE = 'target/dashboard.war'
        TARGET_PATH = "/${params.APP_CONTEXT}-${params.DEPLOY_ENV}"
        APP_VERSION = ''
        SHORT_SHA = ''
        IMAGE_TAG = ''
        IMAGE_REPO = ''
        DOCKER_HUB_USER = ''
        IMAGE_DIGEST = ''
        PREVIOUS_IMAGE = ''
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
                    echo "Registry Mode: ${params.REGISTRY_MODE}"
                    echo "Deploy Target: ${params.DEPLOY_TARGET}"
                    echo "Host Port: ${params.HOST_PORT}"
                    echo "Container Name: ${params.CONTAINER_NAME}"
                    echo "Target Environment: ${params.DEPLOY_ENV}"
                    echo "Run Unit Tests: ${params.RUN_TESTS}"
                    echo "Run Selenium Quality Gate: ${params.RUN_SELENIUM}"
                    echo "=========================================="

                    // 1. Evaluate APP_VERSION from pom.xml
                    try {
                        if (isUnix()) {
                            env.APP_VERSION = sh(script: 'mvn -q -DforceStdout help:evaluate -Dexpression=project.version 2>/dev/null || ./mvnw -q -DforceStdout help:evaluate -Dexpression=project.version', returnStdout: true).trim()
                        } else {
                            env.APP_VERSION = bat(script: '@mvn.cmd -q -DforceStdout help:evaluate -Dexpression=project.version 2>NUL || .\\mvnw.cmd -q -DforceStdout help:evaluate -Dexpression=project.version', returnStdout: true).trim()
                        }
                    } catch (Exception e) {
                        echo "Notice: Maven help:evaluate fallback (${e.message})"
                    }
                    if (!env.APP_VERSION || env.APP_VERSION == '') {
                        def pom = readFile('pom.xml')
                        def matcher = pom =~ '<version>(.*?)</version>'
                        if (matcher.find()) {
                            env.APP_VERSION = matcher.group(1).trim()
                        } else {
                            env.APP_VERSION = '1.0.0'
                        }
                    }

                    // 2. Evaluate SHORT_SHA (first 7 characters of commit)
                    if (isUnix()) {
                        env.SHORT_SHA = sh(script: 'git rev-parse --short=7 HEAD', returnStdout: true).trim()
                    } else {
                        env.SHORT_SHA = bat(script: '@git.exe rev-parse --short=7 HEAD', returnStdout: true).trim()
                    }

                    // 3. Compute IMAGE_TAG
                    env.IMAGE_TAG = "${env.APP_VERSION}-b${env.BUILD_NUMBER}-${env.SHORT_SHA}"

                    // 4. Compute IMAGE_REPO based on REGISTRY_MODE
                    if (params.REGISTRY_MODE == 'dockerhub') {
                        try {
                            withCredentials([usernamePassword(credentialsId: 'dockerhub-credentials', usernameVariable: 'DH_USER', passwordVariable: 'DH_PASS')]) {
                                env.DOCKER_HUB_USER = env.DH_USER
                                env.IMAGE_REPO = "${env.DH_USER}/construction-dashboard"
                            }
                        } catch (Exception e) {
                            echo "Notice: dockerhub-credentials not found in Jenkins, fallback repo name: ganesh-dahiphale/construction-dashboard"
                            env.IMAGE_REPO = "ganesh-dahiphale/construction-dashboard"
                        }
                    } else {
                        env.IMAGE_REPO = "localhost:5000/construction-dashboard"
                    }

                    echo "Derived Metadata Properties:"
                    echo "APP_VERSION = ${env.APP_VERSION}"
                    echo "SHORT_SHA   = ${env.SHORT_SHA}"
                    echo "IMAGE_TAG   = ${env.IMAGE_TAG}"
                    echo "IMAGE_REPO  = ${env.IMAGE_REPO}"
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
            post {
                always {
                    junit testResults: 'target/surefire-reports/*.xml', allowEmptyResults: true
                }
                failure {
                    echo "Deployment skipped because unit tests failed."
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
            post {
                always {
                    junit testResults: 'target/surefire-reports/*.xml', allowEmptyResults: true
                    archiveArtifacts artifacts: 'target/screenshots/*.png, target/site/**/*.html, target/surefire-reports/**', allowEmptyArchive: true
                    script {
                        try {
                            if (Jenkins.instance.pluginManager.getPlugin('htmlpublisher') != null) {
                                publishHTML(target: [
                                    allowMissing: true,
                                    alwaysLinkToLastBuild: true,
                                    keepAll: true,
                                    reportDir: 'target/site',
                                    reportFiles: 'surefire-report.html',
                                    reportName: 'Surefire HTML Report'
                                ])
                            }
                        } catch (Throwable t) {
                            echo "HTML Publisher plugin guarded check: ${t.message}"
                        }
                    }
                }
                failure {
                    echo "Deployment skipped because Selenium tests failed."
                }
            }
        }

        // ========================================================
        // Tomcat Deployment Branch (When DEPLOY_TARGET == 'tomcat')
        // ========================================================
        stage('Deploy to Tomcat') {
            when {
                expression { params.DEPLOY_TARGET == 'tomcat' }
            }
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

        stage('Verify Tomcat') {
            when {
                expression { params.DEPLOY_TARGET == 'tomcat' }
            }
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

        // ========================================================
        // Docker Continuous Deployment Stages (DEPLOY_TARGET == 'docker')
        // ========================================================
        stage('Docker Build') {
            when {
                expression { params.DEPLOY_TARGET == 'docker' || params.DEPLOY_TARGET == 'ansible' }
            }
            steps {
                script {
                    echo "Building runtime Docker image using Dockerfile.runtime and pre-packaged tested WAR: ${env.IMAGE_REPO}:${env.IMAGE_TAG}..."
                    def buildCmd = "docker build -f Dockerfile.runtime " +
                        "--build-arg VERSION=${env.APP_VERSION} " +
                        "--build-arg COMMIT_SHA=${env.SHORT_SHA} " +
                        "--build-arg BUILD_NUMBER=${env.BUILD_NUMBER} " +
                        "--label version=${env.APP_VERSION} " +
                        "--label revision=${env.SHORT_SHA} " +
                        "--label build=${env.BUILD_NUMBER} " +
                        "-t ${env.IMAGE_REPO}:${env.IMAGE_TAG} " +
                        "-t ${env.IMAGE_REPO}:latest ."
                    runCmd(buildCmd)
                }
            }
        }

        stage('Docker Push') {
            when {
                expression { params.DEPLOY_TARGET == 'docker' || params.DEPLOY_TARGET == 'ansible' }
            }
            steps {
                script {
                    if (params.REGISTRY_MODE == 'dockerhub') {
                        echo "Logging in to Docker Hub and pushing images..."
                        withCredentials([usernamePassword(credentialsId: 'dockerhub-credentials', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
                            try {
                                if (isUnix()) {
                                    sh 'echo "$DOCKER_PASS" | docker login -u "$DOCKER_USER" --password-stdin'
                                    sh "docker push ${env.IMAGE_REPO}:${env.IMAGE_TAG}"
                                    sh "docker push ${env.IMAGE_REPO}:latest"
                                } else {
                                    bat 'echo %DOCKER_PASS%| docker login -u "%DOCKER_USER%" --password-stdin'
                                    bat "docker push ${env.IMAGE_REPO}:${env.IMAGE_TAG}"
                                    bat "docker push ${env.IMAGE_REPO}:latest"
                                }
                            } finally {
                                runCmd('docker logout')
                            }
                        }
                    } else {
                        echo "Pushing images to local registry at localhost:5000..."
                        runCmd("docker push ${env.IMAGE_REPO}:${env.IMAGE_TAG}")
                        runCmd("docker push ${env.IMAGE_REPO}:latest")
                    }

                    // Extract and display image digest
                    echo "Extracting image digest..."
                    try {
                        if (isUnix()) {
                            env.IMAGE_DIGEST = sh(script: "docker inspect --format='{{index .RepoDigests 0}}' ${env.IMAGE_REPO}:${env.IMAGE_TAG} 2>/dev/null || true", returnStdout: true).trim()
                        } else {
                            env.IMAGE_DIGEST = bat(script: "@powershell -Command \"(docker inspect --format '{{index .RepoDigests 0}}' ${env.IMAGE_REPO}:${env.IMAGE_TAG} 2>`$null)\"", returnStdout: true).trim()
                        }
                    } catch (Exception e) {
                        echo "Notice: Could not parse RepoDigests: ${e.message}"
                    }
                    echo "Published Image Digest: ${env.IMAGE_DIGEST ?: 'Extracted post-push'}"
                }
            }
        }

        stage('Deploy Container') {
            when {
                expression { params.DEPLOY_TARGET == 'docker' }
            }
            steps {
                script {
                    echo "Recording currently running image of container '${params.CONTAINER_NAME}'..."
                    if (isUnix()) {
                        env.PREVIOUS_IMAGE = sh(script: "docker inspect --format='{{.Config.Image}}' ${params.CONTAINER_NAME} 2>/dev/null || true", returnStdout: true).trim()
                    } else {
                        env.PREVIOUS_IMAGE = bat(script: "@powershell -Command \"docker inspect --format '{{.Config.Image}}' ${params.CONTAINER_NAME} 2>`$null\"", returnStdout: true).trim()
                    }
                    echo "Previous container image: ${env.PREVIOUS_IMAGE ?: 'None'}"

                    echo "Pulling fresh image ${env.IMAGE_REPO}:${env.IMAGE_TAG}..."
                    runCmd("docker pull ${env.IMAGE_REPO}:${env.IMAGE_TAG}")

                    echo "Stopping and removing existing container '${params.CONTAINER_NAME}' if present..."
                    if (isUnix()) {
                        sh "docker rm -f ${params.CONTAINER_NAME} 2>/dev/null || true"
                    } else {
                        bat "@powershell -Command \"docker rm -f ${params.CONTAINER_NAME} 2>`$null; exit 0\""
                    }

                    echo "Running fresh container '${params.CONTAINER_NAME}' on port ${params.HOST_PORT}:8080..."
                    // Note: SPRING_PROFILES_ACTIVE=dev enables seed users and dev tasks for this course demo only
                    def runContainerCmd = "docker run -d --name ${params.CONTAINER_NAME} --restart unless-stopped " +
                        "-p ${params.HOST_PORT}:8080 -e SPRING_PROFILES_ACTIVE=dev -e APP_VERSION=${env.IMAGE_TAG} " +
                        "${env.IMAGE_REPO}:${env.IMAGE_TAG}"
                    runCmd(runContainerCmd)
                }
            }
        }

        stage('Verify Container') {
            when {
                expression { params.DEPLOY_TARGET == 'docker' }
            }
            steps {
                script {
                    def healthStatus = ""
                    def maxSeconds = 90
                    def pollInterval = 3
                    def elapsed = 0
                    def isHealthy = false

                    echo "Polling container health status for up to ${maxSeconds}s..."
                    while (elapsed < maxSeconds) {
                        if (isUnix()) {
                            healthStatus = sh(script: "docker inspect --format='{{.State.Health.Status}}' ${params.CONTAINER_NAME} 2>/dev/null || true", returnStdout: true).trim()
                        } else {
                            healthStatus = bat(script: "@powershell -Command \"(docker inspect --format '{{.State.Health.Status}}' ${params.CONTAINER_NAME} 2>`$null).Trim()\"", returnStdout: true).trim()
                        }
                        echo "Health check poll [${elapsed}s/${maxSeconds}s]: ${healthStatus}"
                        if (healthStatus == 'healthy') {
                            isHealthy = true
                            break
                        }
                        sleep(pollInterval)
                        elapsed += pollInterval
                    }

                    if (!isHealthy) {
                        rollback(params.CONTAINER_NAME, env.PREVIOUS_IMAGE, params.HOST_PORT, "Container health status was '${healthStatus}' (expected: healthy)")
                    }

                    // Verify /health endpoint returns UP
                    echo "Checking http://localhost:${params.HOST_PORT}/health..."
                    def healthResponse = ""
                    if (isUnix()) {
                        healthResponse = sh(script: "curl -s -m 5 http://localhost:${params.HOST_PORT}/health || true", returnStdout: true).trim()
                    } else {
                        healthResponse = bat(script: "@curl.exe -s -m 5 http://localhost:${params.HOST_PORT}/health", returnStdout: true).trim()
                    }
                    echo "Health endpoint response: ${healthResponse}"
                    if (!healthResponse.contains('"status":"UP"') && !healthResponse.contains('UP')) {
                        rollback(params.CONTAINER_NAME, env.PREVIOUS_IMAGE, params.HOST_PORT, "Health endpoint did not return UP: ${healthResponse}")
                    }

                    // Verify login page contains IMAGE_TAG version text
                    echo "Checking http://localhost:${params.HOST_PORT}/login contains version text '${env.IMAGE_TAG}'..."
                    def loginHtml = ""
                    if (isUnix()) {
                        loginHtml = sh(script: "curl -s -m 5 http://localhost:${params.HOST_PORT}/login || true", returnStdout: true).trim()
                    } else {
                        loginHtml = bat(script: "@curl.exe -s -m 5 http://localhost:${params.HOST_PORT}/login", returnStdout: true).trim()
                    }
                    if (!loginHtml.contains(env.IMAGE_TAG)) {
                        rollback(params.CONTAINER_NAME, env.PREVIOUS_IMAGE, params.HOST_PORT, "Login page does not contain expected version string '${env.IMAGE_TAG}'")
                    }

                    echo "==========================================================="
                    echo "VERIFY PASSED: Container is healthy, /health is UP, and version is verified on UI!"
                    echo "==========================================================="
                }
            }
        }

        // ========================================================
        // Ansible Continuous Deployment Stages (DEPLOY_TARGET == 'ansible')
        // ========================================================
        stage('Provision and Deploy (Ansible)') {
            when {
                expression { params.DEPLOY_TARGET == 'ansible' }
            }
            steps {
                script {
                    echo "==========================================================="
                    echo "Provisioning and Deploying Release ${env.IMAGE_TAG} via Ansible..."
                    echo "==========================================================="
                    def extraVars = "-e release_tag=${env.IMAGE_TAG} -e docker_image=${env.IMAGE_REPO}"
                    runAnsiblePlaybook('ansible/playbooks/provision.yml', extraVars, 'ansible-deploy.log')
                }
            }
        }

        stage('Verify Node') {
            when {
                expression { params.DEPLOY_TARGET == 'ansible' }
            }
            steps {
                script {
                    echo "==========================================================="
                    echo "Verifying Target Node Health and Release Version..."
                    echo "==========================================================="
                    // 1. Run Ansible healthcheck playbook
                    runAnsiblePlaybook('ansible/playbooks/healthcheck.yml', '', 'ansible-healthcheck.log')

                    // 2. HTTP Verification from Jenkins machine: /health
                    def targetHost = params.TARGET_HOST ?: '127.0.0.1'
                    def targetPort = params.HOST_PORT ?: '8082'
                    def healthUrl = "http://${targetHost}:${targetPort}/health"
                    def loginUrl = "http://${targetHost}:${targetPort}/login"

                    echo "Testing HTTP endpoint from Jenkins machine: ${healthUrl}..."
                    def healthResponse = ""
                    def maxRetries = 12
                    def isUp = false

                    for (int i = 1; i <= maxRetries; i++) {
                        try {
                            if (isUnix()) {
                                healthResponse = sh(script: "curl -s -m 5 ${healthUrl} || true", returnStdout: true).trim()
                            } else {
                                healthResponse = bat(script: "@curl.exe -s -m 5 ${healthUrl}", returnStdout: true).trim()
                            }
                            echo "Health response (poll ${i}/${maxRetries}): ${healthResponse}"
                            if (healthResponse.contains('"status":"UP"') || healthResponse.contains('"UP"') || healthResponse.contains('UP')) {
                                isUp = true
                                break
                            }
                        } catch (Exception e) {
                            echo "Health poll attempt ${i} notice: ${e.message}"
                        }
                        sleep(5)
                    }

                    if (!isUp) {
                        error("Node health verification failed: ${healthUrl} did not return UP (Response: ${healthResponse})")
                    }

                    // 3. Verify page content contains IMAGE_TAG
                    echo "Testing HTTP login page contains IMAGE_TAG '${env.IMAGE_TAG}'..."
                    def pageHtml = ""
                    if (isUnix()) {
                        pageHtml = sh(script: "curl -s -m 5 ${loginUrl} || true", returnStdout: true).trim()
                    } else {
                        pageHtml = bat(script: "@curl.exe -s -m 5 ${loginUrl}", returnStdout: true).trim()
                    }

                    if (!pageHtml.contains(env.IMAGE_TAG)) {
                        error("Node UI verification failed: ${loginUrl} does not contain IMAGE_TAG '${env.IMAGE_TAG}'")
                    }

                    echo "==========================================================="
                    echo "VERIFY NODE PASSED: Target node is healthy and running ${env.IMAGE_TAG}!"
                    echo "==========================================================="
                }
            }
        }

        stage('Promote') {
            when {
                expression { params.DEPLOY_TARGET == 'docker' || params.DEPLOY_TARGET == 'ansible' }
            }
            steps {
                script {
                    echo "Tagging image ${env.IMAGE_REPO}:${env.IMAGE_TAG} as ${env.IMAGE_REPO}:stable for promotion..."
                    runCmd("docker tag ${env.IMAGE_REPO}:${env.IMAGE_TAG} ${env.IMAGE_REPO}:stable")

                    if (params.REGISTRY_MODE == 'dockerhub') {
                        echo "Pushing stable promotion tag to Docker Hub..."
                        withCredentials([usernamePassword(credentialsId: 'dockerhub-credentials', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
                            try {
                                if (isUnix()) {
                                    sh 'echo "$DOCKER_PASS" | docker login -u "$DOCKER_USER" --password-stdin'
                                    sh "docker push ${env.IMAGE_REPO}:stable"
                                } else {
                                    bat 'echo %DOCKER_PASS%| docker login -u "%DOCKER_USER%" --password-stdin'
                                    bat "docker push ${env.IMAGE_REPO}:stable"
                                }
                            } finally {
                                runCmd('docker logout')
                            }
                        }
                    } else {
                        echo "Pushing stable promotion tag to local registry..."
                        runCmd("docker push ${env.IMAGE_REPO}:stable")
                    }
                    echo "Promotion to stable complete!"
                }
            }
        }
    }

    post {
        always {
            script {
                if (params.DEPLOY_TARGET == 'docker' || params.DEPLOY_TARGET == 'ansible') {
                    try {
                        echo "Pruning dangling image layers..."
                        runCmd("docker image prune -f")
                    } catch (Exception e) {
                        echo "Dangling layer prune notice: ${e.message}"
                    }

                    def targetHost = (params.DEPLOY_TARGET == 'ansible') ? (params.TARGET_HOST ?: '127.0.0.1') : 'localhost'
                    def targetPort = params.HOST_PORT ?: '8082'

                    def deployInfoContent = """Deployment Information - Week 15 Final Release
-----------------------------------------------------------
Application Name: Construction Progress Dashboard
Deploy Target:    ${params.DEPLOY_TARGET}
Registry Mode:    ${params.REGISTRY_MODE}
Image Repository: ${env.IMAGE_REPO}
Image Tag:        ${env.IMAGE_TAG}
Image Digest:     ${env.IMAGE_DIGEST}
Git Commit:       ${env.SHORT_SHA}
Build Number:     #${env.BUILD_NUMBER}
Target Node URL:  http://${targetHost}:${targetPort}
Health URL:       http://${targetHost}:${targetPort}/health
Timestamp:        ${new Date().format('yyyy-MM-dd HH:mm:ss')}
"""
                    writeFile file: 'deploy-info.txt', text: deployInfoContent
                    archiveArtifacts artifacts: 'deploy-info.txt, ansible-deploy.log, ansible-healthcheck.log', allowEmptyArchive: true
                }
            }
            cleanWs()
        }
        success {
            script {
                if (params.DEPLOY_TARGET == 'ansible') {
                    def targetHost = params.TARGET_HOST ?: '127.0.0.1'
                    echo "==========================================================="
                    echo "SUCCESS: Remote node provisioned, deployed and verified via Ansible at:"
                    echo "http://${targetHost}:${params.HOST_PORT}/ (Version: ${env.IMAGE_TAG})"
                    echo "Promoted Image Tag: ${env.IMAGE_REPO}:stable"
                    echo "==========================================================="
                } else if (params.DEPLOY_TARGET == 'docker') {
                    echo "==========================================================="
                    echo "SUCCESS: Container deployed and verified healthy at:"
                    echo "http://localhost:${params.HOST_PORT}/ (Version: ${env.IMAGE_TAG})"
                    echo "Promoted Image Tag: ${env.IMAGE_REPO}:stable"
                    echo "==========================================================="
                } else {
                    echo "==========================================================="
                    echo "SUCCESS: Application deployed and healthy at:"
                    echo "${params.TOMCAT_URL}${env.TARGET_PATH}/"
                    echo "==========================================================="
                }
            }
        }
        failure {
            echo "==========================================================="
            echo "FAILURE: Pipeline execution failed."
            echo "Deployment skipped or rolled back because tests failed or verification error occurred."
            echo "==========================================================="
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

def rollback(String containerName, String prevImage, String hostPort, String reason) {
    echo "==========================================================="
    echo "DEPLOYMENT VERIFICATION FAILED: ${reason}"
    echo "Initiating automatic rollback to PREVIOUS image: ${prevImage}"
    echo "==========================================================="
    if (prevImage && prevImage.trim() != '' && prevImage != 'None') {
        echo "Rolling back to previous image ${prevImage}..."
        if (isUnix()) {
            sh "docker rm -f ${containerName} 2>/dev/null || true"
            sh "docker run -d --name ${containerName} --restart unless-stopped -p ${hostPort}:8080 -e SPRING_PROFILES_ACTIVE=dev ${prevImage}"
        } else {
            bat "@powershell -Command \"docker rm -f ${containerName} 2>`$null; docker run -d --name ${containerName} --restart unless-stopped -p ${hostPort}:8080 -e SPRING_PROFILES_ACTIVE=dev ${prevImage}\""
        }
    }
    error("Deployment verification failed: ${reason}")
}

def runAnsiblePlaybook(String playbook, String extraVars, String logFile) {
    try {
        withCredentials([sshUserPrivateKey(credentialsId: 'cpd-ansible-ssh', keyFileVariable: 'SSH_KEY_FILE', usernameVariable: 'SSH_USER')]) {
            executeAnsibleCmd(playbook, extraVars, "--private-key=${SSH_KEY_FILE}", logFile)
        }
    } catch (Throwable t) {
        echo "Notice: Credential 'cpd-ansible-ssh' not resolved in current credentials store (${t.message}). Falling back to inventory SSH key configuration..."
        executeAnsibleCmd(playbook, extraVars, "", logFile)
    }
}

def executeAnsibleCmd(String playbook, String extraVars, String sshOpt, String logFile) {
    echo "Executing playbook: ${playbook} (log: ${logFile})"
    def envPrefix = "ANSIBLE_CONFIG=ansible/ansible.cfg ANSIBLE_ROLES_PATH=ansible/roles ANSIBLE_HOST_KEY_CHECKING=False"
    if (isUnix()) {
        sh "${envPrefix} ansible-playbook -i ansible/inventory/hosts.yml ${playbook} ${extraVars} ${sshOpt} 2>&1 | tee ${logFile}"
    } else {
        def hasNativeAnsible = bat(script: '@where.exe ansible-playbook 2>NUL || exit 0', returnStdout: true).trim()
        if (hasNativeAnsible && hasNativeAnsible != '') {
            bat "set ANSIBLE_CONFIG=ansible/ansible.cfg&& set ANSIBLE_ROLES_PATH=ansible/roles&& set ANSIBLE_HOST_KEY_CHECKING=False&& ansible-playbook -i ansible/inventory/hosts.yml ${playbook} ${extraVars} ${sshOpt} > ${logFile} 2>&1"
            bat "type ${logFile}"
        } else {
            def wslPath = "/mnt/c/Users/Ganesh/OneDrive/Desktop/DevOps"
            def wslCmd = "wsl.exe -d Ubuntu -u ganesh -- bash -c \"cd ${wslPath} && ${envPrefix} ansible-playbook -i ansible/inventory/hosts.yml ${playbook} ${extraVars} ${sshOpt}\""
            bat "${wslCmd} > ${logFile} 2>&1"
            bat "type ${logFile}"
        }
    }
}

