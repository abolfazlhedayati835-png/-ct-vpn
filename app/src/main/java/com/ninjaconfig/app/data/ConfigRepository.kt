package com.ninjaconfig.app.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * All reads/writes to the "configs" collection in Firestore live here.
 *
 * Firestore structure:
 *   configs/{autoId} -> VpnConfig fields
 *
 * No authentication is used for reading (per requirements: every user sees
 * the same public list). Writing from the admin screen also goes directly
 * to Firestore — protect it with Firestore Security Rules restricting writes
 * (see README.md "Securing admin writes" section) so random users can't
 * write from a decompiled APK.
 */
class ConfigRepository {

    private val db = FirebaseFirestore.getInstance()
    private val collection = db.collection("configs")

    /** Live stream of all configs, grouped by country, ordered by country name then sortOrder. */
    fun observeConfigs(): Flow<List<VpnConfig>> = callbackFlow {
        val registration = collection
            .orderBy("countryName", Query.Direction.ASCENDING)
            .orderBy("sortOrder", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val configs = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(VpnConfig::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(configs)
            }
        awaitClose { registration.remove() }
    }

    suspend fun addConfig(config: VpnConfig) {
        collection.add(config.copy(createdAt = System.currentTimeMillis())).await()
    }

    suspend fun updateConfig(config: VpnConfig) {
        require(config.id.isNotBlank()) { "Config id required for update" }
        collection.document(config.id).set(config).await()
    }

    suspend fun deleteConfig(id: String) {
        collection.document(id).delete().await()
    }
}

/** Groups a flat config list into per-country sections for the home screen. */
fun List<VpnConfig>.groupedByCountry(): List<CountryGroup> =
    groupBy { it.countryCode to it.countryName }
        .map { (key, configs) -> CountryGroup(key.first, key.second, configs) }
        .sortedBy { it.countryName }
