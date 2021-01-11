package com.example.totp

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.nio.file.Files.delete
import android.R.attr.data
import android.content.DialogInterface
import android.text.method.TextKeyListener.clear
import androidx.appcompat.app.AlertDialog


class OtpListAdapter (val context: Context, val otpCards: MutableList<CardMember>): RecyclerView.Adapter<OtpListAdapter.ViewHolder>()
{
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.card_view, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = this.otpCards.count()

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(otpCards[position])

        holder.delBtn.setOnClickListener{
            //f (position != RecyclerView.NO_POSITION)
            //{
            val dialog = AlertDialog.Builder(context)
            dialog.setTitle("確定刪除?") //設定dialog 的title顯示內容
            dialog.setCancelable(false) //關閉 Android 系統的主要功能鍵(menu,home等...)
            dialog.setPositiveButton("確定", { dialogInterface, whichButton ->
                Log.d("del","position="+position)
                Log.d("del","otpCard"+position+" id="+otpCards[position].getId().toString())
                val pref = context.getSharedPreferences("totpStored", Context.MODE_PRIVATE)
                val editor = pref.edit()
                editor.remove(otpCards[position].getId().toString())
                editor.apply()
                otpCards.removeAt(position)
                notifyItemRemoved(position)
                notifyDataSetChanged()
            })
            dialog.setNeutralButton("取消", null)
            dialog.show()

            //notifyDataSetChanged()
            //}

        }
    }

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

        private val name = itemView.findViewById<TextView>(R.id.Name)
        private val password = itemView.findViewById<TextView>(R.id.PassWord)
        private val countdown = itemView.findViewById<TextView>(R.id.CountDown)
        val delBtn = itemView.findViewById<Button>(R.id.delete)

        fun bind(otp: CardMember) {
            name?.text = otp.getName()
            password?.text = otp.getPassword()
            countdown?.text = otp.getCountdown()
        }

    }
}