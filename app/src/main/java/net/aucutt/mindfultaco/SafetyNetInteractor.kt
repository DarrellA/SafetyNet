package net.aucutt.mindfultaco

import android.content.Context
import android.util.Log
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.safetynet.SafetyNet
import com.google.android.gms.safetynet.SafetyNetApi
import kotlinx.coroutines.*
import java.util.*


class SafetyNetInteractor {

    companion object {
        private const val TAG = "SafetyNetInteractor"
        private const val API_KEY = "5"
        val INSTANCE by lazy { SafetyNetInteractor() }
    }

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)
    private val exceptionHandler = CoroutineExceptionHandler { _, e -> Log.d(TAG, e.localizedMessage) }


    fun launch(ctx: Context) = scope.launch(exceptionHandler) {
        Log.d(TAG, "Enrolled in experiment?  ${Thread.currentThread().name}")

        if (safetyNetCheck(ctx)) {
            callSafetyNet(ctx)
        }
    }

    private fun safetyNetCheck(ctx: Context): Boolean {

        Log.d(TAG, "Safety net check ${Thread.currentThread().name}")
        if (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(ctx) != ConnectionResult.SUCCESS) {
            Log.d(TAG, "Play services not available")
            return false
        }
        if (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(
                ctx,
                13000000
            ) != ConnectionResult.SUCCESS
        ) {
            Log.d(TAG, "Attestation API not available")
            return false
        }
        Log.d(TAG, "do it do it  ${Thread.currentThread().name}")
        return true
    }

    private fun callSafetyNet(ctx: Context) {
        SafetyNet.getClient(ctx).attest(UUID.randomUUID().toString().toByteArray(), API_KEY)
            .addOnSuccessListener { attestationResponse -> parseResponse(attestationResponse) }
            .addOnFailureListener { exception -> Log.e(TAG, "Failure calling attest ${exception.message}  ${exception.localizedMessage} ") }
    }

    private fun parseResponse(jwsResponse: SafetyNetApi.AttestationResponse) {
          threadTing(jwsResponse.jwsResult)
    }

    private fun threadTing(shit : String)  =  scope.launch(exceptionHandler) {
        Log.d(TAG, "what the thread?  ${Thread.currentThread().name}")
        OnlineVerify.process(shit)
    }



}