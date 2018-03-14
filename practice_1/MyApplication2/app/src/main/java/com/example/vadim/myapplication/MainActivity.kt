package com.example.vadim.myapplication

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.unit_child.view.*
import okhttp3.OkHttpClient
import okhttp3.Request

class MainActivity : AppCompatActivity() {

    lateinit var repo_list: RecyclerView

    data class GitHubRepositoryInfo(val name: String) {
        class List : ArrayList<GitHubRepositoryInfo>()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Thread(Runnable {
            val client = OkHttpClient()
            val request = Request.Builder()
                    .url("https://api.github.com/users/square/repos")
                    .build()
            val response = client.newCall(request).execute()
            val respBody = response.body()!!.string()
            val repos = Gson().fromJson(respBody, GitHubRepositoryInfo.List::class.java)
            val names = repos.map { it.name }
            runOnUiThread {
                repo_list = findViewById(R.id.repo_list)
                repo_list.layoutManager = GridLayoutManager(this, 1)
                repo_list.adapter = adapter(names, this)
            }

        }).start()
    }

    class adapter(items: List<String>, ctx: Context) : RecyclerView.Adapter<adapter.ViewHolder>() {

        var list = items
        var countext = ctx

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
            return ViewHolder(LayoutInflater.from(countext).inflate(R.layout.unit_child, parent, false))
        }

        override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
            holder?.name?.text = list.get(position)
        }

        override fun getItemCount(): Int {
            return list.size
        }

        class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
            val name = v.unit_name
        }

    }
}