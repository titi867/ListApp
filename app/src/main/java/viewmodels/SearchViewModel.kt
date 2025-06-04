package com.example.listapp.models

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

enum class SortOption {
    NAME_ASC, NAME_DESC, FAVORITES_FIRST
}

class SearchViewModel : ViewModel() {
    private val _query = MutableLiveData("")
    private val _sortOption = MutableLiveData(SortOption.NAME_ASC)

    val query: LiveData<String> = _query
    val sortOption: LiveData<SortOption> = _sortOption

    fun setQuery(query: String) {
        _query.value = query
    }

    fun setSortOption(option: SortOption) {
        _sortOption.value = option
    }
}
