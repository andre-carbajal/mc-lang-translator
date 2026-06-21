package net.andrecarbajal.mclangtranslator.providers

import com.google.gson.Gson
import com.google.gson.GsonBuilder

object GsonProvider {
    val gson: Gson = GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create()
}

