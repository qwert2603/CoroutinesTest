package com.qwert2603.coroutines_test

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import com.qwert2603.coroutines_test.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val adapter = PostsAdapter()

    @Suppress("UNCHECKED_CAST")
    private val viewModel: MainViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T = when (modelClass) {
                MainViewModel::class.java -> MainViewModel(
                    api = Api.create(),
                    postDao = LocalDB.create(this@MainActivity).postDao
                ) as T
                else -> throw RuntimeException("bad VM class $modelClass")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.repos.adapter = adapter

        viewModel.posts.observe(this) { adapter.list = it }
        viewModel.error.observe(this) {
            Toast.makeText(this, "error", Toast.LENGTH_SHORT).show()
        }

        binding.userId.doAfterTextChanged { viewModel.changeUserId(it.toString()) }
    }
}
