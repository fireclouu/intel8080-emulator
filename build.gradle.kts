tasks.register("terminalRun") {
    group = "platform"
    dependsOn(":platform:terminal:run")
}

subprojects {
    plugins.withType<JavaPlugin> {
        extensions.configure<JavaPluginExtension> {
            toolchain { languageVersion = JavaLanguageVersion.of(17) }
        }
    }
}