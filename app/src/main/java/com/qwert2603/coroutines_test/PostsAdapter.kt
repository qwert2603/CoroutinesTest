package com.qwert2603.coroutines_test

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.qwert2603.coroutines_test.databinding.ItemPostBinding

class PostsAdapter : RecyclerView.Adapter<PostsAdapter.PostVH>() {

    var list: List<Post> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getItemCount(): Int = list.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = PostVH(
        ItemPostBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    )

    override fun onBindViewHolder(holder: PostVH, position: Int) {
        holder.bind(list[position])
    }

    class PostVH(private val binding: ItemPostBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(post: Post) = with(itemView) {
            binding.title.text = post.title
            binding.body.text = post.body
        }
    }
}