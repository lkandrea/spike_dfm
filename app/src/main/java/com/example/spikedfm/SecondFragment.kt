package com.example.spikedfm

import android.content.ContentValues.TAG
import android.content.Intent
import android.content.IntentSender.SendIntentException
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.Nullable
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.spikedfm.databinding.FragmentSecondBinding
import com.google.android.play.core.splitinstall.SplitInstallManager
import com.google.android.play.core.splitinstall.SplitInstallManagerFactory
import com.google.android.play.core.splitinstall.SplitInstallRequest
import com.google.android.play.core.splitinstall.SplitInstallSessionState
import com.google.android.play.core.splitinstall.SplitInstallStateUpdatedListener
import com.google.android.play.core.splitinstall.model.SplitInstallSessionStatus

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class SecondFragment : Fragment() {

    private var _binding: FragmentSecondBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentSecondBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonSecond.setOnClickListener {
            findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * DFM Code
     * */
    private var splitInstallManager: SplitInstallManager? = null
    private var mySessionID = 0
    private var splitInstallStateUpdatedListener =
        SplitInstallStateUpdatedListener { state: SplitInstallSessionState ->
            if (state.sessionId() == mySessionID) {
                when (state.status()) {
                    // Large module that has size greater than 10 MB requires user permission
                    SplitInstallSessionStatus.REQUIRES_USER_CONFIRMATION ->
                        try {
                            splitInstallManager?.startConfirmationDialogForResult(
                                state,
                                this.activity ?: return@SplitInstallStateUpdatedListener, 110
                            )
                        } catch (ex: SendIntentException) {
                            // Request failed
                        }
                    SplitInstallSessionStatus.DOWNLOADING -> {
                        Log.i(TAG, "Downloading")
                        // The module is being downloaded
                        val totalBytes = state.totalBytesToDownload().toInt()
                        val progress = state.bytesDownloaded().toInt()
                    }
                    SplitInstallSessionStatus.INSTALLING -> Log.i(TAG, "Installing")
                    SplitInstallSessionStatus.DOWNLOADED -> {
                        Log.i(TAG, "Downloaded")
                        Toast.makeText(this.context, "Module Downloaded", Toast.LENGTH_SHORT).show()
                    }
                    SplitInstallSessionStatus.INSTALLED -> {
                        Log.i(TAG, "Installed")
                        // Use the below line to call your feature module activity
                        Toast.makeText(this.context, "Module Installed", Toast.LENGTH_SHORT).show()
                        val intent = Intent()
                        intent.setClassName(
                            "com.example.app",
                            "com.example.app.feature.AnotherActivity"
                        )
                        intent.putExtra("id", "12354")
                        startActivity(intent)
                    }
                    SplitInstallSessionStatus.CANCELED -> Log.i(TAG, "Canceled")
                    SplitInstallSessionStatus.PENDING -> Log.i(TAG, "Pending")
                    SplitInstallSessionStatus.FAILED -> Log.i(TAG, "Failed")
                    SplitInstallSessionStatus.CANCELING -> {
                        TODO()
                    }
                    SplitInstallSessionStatus.UNKNOWN -> {
                        TODO()
                    }
                }
            }
        }

    override fun onStart() {
        super.onStart()

        splitInstallManager = SplitInstallManagerFactory.create(this.context ?: return)
        splitInstallManager?.registerListener(splitInstallStateUpdatedListener)

        download()
//        btnDownload.setOnClickListener { v -> onClickDownloadFeatureModule() } // Using JAVA_8
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        @Nullable data: Intent?
    ) {
//        Log.d(TAG, "onActivityResult: ")
//        if (requestCode == 110) {
//            if (resultCode == RESULT_OK) {
//                Log.i(TAG, "onActivityResult: Install Approved ")
//            }
//        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun download() {
        if (!splitInstallManager!!.installedModules.contains("some_dynamic")) {
            Log.i(TAG, "Building installer")

            val splitInstallRequest = SplitInstallRequest.newBuilder()
                .addModule("some_dynamic")
                .build()

            splitInstallManager!!.startInstall(splitInstallRequest)
                .addOnSuccessListener { result: Int ->
                    mySessionID = result
                }
                .addOnFailureListener { e: Exception ->
                    Log.i(
                        TAG,
                        "installManager: $e"
                    )
                }
        } else {
            Log.i(TAG, "Calling intent")

            val intent = Intent()
            intent.setClassName("com.example.spikedfm", "com.example.some_dynamic.DfmActivity")
            intent.putExtra("id", "12354")
            startActivity(intent)
        }
    }
}