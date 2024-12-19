package com.example.healthhive.viewmodels


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ProfileViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _profileState = MutableStateFlow(ProfileState())
    val profileState: StateFlow<ProfileState> = _profileState

    init {
        fetchProfile()
    }

    private fun fetchProfile() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            viewModelScope.launch {
                try {
                    val doc = db.collection("users").document(userId).get().await()
                    _profileState.value = _profileState.value.copy(
                        name = doc.getString("name") ?: "",
                        email = auth.currentUser?.email ?: "",
                        mobileNumber = doc.getString("mobileNumber") ?: "",
                        age = doc.getString("age") ?: "",
                        address = doc.getString("address") ?: "",
                        isFetching = false
                    )
                } catch (e: Exception) {
                    _profileState.value = _profileState.value.copy(isFetching = false)
                }
            }
        } else {
            _profileState.value = _profileState.value.copy(isFetching = false)
        }
    }

    fun updateField(field: ProfileField, value: String) {
        _profileState.value = when (field) {
            ProfileField.NAME -> _profileState.value.copy(name = value)
            ProfileField.MOBILE_NUMBER -> _profileState.value.copy(mobileNumber = value)
            ProfileField.AGE -> _profileState.value.copy(age = value)
            ProfileField.ADDRESS -> _profileState.value.copy(address = value)
        }
    }

    fun saveProfile() {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            _profileState.value = _profileState.value.copy(isSaving = true)
            val updatedData = mapOf(
                "name" to _profileState.value.name,
                "mobileNumber" to _profileState.value.mobileNumber,
                "age" to _profileState.value.age,
                "address" to _profileState.value.address
            )
            try {
                db.collection("users").document(userId).set(updatedData, SetOptions.merge()).await()
            } catch (e: Exception) {
                // Handle save failure
            } finally {
                _profileState.value = _profileState.value.copy(isSaving = false)
            }
        }
    }
}

data class ProfileState(
    val name: String = "",
    val email: String = "",
    val mobileNumber: String = "",
    val age: String = "",
    val address: String = "",
    val isFetching: Boolean = true,
    val isSaving: Boolean = false
)

enum class ProfileField {
    NAME, MOBILE_NUMBER, AGE, ADDRESS
}



