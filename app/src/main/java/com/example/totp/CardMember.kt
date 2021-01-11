package com.example.totp

class CardMember {
    private var id: Int = 0
    private var name: String? = null
    private var password: String? = null
    private var countdown: String? = null

    constructor(id: Int, name: String, password: String) {
        this.id = id
        this.name = name
        this.password = password
    }

    fun getId(): Int {
        return id
    }

    fun setId(id: Int) {
        this.id = id
    }

    fun getName(): String? {
        return name
    }

    fun setName(name: String) {
        this.name = name
    }

    fun getPassword(): String? {
        return password
    }

    fun setPassword(password: String) {
        this.password = password
    }

    fun getCountdown(): String? {
        return countdown
    }

    fun setCountdown(countdown: String) {
        this.countdown = countdown
    }
}