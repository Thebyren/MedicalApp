package com.medical.app.util

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

/**
 * Administrador de permisos en tiempo de ejecución para Android.
 * Facilita la solicitud y verificación de permisos de manera más limpia y mantenible.
 */
class PermissionManager private constructor(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner? = null
) : DefaultLifecycleObserver {

    private var onPermissionGranted: (() -> Unit)? = null
    private var onPermissionDenied: ((List<String>) -> Unit)? = null
    private var onPermissionPermanentlyDenied: ((List<String>) -> Unit)? = null

    private var permissionLauncher: ActivityResultLauncher<Array<String>>? = null

    companion object {
        private const val PERMISSION_REQUEST_CODE = 1000

        /**
         * Crea una nueva instancia de [PermissionManager] para un Fragment.
         */
        fun from(fragment: Fragment): PermissionManager {
            val manager = PermissionManager(fragment.requireContext(), fragment)
            manager.setupLauncher(fragment)
            return manager
        }

        /**
         * Crea una nueva instancia de [PermissionManager] para una ComponentActivity.
         */
        fun from(activity: ComponentActivity): PermissionManager {
            val manager = PermissionManager(activity, activity)
            manager.setupLauncher(activity)
            return manager
        }

        /**
         * Crea una nueva instancia de [PermissionManager] para una Activity tradicional.
         * Nota: Para Activities tradicionales, se debe llamar manualmente a handlePermissionResult()
         */
        fun from(activity: Activity): PermissionManager {
            return PermissionManager(activity)
        }
    }

    init {
        lifecycleOwner?.lifecycle?.addObserver(this)
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        cleanup()
    }

    private fun setupLauncher(fragment: Fragment) {
        permissionLauncher = fragment.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { results ->
            handlePermissionResults(results)
        }
    }

    private fun setupLauncher(activity: ComponentActivity) {
        permissionLauncher = activity.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { results ->
            handlePermissionResults(results)
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
     * Verifica si algún permiso ha sido denegado permanentemente.
     * @param permissions Lista de permisos a verificar
     * @return Lista de permisos denegados permanentemente
     */
    fun getPermanentlyDeniedPermissions(vararg permissions: String): List<String> {
        return permissions.filter { permission ->
            ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED &&
                    !shouldShowRequestPermissionRationale(permission)
        }
    }

    /**
     * Verifica si se debe mostrar la explicación de por qué se necesita un permiso.
     * @param permission Permiso a verificar
     * @return true si se debe mostrar la explicación, false en caso contrario
     */
    fun shouldShowRequestPermissionRationale(permission: String): Boolean {
        return when (context) {
            is ComponentActivity -> ActivityCompat.shouldShowRequestPermissionRationale(context, permission)
            is Activity -> ActivityCompat.shouldShowRequestPermissionRationale(context, permission)
            else -> false
        }
    }

    /**
     * Configura el callback para cuando se otorgan todos los permisos solicitados.
     */
    fun onPermissionGranted(callback: () -> Unit): PermissionManager {
        this.onPermissionGranted = callback
        return this
    }

    /**
     * Configura el callback para cuando se deniegan algunos permisos temporalmente.
     */
    fun onPermissionDenied(callback: (List<String>) -> Unit): PermissionManager {
        this.onPermissionDenied = callback
        return this
    }

    /**
     * Configura el callback para cuando se deniegan permanentemente algunos permisos.
     */
    fun onPermissionPermanentlyDenied(callback: (List<String>) -> Unit): PermissionManager {
        this.onPermissionPermanentlyDenied = callback
        return this
    }

    /**
     * Solicita permisos en tiempo de ejecución.
     * @param permissions Lista de permisos a solicitar
     */
    fun requestPermissions(vararg permissions: String) {
        // Filtrar solo los permisos que no están otorgados
        val permissionsToRequest = permissions.filter { permission ->
            ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED
        }

        if (permissionsToRequest.isEmpty()) {
            onPermissionGranted?.invoke()
            return
        }

        when {
            permissionLauncher != null -> {
                // Usar el nuevo sistema de Activity Result API
                permissionLauncher?.launch(permissionsToRequest.toTypedArray())
            }
            context is Activity -> {
                // Fallback para Activities tradicionales
                ActivityCompat.requestPermissions(
                    context,
                    permissionsToRequest.toTypedArray(),
                    PERMISSION_REQUEST_CODE
                )
            }
            else -> {
                throw IllegalStateException(
                    "Context must be an Activity, ComponentActivity, or use from() with Fragment/ComponentActivity"
                )
            }
        }
    }

    /**
     * Maneja el resultado de la solicitud de permisos para Activities tradicionales.
     * Debe ser llamado desde el método onRequestPermissionsResult de la Activity.
     * @param requestCode Código de solicitud
     * @param permissions Lista de permisos solicitados
     * @param grantResults Resultados de la concesión de permisos
     */
    fun handlePermissionResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode != PERMISSION_REQUEST_CODE) return

        val resultsMap = permissions.mapIndexed { index, permission ->
            permission to (grantResults.getOrNull(index) == PackageManager.PERMISSION_GRANTED)
        }.toMap()

        handlePermissionResults(resultsMap)
    }

    private fun handlePermissionResults(results: Map<String, Boolean>) {
        val grantedPermissions = results.filter { it.value }.keys.toList()
        val deniedPermissions = results.filterNot { it.value }.keys.toList()

        if (deniedPermissions.isEmpty()) {
            // Todos los permisos fueron otorgados
            onPermissionGranted?.invoke()
            return
        }

        // Separar permisos denegados temporalmente vs permanentemente
        val permanentlyDenied = deniedPermissions.filter { permission ->
            !shouldShowRequestPermissionRationale(permission)
        }

        val temporarilyDenied = deniedPermissions.filter { permission ->
            shouldShowRequestPermissionRationale(permission)
        }

        // Ejecutar callbacks apropiados
        when {
            permanentlyDenied.isNotEmpty() -> {
                onPermissionPermanentlyDenied?.invoke(permanentlyDenied)
            }
            temporarilyDenied.isNotEmpty() -> {
                onPermissionDenied?.invoke(temporarilyDenied)
            }
        }
    }

    /**
     * Limpia los recursos del PermissionManager.
     */
    private fun cleanup() {
        onPermissionGranted = null
        onPermissionDenied = null
        onPermissionPermanentlyDenied = null
        permissionLauncher = null
    }
}