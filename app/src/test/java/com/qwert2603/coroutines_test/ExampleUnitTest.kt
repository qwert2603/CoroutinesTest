package com.qwert2603.coroutines_test

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import kotlin.random.Random

@ExperimentalCoroutinesApi
class SomeTest {

    private fun TestCoroutineScope.log(q: Any?) =
        println("${Thread.currentThread()} $currentTime $q")

    @Rule
    @JvmField
    val instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    @FlowPreview
    @ExperimentalStdlibApi
    @Test
    fun testSomeUI(): Unit = runBlockingTest {

        val mainViewModel = MainViewModel(
            object : Api {
                override suspend fun getPosts(userId: Long): List<Post> {
                    log("getPosts $userId")
                    delay(250)
                    return listOf(
                        Post(
                            userId = userId,
                            id = Random.nextLong(),
                            title = "post for userId=$userId",
                            body = "some post description"
                        )
                    )
                }
            },
            object : PostDao {
                private var posts = listOf(Post(1, 1, "cached", "cached post"))
                    set(value) {
                        field = value
                        channel.offer(field)
                    }

                private lateinit var channel: Channel<List<Post>>

                override suspend fun savePosts(posts: List<Post>) {
                    log("savePosts $posts")
                    delay(100)
                    this.posts = this.posts + posts
                }

                override fun flowPosts(userId: Long): Flow<List<Post>> {
                    log("flowPosts $userId")
                    channel = Channel(Channel.CONFLATED)
                    channel.offer(posts)
                    return channel
                        .receiveAsFlow()
                        .map { it.filter { it.userId == userId } }
                        .onCompletion { log("flowPosts onCompletion $userId") }
                }
            },
            coroutineContext[CoroutineDispatcher]!!
        )

        // needed for asserting "mainViewModel.posts.value".
        // LiveData isn't calculated w/o observers.
        mainViewModel.posts.observeForever {}

        mainViewModel.changeUserId("1")

        Assert.assertEquals("1", mainViewModel.userId.value)
        Assert.assertEquals(null, mainViewModel.posts.value)

        advanceUntilIdle()
        Assert.assertEquals("1", mainViewModel.userId.value)
        Assert.assertEquals(
            listOf("cached", "post for userId=1"),
            mainViewModel.posts.value?.map { it.title }
        )

        mainViewModel.changeUserId("2")
        Assert.assertEquals("2", mainViewModel.userId.value)

        advanceTimeBy(400)

        Assert.assertEquals("2", mainViewModel.userId.value)
        Assert.assertEquals(
            listOf<String>(),
            mainViewModel.posts.value?.map { it.title }
        )

        advanceUntilIdle()
        Assert.assertEquals("2", mainViewModel.userId.value)
        Assert.assertEquals(
            listOf("post for userId=2"),
            mainViewModel.posts.value?.map { it.title }
        )

    }
}