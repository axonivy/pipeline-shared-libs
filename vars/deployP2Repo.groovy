// deploy a p2 directory to our p2 hosting infrastructure
// param sourceDir: source directory in your workspace
// param destDir: destination directory on server
// example: deployP2Repo sourceDir: 'target/repository/' destDir: 'nightly'

def call(Map config) {
  def destDir = config.destDir
  if (destDir == '') {
    throw new Exception("destDir is empty")
  }

  sshagent(['zugprojenkins-ssh']) {
    def host = 'axonivy1@217.26.54.241'
    def destFolder = "/home/axonivy1/data/p2/${destDir}"

    echo "Upload p2 repository to $host:$destFolder"
    sh "ssh $host mkdir -p $destFolder"
    sh "rsync -r ${config.sourceDir} $host:$destFolder"
    sh "ssh $host touch $destFolder/p2.ready"
  }
}
