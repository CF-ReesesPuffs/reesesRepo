package com.cfreesespuffs.github.giftswapper

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class EndedPartyCheck : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {  // all code to execute is put here
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ended_party)

        val button: Button = findViewById(R.id.button2) // var in Kotlin https://openclassrooms.com/en/courses/5774406-learn-kotlin/5931776-declare-and-initialize-variables
        button.setOnClickListener{
            println("You've been clicked!")
        }
    }
    // Below here to the end } is for functions
}