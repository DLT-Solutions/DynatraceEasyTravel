#!groovy
/***
 * Init section
 */
def artifactoryCredentials =
        [path: 'jarvis-jenkins-easytravel/artifactory-credentials', secretValues: [
                [envVar: 'ARTIFACTORY_USERNAME', vaultKey: 'username'],
                [envVar: 'ARTIFACTORY_PASSWORD', vaultKey: 'password']]]

def awsKeys =
        [path: 'jarvis-jenkins-easytravel/aws/codeartifact', secretValues: [
                [envVar: 'ACCESS_KEY', vaultKey: 'AWS_ACCESS_KEY_ID'],
                [envVar: 'SECRET_KEY', vaultKey: 'AWS_SECRET_ACCESS_KEY']]]

final String[] versionParts = readVersion('version.properties').tokenize('.')
final String buildVersion = versionParts.join('.') + ".${env.BUILD_NUMBER}"
currentBuild.displayName += " - ${buildVersion}"
final boolean buildservertests = true
final String antTestProperties = " -Dbuildservertests=${buildservertests}"
final String antProperties = " -Dversion.buildnumber=${env.BUILD_NUMBER} " +
        generateProperties("-D%s=%s", versionParts, ["version.major", "version.minor", "version.revision"])
final String gradleVersionProperties = " -PversionBuildnumber=${env.BUILD_NUMBER} -Pversion=${buildVersion} " +
        generateProperties("-P%s=%s", versionParts, ["versionMajor", "versionMinor", "versionRevision"])

final String awsRegion = 'eu-central-1'
final String ownerCodeArtifact = '246186168471'
final String domainCodeArtifact = 'demoability'
final int tokenDurationSecond = 1200
String token = ''

/**
 * CI script build
 */
pipeline {
    agent {
        kubernetes {
            label "easytravel-pipeline"
            yamlFile '.ci/docker.yaml'
            idleMinutes 15
        }
    }

    environment {
        ARTIFACTORY = 'SECRET'
        ARTIFACTORY_REPOSITORY_NAME = 'demoability-release-local'
        GROUP_ID = 'com.dynatrace.easytravel'
        JAVA_HOME = "${env.JAVA_HOME_11}"
        ANT_ARGS = "-autoproxy"
        ANT_OPTS = "-Xmx2048m"
    }

    stages {
        stage('Build') {
            environment {
                ARTIFACT = 'dynatrace-easytravel-java'
                VERSION = "${version}"
                ARTIFACT_PATH = "${env.GROUP_ID.replaceAll('\\.','/')}/${env.ARTIFACT}/${env.VERSION}"
                ARTIFACT_NAME = "${env.ARTIFACT}-${env.VERSION}"
            }
            steps {
                // Clean before build
                cleanWs()
                // We need to explicitly checkout from SCM here
                checkout scm
                sh getAntBuildCommand('all', antProperties)
                sh getAntBuildCommand('copyAngularFromArtifactorySrc', antProperties)
                sh getAntBuildCommand('copySourceCode', antProperties)
            }
        }
        stage('Trigger SonarQube') {
            // Only trigger Sonar on master - gets aggregated and scheduled once a day
            when {
                branch 'master'
            }
            steps {
                script {
                    def triggerJob = "DemoApps/easytravel-sonar/${BRANCH_NAME.replaceAll('/', '%2F')}"
                    def triggerParameters = [[$class: 'StringParameterValue', name: 'runType', value: 'Trigger-Schedule'],
                                             [$class: 'StringParameterValue', name: 'versionET', value: buildVersion]]
                    echo "Triggering build job: '${triggerJob}' with parameters: ${triggerParameters}"
                    build job: triggerJob,
                            parameters: triggerParameters,
                            wait: false
                }
            }
        }
        stage("Generate aws token") {
            when {
                branch 'master'
            }
            steps {
                script{
                    container('aws-cli') {
                        withVault(vaultSecrets: [awsKeys]) {
                            token = sh (
                                    script: 'AWS_ACCESS_KEY_ID=$ACCESS_KEY AWS_SECRET_ACCESS_KEY=$SECRET_KEY ' + "aws codeartifact get-authorization-token --region=$awsRegion --domain $domainCodeArtifact --domain-owner $ownerCodeArtifact --query authorizationToken --output text --duration-seconds $tokenDurationSecond",
                                    returnStdout: true
                            ).trim()
                        }
                    }
                }
            }
        }
        stage('Publish') {
            when {
                branch 'master'
            }
            environment {
                CODEARTIFACT_AUTH_TOKEN = "$token"
            }
            steps {
                script {
                    withVault(vaultSecrets: [artifactoryCredentials]) {
                        sh getGradlePublishCommand('buildSrc.gradle', env.ARTIFACTORY_REPOSITORY_NAME, gradleVersionProperties) + ' -Partifactory_user=$ARTIFACTORY_USERNAME -Partifactory_password=$ARTIFACTORY_PASSWORD'
                        sh getGradlePublishCommand('buildJava64.gradle', env.ARTIFACTORY_REPOSITORY_NAME, gradleVersionProperties) + ' -Partifactory_user=$ARTIFACTORY_USERNAME -Partifactory_password=$ARTIFACTORY_PASSWORD'
                    }
                }
            }
        }
        stage('Unit Tests') {
            options {
                timeout(time: 150, unit: 'MINUTES')
            }
            steps {
                sh executeAntBuildTestCommand('test', antTestProperties)
            }
            post {
                always {
                    junit(
                            keepLongStdio: true,
                            testResults: 'TravelTest/report/TEST-*.xml'
                    )
                    publishHTML([allowMissing         : false,
                                 alwaysLinkToLastBuild: true,
                                 keepAll              : true,
                                 reportDir            : "TravelTest/report",
                                 reportFiles          : 'index.html',
                                 reportName           : 'HTML Report',
                                 reportTitles         : ''])
                }
                unstable {
                    error("Some of the Unit Tests failed - please have a look at the test report")
                }
            }
        }
        stage('Build Installers') {
            when {
                branch 'master'
            }
            steps {
                script {
                    def triggerParameters = [[$class: 'StringParameterValue', name: 'versionET', value: buildVersion]]
                    ["easytravel-installer-linux-x86-64", "easytravel-installer-windows-x86-64", "windows-x86-64-dotNET-45"].each { platform ->
                        def triggerJob = "DemoApps/easytravel-installers/${platform}/${BRANCH_NAME.replaceAll('/', '%2F')}"
                        echo "Triggering build job: '${triggerJob}' with parameters: ${triggerParameters}"
                        build job: triggerJob, parameters: triggerParameters
                    }
                }
            }
        }
        stage('Integration tests') {
            when {
                branch 'master'
            }
            steps {
                script {
                    def triggerParameters = [[$class: 'StringParameterValue', name: 'versionET', value: buildVersion]]
                    ["windows", "linux", "distributed"].each { platform ->
                        def triggerJob = "DemoApps/easytravel-integration-tests/${platform}/${BRANCH_NAME.replaceAll('/', '%2F')}"
                        echo "Triggering build job: '${triggerJob}' with parameters: ${triggerParameters}"
                        build job: triggerJob, parameters: triggerParameters
                    }
                }
            }
        }
        stage('Trigger docker image') {
            when {
                branch 'master'
            }
            steps {
                script {
                    def triggerJob = "DemoApps/easytravel-dockerbuilder/${BRANCH_NAME.replaceAll('/', '%2F')}"
                    def triggerParameters = [[$class: 'BooleanParameterValue', name: 'TAG_LATEST', value: true],
                                             [$class: 'StringParameterValue', name: 'BUILD_VERSION', value: buildVersion]]
                    echo "Triggering build job: '${triggerJob}' with parameters: ${triggerParameters}"
                    build job: triggerJob,
                            parameters: triggerParameters,
                            wait: true
                }
            }
        }
    }
    post {
        failure {
            emailext(recipientProviders: [culprits()], subject: '$DEFAULT_SUBJECT', mimeType: 'text/html', body: '${SCRIPT, template = "managed:cluster-email.groovy"}', to: 'rp@dynatrace.com')
        }
    }
}

static String executeAntBuildTestCommand(String target, String antTestProperties) {
    return '/opt/apache-ant-1.7.1/bin/ant -file Distribution/build.xml ' + target + antTestProperties
}

static String getAntBuildCommand(String target, String antProperties) {
    return '/opt/apache-ant-1.7.1/bin/ant -file Distribution/build.xml ' + target + antProperties
}

static String getGradlePublishCommand(String target, String repository, String versionProperties) {
    return './Distribution/publishing/gradlew publish -b ./Distribution/publishing/' + target +
            versionProperties + ' -PrepositoryPath=' + repository + ' -i -s'
}

static String generateProperties(String template, String[] versionParts, List<String> propertiesNames) {
    StringBuilder builder = new StringBuilder()
    for (int i = 0; i < propertiesNames.size(); i++) {
        builder.append(String.format(template, propertiesNames[i], versionParts[i])).append(" ")
    }
    return builder.toString()
}