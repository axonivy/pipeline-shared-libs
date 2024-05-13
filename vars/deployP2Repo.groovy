
/**
 * deploy a p2 repo (unzipped) to a server with ssh
 *
 * @param sourceFolder source directory of the p2 repo in your workspace
 * @param sshUser user name used to login via ssh on the target server (default=ubuntu)
 * @param sshHost host name or ip of the target ssh server
 * @param targetFolder target directory on server
 */
def call(String sourceFolder, String sshUser = 'ubuntu', String sshHost, String targetFolder) {
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
