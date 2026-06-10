package com.pillow.data.repository

import com.pillow.data.db.dao.CategoryDao
import com.pillow.data.db.entity.CategoryEntity
import com.pillow.domain.model.Category
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CategoryRepository @Inject constructor(
    private val categoryDao: CategoryDao
) {
    suspend fun createCategory(category: Category): Long {
        val entity = categoryToEntity(category)
        return categoryDao.insertCategory(entity)
    }

    suspend fun updateCategory(category: Category) {
        val entity = categoryToEntity(category)
        categoryDao.updateCategory(entity)
    }

    /** Deletes a bucket unless it is the protected default bucket. */
    suspend fun deleteCategory(categoryId: Long) {
        val category = categoryDao.getCategoryById(categoryId)
        if (category != null && !category.isDefault) {
            categoryDao.deleteCategory(category)
        }
    }

    suspend fun getCategoryById(categoryId: Long): Category? {
        val entity = categoryDao.getCategoryById(categoryId)
        return entity?.let { entityToCategory(it) }
    }

    /** Returns the default bucket's id, creating it on first run if needed. */
    suspend fun ensureDefaultBucket(): Long {
        val existing = categoryDao.getDefaultCategory()
        if (existing != null) return existing.id
        return categoryDao.insertCategory(
            CategoryEntity(
                name = "General",
                color = "#6B6B9D",
                isDefault = true
            )
        )
    }

    suspend fun getDefaultBucketId(): Long? = categoryDao.getDefaultCategory()?.id

    fun getAllCategoriesFlow(): Flow<List<Category>> {
        return categoryDao.getAllCategoriesFlow().map { entities ->
            entities.map { entityToCategory(it) }
        }
    }

    suspend fun getAllCategories(): List<Category> {
        return categoryDao.getAllCategories().map { entityToCategory(it) }
    }

    private fun categoryToEntity(category: Category): CategoryEntity {
        return CategoryEntity(
            id = category.id,
            name = category.name,
            createdAt = category.createdAt,
            color = category.color,
            isDefault = category.isDefault
        )
    }

    private fun entityToCategory(entity: CategoryEntity): Category {
        return Category(
            id = entity.id,
            name = entity.name,
            createdAt = entity.createdAt,
            color = entity.color,
            isDefault = entity.isDefault
        )
    }
}
