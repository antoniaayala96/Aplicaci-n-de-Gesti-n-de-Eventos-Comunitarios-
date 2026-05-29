package com.antonia.comunidadactiva.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.antonia.comunidadactiva.DetalleEventoActivity
import com.antonia.comunidadactiva.R
import com.antonia.comunidadactiva.models.Evento

class EventoAdapter(
    private val listaEventos: List<Evento>
) : RecyclerView.Adapter<EventoAdapter.EventoViewHolder>() {

    class EventoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titulo: TextView = itemView.findViewById(R.id.tvTituloEvento)
        val fechaHora: TextView = itemView.findViewById(R.id.tvFechaHora)
        val ubicacion: TextView = itemView.findViewById(R.id.tvUbicacion)
        val categoria: TextView = itemView.findViewById(R.id.tvCategoria)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_evento, parent, false)
        return EventoViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventoViewHolder, position: Int) {
        val evento = listaEventos[position]

        holder.titulo.text = evento.titulo
        holder.fechaHora.text = "${evento.fecha} - ${evento.hora}"
        holder.ubicacion.text = evento.ubicacion
        holder.categoria.text = evento.categoria

        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, DetalleEventoActivity::class.java)
            intent.putExtra("idEvento", evento.id)
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = listaEventos.size
}