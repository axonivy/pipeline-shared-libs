
def fail(String paramName) {
    echo "ERROR: no ${paramName} provided."
    currentBuild.result = 'FAILURE'
}

/**
 * deploy a p2 repo (unzipped) to a server with ssh
 */
def call(config = [:]) {
  def sourceFolder = config.sourceFolder
  if (!sourceFolder) {
    fail('sourceFolder')
    return
  }
  def sshUser = config.sshUser
  if (!sshUser) {
    sshUser = 'ubuntu'
  }
  def sshHost = config.sshHost
  if (!sshHost) {
    fail('sshHost')
    return
  }
  def targetFolder = config.targetFolder
  if (!targetFolder) {
    fail('targetFolder')
    return
  }

  def host = sshUser + '@' + sshHost
  docker.image('axonivy/build-container:ssh-client-1').inside {
    sshagent(['zugprojenkins-ssh']) {
      echo "Upload p2 repo from ${sourceFolder} to ${host}:${targetFolder}"
      sh "ssh ${host} mkdir -p ${targetFolder}"
      sh "rsync -r ${sourceFolder} ${host}:${targetFolder}"
      sh "ssh ${host} touch ${targetFolder}p2.ready"
    }
  }
}
