dependencies {
    dokka(projects.lbAndroidApplication)
    dokka(projects.lbAndroidFlavors)
    dokka(projects.lbAndroidLibrary)
    dokka(projects.lbDetekt)
    dokka(projects.lbDokka)
    dokka(projects.lbMultiplatformLibrary)
    dokka(projects.lbResources)
    dokka(projects.lbCache)
}

dokka {
    moduleName.set("Gradle plugins documentation")
}
