package net.aucutt.mindfultaco

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

    }

    override fun onResume() {
        super.onResume()
        SafetyNetInteractor.INSTANCE.launch(this)
    }

    fun geritol(view : View) {
        SafetyNetInteractor.INSTANCE.launch(this)
    }
}
