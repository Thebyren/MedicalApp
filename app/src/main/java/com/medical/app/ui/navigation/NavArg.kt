package com.medical.app.ui.navigation

/**
 * Sealed class that defines all possible navigation arguments in the app.
 */
sealed class NavArg(val key: String) {
    object PatientId : NavArg("patientId")
    // Add other navigation arguments here as needed
    
    companion object {
        /**
         * Get the argument key as a string that can be used with Safe Args.
         */
        operator fun invoke(arg: NavArg): String = arg.key
    }
}

/**
 * Extension function to get the string value of a NavArg.
 */
val NavArg.safeArgs: String get() = key
