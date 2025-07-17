pluginManagement {
    repositories {
        google()
        jcenter()
        mavenCentral()
        maven(url = "https://jitpack.io")
        gradlePluginPortal()
        maven ( url = "https://dl-maven-android.mintegral.com/repository/mbridge_android_sdk_oversea" )
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        jcenter()
        maven(url = "https://jitpack.io")
        mavenCentral()
        maven ( url = "https://dl-maven-android.mintegral.com/repository/mbridge_android_sdk_oversea" )
    }
}

rootProject.name = "BudgetExpenseManager"
include(":app")
 