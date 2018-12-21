def call(Map config) {
	configFileProvider([configFile(fileId: 'global-maven-settings', variable: 'GLOBAL_MAVEN_SETTINGS')]) {
		sh "mvn --batch-mode -gs $GLOBAL_MAVEN_SETTINGS -e ${config.cmd} "
	}
}
