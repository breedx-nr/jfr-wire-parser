
allprojects {
    group = "com.newrelic.jfr"
    repositories {
        mavenLocal()
        mavenCentral()
    }
}

apply(plugin = "java-library")
apply(plugin = "maven-publish")

dependencies {
	"api"("com.newrelic.telemetry:telemetry:0.7.0")
	"api"("com.newrelic.telemetry:telemetry-http-okhttp:0.7.0")
}

configure<JavaPluginExtension> {
	withSourcesJar()
	withJavadocJar()
}

tasks.named<Test>("test") {
	useJUnitPlatform()
	testLogging {
		events("passed", "skipped", "failed")
	}
}
