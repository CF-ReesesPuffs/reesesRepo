package com.cfreesespuffs.github.giftswapper.Activities

import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.amplifyframework.datastore.generated.model.Party
import com.cfreesespuffs.github.giftswapper.Adapters.PartyAdapter
import com.cfreesespuffs.github.giftswapper.R

class EndedPartyCheck : AppCompatActivity() {

    val parties: ArrayList<Party> = ArrayList()
    lateinit var endedPartyAdapter: PartyAdapter

    override fun onCreate(savedInstanceState: Bundle?) {  // all code to execute is put here
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ended_party)

        var endedPartyRv : RecyclerView = findViewById(R.id.giftRecycler);
        endedPartyRv.adapter = endedPartyAdapter
        endedPartyRv.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        endedPartyAdapter.notifyDataSetChanged()

//        val button: Button = findViewById(R.id.button2) // var in Kotlin https://openclassrooms.com/en/courses/5774406-learn-kotlin/5931776-declare-and-initialize-variables
//        button.setOnClickListener{
//            println("You've been clicked!")
//        }
    } // Below here to the end } is for functions
}