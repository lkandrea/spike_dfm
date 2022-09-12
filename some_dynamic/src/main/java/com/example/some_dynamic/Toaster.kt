package com.example.some_dynamic

import android.content.Context
import android.widget.Toast

class Toaster() {
    fun showToast(context: Context) {
        Toast.makeText(context, "Hi DFM", Toast.LENGTH_SHORT).show()
    }
}