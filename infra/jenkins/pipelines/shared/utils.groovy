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