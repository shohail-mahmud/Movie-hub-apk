package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.lang.reflect.Type

class LocalStorageManager(context: Context) {

    private val sharedPrefs: SharedPreferences = context.getSharedPreferences(
        "moviehub_local_storage",
        Context.MODE_PRIVATE
    )

    private val moshi = Moshi.Builder().build()
    private val type: Type = Types.newParameterizedType(List::class.java, CompactMedia::class.java)
    private val jsonAdapter: JsonAdapter<List<CompactMedia>> = moshi.adapter(type)

    private val _watchlistFlow = MutableStateFlow<List<CompactMedia>>(emptyList())
    val watchlistFlow: StateFlow<List<CompactMedia>> = _watchlistFlow.asStateFlow()

    private val _historyFlow = MutableStateFlow<List<CompactMedia>>(emptyList())
    val historyFlow: StateFlow<List<CompactMedia>> = _historyFlow.asStateFlow()

    companion object {
        private const val WATCHLIST_KEY = "mh.watchlist"
        private const val HISTORY_KEY = "mh.history"
        private const val MAX_ITEMS = 200
    }

    init {
        _watchlistFlow.value = loadList(WATCHLIST_KEY)
        _historyFlow.value = loadList(HISTORY_KEY)
    }

    private fun loadList(key: String): List<CompactMedia> {
        val json = sharedPrefs.getString(key, null)
        return if (json.isNullOrBlank()) {
            emptyList()
        } else {
            try {
                jsonAdapter.fromJson(json) ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    private fun saveList(key: String, list: List<CompactMedia>) {
        try {
            val json = jsonAdapter.toJson(list)
            sharedPrefs.edit().putString(key, json).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // --- WATCHLIST OPERATIONS ---

    fun isInWatchlist(id: Int): Boolean {
        return _watchlistFlow.value.any { it.id == id }
    }

    fun toggleWatchlist(item: CompactMedia) {
        val current = _watchlistFlow.value.toMutableList()
        val index = current.indexOfFirst { it.id == item.id }
        if (index >= 0) {
            current.removeAt(index)
        } else {
            // Newest items first, de-duplicate, cap at 200
            current.add(0, item.copy(savedAt = System.currentTimeMillis()))
            if (current.size > MAX_ITEMS) {
                current.removeAt(current.lastIndex)
            }
        }
        _watchlistFlow.value = current
        saveList(WATCHLIST_KEY, current)
    }

    fun removeFromWatchlist(id: Int) {
        val current = _watchlistFlow.value.filter { it.id != id }
        _watchlistFlow.value = current
        saveList(WATCHLIST_KEY, current)
    }

    fun clearWatchlist() {
        _watchlistFlow.value = emptyList()
        saveList(WATCHLIST_KEY, emptyList())
    }

    // --- HISTORY OPERATIONS ---

    fun addToHistory(item: CompactMedia) {
        val current = _historyFlow.value.toMutableList()
        // Remove if exists to de-duplicate and place most recent at top
        current.removeAll { it.id == item.id }
        
        current.add(0, item.copy(savedAt = System.currentTimeMillis()))
        if (current.size > MAX_ITEMS) {
            current.removeAt(current.lastIndex)
        }
        _historyFlow.value = current
        saveList(HISTORY_KEY, current)
    }

    fun removeFromHistory(id: Int) {
        val current = _historyFlow.value.filter { it.id != id }
        _historyFlow.value = current
        saveList(HISTORY_KEY, current)
    }

    fun clearHistory() {
        _historyFlow.value = emptyList()
        saveList(HISTORY_KEY, emptyList())
    }
}
