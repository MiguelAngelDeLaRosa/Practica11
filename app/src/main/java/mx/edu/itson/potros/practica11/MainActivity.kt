package mx.edu.itson.potros.practica11

import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue


class MainActivity : AppCompatActivity() {
    private var txtid: EditText? = null
    private var txtnom: EditText? = null
    private var btnbus: Button? = null
    private var btnmod: Button? = null
    private var btnreg: Button? = null
    private var btneli: Button? = null
    private var lvDatos: ListView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        txtid   = findViewById(R.id.txtid);
        txtnom  = findViewById(R.id.txtnom);
        btnbus  = findViewById(R.id.btnbus);
        btnmod  = findViewById(R.id.btnmod);
        btnreg  = findViewById(R.id.btnreg);
        btneli  = findViewById(R.id.btneli);
        lvDatos = findViewById(R.id.lvDatos);

        botonBuscar()
        botonModificar()
        botonRegistrar()
        botonEliminar()
        listarLuchadores()
    }

    private fun botonEliminar() {
        btneli?.setOnClickListener{
            if (txtid?.text.toString().trim().isEmpty()){
                Toast.makeText(this, "Digite el ID del luchador a eliminar!!", Toast.LENGTH_SHORT).show()
            } else {
                val id = Integer.parseInt(txtid?.text.toString())

                val db = FirebaseDatabase.getInstance().getReference("Luchador")
                db.addListenerForSingleValueEvent(object : ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        var res = false
                        for (x: DataSnapshot in snapshot.children){
                            if(id.toString() == x.child("id").value.toString()){
                                val alertDialog = AlertDialog.Builder(this@MainActivity)
                                alertDialog.setCancelable(false)
                                alertDialog.setTitle("Pregunta")
                                alertDialog.setMessage("Estas seguro de eliminar el registro?")

                                alertDialog.setNegativeButton("Cancelar", object: DialogInterface.OnClickListener{
                                    override fun onClick(dialog: DialogInterface?, which: Int) {
                                        TODO("Not yet implemented")
                                    }

                                })
                                alertDialog.setPositiveButton("Aceptar", object: DialogInterface.OnClickListener{
                                    override fun onClick(dialog: DialogInterface?, which: Int) {
                                        res = true
                                        ocultarTeclado()
                                        x.ref.removeValue()
                                        listarLuchadores()
                                    }

                                })
                                alertDialog.show()
                                break
                            }
                        }
                        if (res == false){
                            ocultarTeclado()
                            Toast.makeText(this@MainActivity, "ID ($id) No encontrado, imposible eliminar", Toast.LENGTH_SHORT).show()

                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                })
            }
        }
    }

    private fun botonRegistrar() {
        btnreg?.setOnClickListener(){
            if (txtid?.text.toString().trim().isEmpty() ||
                txtnom?.text.toString().trim().isEmpty()){
                ocultarTeclado()
                Toast.makeText(this, "Complete los campos faltantes", Toast.LENGTH_SHORT).show()
            } else {
                val id = Integer.parseInt(txtid?.text.toString())
                val nombre = txtnom?.text.toString()

                val db = FirebaseDatabase.getInstance().getReference("Luchador")

                db.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        var res: Boolean = false
                        val aux: String = id.toString()
                        for (x: DataSnapshot in snapshot.children){
                            if(x.child("id").getValue<Int>().toString().equals(aux)){
                                res = true
                                Toast.makeText(this@MainActivity, "El ID ${id} ya existe", Toast.LENGTH_SHORT).show()
                                ocultarTeclado()
                                break
                            }
                        }
                        var res2: Boolean = false

                        for (x: DataSnapshot in snapshot.children){
                            if(x.child("nombre").getValue<String>().toString().equals(nombre)){
                                res2 = true
                                Toast.makeText(this@MainActivity, "El nombre ${nombre} ya existe", Toast.LENGTH_SHORT).show()
                                ocultarTeclado()
                                break
                            }
                        }

                        if (res == false && res2 == false) {
                            val luchador = Luchador(id, nombre)
                            db.push().setValue(luchador)
                            ocultarTeclado()
                            Toast.makeText(this@MainActivity, "Se agrego al luchador", Toast.LENGTH_SHORT).show()
                            txtid?.setText("")
                            txtnom?.setText("")
                        }

                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                })
            }
        }
    }

    private fun listarLuchadores() {
        val db = FirebaseDatabase.getInstance().getReference("Luchador")

        val listaLuchador = ArrayList<Luchador>()
        val ada = ArrayAdapter<Luchador>(this, android.R.layout.simple_list_item_1, listaLuchador)

        lvDatos?.adapter = ada

        db.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val luchador: Luchador? = snapshot.getValue(Luchador::class.java)
                if (luchador != null) {
                    listaLuchador.add(luchador)
                    ada.notifyDataSetChanged()
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                ada.notifyDataSetChanged()
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                TODO("Not yet implemented")
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                TODO("Not yet implemented")
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

        // Consulta un dato al hacer click en el
        lvDatos?.onItemClickListener = object : AdapterView.OnItemClickListener {
            override fun onItemClick(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val luchador : Luchador = listaLuchador[position]
                val a = AlertDialog.Builder(this@MainActivity)
                a.setCancelable(true)
                a.setTitle("Luchador seleccionado")
                var msg = "ID: "+luchador.id + "\n"
                msg += "Nombre: "+luchador.nombre + "\n"

                a.setMessage(msg)
                a.show()
            }

        }
    }

    private fun botonModificar() {
        btnmod?.setOnClickListener{
            if (txtid?.text.toString().trim().isEmpty() ||
                txtnom?.text.toString().trim().isEmpty()){
                ocultarTeclado()
                Toast.makeText(this, "Complete los campos faltantes para actualizar", Toast.LENGTH_SHORT).show()
            } else {
                val id = Integer.parseInt(txtid?.text.toString())
                val nombre = txtnom?.text.toString()

                val db = FirebaseDatabase.getInstance().getReference("Luchador")

                db.addListenerForSingleValueEvent(object : ValueEventListener {
                    var res2 = false
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for (x: DataSnapshot in snapshot.children){
                            if(x.child("nombre").getValue<Int>().toString().equals(nombre)){
                                res2 = true
                                ocultarTeclado()
                                Toast.makeText(this@MainActivity, "El nombre: $nombre ya existe", Toast.LENGTH_SHORT).show()
                                txtnom?.setText("")
                                txtid?.setText("")
                                listarLuchadores()
                                break
                            }
                        }

                        if (res2 == false){
                            var res = false
                            val aux: String = id.toString()
                            for (x: DataSnapshot in snapshot.children){
                                if(x.child("id").getValue<Int>().toString() == aux){
                                    res = true
                                    ocultarTeclado()
                                    x.ref.child("nombre").setValue(nombre)
                                    txtnom?.setText("")
                                    txtid?.setText("")
                                    listarLuchadores()
                                    break
                                }
                            }

                            if (res == false) {
                                ocultarTeclado()
                                Toast.makeText(this@MainActivity, "ID: $id no encontrado, imposible modificar", Toast.LENGTH_SHORT).show()
                                txtid?.setText("")
                                txtnom?.setText("")
                            }
                        }

                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                })
            }
        }
    }

    // Cierra el metodo o funcion botonBuscar
    private fun botonBuscar() {
        btnbus?.setOnClickListener(){
            if (txtid?.text.toString().trim().isEmpty()){
                Toast.makeText(this, "Digite el ID del luchador!!", Toast.LENGTH_SHORT).show()
            } else {
                val id = Integer.parseInt(txtid?.text.toString())

                val db = FirebaseDatabase.getInstance().getReference("Luchador")
                db.addListenerForSingleValueEvent(object : ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        var res = false
                        for (x: DataSnapshot in snapshot.children){
                            if(id.toString().equals(x.child("id").value.toString())){
                                res = true
                                ocultarTeclado()
                                txtnom?.setText(x.child("nombre").value.toString())
                                break
                            }
                        }

                        if (res == false){
                            ocultarTeclado()
                            Toast.makeText(this@MainActivity, "ID ($id) No encontrado", Toast.LENGTH_SHORT).show()

                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                })
            }
        }
    }

    private fun ocultarTeclado() {
        val view = this.currentFocus
        if (view != null) {
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    } // Cierra el método ocultarTeclado.

}