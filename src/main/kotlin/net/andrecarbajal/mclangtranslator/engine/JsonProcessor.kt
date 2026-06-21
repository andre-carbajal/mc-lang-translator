package net.andrecarbajal.mclangtranslator.engine

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import net.andrecarbajal.mclangtranslator.providers.GsonProvider
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path

object JsonProcessor {
    fun read(sourceFile: VirtualFile): LinkedHashMap<String, String> {
        val text = String(sourceFile.contentsToByteArray(), StandardCharsets.UTF_8).removePrefix("\uFEFF")
        val root = JsonParser.parseString(text)
        require(root.isJsonObject) { "${sourceFile.name} must be a flat JSON object" }
        val data = linkedMapOf<String, String>()
        root.asJsonObject.entrySet().forEach { (key, value) ->
            require(value.isJsonPrimitive && value.asJsonPrimitive.isString) {
                "${sourceFile.name} must be a flat String-to-String JSON map; invalid key: $key"
            }
            data[key] = value.asString
        }
        return data
    }

    fun readIfExists(path: Path): LinkedHashMap<String, String> {
        if (!Files.exists(path)) return linkedMapOf()
        val root = JsonParser.parseString(Files.readString(path, StandardCharsets.UTF_8).removePrefix("\uFEFF"))
        if (!root.isJsonObject) return linkedMapOf()
        val data = linkedMapOf<String, String>()
        root.asJsonObject.entrySet().forEach { (key, value) ->
            if (value.isJsonPrimitive && value.asJsonPrimitive.isString) {
                data[key] = value.asString
            }
        }
        return data
    }

    fun write(sourceFile: VirtualFile, locale: String, data: LinkedHashMap<String, String>): Path {
        val outputPath = sourceFile.parent.toNioPath().resolve("$locale.json")
        val jsonObject = JsonObject()
        data.forEach { (key, value) -> jsonObject.addProperty(key, value) }
        Files.writeString(outputPath, GsonProvider.gson.toJson(jsonObject), StandardCharsets.UTF_8)
        VfsUtil.markDirtyAndRefresh(false, false, false, outputPath.toFile())
        return outputPath
    }
}

