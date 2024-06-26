package com.android.PetPamper.ui.screen.chat

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.android.PetPamper.R

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun UsersScreen(onBackPressed: () -> Unit, navController: NavController) {
  val pets =
      listOf(
          Pet(
              name = "Simo",
              description = "simo@mghrib.ma",
              dateOfBirth = "2018-05-12",
              pictureRes = R.drawable.placeholder),
          Pet(
              name = "Ssi 3bdilah",
              description = "hitler@gmail.com",
              dateOfBirth = "2020-03-01",
              pictureRes = R.drawable.placeholder),
          // Add more pets as needed
      )

  Scaffold(
      topBar = {
        TopAppBar(
            title = { Text("Users") },
            navigationIcon = {
              IconButton(onClick = onBackPressed) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
              }
            })
      },
  ) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
          items(pets) { pet -> UserCard(pet, navController) }
        }
  }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun UserCard(pet: Pet, navController: NavController) {
  Card(
      modifier = Modifier.fillMaxWidth().padding(1.dp),
      elevation = 4.dp,
      onClick = { navController.navigate("ChatScreen") }) {
        Row(modifier = Modifier.padding(1.dp), verticalAlignment = Alignment.CenterVertically) {
          Image(
              painter = painterResource(id = pet.pictureRes),
              contentDescription = null,
              modifier = Modifier.size(64.dp).clip(CircleShape))
          Column(modifier = Modifier.weight(1f).padding(start = 16.dp)) {
            Text(text = pet.name, style = MaterialTheme.typography.subtitle1)
            Text(text = pet.description, style = MaterialTheme.typography.body2)
          }
        }
      }
}
