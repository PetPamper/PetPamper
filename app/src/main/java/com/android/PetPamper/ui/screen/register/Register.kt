package com.android.PetPamper.ui.screen.register

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.android.PetPamper.R
import com.android.PetPamper.database.FirebaseConnection
import com.android.PetPamper.model.Address
import com.android.PetPamper.model.Groomer
import com.android.PetPamper.model.User
import com.android.PetPamper.ui.screen.CustomTextButton
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

class SignUpViewModel {

  var name by mutableStateOf("")
  var email by mutableStateOf("")
  var phoneNumber by mutableStateOf("")
  var address by mutableStateOf(Address("", "", "", ""))
  var password by mutableStateOf("")
}

@Composable
fun Register(currentStep1: Int, viewModel: SignUpViewModel, navController: NavController) {
    val firebaseConnection = FirebaseConnection()
    val db = Firebase.firestore

    var currentStep by remember { mutableIntStateOf(currentStep1) }

    var registeredAsGroomer by remember { mutableStateOf(false) }

    when (currentStep) {
        1 -> {
            Column {
                CustomTextButton(
                    tag = "I already have a groomer account",
                    testTag = "AlreadyGroomerButton") {
                    currentStep = 10
                }
                RegisterLayout(
                    viewModel = viewModel,
                    1,
                    false,
                    "Let’s start with your name",
                    "Name",
                    onNext = { newName ->
                        viewModel.name = newName
                        currentStep++
                    })
            }
        }

        2 ->
            RegisterLayout(
                viewModel,
                2,
                false,
                "Hello ${viewModel.name}, enter your email",
                "Email",
                onNext = { newEmail ->
                    viewModel.email = newEmail
                    currentStep++
                })

        3 ->
            RegisterLayout(
                viewModel,
                3,
                false,
                "What’s your phone number?",
                "Phone Number",
                onNext = { newPhoneNumber ->
                    viewModel.phoneNumber = newPhoneNumber
                    currentStep++
                })

        4 ->
            RegisterLayout(
                viewModel,
                4,
                true,
                "Enter your Address?",
                "Street",
                onNextAddress = { street, city, state, postalCode ->
                    viewModel.address.city = city
                    viewModel.address.state = state
                    viewModel.address.street = street
                    viewModel.address.postalCode = postalCode
                    currentStep++
                    if (registeredAsGroomer) currentStep += 2
                })

        5 ->
            RegisterLayout(
                viewModel,
                5,
                false,
                "Great! Create your password",
                "Password",
                onNext = { password ->
                    viewModel.password = password
                    currentStep++
                })

        6 -> {
            RegisterLayout(
                viewModel,
                6,
                false,
                "Confirm your password",
                "Confirm Password",
                confirmPassword = viewModel.password,
                onNext = { confirmedPassword ->
                    if (viewModel.password == confirmedPassword) {
                        currentStep++
                    } else {
                        // Show error message
                    }
                })
        }

        7 -> {
            if (!registeredAsGroomer) {
                firebaseConnection.registerUser(
                    viewModel.email,
                    viewModel.password,
                    onSuccess = {
                        firebaseConnection.addUser(
                            User(
                                viewModel.name,
                                viewModel.email,
                                viewModel.phoneNumber,
                                viewModel.address
                            ),
                            onSuccess = { currentStep++ },
                            onFailure = { error ->
                                Log.e(
                                    "SignUp",
                                    "Registration failed",
                                    error
                                )
                            })
                    },
                    onFailure = { error -> Log.e("SignUp", "Registration failed", error) })
            } else {
                // Need to check that user wasn't already registered to avoid duplicate accounts
                val userRef = db.collection("users")
                    .document(viewModel.email)
                userRef.get()
                    .addOnSuccessListener { document ->
                        if (!document.exists()) {
                            firebaseConnection.addUser(
                                User(
                                viewModel.name,
                                viewModel.email,
                                viewModel.phoneNumber,
                                viewModel.address
                                ),
                                onSuccess = { currentStep++ },
                                onFailure = {
                                        error -> Log.e("SignUp", "Registration failed", error)
                                })
                        } else {
                            Log.e("AlreadyRegistered",
                                "user was already registered")
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.e(
                            "Firebase query", "Get failed with ",
                            exception
                        )
                    }
            }
        }

        8 -> {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .testTag("ForgetPassword")
            ) {
                val maxHeight = with(LocalDensity.current) { constraints.maxHeight.toDp() }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Congratulation! You successfully created an account.",
                        style =
                        TextStyle(
                            fontSize = 20.sp,
                            lineHeight = 24.sp,
                            fontWeight = FontWeight(800),
                            color = Color(0xFF2490DF),
                            textAlign = TextAlign.Center,
                        ),
                        modifier = Modifier.testTag("SuccessfullMessage")
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Spacer(modifier = Modifier.height(10.dp))
                    Image(
                        painter = painterResource(id = R.drawable.check_success),
                        contentDescription = "Succuss Icon",
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Bottom,
                        horizontalAlignment = Alignment.End
                    ) {
                        Button(
                            onClick = { navController.navigate("LoginScreen") },
                            modifier = Modifier.wrapContentWidth(), // Make the button wrap its content
                            colors =
                            ButtonDefaults.buttonColors( // Set the button's background color
                                containerColor = Color(0xFF2491DF)
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ArrowForward,
                                contentDescription = "Go forward",
                                tint = Color.White // Set the icon color to blue
                            )
                        }
                    }
                }
            }
        }

        10 -> {
            var email by remember { mutableStateOf("") }
            var password by remember { mutableStateOf("") }
            var login by remember { mutableStateOf(true) }

            var errorMessage by remember {
                mutableStateOf("Login failed, email or password is incorrect")
            }

            Column (
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)) {
                Spacer(modifier = Modifier.height(5.dp))

                Text(
                    text = "Please enter your user credentials",
                    style =
                    TextStyle(
                        fontSize = 23.sp,
                        lineHeight = 24.sp,
                        fontWeight = FontWeight(800),
                        color = Color(0xFF2490DF),
                        textAlign = TextAlign.Center,
                    ),
                    modifier = Modifier.testTag("AlreadyUserText")
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(15.dp))

                if (!login) {
                    Text(
                        text = errorMessage,
                        color = Color.Red,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("ErrorMessage")
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                }

                CustomTextButton(
                    "Forgot password?",
                    "",
                    "forgetButton"
                ) { navController.navigate("EmailScreen") }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        errorMessage = ""
                        if (email.isBlank() || password.isBlank()) {
                            login = false
                        } else {
                            firebaseConnection.loginUser(
                                email,
                                password,
                                {
                                    val groomerRef = db.collection("groomers")
                                        .document(email)
                                    groomerRef.get()
                                        .addOnSuccessListener { document ->
                                            if (document.exists()) {
                                                login = true
                                                Log.d(
                                                    "Firebase query", "User found," +
                                                            " name is ${document.get("name")}"
                                                )
                                                viewModel.name = document.get("name").toString()
                                                viewModel.email = document.get("email").toString()
                                                viewModel.phoneNumber =
                                                    document.get("phoneNumber").toString()
                                                registeredAsGroomer = true
                                                currentStep = 4
                                            } else {
                                                login = false
                                                errorMessage = "Groomer is not registered"
                                                Log.e("Firebase query", "No such groomer")
                                            }
                                        }
                                        .addOnFailureListener { exception ->
                                            login = false
                                            errorMessage =
                                                "Login failed, email or password is incorrect"
                                            Log.e(
                                                "Firebase query", "Get failed with ",
                                                exception
                                            )
                                        }
                                },
                                { login = false })
                        }
                    },
                    colors = ButtonDefaults.buttonColors(Color(0xFF2491DF)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("LoginButton")
                ) {
                    Text("LOG IN", fontSize = 18.sp)
                }
            }
        }

        // Add more steps as needed
    }
}

    @Composable
    fun RegisterLayout(
        viewModel: SignUpViewModel,
        currentStep: Int,
        isAddress: Boolean,
        textShown: String,
        fieldName: String,
        confirmPassword: String? = null,
        onNext: ((String) -> Unit)? = null,
        onNextAddress: ((String, String, String, String) -> Unit)? = null
    ) {

        var textField by remember { mutableStateOf("") }
        var city by remember { mutableStateOf("") }
        var state by remember { mutableStateOf("") }
        var postalCode by remember { mutableStateOf("") }
        var errorText by remember { mutableStateOf("") }

        fun proceedWithNext() {
            var isValidInput = true

            when (fieldName) {
                "Name" ->
                    if (!isValidName(textField)) {
                        errorText = "Please enter a valid name."
                        isValidInput = false
                    }

                "Email" ->
                    if (!isValidEmail(textField)) {
                        errorText = "Please enter a valid email."
                        isValidInput = false
                    }

                "Password" ->
                    if (!isValidPassword(textField)) {
                        errorText = "Password must be at least 8 characters."
                        isValidInput = false
                    }

                "Confirm Password" ->
                    if (textField != confirmPassword) {
                        errorText = "Passwords do not match."
                        isValidInput = false
                    }

                // Add more cases as necessary for other fields
            }

            if (isValidInput) {
                errorText = ""
                if (isAddress) {
                    onNextAddress?.invoke(textField, city, state, postalCode)
                } else {
                    onNext?.invoke(textField)
                }
            }
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("RegisterScreen")
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {

                Row(verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .testTag("RegisterScreen"))
                {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Go back",
                        modifier = Modifier
                            .clickable {

                            }
                            .size(30.dp),
                        tint = Color.Black
                    )

                    Spacer(modifier = Modifier.width(20.dp))

                    Text(
                        text = textShown,
                        style =
                        TextStyle(
                            fontSize = 23.sp,
                            lineHeight = 24.sp,
                            fontWeight = FontWeight(800),
                            color = Color(0xFF2490DF),
                            textAlign = TextAlign.Center,
                        ),
                        modifier = Modifier.testTag("EmailText")
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = textField,
                    onValueChange = { textField = it },
                    label = { Text(fieldName) },
                    singleLine = true,
                    visualTransformation =
                    if (fieldName == "Password" || fieldName == "Confirm Password")
                        PasswordVisualTransformation()
                    else VisualTransformation.None,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("NameTextInput"),
                    colors =
                    OutlinedTextFieldDefaults.colors(
                        focusedBorderColor =
                        Color(0xFF2491DF), // Border color when the TextField is focused
                        focusedLabelColor =
                        Color(0xFF2491DF), // Label color when the TextField is focused
                        unfocusedBorderColor =
                        Color.Gray, // Additional customization for other states
                        unfocusedLabelColor = Color.Gray
                    )
                )

                Text(
                    text = errorText,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .testTag("errorText")
                )

                if (isAddress) {

                    OutlinedTextField(
                        value = city,
                        onValueChange = { city = it },
                        label = { Text("city") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("cityTag"),
                        colors =
                        OutlinedTextFieldDefaults.colors(
                            focusedBorderColor =
                            Color(0xFF2491DF), // Border color when the TextField is focused
                            focusedLabelColor =
                            Color(0xFF2491DF), // Label color when the TextField is focused
                            unfocusedBorderColor =
                            Color.Gray, // Additional customization for other states
                            unfocusedLabelColor = Color.Gray
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = state,
                        onValueChange = { state = it },
                        label = { Text("State") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("stateTag"),
                        colors =
                        OutlinedTextFieldDefaults.colors(
                            focusedBorderColor =
                            Color(0xFF2491DF), // Border color when the TextField is focused
                            focusedLabelColor =
                            Color(0xFF2491DF), // Label color when the TextField is focused
                            unfocusedBorderColor =
                            Color.Gray, // Additional customization for other states
                            unfocusedLabelColor = Color.Gray
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = postalCode,
                        onValueChange = { postalCode = it },
                        label = { Text("Postal Code") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("postalTag"),
                        colors =
                        OutlinedTextFieldDefaults.colors(
                            focusedBorderColor =
                            Color(0xFF2491DF), // Border color when the TextField is focused
                            focusedLabelColor =
                            Color(0xFF2491DF), // Label color when the TextField is focused
                            unfocusedBorderColor =
                            Color.Gray, // Additional customization for other states
                            unfocusedLabelColor = Color.Gray
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Box(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier =
                        if(textField.isNotBlank()){
                            Modifier
                                .fillMaxWidth()
                                .padding(bottom = 50.dp, start = 16.dp)
                        } else {
                            Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .padding(16.dp)
                        },
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.End
                    ) {
                        Button(
                            onClick = { proceedWithNext() },
                            modifier =
                            Modifier
                                .wrapContentWidth()
                                .testTag("arrowButton"), // Make the button wrap its content
                            colors =
                            ButtonDefaults.buttonColors( // Set the button's background color
                                containerColor = Color(0xFF2491DF)
                            )
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Go forward",
                                tint = Color.White,
                                // Set the icon color to blue
                            )
                        }

                        Spacer(
                            modifier =
                            Modifier.height(16.dp)
                        ) // This adds space between the button and the
                        // progress
                        // bar

                        val progress = currentStep.toFloat() / 7
                        LinearProgressIndicator(
                            progress = { progress },
                            color = Color(0xFF2491DF),
                            modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(10.dp))
                        )
                    }
                }
            }
        }
    }


fun isValidName(name: String) = name.isNotBlank() // Add more conditions as necessary

fun isValidEmail(email: String) =
    email.contains('@') && email.contains('.') // Simplified validation

fun isValidPassword(password: String) = password.length >= 8 // Basic condition for demonstration

@Preview
@Composable
fun RegisterPreview() {
  val viewModel = remember { SignUpViewModel() } // In actual app, provide this via ViewModel

  val navController = rememberNavController()
  Register(1, viewModel, navController)
}

