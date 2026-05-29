package com.antonia.comunidadactiva

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.antonia.comunidadactiva.adapters.EventoAdapter
import com.antonia.comunidadactiva.models.Evento
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import android.view.MenuItem
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var rvEventos: RecyclerView

    private lateinit var fabCrearEvento: FloatingActionButton
    private lateinit var tvSinEventos: TextView
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var database: DatabaseReference

    private lateinit var toolbar: MaterialToolbar
    private lateinit var auth: FirebaseAuth

    private val listaEventos = ArrayList<Evento>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        rvEventos = findViewById(R.id.rvEventos)
        fabCrearEvento = findViewById(R.id.fabCrearEvento)
        tvSinEventos = findViewById(R.id.tvSinEventos)
        bottomNavigation = findViewById(R.id.bottomNavigation)
        auth = FirebaseAuth.getInstance()

        toolbar = findViewById(R.id.topAppBar)

        rvEventos.layoutManager = LinearLayoutManager(this)
        database = FirebaseDatabase.getInstance().getReference("eventos")

        toolbar.setOnMenuItemClickListener { item: MenuItem ->

            when (item.itemId) {

                R.id.menu_logout -> {
                    auth.signOut()

                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()

                    true
                }

                else -> false
            }
        }

        fabCrearEvento.setOnClickListener {
            startActivity(Intent(this, CrearEventoActivity::class.java))
        }

        bottomNavigation.selectedItemId = R.id.nav_inicio

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_inicio -> true

                R.id.nav_mis_eventos -> {
                    startActivity(Intent(this, MisEventosActivity::class.java))
                    true
                }

                R.id.nav_perfil -> {
                    startActivity(Intent(this, PerfilActivity::class.java))
                    true
                }

                else -> false
            }
        }

        cargarEventos()
    }

    private fun cargarEventos() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listaEventos.clear()

                for (eventoSnapshot in snapshot.children) {
                    val evento = eventoSnapshot.getValue(Evento::class.java)
                    if (evento != null) {
                        listaEventos.add(evento)
                    }
                }

                if (listaEventos.isEmpty()) {
                    tvSinEventos.visibility = View.VISIBLE
                    rvEventos.visibility = View.GONE
                } else {
                    tvSinEventos.visibility = View.GONE
                    rvEventos.visibility = View.VISIBLE
                    rvEventos.adapter = EventoAdapter(listaEventos)
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }
}