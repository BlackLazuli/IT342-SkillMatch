/*package com.example.skillmatch.customer

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.skillmatch.R
import com.example.skillmatch.adapter.ProfessionalAdapter
import com.example.skillmatch.api.ApiClient
import com.example.skillmatch.model.Professional
import com.example.skillmatch.utils.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CustomerDashboard : AppCompatActivity() {
    
    private lateinit var recyclerView: RecyclerView
    private lateinit var searchView: SearchView
    private lateinit var progressBar: ProgressBar
    private lateinit var noResultsText: TextView
    private lateinit var sessionManager: SessionManager
    private lateinit var profileButton: ImageView
    private lateinit var homeButton: ImageView
    private lateinit var calendarButton: ImageView
    private lateinit var settingsButton: ImageView
    
    private var professionals = mutableListOf<Professional>()
    private lateinit var adapter: ProfessionalAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customer_dashboard)
        
        // Initialize views
        recyclerView = findViewById(R.id.professionalsRecyclerView)
        searchView = findViewById(R.id.searchView)
        progressBar = findViewById(R.id.progressBar)
        noResultsText = findViewById(R.id.noResultsText)
        profileButton = findViewById(R.id.profileButton)
        homeButton = findViewById(R.id.homeButton)
        calendarButton = findViewById(R.id.calendarButton)
        settingsButton = findViewById(R.id.settingsButton)
        
        // Initialize session manager
        sessionManager = SessionManager(this)
        
        // Set up RecyclerView
        adapter = ProfessionalAdapter(professionals) { professional ->
            // Handle professional item click
            val intent = Intent(this, ProfessionalDetailActivity::class.java)
            intent.putExtra("PROFESSIONAL_ID", professional.userId)
            startActivity(intent)
        }
        
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
        
        // Set up search functionality
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { searchProfessionals(it) }
                return true
            }
            
            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.isNullOrBlank()) {
                    loadProfessionals() // Load all professionals if search is cleared
                }
                return true
            }
        })
        
        // Set up navigation buttons
        homeButton.setOnClickListener {
            // Already on home screen, do nothing or refresh
            loadProfessionals()
        }
        
        profileButton.setOnClickListener {
            val intent = Intent(this, EditCustomerProfile::class.java)
            startActivity(intent)
        }
        
        calendarButton.setOnClickListener {
            val intent = Intent(this, CustomerAppointments::class.java)
            startActivity(intent)
        }
        
        settingsButton.setOnClickListener {
            val intent = Intent(this, CustomerSettings::class.java)
            startActivity(intent)
        }
        
        // Load professionals when activity starts
        loadProfessionals()
    }
    
    private fun loadProfessionals() {
        showLoading(true)
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ApiClient.apiService.getAllProfessionals()
                
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body() != null) {
                        professionals.clear()
                        professionals.addAll(response.body()!!)
                        adapter.notifyDataSetChanged()
                        
                        showNoResults(professionals.isEmpty())
                    } else {
                        Toast.makeText(
                            this@CustomerDashboard,
                            "Failed to load professionals: ${response.code()}",
                            Toast.LENGTH_SHORT
                        ).show()
                        showNoResults(true)
                    }
                    showLoading(false)
                }
            } catch (e: Exception) {
                Log.e("CustomerDashboard", "Error loading professionals", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@CustomerDashboard,
                        "Error: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    showNoResults(true)
                    showLoading(false)
                }
            }
        }
    }
    
    private fun searchProfessionals(query: String) {
        showLoading(true)
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ApiClient.apiService.searchProfessionals(query)
                
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body() != null) {
                        professionals.clear()
                        professionals.addAll(response.body()!!)
                        adapter.notifyDataSetChanged()
                        
                        showNoResults(professionals.isEmpty())
                    } else {
                        showNoResults(true)
                    }
                    showLoading(false)
                }
            } catch (e: Exception) {
                Log.e("CustomerDashboard", "Error searching professionals", e)
                withContext(Dispatchers.Main) {
                    showNoResults(true)
                    showLoading(false)
                }
            }
        }
    }
    
    private fun showLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        recyclerView.visibility = if (isLoading) View.GONE else View.VISIBLE
    }
    
    private fun showNoResults(show: Boolean) {
        noResultsText.visibility = if (show) View.VISIBLE else View.GONE
    }
}*/