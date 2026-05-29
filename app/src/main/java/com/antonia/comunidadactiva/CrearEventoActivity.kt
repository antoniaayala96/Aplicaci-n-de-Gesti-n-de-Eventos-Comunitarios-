
package com.antonia.comunidadactiva

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.antonia.comunidadactiva.models.Evento
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.antonia.comunidadactiva.utils.Validaciones

class CrearEventoActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crear_evento)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        val etTitulo = findViewById<EditText>(R.id.etTitulo)
        val etDescripcion = findViewById<EditText>(R.id.etDescripcion)
        val etFecha = findViewById<EditText>(R.id.etFecha)
        val etHora = findViewById<EditText>(R.id.etHora)
        val etUbicacion = findViewById<EditText>(R.id.etUbicacion)
        val etCategoria = findViewById<EditText>(R.id.etCategoria)
        val btnGuardar = findViewById<Button>(R.id.btnGuardarEvento)
        val etImagenUrl = findViewById<EditText>(R.id.etImagenUrl)

        btnGuardar.setOnClickListener {
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
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (!Validaciones.urlValida(imagenUrl)) {
                Toast.makeText(this, "URL de imagen inválida", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val ref = database.reference.child("eventos")
            val idEvento = ref.push().key ?: ""

            val evento = Evento(
                id = idEvento,
                titulo = titulo,
                descripcion = descripcion,
                fecha = fecha,
                hora = hora,
                ubicacion = ubicacion,
                categoria = categoria,
                imagenUrl = imagenUrl,
                creadorId = auth.currentUser?.uid ?: ""

            )

            ref.child(idEvento).setValue(evento)
                .addOnSuccessListener {
                    Toast.makeText(this, "Evento guardado correctamente", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error al guardar evento", Toast.LENGTH_SHORT).show()
                }
        }
    }
}