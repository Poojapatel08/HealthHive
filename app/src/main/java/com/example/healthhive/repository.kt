import com.example.healthhive.models.Appointment
import com.example.healthhive.models.Order
import com.example.healthhive.models.Reminder
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirestoreRepository {
    private val firestore = FirebaseFirestore.getInstance()

    // Function to retrieve user-specific orders
    suspend fun getOrders(userId: String): List<Order> {
        return firestore.collection("orders")
            .whereEqualTo("userId", userId)
            .get().await()
            .toObjects(Order::class.java)
    }

    // Retrieve appointments
    suspend fun getAppointments(userId: String): List<Appointment> {
        return firestore.collection("appointments")
            .whereEqualTo("userId", userId)
            .get().await()
            .toObjects(Appointment::class.java)
    }

    // Retrieve reminders
    suspend fun getReminders(userId: String): List<Reminder> {
        return firestore.collection("reminders")
            .whereEqualTo("userId", userId)
            .get().await()
            .toObjects(Reminder::class.java)
    }
}
