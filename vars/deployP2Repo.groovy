

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
  Boolean updateCompositeRepo = config.updateCompositeRepo
  if (!updateCompositeRepo) {
    updateCompositeRepo = false
  }
  // simplify later, we always separate repoPath and qualifier
  def qualifier = config.qualifier
  if (!qualifier) {
    if (updateCompositeRepo) {
      throw new Exception("qualifier must be set if updateCompositeRepo='true'.")
    }
  }
  def repoPath = config.repoPath
  if (!repoPath) {
    if (updateCompositeRepo) {
      throw new Exception("repoPath must be set if updateCompositeRepo='true'.")
    }
  }
  def targetRootPath = config.targetRootPath
  if (!targetRootPath) {
    if (updateCompositeRepo) {
      throw new Exception("targetRootPath must be set if updateCompositeRepo='true'.")
    }
  } else {
    targetFolder = "${targetRootPath}/${repoPath}/${qualifier}/"
  }
  if (!targetFolder) {
    fail('targetFolder')
  }

  if (updateCompositeRepo) {
    generateCompositeRepo(repoPath, qualifier)
  }

  def host = sshUser + '@' + sshHost
  docker.image('axonivy/build-container:ssh-client-1').inside {
    sshagent(['zugprojenkins-ssh']) {
      echo "Upload p2 repo from ${sourceFolder} to ${host}:${targetFolder}"
      sh "ssh ${host} mkdir -p ${targetFolder}"
      sh "rsync -r ${sourceFolder} ${host}:${targetFolder}"
      sh "ssh ${host} touch ${targetFolder}p2.ready"
      if (updateCompositeRepo) {
        def targetCompositePath = "${targetRootPath}/${repoPath}/"
        echo "Upload p2 composite repository to ${host}:${targetCompositePath}"
        sh "scp target/composite-repo/compositeArtifacts.xml ${host}:${targetCompositePath}"
        sh "scp target/composite-repo/compositeContent.xml ${host}:${targetCompositePath}"
        sh "scp target/composite-repo/p2.index ${host}:${targetCompositePath}"
      }
    }
  }
}

def fail(String paramName) {
    echo "ERROR: no ${paramName} provided."
    throw new Exception("no ${paramName} provided.")
}

def generateCompositeRepo(String repoPath, String qualifier) {
  def timestamp = String.valueOf(new Date().getTime())
  def compositeArtifactsXmlContent = """<?xml version='1.0' encoding='UTF-8'?>
<?compositeArtifactRepository version='1.0.0'?>
<repository name='${repoPath} composite repository' type='org.eclipse.equinox.internal.p2.artifact.repository.CompositeArtifactRepository' version='1.0.0'>
  <properties size='3'>
    <property name='p2.timestamp' value='${timestamp}'/>
    <property name='p2.compressed' value='false'/>
    <property name='p2.atomic.composite.loading' value='true'/>
  </properties>
  <children size='1'>
    <child location='${qualifier}'/>
  </children>
</repository>
"""

  def compositeContentXmlContent = """<?xml version='1.0' encoding='UTF-8'?>
<?compositeMetadataRepository version='1.0.0'?>
<repository name='${repoPath} composite repository' type='org.eclipse.equinox.internal.p2.metadata.repository.CompositeMetadataRepository' version='1.0.0'>
  <properties size='3'>
    <property name='p2.timestamp' value='${timestamp}'/>
    <property name='p2.compressed' value='false'/>
    <property name='p2.atomic.composite.loading' value='true'/>
  </properties>
  <children size='1'>
    <child location='${qualifier}'/>
  </children>
</repository>
"""

  def p2IndexContent = """version=1
metadata.repository.factory.order=compositeContent.xml,\\!
artifact.repository.factory.order=compositeArtifacts.xml,\\!
"""

  dir('target/composite-repo') {
    writeFile encoding: 'UTF-8', file: 'compositeArtifacts.xml', text: compositeArtifactsXmlContent
    writeFile encoding: 'UTF-8', file: 'compositeContent.xml', text: compositeContentXmlContent
    writeFile encoding: 'UTF-8', file: 'p2.index', text: p2IndexContent
  }
}
