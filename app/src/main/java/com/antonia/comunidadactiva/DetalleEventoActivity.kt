package com.antonia.comunidadactiva

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.antonia.comunidadactiva.models.Comentario
import com.antonia.comunidadactiva.models.Evento
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import android.widget.ImageView
import com.bumptech.glide.Glide
import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class DetalleEventoActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference

    private lateinit var imgDetalleEvento: ImageView
    private lateinit var auth: FirebaseAuth
    private var idEvento: String = ""
    private var eventoActual: Evento? = null

    private lateinit var tvTitulo: TextView
    private lateinit var tvDescripcion: TextView
    private lateinit var tvFechaHora: TextView
    private lateinit var tvUbicacion: TextView
    private lateinit var tvCategoria: TextView

    private lateinit var btnConfirmar: Button
    private lateinit var btnEditar: Button
    private lateinit var btnEliminar: Button
    private lateinit var btnCompartir: Button

    private lateinit var ratingEvento: RatingBar
    private lateinit var etComentario: EditText
    private lateinit var btnGuardarComentario: Button
    private lateinit var tvComentarios: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalle_evento)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        idEvento = intent.getStringExtra("idEvento") ?: ""

        tvTitulo = findViewById(R.id.tvTituloDetalle)
        tvDescripcion = findViewById(R.id.tvDescripcionDetalle)
        tvFechaHora = findViewById(R.id.tvFechaHoraDetalle)
        tvUbicacion = findViewById(R.id.tvUbicacionDetalle)
        tvCategoria = findViewById(R.id.tvCategoriaDetalle)
        imgDetalleEvento = findViewById(R.id.imgDetalleEvento)
        btnConfirmar = findViewById(R.id.btnConfirmarAsistencia)
        btnEditar = findViewById(R.id.btnEditarEvento)
        btnEliminar = findViewById(R.id.btnEliminarEvento)
        btnCompartir = findViewById(R.id.btnCompartirEvento)

        ratingEvento = findViewById(R.id.ratingEvento)
        etComentario = findViewById(R.id.etComentario)
        btnGuardarComentario = findViewById(R.id.btnGuardarComentario)
        tvComentarios = findViewById(R.id.tvComentarios)


        cargarDetalleEvento()
        cargarComentarios()
        cargarCantidadAsistentes()

        btnConfirmar.setOnClickListener {
            confirmarAsistencia()
        }

        btnEditar.setOnClickListener {
            val intent = Intent(this, EditarEventoActivity::class.java)
            intent.putExtra("idEvento", idEvento)
            startActivity(intent)
        }

        btnEliminar.setOnClickListener {
            eliminarEvento()
        }

        btnCompartir.setOnClickListener {
            compartirEvento()
        }

        btnGuardarComentario.setOnClickListener {
            guardarComentario()
        }

    }

    private fun cargarDetalleEvento() {
        database.child("eventos").child(idEvento)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    eventoActual = snapshot.getValue(Evento::class.java)

                    eventoActual?.let { evento ->
                        tvTitulo.text = evento.titulo
                        tvDescripcion.text = evento.descripcion
                        tvFechaHora.text = "${evento.fecha} - ${evento.hora}"
                        tvUbicacion.text = evento.ubicacion
                        tvCategoria.text = evento.categoria
                        if (evento.imagenUrl.isNotEmpty()) {
                            Glide.with(this@DetalleEventoActivity)
                                .load(evento.imagenUrl)
                                .placeholder(R.mipmap.ic_launcher)
                                .error(R.mipmap.ic_launcher)
                                .into(imgDetalleEvento)
                        } else {
                            imgDetalleEvento.setImageResource(R.mipmap.ic_launcher)
                        }

                        val usuarioActual = auth.currentUser?.uid

                        if (evento.creadorId != usuarioActual) {
                            btnEditar.visibility = View.GONE
                            btnEliminar.visibility = View.GONE
                        } else {
                            btnEditar.visibility = View.VISIBLE
                            btnEliminar.visibility = View.VISIBLE
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        this@DetalleEventoActivity,
                        "Error al cargar evento",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun confirmarAsistencia() {
        val uid = auth.currentUser?.uid ?: return

        val ref = database.child("participaciones")
            .child(idEvento)
            .child(uid)

        ref.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                Toast.makeText(this, "Ya confirmaste asistencia", Toast.LENGTH_SHORT).show()
            } else {
                val asistencia = hashMapOf(
                    "usuarioId" to uid,
                    "eventoId" to idEvento,
                    "estado" to "Confirmado"
                )

                ref.setValue(asistencia)
                    .addOnSuccessListener {
                        btnConfirmar.text = "Asistencia confirmada ✓"
                        btnConfirmar.isEnabled = false
                        Toast.makeText(this, "Asistencia confirmada", Toast.LENGTH_SHORT).show()
                        mostrarNotificacionAsistencia()
                    }

                    .addOnFailureListener {
                        Toast.makeText(this, "Error al confirmar asistencia", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

    private fun cargarCantidadAsistentes() {
        database.child("participaciones")
            .child(idEvento)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val total = snapshot.childrenCount
                    btnConfirmar.text = "Asistir ($total)"
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun eliminarEvento() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Eliminar evento")
            .setMessage("¿Seguro que deseas eliminar este evento?")
            .setPositiveButton("Sí, eliminar") { _, _ ->
                database.child("eventos").child(idEvento)
                    .removeValue()
                    .addOnSuccessListener {
                        Toast.makeText(this, "Evento eliminado", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Error al eliminar evento", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun compartirEvento() {
        val evento = eventoActual ?: return

        val texto = """
            Te invito al evento: ${evento.titulo}
            Fecha: ${evento.fecha}
            Hora: ${evento.hora}
            Lugar: ${evento.ubicacion}
            Descripción: ${evento.descripcion}
        """.trimIndent()

        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, texto)

        startActivity(Intent.createChooser(intent, "Compartir evento"))
    }

    private fun guardarComentario() {
        val uid = auth.currentUser?.uid ?: return
        val texto = etComentario.text.toString().trim()
        val calificacion = ratingEvento.rating

        if (texto.isEmpty()) {
            Toast.makeText(this, "Escribe un comentario", Toast.LENGTH_SHORT).show()
            return
        }
        if (calificacion == 0f) {
            Toast.makeText(this, "Selecciona una calificación", Toast.LENGTH_SHORT).show()
            return
        }

        database.child("usuarios").child(uid).get()
            .addOnSuccessListener { usuarioSnapshot ->

                val nombreUsuario = usuarioSnapshot.child("nombre").value.toString()

                val ref = database.child("comentarios").child(idEvento)
                val idComentario = ref.push().key ?: ""

                val comentario = Comentario(
                    id = idComentario,
                    eventoId = idEvento,
                    usuarioId = uid,
                    nombreUsuario = nombreUsuario,
                    texto = texto,
                    calificacion = calificacion
                )

                ref.child(idComentario)
                    .setValue(comentario)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Comentario guardado", Toast.LENGTH_SHORT).show()
                        etComentario.text.clear()
                        ratingEvento.rating = 0f
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Error al guardar comentario", Toast.LENGTH_SHORT).show()
                    }
            }
    }

    private fun cargarComentarios() {
        database.child("comentarios").child(idEvento)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val textoComentarios = StringBuilder()
                    textoComentarios.append("Comentarios:\n\n")

                    for (comentarioSnapshot in snapshot.children) {
                        val comentario = comentarioSnapshot.getValue(Comentario::class.java)

                        comentario?.let {
                            textoComentarios.append("${it.nombreUsuario}\n")
                            textoComentarios.append("⭐ ${it.calificacion}/5\n")
                            textoComentarios.append("${it.texto}\n\n")
                        }
                    }

                    if (snapshot.childrenCount == 0L) {
                        tvComentarios.text = "Sé el primero en comentar este evento."
                    } else {
                        tvComentarios.text = textoComentarios.toString()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        this@DetalleEventoActivity,
                        "Error al cargar comentarios",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }
    private fun crearCanalNotificacion() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canal = NotificationChannel(
                "canal_eventos",
                "Eventos Comunitarios",
                NotificationManager.IMPORTANCE_DEFAULT
            )

            canal.description = "Notificaciones de eventos comunitarios"

            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(canal)
        }
    }

    private fun mostrarNotificacionAsistencia() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    100
                )
                return
            }
        }

        val notificacion = NotificationCompat.Builder(this, "canal_eventos")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Asistencia confirmada")
            .setContentText("Te has registrado correctamente al evento.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        NotificationManagerCompat.from(this).notify(1, notificacion)
    }
}