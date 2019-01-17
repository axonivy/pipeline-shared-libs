def deployP2Repository(Map config) {
  stage('deploy-p2-repo') {
    agent { 
      docker 'maven:3-alpine'
      reuseNode true
    } 
    steps {
      echo 'im running on the same node and workspace!'
    }
  }
}

  //sshagent(['zugprojenkins-ssh']) {
  //  def host = 'axonivy1@217.26.54.241'
  //  def destFolder = "/home/axonivy1/data/p2/${config.destDir}"

  ///  echo "Upload p2 repository to $host:$destFolder"
  //  sh "ssh $host mkdir -p $destFolder"
  //  sh "rsync -r ${config.sourceDir} $host:$destFolder"
  //  sh "ssh $host touch $destFolder/p2.ready"
  //}


//deployP2Repo sourceDir: 'target/repository/' destDir: 'nightly' 
