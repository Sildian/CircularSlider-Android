package com.sildian.apps.circularslider

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.sildian.apps.circularsliderlibrary.ValueFormatter
import kotlinx.android.synthetic.main.activity_main.*
import java.text.NumberFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val circularSlider = activity_main_circular_slider_3
        circularSlider.valueFormatter = CurrencyFormatter()

        circularSlider.addOnValueChangedListener { view, value ->
            Toast.makeText(this, value.toString(), Toast.LENGTH_SHORT).show()
        }
    }

    private class CurrencyFormatter: ValueFormatter {

        override fun formatValue(value: Int, context: Context?): String =
            NumberFormat.getCurrencyInstance(Locale.UK).format(value)
    }
}