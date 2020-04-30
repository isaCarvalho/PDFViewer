package com.example.pdfviewer.adapter

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.pdfviewer.LinksActivity
import com.example.pdfviewer.R
import com.example.pdfviewer.ViewActivity
import com.example.pdfviewer.database.DatabaseController
import com.example.pdfviewer.model.Link
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList

class LinkAdapter(private var myDataset : ArrayList<Link>) :
    RecyclerView.Adapter<LinkAdapter.MyViewHolder>(), Filterable
{
    private val listCopy = ArrayList<Link>(myDataset.toMutableList())

    class MyViewHolder(v: View) : RecyclerView.ViewHolder(v)
    {
        val url : TextView = v.findViewById(R.id.linkUrl)
        val imgDelete : ImageView = v.findViewById(R.id.imgDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_link, parent, false)

        return MyViewHolder(v)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        val string = if (myDataset[position].url.length > 30)
            "${myDataset[position].url.take(27)}..."
        else
            myDataset[position].url

        holder.url.text = string
        holder.url.setOnClickListener {v ->

            val intent = Intent(v.context, ViewActivity::class.java).apply {
                putExtra("ViewType", "internet")
                putExtra("url", myDataset[position].url)
            }

            startActivity(v.context, intent, null)
        }

        holder.imgDelete.setOnClickListener {v ->
            try {
                DatabaseController(v.context).delete(myDataset[position].id)

                myDataset.removeAt(position)
                notifyDataSetChanged()
            } catch (e : Exception) {
                Log.e("ERROR", e.message!!)

                Toast.makeText(LinksActivity().applicationContext,
                    "Erro ao deletar link",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun getItemCount(): Int = myDataset.size

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val listFilter = ArrayList<Link>()

                if (constraint.isNullOrEmpty())
                    listFilter.addAll(listCopy)
                else
                {
                    val filterPatterns = constraint.toString().toLowerCase(Locale.ROOT).trim()

                    listCopy.forEach { item ->
                        if (item.url.toLowerCase(Locale.ROOT).contains(filterPatterns))
                            listFilter.add(item)
                    }
                }

                val result = FilterResults()
                result.values = listFilter

                return result
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                myDataset.clear()
                myDataset = results!!.values as ArrayList<Link>

                notifyDataSetChanged()
            }
        }
    }
}