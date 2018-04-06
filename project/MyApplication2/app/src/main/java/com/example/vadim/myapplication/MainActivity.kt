package com.example.vadim.myapplication

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.unit_child.view.*
import okhttp3.OkHttpClient
import okhttp3.Request
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        //recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = GridLayoutManager(this, 1)

        val content = Content.List()
        //recyclerView.adapter = Adapter(content)

        launch(UI) {
            val cachedContents = loadingContentFromCache(application as App).await()
            if (cachedContents.isNotEmpty()) {
                content.addAll(cachedContents)
                recyclerView.adapter = Adapter(content)
            } else {
                val serverContentJob = loadingContentFromServer()
                serverContentJob.start()
                val serverContent = serverContentJob.await()
                savePhotos(application as App, serverContent)
                content.addAll(serverContent)
                recyclerView.adapter = Adapter(content)
            }
        }

        //getContentFromServer()
    }

    fun getContentFromServer(){
        Thread(Runnable {
            val client = OkHttpClient()
            val request = Request.Builder()
                    .url("http://85.143.216.90:8082/films/")
                    .build()
            val response = client.newCall(request).execute()
            val respBody = response.body()!!.string()
            val repos = Gson().fromJson(respBody, Content.List::class.java)
            //val names = repos.map { it.name }
            runOnUiThread {
                recyclerView.adapter = Adapter(repos)
            }

        }).start()
    }





    class Adapter(items: ArrayList<Content>) : RecyclerView.Adapter<Adapter.ViewHolder>() {

        var list = items

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
            return ViewHolder(LayoutInflater.from(parent?.context).inflate(R.layout.unit_child, parent, false))
        }

        override fun getItemCount(): Int {
            return list.size
        }

        override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
            holder?.textViewName?.text = list[position].name
            holder?.textViewYear?.text = list[position].year
            holder?.textViewCountry?.text = list[position].country
            holder?.textViewDescription?.text = list[position].description
            holder?.textViewGenre?.text = list[position].genre
            Picasso.get()
                   .load(list[position].picture)
                   .into(holder?.imageView)
        }

        class ViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {
            var textViewName: TextView? = itemView?.textViewName
            var textViewYear: TextView? = itemView?.textViewYear
            var textViewCountry: TextView? = itemView?.textViewCountry
            var textViewDescription: TextView? = itemView?.textViewDescription
            var textViewGenre: TextView? = itemView?.textViewGenre
            var imageView: ImageView? = itemView?.imageView
        }

    }
}