package com.example.alexander.trams.logic

open class Station (val id: Int, val name: String, val type: String = "Station") {
    override fun toString(): String {
        return "{id: ${id}, name: ${name}, type: ${type}}"
    }
}