package com.ble.scan.scanner.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first


val Context.dataStore by preferencesDataStore("settings")
class BleScanDataStore(
    private val dataStore: DataStore<Preferences>
) {
    private val sortDescKey = booleanPreferencesKey("sortDesc")
    private val filterAllKey = booleanPreferencesKey("filterAll")

    suspend fun getSortDesc() = dataStore.data.first()[sortDescKey] ?: true
    suspend fun getFilterAll() = dataStore.data.first()[filterAllKey] ?: true

    suspend fun setSort(desc: Boolean) = dataStore.edit {
        it[sortDescKey] = desc
    }
    suspend fun setFilter(all: Boolean) = dataStore.edit {
        it[filterAllKey] = all

    }
}