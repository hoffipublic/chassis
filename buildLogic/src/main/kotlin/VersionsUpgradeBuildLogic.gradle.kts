// only to be applied to ROOT projects and not(!) to multiproject subprojects!
plugins {
    id("com.github.ben-manes.versions")
    id("nl.littlerobots.version-catalog-update")
    id("se.ascp.gradle.gradle-versions-filter")
}

versionCatalogUpdate {
    val libsVersionsTomlFile = libsVersionsTomlFile()
    catalogFile.set(libsVersionsTomlFile) // file(File()) // TODO remove comment
    keep {
        keepUnusedVersions.set(true)
        keepUnusedLibraries.set(true)
        keepUnusedPlugins.set(true)
    }
}
versionsFilter {
    outPutFormatter.set("json,plain") // you at least need "json"
    strategy.set(se.ascp.gradle.Strategy.EXCLUSIVE)
    exclusiveQualifiers.addAll("beta","rc","cr","m","preview","b" )
    checkForGradleUpdate.set(true)
    log.set(false)
}
