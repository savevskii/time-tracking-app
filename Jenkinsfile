IS_RELEASE = params.RELEASE_BUILD && env.BRANCH_NAME == 'master'
DOCKER_REGISTRY = 'docker-registry-internal.netcetera.com/fsavevsk'
IMAGE_NAME = 'time-tracking-app'

pipeline {

    agent {
        docker {
            image 'docker-registry-internal-release.netcetera.com/nca-341-2/toolbox:1.1.3-openjdk21'
            args '-v /var/run/docker.sock:/var/run/docker.sock'
            alwaysPull true
            label 'docker'
        }
    }

    environment {
        BITBUCKET_USER = credentials('11d1c8ab-bef2-4ed4-b79d-eb8d3db3d9f2')
        TESTCONTAINERS_RYUK_DISABLED = 'true'
    }

    parameters {
        booleanParam(name: 'RELEASE_BUILD', defaultValue: false, description: 'Trigger a release build')
        string(name: 'RELEASE_VERSION', defaultValue: '', description: 'Version number for the release')
        string(name: 'DEVELOPMENT_VERSION', defaultValue: '',
                description: 'Version number for the development stream after the release')
    }

    stages {

        stage('Bitbucket Notification') {
            steps {
                script {
                    bitbucketNotification("INPROGRESS")
                }
            }
        }

        stage('Maven Build & Unit Tests') {
            steps {
                script {
                    sh "mvn -B -ntp clean package"

                    // Spring boot unit tests result
                    junit allowEmptyResults: true, testResults: '**/target/surefire-reports/*.xml'

                    // React unit tests result
                    junit allowEmptyResults: true, testResults: '**/test-results/*.xml'
                }
            }
        }

        stage('Integration Tests') {
            steps {
                script {
                    sh "mvn -B -ntp failsafe:integration-test failsafe:verify"

                    // integration test results
                    junit allowEmptyResults: true, testResults: '**/target/failsafe-reports/*.xml'
                }
            }
        }

        stage('Release Build') {
            when {
                expression { return IS_RELEASE }
            }
            steps {
                script {
                    sh """
                       mvn -B -ntp \
                       -Dusername=\"${env.BITBUCKET_USER_USR}\" \
                       -Dpassword=\"${env.BITBUCKET_USER_PSW}\" \
                       -DreleaseVersion=\"${params.RELEASE_VERSION}\" \
                       -DdevelopmentVersion=\"${params.DEVELOPMENT_VERSION}\" \
                       release:prepare release:perform
                    """
                }
            }
        }

        stage('Build and Publish Docker Image') {
            when {
                branch 'master'
            }
            steps {
                script {
                    VERSION = sh(script: "mvn help:evaluate -Dexpression=project.version -q -DforceStdout", returnStdout: true).trim()
                    sh 'echo "Pom version is: $VERSION"'
                    IMAGE_TAG = IS_RELEASE ? "${params.RELEASE_VERSION}" : VERSION

                    sh "docker build -t ${DOCKER_REGISTRY}/${IMAGE_NAME}:${IMAGE_TAG} ."
                    sh "docker push ${DOCKER_REGISTRY}/${IMAGE_NAME}:${IMAGE_TAG}"
                }
            }
        }

        stage('CD: Deploy to Staging') {
            when {
                branch 'master'
            }
            steps {
                script {
                    sh 'echo "Deployed to Staging"'
                }
            }
        }

        stage('CD: Deploy to Production') {
            when {
                expression { return IS_RELEASE }
            }
            steps {
                script {
                    sh 'echo "Deployed to Production"'
                }
            }
        }

    }

    post {
        always {
            script {
                currentBuild.result = currentBuild.result ?: 'SUCCESS'
                bitbucketNotification(currentBuild.result)
            }
        }
    }
}

void bitbucketNotification(String status) {
    notifyBitbucket( \
             buildName: "${JOB_NAME}#${BUILD_NUMBER}",                  \
             buildStatus: status,                  \
             commitSha1: '',                  \
             considerUnstableAsSuccess: false,                  \
             credentialsId: '11d1c8ab-bef2-4ed4-b79d-eb8d3db3d9f2',                  \
             disableInprogressNotification: false,                  \
             ignoreUnverifiedSSLPeer: false,                  \
             includeBuildNumberInKey: false,                  \
             prependParentProjectKey: false,                  \
             projectKey: '',                  \
             stashServerBaseUrl: 'https://extranet.netcetera.biz/bitbucket')
}