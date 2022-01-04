def call(Map config) {

  branch = config.branch
  message = config.message

  sh "git config --global user.name 'ivy-techuser'"
  sh "git config --global user.email 'support@axonivy.com'"
  sh "git checkout -b ${branch}"
  sh "git commit -a -m '${message}'"
  withEnv(['GIT_SSH_COMMAND=ssh -o StrictHostKeyChecking=no']) {
    sshagent(credentials: ['bitbucket-ivy-techuser']) {
      sh "git push -u origin ${branch}"
    }
  }

  /*
  def shellScript = libraryResource 'bitbucketPr.sh'
  withCredentials([usernamePassword(credentialsId: 'ivy-techuser-bitbucket', usernameVariable: 'user', passwordVariable: 'password')]) {
    sh "${shellScript} '${branch}' '${branch}' '${message}'"
  }
  */
}
