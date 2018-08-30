def call(currentBuild) {
  String buildResult = currentBuild.currentResult;
  if (buildResult == "SUCCESS") {
    slackSend color: "good", message: "${env.JOB_NAME} | #${env.BUILD_NUMBER} | ${currentBuild.durationString} | ${currentBuild.absoluteUrl}"
  }
  else if (buildResult == "FAILURE") { 
    slackSend color: "danger", message: "Job: ${env.JOB_NAME} with buildnumber ${env.BUILD_NUMBER} was failed"
  }
  else if( buildResult == "UNSTABLE") { 
    slackSend color: "warning", message: "Job: ${env.JOB_NAME} with buildnumber ${env.BUILD_NUMBER} was unstable"
  }
  else {
    slackSend color: "danger", message: "Job: ${env.JOB_NAME} with buildnumber ${env.BUILD_NUMBER} its resulat was unclear"	
  }
}