package com.antonia.comunidadactiva

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class PerfilActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var tvNombre: TextView
    private lateinit var tvCorreo: TextView
    private lateinit var btnMisEventos: Button
    private lateinit var btnCerrarSesion: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil)

        auth = FirebaseAuth.getInstance()

        tvNombre = findViewById(R.id.tvNombrePerfil)
        tvCorreo = findViewById(R.id.tvCorreoPerfil)
        btnMisEventos = findViewById(R.id.btnMisEventos)
        btnCerrarSesion = findViewById(R.id.btnCerrarSesionPerfil)

        cargarPerfil()

        btnMisEventos.setOnClickListener {
            startActivity(Intent(this, MisEventosActivity::class.java))
        }

        btnCerrarSesion.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun cargarPerfil() {
        val uid = auth.currentUser?.uid ?: return
        val correo = auth.currentUser?.email ?: ""

        tvCorreo.text = correo

        FirebaseDatabase.getInstance().reference
            .child("usuarios")
            .child(uid)
            .get()
            .addOnSuccessListener {
                val nombre = it.child("nombre").value?.toString() ?: "Usuario"
                tvNombre.text = nombre
            }
    }
}