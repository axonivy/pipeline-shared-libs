Boolean call(Map config) {
  def branchName = config.branchName
  branchName = branchName ?: env.BRANCH_NAME
  return branchName == 'master' || branchName.startsWith('release/')
}
