
package com.es.camerax

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.Navigation

private const val PERMISSIONS_REQUEST_CODE = 10
private val PERMISSIONS_REQUIRED = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO)

/**
 * The sole purpose of this fragment is to request permissions and, once granted, display the
 * camera fragment to the user.
 */
class PermissionsFragment : Fragment() {
    val navOptions = NavOptions.Builder().setPopUpTo(R.id.permissionsFragment, true).build()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!hasPermissions()) {
            // Request camera-related permissions
            requestPermissions(PERMISSIONS_REQUIRED, PERMISSIONS_REQUEST_CODE)
        } else {
            // If permissions have already been granted, proceed
            Navigation.findNavController(requireActivity(), R.id.fragment_container)
                    .navigate(R.id.action_permissions_to_camera, null,
                            navOptions)
        }

    }

    private fun hasPermissions(): Boolean {
        for (permission in PERMISSIONS_REQUIRED) {
            if (ContextCompat.checkSelfPermission(requireContext(), permission) !=
                    PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

    override fun onRequestPermissionsResult(
            requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Take the user to the success fragment when permission is granted
                Toast.makeText(context, "Permission request granted", Toast.LENGTH_LONG).show()
                Navigation.findNavController(requireActivity(), R.id.fragment_container)
                        .navigate(R.id.action_permissions_to_camera, null,
                                navOptions)
            } else {
                Toast.makeText(context, "Permission request denied", Toast.LENGTH_LONG).show()
            }
        }
    }
}
