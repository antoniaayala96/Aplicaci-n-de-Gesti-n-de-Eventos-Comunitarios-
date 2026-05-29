package com.antonia.comunidadactiva

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.antonia.comunidadactiva.models.Evento
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class HistorialActivity : AppCompatActivity() {

    private lateinit var tvHistorial: TextView
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private val texto = StringBuilder()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_historial)

        tvHistorial = findViewById(R.id.tvHistorial)
        database = FirebaseDatabase.getInstance().reference
        auth = FirebaseAuth.getInstance()

        cargarHistorial()
    }

    private fun cargarHistorial() {
        val uid = auth.currentUser?.uid ?: return

        texto.clear()
        texto.append("HISTORIAL DE PARTICIPACIÓN\n\n")

        database.child("participaciones")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    if (!snapshot.hasChildren()) {
                        tvHistorial.text = "Aún no has confirmado asistencia a eventos."
                        return
                    }

                    var encontrados = 0

                    for (eventoNodo in snapshot.children) {
                        if (eventoNodo.hasChild(uid)) {
                            encontrados++
                            val idEvento = eventoNodo.key ?: ""

                            database.child("eventos").child(idEvento)
                                .addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(eventoSnapshot: DataSnapshot) {
                                        val evento = eventoSnapshot.getValue(Evento::class.java)

                                        evento?.let {
                                            texto.append("• ${it.titulo}\n")
                                            texto.append("Fecha: ${it.fecha} - ${it.hora}\n")
                                            texto.append("Lugar: ${it.ubicacion}\n")
                                            texto.append("Categoría: ${it.categoria}\n")
                                            texto.append("Estado: Asistencia confirmada\n\n")
                                        }

                                        tvHistorial.text = texto.toString()
                                    }

                                    override fun onCancelled(error: DatabaseError) {}
                                })
                        }
                    }

                    if (encontrados == 0) {
                        tvHistorial.text = "Aún no has confirmado asistencia a eventos."
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    tvHistorial.text = "Error al cargar historial."
                }
            })
    }
}