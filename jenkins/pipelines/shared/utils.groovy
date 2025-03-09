def retryCommand(command, retries = 3) {
    int attempt = 0
    while (attempt < retries) {
        try {
            sh command
            return
        } catch (Exception e) {
            attempt++
            if (attempt >= retries) {
                error "Command failed after ${retries} attempts: ${command}"
            }
            sleep 5 // Wait before retrying
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

return this