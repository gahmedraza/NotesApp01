package com.raza.notesapp01

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.raza.notesapp01.ui.theme.NotesApp01Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //enableEdgeToEdge()
        setContent {
            NotesApp01Theme {
                Dashboard()
            }
        }
    }
}

@Composable
fun Dashboard() {
    val db = TodoDatabase.getDatabase(LocalContext.current)
    val repository = TodoRepository(db.todoDao())
    val factory = DashboardViewModelFactory(repository)

    val viewModel: DashboardViewModel = viewModel(factory = factory)

    val todo by viewModel.todos.collectAsState(initial = emptyList())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {

        TextField(
            //mandatory
            value = viewModel.text,
            onValueChange = { newText ->
                viewModel.onTextChange(newText)
            },
            //optional
            label = { Text("Enter your note here") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.padding(12.dp))

        Button(
            //mandatory
            onClick = {
                viewModel.addOrUpdateTodo()
            },
            content = {
                Text(text = "Add Note")
            },
            //optional
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.padding(42.dp))

        LazyColumn {
            itemsIndexed(todo) { index, todo ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = todo.value)
                    Spacer(modifier = Modifier.weight(1f))

                    Button(
                        onClick = {
                            viewModel.startEditing(todo)
                        }
                    ) {
                        Text(text = "Edit")
                    }

                    Spacer(modifier = Modifier.padding(4.dp))

                    Button(
                        onClick = {
                            viewModel.deleteTodo(todo)
                        },

                        ) {
                        Text(text = "Delete")
                    }
                }
            }
        }
    }


}

@Preview(showBackground = true)
@Composable
fun DashboardPreview() {
    NotesApp01Theme {
        Dashboard()
    }
}
