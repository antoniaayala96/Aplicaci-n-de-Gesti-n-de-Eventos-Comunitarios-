package com.antonia.comunidadactiva

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.antonia.comunidadactiva.models.Evento
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class MisEventosActivity : AppCompatActivity() {

    private lateinit var tvMisEventos: TextView
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private val texto = StringBuilder()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mis_eventos)

        tvMisEventos = findViewById(R.id.tvMisEventos)
        database = FirebaseDatabase.getInstance().reference
        auth = FirebaseAuth.getInstance()

        cargarMisEventos()
    }

    private fun cargarMisEventos() {
        val uid = auth.currentUser?.uid ?: return
        texto.clear()

        texto.append("MIS EVENTOS\n\n")
        texto.append("Eventos creados por mí\n")
        texto.append("-------------------------\n\n")

        database.child("eventos")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    var creados = 0

                    for (eventoSnapshot in snapshot.children) {
                        val evento = eventoSnapshot.getValue(Evento::class.java)

                        if (evento != null && evento.creadorId == uid) {
                            creados++
                            texto.append("• ${evento.titulo}\n")
                            texto.append("Fecha: ${evento.fecha} - ${evento.hora}\n")
                            texto.append("Lugar: ${evento.ubicacion}\n")
                            texto.append("Categoría: ${evento.categoria}\n\n")
                        }
                    }

                    if (creados == 0) {
                        texto.append("Aún no has creado eventos.\n\n")
                    }

                    texto.append("\nEventos a los que asistiré\n")
                    texto.append("---------------------------\n\n")

                    cargarEventosAsistidos(uid)
                }

                override fun onCancelled(error: DatabaseError) {
                    tvMisEventos.text = "Error al cargar tus eventos."
                }
            })
    }

    private fun cargarEventosAsistidos(uid: String) {
        database.child("participaciones")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    var asistidos = 0

                    if (!snapshot.hasChildren()) {
                        texto.append("Aún no has confirmado asistencia a eventos.\n")
                        tvMisEventos.text = texto.toString()
                        return
                    }

                    for (eventoNodo in snapshot.children) {
                        if (eventoNodo.hasChild(uid)) {
                            asistidos++
                            val idEvento = eventoNodo.key ?: ""

                            database.child("eventos").child(idEvento)
                                .addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(eventoSnapshot: DataSnapshot) {
                                        val evento = eventoSnapshot.getValue(Evento::class.java)

                                        evento?.let {
                                            texto.append("• ${it.titulo}\n")
                                            texto.append("Fecha: ${it.fecha} - ${it.hora}\n")
                                            texto.append("Lugar: ${it.ubicacion}\n")
                                            texto.append("Categoría: ${it.categoria}\n\n")
                                        }

                                        tvMisEventos.text = texto.toString()
                                    }

                                    override fun onCancelled(error: DatabaseError) {}
                                })
                        }
                    }

                    if (asistidos == 0) {
                        texto.append("Aún no has confirmado asistencia a eventos.\n")
                        tvMisEventos.text = texto.toString()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    tvMisEventos.text = "Error al cargar eventos asistidos."
                }
            })
    }
}