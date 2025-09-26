package com.medical.app.util

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

/**
 * Administrador de permisos en tiempo de ejecución para Android.
 * Facilita la solicitud y verificación de permisos de manera más limpia y mantenible.
 */
class PermissionManager private constructor(
    private val context: Context
) {
    private var onPermissionGranted: (() -> Unit)? = null
    private var onPermissionDenied: ((List<String>) -> Unit)? = null
    private var onPermissionPermanentlyDenied: ((List<String>) -> Unit)? = null
    
    companion object {
        /**
         * Crea una nueva instancia de [PermissionManager] para un Fragment.
         */
        fun from(fragment: Fragment): PermissionManager {
            return PermissionManager(fragment.requireContext())
        }
        
        /**
         * Crea una nueva instancia de [PermissionManager] para una Activity.
         */
        fun from(activity: Activity): PermissionManager {
            return PermissionManager(activity)
        }
    }
    
    /**
     * Verifica si se han otorgado todos los permisos solicitados.
     * @param permissions Lista de permisos a verificar
     * @return true si todos los permisos están otorgados, false en caso contrario
     */
    fun hasPermissions(vararg permissions: String): Boolean {
        return permissions.all { permission ->
            ContextCompat.checkSelfPermission(
                context,
                permission
            ) == PackageManager.PERMISSION_GRANTED
        }
    }
    
    /**
     * Verifica si se debe mostrar la explicación de por qué se necesita un permiso.
     * @param permission Permiso a verificar
     * @return true si se debe mostrar la explicación, false en caso contrario
     */
    fun shouldShowRequestPermissionRationale(permission: String): Boolean {
        return when (context) {
            is AppCompatActivity -> (context as AppCompatActivity).shouldShowRequestPermissionRationale(permission)
            is android.app.Activity -> (context as android.app.Activity).shouldShowRequestPermissionRationale(permission)
            else -> false
        }
    }
    
    /**
     * Configura el callback para cuando se otorgan los permisos.
     */
    fun onPermissionGranted(callback: () -> Unit): PermissionManager {
        this.onPermissionGranted = callback
        return this
    }
    
    /**
     * Configura el callback para cuando se deniegan los permisos.
     */
    fun onPermissionDenied(callback: (List<String>) -> Unit): PermissionManager {
        this.onPermissionDenied = callback
        return this
    }
    
    /**
     * Configura el callback para cuando se deniegan permanentemente los permisos.
     */
    fun onPermissionPermanentlyDenied(callback: (List<String>) -> Unit): PermissionManager {
        this.onPermissionPermanentlyDenied = callback
        return this
    }
    
    /**
     * Maneja el resultado de la solicitud de permisos.
     * Debe ser llamado desde el método onRequestPermissionsResult de la Activity o Fragment.
     * @param requestCode Código de solicitud
     * @param permissions Lista de permisos solicitados
     * @param grantResults Resultados de la concesión de permisos
     */
    fun handlePermissionResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            val grantedPermissions = mutableListOf<String>()
            val deniedPermissions = mutableListOf<String>()
            val permanentlyDeniedPermissions = mutableListOf<String>()
            
            permissions.forEachIndexed { index, permission ->
                if (grantResults[index] == PackageManager.PERMISSION_GRANTED) {
                    grantedPermissions.add(permission)
                } else {
                    if (shouldShowRequestPermissionRationale(permission)) {
                        deniedPermissions.add(permission)
                    } else {
                        permanentlyDeniedPermissions.add(permission)
                    }
                }
            }
            
            when {
                deniedPermissions.isNotEmpty() -> onPermissionDenied?.invoke(deniedPermissions)
                permanentlyDeniedPermissions.isNotEmpty() -> onPermissionPermanentlyDenied?.invoke(permanentlyDeniedPermissions)
                else -> onPermissionGranted?.invoke()
            }
        }
    }
    
    /**
     * Solicita permisos en tiempo de ejecución.
     * @param permissions Lista de permisos a solicitar
     */
    fun requestPermissions(vararg permissions: String) {
        when (context) {
            is AppCompatActivity -> {
                val activity = context as AppCompatActivity
                val launcher = activity.activityResultRegistry.register(
                    "permission_launcher",
                    ActivityResultContracts.RequestMultiplePermissions()
                ) { results ->
                    handlePermissionResults(results)
                }
                launcher.launch(permissions.toList().toTypedArray())
            }
            is android.app.Activity -> {
                val activity = context as android.app.Activity
                activity.requestPermissions(permissions.toTypedArray(), PERMISSION_REQUEST_CODE)
            }
            else -> throw IllegalStateException("Context must be an Activity or AppCompatActivity")
        }
    }
    
    private fun handlePermissionResults(results: Map<String, Boolean>) {
        val grantedPermissions = results.filter { it.value }.keys.toList()
        val deniedPermissions = results.filterNot { it.value }.keys.toList()
        
        if (deniedPermissions.isEmpty()) {
            onPermissionGranted?.invoke()
        } else {
            val permanentlyDenied = deniedPermissions.filter { permission ->
                !shouldShowRequestPermissionRationale(permission)
            }
            
            val temporarilyDenied = deniedPermissions - permanentlyDenied.toSet()
            
            if (temporarilyDenied.isNotEmpty()) {
                onPermissionDenied?.invoke(temporarilyDenied)
            }
            
            if (permanentlyDenied.isNotEmpty()) {
                onPermissionPermanentlyDenied?.invoke(permanentlyDenied)
            }
        }
    }
    
    private companion object {
        private const val PERMISSION_REQUEST_CODE = 1000
    }
}
