dependencies {
    dokka(projects.lbAndroidApplication)
    dokka(projects.lbAndroidFlavors)
    dokka(projects.lbAndroidLibrary)
    dokka(projects.lbCache)
    dokka(projects.lbDetekt)
    dokka(projects.lbDokka)
    dokka(projects.lbMultiplatformLibrary)
    dokka(projects.lbResources)
}

dokka {
    moduleName.set("Gradle plugins documentation")
}
