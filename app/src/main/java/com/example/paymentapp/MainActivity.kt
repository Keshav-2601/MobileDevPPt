package com.example.paymentapp

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.paymentapp.ui.theme.PaymentAppTheme
import com.stripe.android.PaymentConfiguration
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONObject
import java.io.IOException

class MainActivity : ComponentActivity() {

    private lateinit var paymentSheet: PaymentSheet // Declare PaymentSheet
    private var clientSecret: String? = null       // To hold the client secret

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        PaymentConfiguration.init(
            applicationContext,
            "pk_test_51QP7rqG6T9vV3qd5B3zk3tu3g0UREoLp1vaEmIz5Arpk1rSifIpLMtfSaYFgKWc3GSNhRlMHI8z0weUKl372v6tq001DyDkVv3" // Replace with your actual publishable key
        )


        paymentSheet = PaymentSheet(this, ::onPaymentSheetResult)

        setContent {
            PaymentAppTheme {
                PaymentScreen { amount ->
                    createPaymentIntent(amount) // Call backend to create PaymentIntent
                }
            }
        }
    }

    @Composable
    fun PaymentScreen(onPayClick: (String) -> Unit) {
        var amount by remember { mutableStateOf("") }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TextField(
                value = amount,
                onValueChange = { amount = it }, // Update the amount on input
                label = { Text("Enter Amount") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (amount.isNotBlank()) { // Ensure amount is not empty
                        onPayClick(amount) // Trigger payment when button is clicked
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Pay")
            }
        }
    }

    // Create PaymentIntent by making a backend call
    private fun createPaymentIntent(amount: String) {
        val client = OkHttpClient()
        val url = "http://192.168.217.125:3000/create-payment-intent".trim() // Replace with your backend URL

        // Prepare JSON request body
        val jsonBody = JSONObject()
        jsonBody.put("amount", amount.toInt() * 100) // Convert amount to cents
        jsonBody.put("currency", "usd")

        val body = RequestBody.create(
            "application/json".toMediaTypeOrNull(),
            jsonBody.toString()
        )

        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        // Make the network call
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                println("Request failed: ${e.message}")
                Log.e("PaymentIntent", "Request failed", e)
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    val jsonResponse = JSONObject(responseBody ?: "")
                    clientSecret = jsonResponse.getString("message")
                    println("Request succeeded: $clientSecret")
                    Log.i("PaymentIntent", "Request succeeded: $clientSecret")
                    runOnUiThread {
                        presentPaymentSheet(clientSecret) // Present PaymentSheet
                    }
                } else {
                    println("Server error: ${response.code}")
                    Log.e("PaymentIntent", "Server error: ${response.code}")
                    runOnUiThread {
                        Toast.makeText(this@MainActivity, "Server error: ${response.code}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    // Present the PaymentSheet
    private fun presentPaymentSheet(clientSecret: String?) {
        clientSecret?.let {
            paymentSheet.presentWithPaymentIntent(
                it,
                PaymentSheet.Configuration(
                    "by Stripe"
                )
            )
        }
    }

    // Handle PaymentSheet results
    private fun onPaymentSheetResult(paymentSheetResult: PaymentSheetResult) {
        when (paymentSheetResult) {
            is PaymentSheetResult.Completed -> {
                Toast.makeText(this, "Payment Successful!", Toast.LENGTH_SHORT).show()
            }
            is PaymentSheetResult.Canceled -> {
                Toast.makeText(this, "Payment Canceled!", Toast.LENGTH_SHORT).show()
            }
            is PaymentSheetResult.Failed -> {
                Toast.makeText(this, "Payment Failed: ${paymentSheetResult.error.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}


