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
        maven {
            url = uri("https://api.mapbox.com/downloads/v2/releases/maven")
            // Do not change the username below. It should always be "mapbox" (not your username).
            credentials.username = "mapbox"
            // Use the secret token stored in gradle.properties as the password
//
//            providers.gradleProperty("MAPBOX_DOWNLOADS_TOKEN").get()
            credentials.password = "sk.eyJ1IjoiZmFyaWR3YWpkaWEiLCJhIjoiY20yajNsdW10MDFqOTJqcjB1d3F3eG9rNCJ9.yki0bdm6plcwr2d3vRuNeg"
            authentication.create<BasicAuthentication>("basic")
        }
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://api.mapbox.com/downloads/v2/releases/maven")
            // Do not change the username below. It should always be "mapbox" (not your username).
            credentials.username = "mapbox"
            // Use the secret token stored in gradle.properties as the password
//
//            providers.gradleProperty("MAPBOX_DOWNLOADS_TOKEN").get()
            credentials.password = "sk.eyJ1IjoiZmFyaWR3YWpkaWEiLCJhIjoiY20yajNsdW10MDFqOTJqcjB1d3F3eG9rNCJ9.yki0bdm6plcwr2d3vRuNeg"
            authentication.create<BasicAuthentication>("basic")
        }
    }
}

rootProject.name = "Tugas_Maps"
include(":app")
 