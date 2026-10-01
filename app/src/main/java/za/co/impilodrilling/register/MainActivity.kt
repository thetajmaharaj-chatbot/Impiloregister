package za.co.impilodrilling.register
import android.app.AlertDialog
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.*

class MainActivity:AppCompatActivity(){
 private val prefs by lazy{getSharedPreferences("impilo_register",MODE_PRIVATE)}
 private val staff=mutableListOf<String>()
 private lateinit var spinner:Spinner; private lateinit var note:EditText; private lateinit var dateLabel:TextView
 private var selectedDate=SimpleDateFormat("yyyy-MM-dd",Locale.US).format(Date())
 override fun onCreate(b:Bundle?){super.onCreate(b);setContentView(R.layout.activity_main)
  spinner=findViewById(R.id.staffSpinner);note=findViewById(R.id.note);dateLabel=findViewById(R.id.dateLabel);dateLabel.text=prettyDate()
  loadStaff();refreshStaff()
  findViewById<Button>(R.id.addStaff).setOnClickListener{staffDialog(null)}
  findViewById<Button>(R.id.editStaff).setOnClickListener{if(staff.isNotEmpty())staffDialog(spinner.selectedItem.toString()) else msg("Add a staff member first")}
  findViewById<Button>(R.id.deleteStaff).setOnClickListener{deleteSelected()}
  findViewById<CalendarView>(R.id.calendar).setOnDateChangeListener{_,y,m,d->selectedDate=String.format(Locale.US,"%04d-%02d-%02d",y,m+1,d);dateLabel.text=prettyDate();loadNote()}
  spinner.onItemSelectedListener=object:android.widget.AdapterView.OnItemSelectedListener{override fun onItemSelected(p:android.widget.AdapterView<*>?,v:android.view.View?,pos:Int,id:Long){loadNote()};override fun onNothingSelected(p:android.widget.AdapterView<*>?){}}
  findViewById<Button>(R.id.saveNote).setOnClickListener{if(staff.isEmpty())msg("Add a staff member first") else{prefs.edit().putString(noteKey(),note.text.toString()).apply();msg("Note saved")}}
 }
 private fun staffDialog(old:String?){val input=EditText(this);input.hint="Staff name";input.setText(old?:"");input.setSelection(input.text.length)
  AlertDialog.Builder(this).setTitle(if(old==null)"Add staff member" else "Edit staff name").setView(input).setNegativeButton("Cancel",null).setPositiveButton("Save"){_,_->
   val name=input.text.toString().trim();if(name.isEmpty()){msg("Enter a name");return@setPositiveButton}
   if(old==null){if(staff.any{it.equals(name,true)})msg("Staff member already exists") else{staff.add(name);saveStaff();refreshStaff(name)}}
   else if(!old.equals(name,true)&&staff.any{it.equals(name,true)})msg("That name already exists") else{val i=staff.indexOf(old);if(i>=0){migrateNotes(old,name);staff[i]=name;saveStaff();refreshStaff(name)}}
  }.show()}
 private fun deleteSelected(){if(staff.isEmpty()){msg("No staff member selected");return};val name=spinner.selectedItem.toString()
  AlertDialog.Builder(this).setTitle("Delete $name?").setMessage("The staff member and all saved notes for them will be removed.").setNegativeButton("Cancel",null).setPositiveButton("Delete"){_,_->removeNotes(name);staff.remove(name);saveStaff();refreshStaff();msg("$name deleted")}.show()}
 private fun migrateNotes(old:String,new:String){val e=prefs.all.entries.filter{it.key.startsWith("note|$old|")};val ed=prefs.edit();e.forEach{val nk=it.key.replaceFirst("note|$old|","note|$new|");ed.putString(nk,it.value as? String?);ed.remove(it.key)};ed.apply()}
 private fun removeNotes(name:String){val ed=prefs.edit();prefs.all.keys.filter{it.startsWith("note|$name|")}.forEach{ed.remove(it)};ed.apply()}
 private fun prettyDate():String{val i=SimpleDateFormat("yyyy-MM-dd",Locale.US).parse(selectedDate)?:Date();return SimpleDateFormat("EEEE, d MMMM yyyy",Locale.getDefault()).format(i)}
 private fun noteKey()="note|"+(spinner.selectedItem?:"")+"|"+selectedDate
 private fun loadNote(){note.setText(if(staff.isEmpty())"" else prefs.getString(noteKey(),""))}
 private fun loadStaff(){val a=JSONArray(prefs.getString("staff","[]"));for(i in 0 until a.length())staff.add(a.getString(i))}
 private fun saveStaff(){val a=JSONArray();staff.forEach{a.put(it)};prefs.edit().putString("staff",a.toString()).apply()}
 private fun refreshStaff(select:String?=null){spinner.adapter=ArrayAdapter(this,android.R.layout.simple_spinner_dropdown_item,staff);if(select!=null){val i=staff.indexOf(select);if(i>=0)spinner.setSelection(i)};loadNote()}
 private fun msg(s:String)=Toast.makeText(this,s,Toast.LENGTH_SHORT).show()
}