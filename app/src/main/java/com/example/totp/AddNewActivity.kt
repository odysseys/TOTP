package com.example.totp

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.View
import android.widget.Button
import android.widget.Toast
import com.google.zxing.integration.android.IntentIntegrator
import java.io.IOException

class AddNewActivity : AppCompatActivity(), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_new)

        val addIdBtn: Button = findViewById(R.id.add_id_btn) //新增身份識別 按鈕
        addIdBtn.setOnClickListener(this)
        val addOtpBtn: Button = findViewById(R.id.add_otp_btn) //新增OTP 按鈕
        addOtpBtn.setOnClickListener(this)
        val backBtn: Button = findViewById(R.id.back_btn) //回上一頁 按鈕
        backBtn.setOnClickListener(this)
    }

    override fun onClick(v: View?){
        when(v?.id){
            R.id.add_id_btn ->
            {

            }
            R.id.add_otp_btn ->
            {
                try {
                    var scanIntegrator = IntentIntegrator(this)
                    scanIntegrator.setPrompt("請掃描")
                    scanIntegrator.setTimeout(300000)
                    scanIntegrator.setOrientationLocked(false)
                    scanIntegrator.initiateScan()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            R.id.back_btn ->
            {
                var intent = Intent()
                intent.setClass(this, EntryActivity::class.java)
                startActivity(intent)
            }
        }
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        val scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent)
        if (scanningResult != null) {
            if (scanningResult.contents != null) {
                val scanContent = scanningResult.contents
                if (scanContent != "") {
                    Toast.makeText(applicationContext, "掃描內容: $scanContent", Toast.LENGTH_LONG).show()
                    val uri = Uri.parse(scanContent)
                    val secret = uri.getQueryParameter("secret")
                    val otpMethod = uri.host
                    val uName = uri.path!!.substring(1) //remove first '/'
                    val pref = getSharedPreferences("totpStored", Context.MODE_PRIVATE)
                    val editor = pref.edit()
                    val index = pref.getInt("index", 0)
                    //editor.putInt("id" + count, count)
                    editor.putString(index.toString(), (uName + "," + secret))
                    //editor.putString("secret" + count, secret)
                    editor.putInt("index", index + 1)
                    editor.apply()
                    if(!secret.isNullOrBlank())
                    {
                        intent?.setClass(this, MainActivity::class.java)
                        intent?.putExtra("key", secret)
                        intent?.putExtra("name", uName)
                        startActivity(intent)
                    }
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, intent)
            Toast.makeText(applicationContext, "發生錯誤", Toast.LENGTH_LONG).show()
        }
    }
}
