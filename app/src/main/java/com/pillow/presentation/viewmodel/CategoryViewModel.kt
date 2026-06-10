package com.pillow.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pillow.data.repository.CategoryRepository
import com.pillow.domain.model.Category
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _categoriesState = MutableStateFlow<List<Category>>(emptyList())
    val categoriesState: StateFlow<List<Category>> = _categoriesState.asStateFlow()

    private val _isLoadingState = MutableStateFlow(false)
    val isLoadingState: StateFlow<Boolean> = _isLoadingState.asStateFlow()

    private val _defaultBucketIdState = MutableStateFlow<Long?>(null)
    val defaultBucketIdState: StateFlow<Long?> = _defaultBucketIdState.asStateFlow()

    init {
        viewModelScope.launch {
            // Make sure the protected default bucket exists, then expose its id.
            _defaultBucketIdState.value = categoryRepository.ensureDefaultBucket()
        }
        loadAllCategories()
    }

    fun loadAllCategories() {
        viewModelScope.launch {
            _isLoadingState.value = true
            categoryRepository.getAllCategoriesFlow().collectLatest { categories ->
                _categoriesState.value = categories
                _isLoadingState.value = false
            }
        }
    }

    fun createCategory(category: Category) {
        viewModelScope.launch {
            try {
                categoryRepository.createCategory(category)
                loadAllCategories()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun updateCategory(category: Category) {
        viewModelScope.launch {
            try {
                categoryRepository.updateCategory(category)
                loadAllCategories()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deleteCategory(categoryId: Long) {
        viewModelScope.launch {
            try {
                categoryRepository.deleteCategory(categoryId)
                loadAllCategories()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
