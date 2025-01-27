package org.zoid.latestZoidCore.utils

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import net.kyori.adventure.text.minimessage.MiniMessage
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.concurrent.CompletableFuture

object TierUtils {

    private const val TIER_URL = "https://mctiers.com/api/rankings/"
    private const val MOJANG_API_URL = "https://api.mojang.com/users/profiles/minecraft/"

    enum class PlayerTier(val tierValue: Int, val displayName: String) {
        LT5(1, "<#D3D3D3>LT5"), HT5(2, "<#808080>HT5"),
        LT4(3, "<#90EE90>LT4"), HT4(4, "<#006400>HT4"),
        LT3(5, "<#EEE8AA>LT3"), HT3(6, "<#DAA520>HT3"),
        LT2(7, "<#FFE4B5>LT2"), HT2(8, "<#FFA500>HT2"),
        LT1(9, "<#FFB6C1>LT1"), HT1(10, "<#FF0000>HT1"),
        UNRANKED(-1, "<#D3D3D3>N/A");

        companion object {
            private val tierMap = values().associateBy { it.tierValue }
            fun from(tierValue: Int): PlayerTier = tierMap[tierValue] ?: UNRANKED
        }
    }

    data class TierlistPlayer(val uuid: UUID, val tier: PlayerTier)

    private fun fetchUUIDFromUsername(username: String): UUID? {
        return try {
            val connection = URL(MOJANG_API_URL + username).openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connect()

            if (connection.responseCode == 200) {
                val responseString = connection.inputStream.readAllBytes().toString(StandardCharsets.UTF_8)
                val jsonObject = JsonParser.parseString(responseString).asJsonObject
                val uuidString = jsonObject.get("id").asString
                UUID.fromString(
                    uuidString.replaceFirst(
                        "(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})".toRegex(),
                        "$1-$2-$3-$4-$5"
                    )
                )
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }

    fun requestFromAPI(username: String): CompletableFuture<TierlistPlayer> = CompletableFuture.supplyAsync {
        val uuid = fetchUUIDFromUsername(username) ?: return@supplyAsync TierlistPlayer(UUID.randomUUID(), PlayerTier.UNRANKED)

        try {
            val connection = URL(TIER_URL + uuid.toString().replace("-", "")).openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connect()

            if (connection.responseCode == 200) {
                val responseString = connection.inputStream.readAllBytes().toString(StandardCharsets.UTF_8)
                val tierObject = JsonParser.parseString(responseString).asJsonObject.get("vanilla")?.asJsonObject
                tierObject?.let {
                    val tier = it.get("tier").asInt
                    val pos = it.get("pos").asInt
                    val tierValue = if (pos == 0) 12 - tier * 2 else 11 - tier * 2
                    return@supplyAsync TierlistPlayer(uuid, PlayerTier.from(tierValue))
                }
            }
        } catch (_: Exception) {}
        TierlistPlayer(uuid, PlayerTier.UNRANKED)
    }

    fun formatTierDisplayName(tier: PlayerTier): String = MiniMessage.miniMessage().serialize(
        MiniMessage.miniMessage().deserialize(tier.displayName)
    )
}