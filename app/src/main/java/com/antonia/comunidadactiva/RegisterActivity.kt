package com.antonia.comunidadactiva

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.antonia.comunidadactiva.utils.Validaciones

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        val nombre = findViewById<EditText>(R.id.etNombre)
        val correo = findViewById<EditText>(R.id.etCorreo)
        val password = findViewById<EditText>(R.id.etPassword)
        val btnRegistro = findViewById<Button>(R.id.btnRegistro)

        btnRegistro.setOnClickListener {

            val nombreTexto = nombre.text.toString().trim()
            val correoTexto = correo.text.toString().trim()
            val passwordTexto = password.text.toString().trim()

            // VALIDACIÓN 1
            if (nombreTexto.isEmpty() || correoTexto.isEmpty() || passwordTexto.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // VALIDACIÓN 2
            if (!Validaciones.correoValido(correoTexto)) {
                Toast.makeText(this, "Correo inválido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // VALIDACIÓN 3
            if (!Validaciones.passwordValida(passwordTexto)) {
                Toast.makeText(
                    this,
                    "La contraseña debe tener mínimo 6 caracteres",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            // REGISTRO FIREBASE
            auth.createUserWithEmailAndPassword(correoTexto, passwordTexto)
                .addOnCompleteListener { task ->

                    if (task.isSuccessful) {

                        val uid = auth.currentUser?.uid ?: ""

                        val usuario = hashMapOf(
                            "nombre" to nombreTexto,
                            "correo" to correoTexto
                        )

                        database.reference
                            .child("usuarios")
                            .child(uid)
                            .setValue(usuario)

                        startActivity(Intent(this, MainActivity::class.java))
                        finish()

                    } else {
                        Toast.makeText(
                            this,
                            "Error al registrarse",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }
    }
}