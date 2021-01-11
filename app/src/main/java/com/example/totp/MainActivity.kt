package com.example.totp

import android.Manifest
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import java.nio.ByteBuffer
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.experimental.and
import kotlin.math.pow
import java.util.*
import android.os.CountDownTimer
import org.apache.commons.codec.binary.Base32
import android.content.pm.PackageManager
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.core.app.ActivityCompat
import java.io.IOException
import com.google.zxing.integration.android.IntentIntegrator
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var countDown: TextView
    private lateinit var qrCode: TextView
    var base32key = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567"
    val otpList = ArrayList<CardMember>()
    lateinit var mRecycler: RecyclerView
    private lateinit var addBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        getPermissionsCamera()
        countDown = findViewById(R.id.CountDown) //計時器秒數TextView
        qrCode = findViewById(R.id.QRcode) //顯示QR code內容
        addBtn = findViewById(R.id.addBtn) //+按鈕
        addBtn.setOnClickListener(this)
        updateTOTPHandler.postDelayed(updateTimerThread, 0)//每到系統時間0/30秒時重複執行

        mRecycler = findViewById(R.id.otp_recycleView)
        mRecycler.layoutManager = LinearLayoutManager(this)
        mRecycler.adapter = OtpListAdapter(this, otpList)

        val pref = getSharedPreferences("totpStored", Context.MODE_PRIVATE)
        val items = pref.all
        items.remove("index")
        var clist : MutableList<CardMember> = mutableListOf()
        for(item in items){
            val data = pref.getString(item.key, "")
            val datas = data!!.split(",")
            clist.add(CardMember(item.key.toInt(), datas[0], genTotp(datas[1])))
        }
        val orderedList = clist.sortedByDescending{it.getId()} //從新到舊的順序放到otpList

        otpList.addAll(orderedList)
        (mRecycler.adapter)?.notifyItemInserted(0)
        (mRecycler.adapter)?.notifyItemRangeChanged(0,items.size)
        (mRecycler.adapter)?.notifyDataSetChanged()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.addBtn -> {
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
        }
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        val scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent)
        if (scanningResult != null) {
            if (scanningResult.contents != null) {
                val scanContent = scanningResult.contents
                if (scanContent != "") {
                    //Toast.makeText(applicationContext, "掃描內容: $scanContent", Toast.LENGTH_LONG).show()
                    qrCode.text = scanContent
                    val uri = Uri.parse(scanContent)
                    val secret = uri.getQueryParameter("secret")
                    val otpMethod = uri.host
                    val uName = uri.path!!.substring(1) //remove first '/'
                    Toast.makeText(applicationContext, "UserName: $uName", Toast.LENGTH_LONG).show()
                    if(!secret.isNullOrBlank())
                    {
                        base32key = secret
                        //updateTOTPHandler.post(updateTimerThread) //更新PassWord
                    }
                    val pref = getSharedPreferences("totpStored", Context.MODE_PRIVATE)
                    val editor = pref.edit()
                    val index = pref.getInt("index", 0)
                    editor.putString(index.toString(), (uName + "," + secret))
                    editor.putInt("index", index + 1)
                    editor.apply()
                    otpList.add(0,CardMember(index, uName, genTotp(base32key)))
                    (mRecycler.adapter)?.notifyItemInserted(0)
                    (mRecycler.adapter)?.notifyDataSetChanged()
                }

            }
        } else {
            super.onActivityResult(requestCode, resultCode, intent)
            Toast.makeText(applicationContext, "發生錯誤", Toast.LENGTH_LONG).show()
        }
    }

    val updateTOTPHandler = android.os.Handler()
    private val updateTimerThread = object : Runnable {
        override fun run() {
            val pref = getSharedPreferences("totpStored", Context.MODE_PRIVATE)
            for(i in 0 until otpList.size){
                Log.d("updOTP", otpList[i].getId().toString())
                Log.d("updOTP", pref.getString(otpList[i].getId().toString(), "")!!.split(",")[1])
                otpList[i] = CardMember(otpList[i].getId(), otpList[i].getName()!!, genTotp(pref.getString(otpList[i].getId().toString(), "")!!.split(",")[1]))
                (mRecycler.adapter)?.notifyItemChanged(i)
            }
            val counterTime = 30000 - System.currentTimeMillis().rem(30000) //下次更新的時間間隔
            var countDownVal = counterTime.div(1000) + 2 //計時器開始的秒數 +2: shift from -1~28 to 1~30
            val timer = object: CountDownTimer(counterTime, 1000) {
                override fun onTick(millisUntilFinished: Long)
                {
                    countDownVal--
                    countDown.text = countDownVal.toString()
                }
                override fun onFinish() {}
            }
            timer.start()
            Log.d("code","System.currentTimeMillis().rem(30000)=" + System.currentTimeMillis().rem(30000))
            updateTOTPHandler.postDelayed(this, counterTime)
        }
    }

    private fun genTotp(base32key: String) : String {
        val time_period = Date(System.currentTimeMillis()) //ms from 1970/1/1 UTC
        val time_step = 30000 //ms
        var counter:Long
        if(time_step == 0) counter = 0 else counter = (time_period.time.div(time_step)) //避免除數＝0
        //Log.d("code", "time_period=" + time_period.time.toInt() + ", counter=" + counter)
        val message = ByteBuffer.allocate(8).putLong(counter).array() //設定seed
        val codeDigits = 6 //設定輸出密碼為6位數
        val mac = Mac.getInstance("HmacSHA1")
        val key: ByteArray = base32key.base32Decode() //把base32 key decode成byte array
        val secretKey = SecretKeySpec(key, mac.algorithm)
        mac.init(secretKey)
        val hash = mac.doFinal(message)
        val offset = hash.last().and(0x0F).toInt()
        var binaryInt:Int = 0
        for(i in 0..3){
            binaryInt = binaryInt shl 8
            //Log.d("code","binaryn1=" + binaryInt + ", i=" + i)
            if(i==0){
                binaryInt = binaryInt or (hash[offset + i] and 0x7F.toByte()).toInt()
            }
            else{
                binaryInt = binaryInt or ((hash[offset + i] and 0xFF.toByte()).toInt() shl 24 ushr 24) //避免負數時高位補1
                //Log.d("code", "hash[offset + i] and 0xFF=" + ((hash[offset + i] and 0xFF.toByte()).toInt() shl 24 ushr 24))
            }
            //Log.d("code","binaryn2=" + binaryInt + ", i=" + i)
        }
        val codeInt = binaryInt.rem(10.0.pow(codeDigits).toInt())
        var codeString = codeInt.toString()
        if(codeString.length < codeDigits)
        {
            codeString = "0".repeat(codeDigits - codeString.length) + codeString
        }
        return codeString
    }

    fun String.base32Decode(): ByteArray {
        val base32Codec = Base32()
        return base32Codec.decode(this)
    }

    fun getPermissionsCamera(){
        if(ActivityCompat.checkSelfPermission(this,Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,arrayOf(Manifest.permission.CAMERA),1)
        }
    }
}