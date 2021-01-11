package com.example.totp

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.view.View





class EntryActivity : AppCompatActivity(), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_entry)

        val setupBtn: Button = findViewById(R.id.start_setup_btn) //開始設定 按鈕
        setupBtn.setOnClickListener(this)
        val pref = getSharedPreferences("totpStored", Context.MODE_PRIVATE)
        val items = pref.all
        items.remove("index")
        if (items.size > 0){
            var intent = Intent()
            intent.setClass(this, MainActivity::class.java)
            startActivity(intent)
        }

    }

    override fun onClick(v: View?){
        when(v?.id){
            R.id.start_setup_btn ->
            {
                var intent = Intent()
                intent.setClass(this, AddNewActivity::class.java)
                startActivity(intent)
            }
        }
    }

}
