pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://repo.maven.apache.org/maven2/") }
        maven { url = uri("https://jitpack.io") }
    }
}
rootProject.name = "fitlife_sumyatnoe"
include(":app")
 