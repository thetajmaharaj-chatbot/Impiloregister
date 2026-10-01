package za.co.impilodrilling.register
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.*
class MainActivity:AppCompatActivity(){
 private val prefs by lazy{getSharedPreferences("impilo_register",MODE_PRIVATE)}; private val staff=mutableListOf<String>(); private lateinit var spinner:Spinner; private lateinit var note:EditText; private lateinit var dateLabel:TextView; private var selectedDate=SimpleDateFormat("yyyy-MM-dd",Locale.US).format(Date())
 override fun onCreate(b:Bundle?){super.onCreate(b);setContentView(R.layout.activity_main);spinner=findViewById(R.id.staffSpinner);note=findViewById(R.id.note);dateLabel=findViewById(R.id.dateLabel);loadStaff();refreshStaff();dateLabel.text=selectedDate
 findViewById<Button>(R.id.addStaff).setOnClickListener{val input=findViewById<EditText>(R.id.staffName);val name=input.text.toString().trim();if(name.isNotEmpty()&&!staff.contains(name)){staff.add(name);saveStaff();refreshStaff();input.text.clear()}}
 findViewById<CalendarView>(R.id.calendar).setOnDateChangeListener{_,y,m,d->selectedDate=String.format(Locale.US,"%04d-%02d-%02d",y,m+1,d);dateLabel.text=selectedDate;loadNote()}
 spinner.onItemSelectedListener=object:android.widget.AdapterView.OnItemSelectedListener{override fun onItemSelected(p:android.widget.AdapterView<*>?,v:android.view.View?,pos:Int,id:Long){loadNote()};override fun onNothingSelected(p:android.widget.AdapterView<*>?){}}
 findViewById<Button>(R.id.saveNote).setOnClickListener{if(staff.isEmpty()){Toast.makeText(this,"Add a staff member first",Toast.LENGTH_SHORT).show()}else{prefs.edit().putString(noteKey(),note.text.toString()).apply();Toast.makeText(this,"Saved",Toast.LENGTH_SHORT).show()}}}
 private fun noteKey()="note|"+(spinner.selectedItem?:"")+"|"+selectedDate
 private fun loadNote(){note.setText(if(staff.isEmpty())"" else prefs.getString(noteKey(),""))}
 private fun loadStaff(){val a=JSONArray(prefs.getString("staff","[]"));for(i in 0 until a.length())staff.add(a.getString(i))}
 private fun saveStaff(){val a=JSONArray();staff.forEach{a.put(it)};prefs.edit().putString("staff",a.toString()).apply()}
 private fun refreshStaff(){spinner.adapter=ArrayAdapter(this,android.R.layout.simple_spinner_dropdown_item,staff);loadNote()}
}