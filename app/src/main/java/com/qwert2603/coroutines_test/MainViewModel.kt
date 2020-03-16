package com.qwert2603.coroutines_test

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*

class MainViewModel(
    private val api: Api,
    private val postDao: PostDao,
    coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    @VisibleForTesting
    val userId = MutableLiveData<String>()

    val error = MutableLiveData<Unit>()

    fun changeUserId(userId: String) {
        this.userId.value = userId
    }

    @FlowPreview
    @ExperimentalCoroutinesApi
    val posts: LiveData<List<Post>> = userId
        .asFlow()
        .debounce(400)
        .mapNotNull { it.toLongOrNull() }
        .transformLatest { userId ->
            emit(postDao.flowPosts(userId).first())

            try {
                val posts = api.getPosts(userId)
                postDao.savePosts(posts)
            } catch (e: Exception) {
                e.printStackTrace()
                error.value = Unit
            }
            emitAll(postDao.flowPosts(userId))
        }
        .flowOn(coroutineDispatcher)
        .asLiveData()
}