package com.android.PetPamper.database

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.android.PetPamper.model.Address
import com.android.PetPamper.model.Groomer
import com.android.PetPamper.model.GroomerReviews
import com.android.PetPamper.model.Reservation
import com.android.PetPamper.model.User
import com.android.PetPamper.resources.distance
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.firestore
import java.util.Calendar

class FirebaseConnection {

  private val db: FirebaseFirestore = Firebase.firestore

  fun addUser(user: User, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
    db.collection("users")
        .document(user.email)
        .set(user)
        .addOnSuccessListener { onSuccess() }
        .addOnFailureListener { exception -> onFailure(exception) }
  }

  fun updateUserAddress(uid: String, addressData: Map<String, Any>, onComplete: () -> Unit) {
    db.collection("users")
        .document(uid)
        .update(addressData)
        .addOnSuccessListener { onComplete() }
        .addOnFailureListener { exception ->
          println("Error updating address: ${exception.localizedMessage}")
          onComplete()
        }
  }

  fun changeAddress(email: String, address: Address) {
    db.collection("users").document(email).update("address", address)
  }

  // method to verify if an email is already registered

  fun verifyEmail(email: String, userType: String): Task<Boolean> {
    val source = TaskCompletionSource<Boolean>()

    db.collection(
            if (userType == "user") {
              "users"
            } else {
              "groomers"
            })
        .whereEqualTo("email", email)
        .get()
        .addOnCompleteListener { task ->
          if (task.isSuccessful) {
            if (task.result?.isEmpty == false) {
              source.setResult(true) // Set true if email exists
            } else {
              source.setResult(false) // Set false if email does not exist
            }
          } else {
            // If the query failed, set the exception
            source.setException(task.exception ?: Exception("Failed to verify email"))
          }
        }

    return source.task
  }

  fun addGroomer(groomer: Groomer, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
    db.collection("groomers")
        .document(groomer.email) // Using email as a unique identifier; adjust if needed
        .set(groomer)
        .addOnSuccessListener { onSuccess() }
        .addOnFailureListener { exception -> onFailure(exception) }
  }

  fun addGroomerReview(
      groomerReview: GroomerReviews,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    db.collection("groomerReviews")
        .document(groomerReview.email)
        .set(groomerReview)
        .addOnSuccessListener { onSuccess() }
        .addOnFailureListener { exception -> onFailure(exception) }
  }

  fun getUserData(uid: String): Task<DocumentSnapshot> {
    return db.collection("users").document(uid).get()
  }

  fun fetchGroomerData(email: String, onComplete: (Groomer) -> Unit) {
    db.collection("groomers").get().addOnCompleteListener { task ->
      if (task.isSuccessful) {

        val groomers = task.result?.toObjects(Groomer::class.java)
        val thisGroomer = groomers?.find { it.email == email } ?: Groomer()
        onComplete(thisGroomer)
      } else {
        Log.d("groomerdatafetch", "get failed with ", task.exception)
      }
    }
  }

  fun addReservationToFirebase(
      reservation: Reservation,
      context: Context,
      onConfirmation: () -> Unit,
      onError: (String) -> Unit
  ) {
    db.collection("reservations")
        .document(reservation.reservationId)
        .set(reservation)
        .addOnSuccessListener {
          Toast.makeText(context, "Reservation confirmed!", Toast.LENGTH_SHORT).show()
          onConfirmation()
        }
        .addOnFailureListener { e ->
          Toast.makeText(context, "Failed to confirm reservation: ${e.message}", Toast.LENGTH_LONG)
              .show()
          onError(e.message ?: "Unknown error")
        }
  }

  fun fetchAvailableDates(email: String, onDatesFetched: (List<String>) -> Unit) {
    val db = FirebaseFirestore.getInstance()
    val datesRef = db.collection("groomerAvailabilities").document(email).collection("dates")

    datesRef.get().addOnCompleteListener { task ->
      if (task.isSuccessful) {
        val documents = task.result
        if (documents != null) {
          val datesList = documents.documents.mapNotNull { it.id }
          onDatesFetched(datesList)
        } else {
          println("No dates found for $email")
          onDatesFetched(emptyList())
        }
      } else {
        println("Error fetching available dates: ${task.exception?.localizedMessage}")
        onDatesFetched(emptyList())
      }
    }
  }

  fun fetchAvailableHours(email: String, date: String, onHoursFetched: (List<String>) -> Unit) {
    val db = FirebaseFirestore.getInstance()
    val docRef =
        db.collection("groomerAvailabilities").document(email).collection("dates").document(date)

    docRef.get().addOnCompleteListener { task ->
      if (task.isSuccessful) {
        val document = task.result
        if (document != null && document.exists()) {
          val timestamps = document.get("availableHours") as? List<Timestamp> ?: listOf()
          // Convert Timestamps to "hour:minute" strings
          val hoursList =
              timestamps.map { ts ->
                val calendar =
                    Calendar.getInstance().apply {
                      timeInMillis = ts.seconds * 1000 // Convert seconds to milliseconds
                    }
                "${calendar.get(Calendar.HOUR_OF_DAY)}:${String.format("%02d", calendar.get(Calendar.MINUTE))}"
              }
          onHoursFetched(hoursList)
        } else {
          println("No available hours found for $date")
          onHoursFetched(emptyList())
        }
      } else {
        println("Error fetching available hours: ${task.exception?.localizedMessage}")
        onHoursFetched(emptyList())
      }
    }
  }

  fun getUserUidByEmail(email: String): Task<QuerySnapshot> {
    return db.collection("users").whereEqualTo("email", email).get()
  }

  fun updateAvailableHours(email: String, newHours: List<Calendar>, onComplete: () -> Unit) {
    // Convert each Calendar instance to a Timestamp for Firebase storage
    val hoursData =
        newHours.map { calendar ->
          hashMapOf("timestamp" to Timestamp(calendar.timeInMillis / 1000, 0))
        }

    // Update the Firestore document
    db.collection("groomerAvailabilities")
        .document(email)
        .set(mapOf("availableHours" to hoursData))
        .addOnSuccessListener {
          onComplete() // Call onComplete callback when update is successful
        }
        .addOnFailureListener { e ->
          println("Error updating available hours: ${e.localizedMessage}")
          onComplete() // Optionally handle errors, still call onComplete to signal end of operation
        }
  }

  fun fetchNearbyGroomers(address: Address): Task<List<Groomer>> {
    // Create a task completion source
    val source = TaskCompletionSource<List<Groomer>>()

    // Fetch all groomers
    db.collection("groomers").get().addOnCompleteListener { task ->
      if (task.isSuccessful) {
        val groomers = task.result?.toObjects(Groomer::class.java)

        val nearbyGroomers = mutableListOf<Groomer>()

        // Calculate the distance for each groomer
        groomers?.forEach { groomer ->
          val distance =
              distance(
                  address.location.latitude,
                  address.location.longitude,
                  groomer.address.location.latitude,
                  groomer.address.location.longitude)

          // If the distance is less than or equal to 10 kilometers, add the groomer to the list
          Log.d(
              "GroomersFirebase",
              "Distance to ${groomer.name}: $distance from ${address.location.name}")
          if (distance <= 10 && !nearbyGroomers.contains(groomer)) {
            nearbyGroomers.add(groomer)
          }
        }

        // Set the result of the task
        source.setResult(nearbyGroomers)
        Log.d("GroomersFirebase", "Nearby groomers: $nearbyGroomers")
      } else {
        // If the task failed, set the exception
        source.setException(task.exception ?: Exception("Failed to fetch groomers"))
      }
    }

    // Return the task
    return source.task
  }

  fun fetchGroomerReviews(email: String): Task<GroomerReviews> {
    val source = TaskCompletionSource<GroomerReviews>()

    db.collection("groomerReviews").whereEqualTo("email", email).get().addOnCompleteListener { task
      ->
      if (task.isSuccessful) {
        val reviews = task.result?.toObjects(GroomerReviews::class.java)
        if (reviews != null && reviews.isNotEmpty()) {
          source.setResult(reviews[0])
        } else {
          source.setException(Exception("No review found for this email"))
        }
      } else {
        source.setException(task.exception ?: Exception("Failed to fetch reviews"))
      }
    }

    return source.task
  }

  fun registerUser(
      email: String,
      password: String,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    FirebaseAuth.getInstance()
        .createUserWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
          if (task.isSuccessful) {
            onSuccess()
          } else {
            onFailure(task.exception ?: Exception("Registration failed"))
          }
        }
  }

  fun loginUser(
      email: String,
      password: String,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password).addOnCompleteListener {
        task ->
      if (task.isSuccessful) {
        onSuccess()
      } else {
        onFailure(task.exception ?: Exception("Login failed"))
      }
    }
  }

  fun resetUserPassword(password: String, password2: String) {}

  fun verifyObCode(oobCode: String): Boolean {
    return FirebaseAuth.getInstance().verifyPasswordResetCode(oobCode).isSuccessful
  }

  fun restPasswordSendEmail(email: String): Boolean {
    var res = false
    FirebaseAuth.getInstance().sendPasswordResetEmail(email).addOnCompleteListener { task ->
      if (task.isSuccessful) {
        res = true
      }
    }
    return res
  }
}
