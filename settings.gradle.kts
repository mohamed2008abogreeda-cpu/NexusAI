pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // Required for nodejs-mobile-android if not published to maven central
        maven { url = uri("https://jitpack.io") } 
    }
}

rootProject.name = "NexusAI"
include(":app")
