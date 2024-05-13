
def fail(String paramName) {
    echo "ERROR: no ${paramName} provided."
    throw new Exception("no ${paramName} provided.")
}

/**
 * deploy a p2 repo (unzipped) to a server with ssh
 */
def call(config = [:]) {
  def sourceFolder = config.sourceFolder
  if (!sourceFolder) {
    fail('sourceFolder')
  }
  def sshUser = config.sshUser
  if (!sshUser) {
    sshUser = 'ubuntu'
  }
  def sshHost = config.sshHost
  if (!sshHost) {
    fail('sshHost')
  }
  def targetFolder = config.targetFolder
  if (!targetFolder) {
    fail('targetFolder')
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
