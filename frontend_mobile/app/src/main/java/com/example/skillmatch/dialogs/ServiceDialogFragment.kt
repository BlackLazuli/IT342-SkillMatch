package com.example.skillmatch.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.example.skillmatch.R
import com.example.skillmatch.models.Service

class ServiceDialogFragment : DialogFragment() {

    private var service: Service? = null
    private var listener: ServiceDialogListener? = null
    
    private lateinit var dialogTitle: TextView
    private lateinit var serviceNameInput: EditText
    private lateinit var serviceDescriptionInput: EditText
    private lateinit var servicePricingInput: EditText
    private lateinit var cancelButton: Button
    private lateinit var saveButton: Button

    interface ServiceDialogListener {
        fun onServiceSaved(service: Service)
    }

    companion object {
        private const val ARG_SERVICE = "service"

        fun newInstance(service: Service?): ServiceDialogFragment {
            val fragment = ServiceDialogFragment()
            service?.let {
                val args = Bundle()
                args.putParcelable(ARG_SERVICE, it)
                fragment.arguments = args
            }
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            // Fix the type mismatch by using getParcelable with the correct type
            service = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                it.getParcelable(ARG_SERVICE, Service::class.java)
            } else {
                @Suppress("DEPRECATION")
                it.getParcelable(ARG_SERVICE)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.dialog_service, container, false)
        
        // Initialize views
        dialogTitle = view.findViewById(R.id.dialogTitle)
        serviceNameInput = view.findViewById(R.id.serviceNameInput)
        serviceDescriptionInput = view.findViewById(R.id.serviceDescriptionInput)
        servicePricingInput = view.findViewById(R.id.servicePricingInput)
        cancelButton = view.findViewById(R.id.cancelButton)
        saveButton = view.findViewById(R.id.saveButton)
        
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set up the dialog title
        dialogTitle.text = if (service == null) "Add Service" else "Edit Service"

        // Pre-fill fields if editing
        service?.let {
            serviceNameInput.setText(it.name)
            serviceDescriptionInput.setText(it.description)
            servicePricingInput.setText(it.price?.toString() ?: "")
        }

        // Set up buttons
        cancelButton.setOnClickListener {
            dismiss()
        }

        saveButton.setOnClickListener {
            val name = serviceNameInput.text.toString()
            val description = serviceDescriptionInput.text.toString()
            val pricingText = servicePricingInput.text.toString()
            val price = if (pricingText.isNotEmpty()) pricingText.toDoubleOrNull() else null

            if (name.isBlank()) {
                serviceNameInput.error = "Service name is required"
                return@setOnClickListener
            }

            val updatedService = Service(
                id = service?.id ?: System.currentTimeMillis(), // Generate a temporary ID if new
                name = name,
                description = description,
                price = price
            )

            listener?.onServiceSaved(updatedService)
            dismiss()
        }
    }

    fun setServiceDialogListener(listener: ServiceDialogListener) {
        this.listener = listener
    }
}