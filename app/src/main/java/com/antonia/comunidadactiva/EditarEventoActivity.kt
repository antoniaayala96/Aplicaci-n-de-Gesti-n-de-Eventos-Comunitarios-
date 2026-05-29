package com.antonia.comunidadactiva

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.antonia.comunidadactiva.models.Evento
import com.antonia.comunidadactiva.utils.Validaciones
import com.google.firebase.database.*

class EditarEventoActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private var idEvento: String = ""

    private lateinit var etTitulo: EditText
    private lateinit var etDescripcion: EditText
    private lateinit var etFecha: EditText
    private lateinit var etHora: EditText
    private lateinit var etUbicacion: EditText
    private lateinit var etCategoria: EditText
    private lateinit var etImagenUrl: EditText
    private lateinit var btnActualizar: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editar_evento)

        database = FirebaseDatabase.getInstance().reference
        idEvento = intent.getStringExtra("idEvento") ?: ""

        etTitulo = findViewById(R.id.etTituloEditar)
        etDescripcion = findViewById(R.id.etDescripcionEditar)
        etFecha = findViewById(R.id.etFechaEditar)
        etHora = findViewById(R.id.etHoraEditar)
        etUbicacion = findViewById(R.id.etUbicacionEditar)
        etCategoria = findViewById(R.id.etCategoriaEditar)
        etImagenUrl = findViewById(R.id.etImagenUrlEditar)
        btnActualizar = findViewById(R.id.btnActualizarEvento)

        cargarDatosEvento()

        btnActualizar.setOnClickListener {
            actualizarEvento()
        }
    }

    private fun cargarDatosEvento() {
        database.child("eventos").child(idEvento)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val evento = snapshot.getValue(Evento::class.java)

                    evento?.let {
                        etTitulo.setText(it.titulo)
                        etDescripcion.setText(it.descripcion)
                        etFecha.setText(it.fecha)
                        etHora.setText(it.hora)
                        etUbicacion.setText(it.ubicacion)
                        etCategoria.setText(it.categoria)
                        etImagenUrl.setText(it.imagenUrl)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        this@EditarEventoActivity,
                        "Error al cargar datos",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun actualizarEvento() {
        val titulo = etTitulo.text.toString().trim()
        val descripcion = etDescripcion.text.toString().trim()
        val fecha = etFecha.text.toString().trim()
        val hora = etHora.text.toString().trim()
        val ubicacion = etUbicacion.text.toString().trim()
        val categoria = etCategoria.text.toString().trim()
        val imagenUrl = etImagenUrl.text.toString().trim()

        if (titulo.isEmpty() || descripcion.isEmpty() || fecha.isEmpty() ||
            hora.isEmpty() || ubicacion.isEmpty() || categoria.isEmpty()
        ) {
            Toast.makeText(this, "Completa todos los campos obligatorios", Toast.LENGTH_SHORT).show()
            return
        }

        if (!Validaciones.urlValida(imagenUrl)) {
            Toast.makeText(this, "URL de imagen inválida", Toast.LENGTH_SHORT).show()
            return
        }

        val datos = mapOf<String, Any>(
            "titulo" to titulo,
            "descripcion" to descripcion,
            "fecha" to fecha,
            "hora" to hora,
            "ubicacion" to ubicacion,
            "categoria" to categoria,
            "imagenUrl" to imagenUrl
        )

        database.child("eventos").child(idEvento).updateChildren(datos)
            .addOnSuccessListener {
                Toast.makeText(this, "Evento actualizado", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al actualizar", Toast.LENGTH_SHORT).show()
            }
    }
}