===== FILE: ./all_kotlin_files.kt =====



===== FILE: ./app/src/androidTest/java/com/raza/notesapp01/ExampleInstrumentedTest.kt =====
package com.raza.notesapp01

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.raza.notesapp01", appContext.packageName)
    }
}


===== FILE: ./app/src/main/java/com/raza/notesapp01/DashboardViewModel.kt =====
package com.raza.notesapp01

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: TodoRepository
) : ViewModel() {
    var text by mutableStateOf("")

    val todos = repository.todos

    /*mutableStateListOf(
        TodoEntity(1, "Buy milk"),
        TodoEntity(2, "Learn compose"),
        TodoEntity(3, "Walk in the morning")
    )*/

    init {
        viewModelScope.launch {
            repository.syncFromDatabase()
        }
    }

    var editedTodo by mutableStateOf<TodoEntity?>(null)

    fun addOrUpdateTodo() {
        viewModelScope.launch {
            editedTodo?.let { todo ->
                //update
                repository.update(TodoEntity(todo.id, text))
            } ?: run {
                //add
                repository.insert(TodoEntity(value = text))
            }
        }
    }

    fun deleteTodo(todo: TodoEntity) {
        viewModelScope.launch {
            repository.delete(todo)
        }
    }

    fun startEditing(todo: TodoEntity) {
        text = todo.value
        editedTodo = todo
    }

    fun onTextChange(newText: String) {
        text = newText
        Log.d("TAG", " = $newText")
    }
}


===== FILE: ./app/src/main/java/com/raza/notesapp01/di/AppModule.kt =====
package com.raza.notesapp01.di

import android.content.Context
import com.google.firebase.firestore.FirebaseFirestore
import com.raza.notesapp01.TodoDao
import com.raza.notesapp01.TodoDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    //Firestore
    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

    //Database
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): TodoDatabase {
        return TodoDatabase.getDatabase(context)
    }

    //Dao
    @Provides
    fun provideTodoDao(
        database: TodoDatabase
    ): TodoDao {
        return database.todoDao()
    }
}


===== FILE: ./app/src/main/java/com/raza/notesapp01/MainActivity.kt =====
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.raza.notesapp01.ui.theme.NotesApp01Theme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
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
    val viewModel: DashboardViewModel = hiltViewModel()

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



===== FILE: ./app/src/main/java/com/raza/notesapp01/TodoApplication.kt =====
package com.raza.notesapp01

import android.app.Application
import android.content.Context
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class TodoApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()

        scheduleOneTimeTodoSync()
        schedulePeriodicTodoSync(this)
    }

    fun scheduleOneTimeTodoSync() {
        val request = OneTimeWorkRequestBuilder<TodoSyncWorker>().build()

        WorkManager.getInstance(this).enqueue(request)
    }

    fun schedulePeriodicTodoSync(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = PeriodicWorkRequestBuilder<TodoSyncWorker>(
            6, TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                "todo_periodic_sync",
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
    }
}


===== FILE: ./app/src/main/java/com/raza/notesapp01/TodoDao.kt =====
package com.raza.notesapp01

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TodoDao {

    @Query(
        "SELECT * FROM todos " +
                "WHERE isDeleted = 0"
    )
    fun getAllTodos(): Flow<List<TodoEntity>>

    @Insert
    suspend fun insertTodo(todo: TodoEntity)

    @Update
    suspend fun updateTodo(todo: TodoEntity)

    @Delete
    suspend fun deleteTodo(todo: TodoEntity)

    @Query(
        "DELETE FROM todos " +
                "WHERE isDeleted=1 AND lastModified < :thresholdTime"
    )
    suspend fun hardDeleteTodos(thresholdTime: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(todos: List<TodoEntity>)

    @Query("DELETE FROM todos")
    suspend fun clearAll()
}


===== FILE: ./app/src/main/java/com/raza/notesapp01/TodoDatabase.kt =====
package com.raza.notesapp01

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [TodoEntity::class],
    version = 1,
    exportSchema = false
)
abstract class TodoDatabase : RoomDatabase() {
    abstract fun todoDao(): TodoDao

    companion object {
        @Volatile
        private var INSTANCE: TodoDatabase? = null

        fun getDatabase(context: Context): TodoDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TodoDatabase::class.java,
                    "todo_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}


===== FILE: ./app/src/main/java/com/raza/notesapp01/TodoEntity.kt =====
package com.raza.notesapp01

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "todos")
data class TodoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    var value: String = "",
    var lastModified: Long = System.currentTimeMillis(),
    var isDeleted: Boolean = false
)


===== FILE: ./app/src/main/java/com/raza/notesapp01/TodoRepository.kt =====
package com.raza.notesapp01

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.forEach
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class TodoRepository
    @Inject constructor(
    private val dao: TodoDao,
    private val firestore: FirebaseFirestore,
    @ApplicationContext private val context: Context
) {

    val todos: Flow<List<TodoEntity>> = dao.getAllTodos()

    suspend fun insert(todo: TodoEntity) {
        todo.lastModified = System.currentTimeMillis()
        dao.insertTodo(todo)
        enqueueSyncWorker()
    }

    suspend fun delete(todo: TodoEntity) {
        todo.isDeleted = true
        todo.lastModified =
            System.currentTimeMillis()

        dao.updateTodo(todo)
        enqueueSyncWorker()
        //uploadAllToFirebase()
    }

    suspend fun update(todo: TodoEntity) {
        todo.lastModified = System.currentTimeMillis()
        dao.updateTodo(todo)
        enqueueSyncWorker()
    }

    suspend fun uploadAllToFirebase() {
        val list = dao.getAllTodos().first()

        val collection = firestore.collection("todos")

        list.forEach { todo ->
            collection.document(todo.id.toString())
                .set(todo)
        }
    }

    suspend fun syncFromFirebase() {
        val snapshot = firestore.collection("todos")
            .get().await()
        val remoteTodos = snapshot.toObjects(TodoEntity::class.java)

        val localTodos = dao.getAllTodos().first()

        val merged = mutableListOf<TodoEntity>()

        remoteTodos.forEach { remote ->
            val local = localTodos.find { it.id == remote.id }

            if (local == null) {
                //exists only in firebase
                merged.add(remote)
            } else {
                //conflict case
                if (remote.lastModified > local.lastModified) {
                    merged.add(remote)
                    //firebase wins
                } else {
                    merged.add(local)
                    //local wins
                }
            }
        }

        Log.d("TAG", "todos = ${merged.size}")

        dao.clearAll()
        dao.insertAll(merged)
    }

    suspend fun syncFromDatabase() {
        val snapshot = firestore.collection("todos")
            .get().await()

        val remoteTodos = snapshot.toObjects(TodoEntity::class.java)

        dao.clearAll()
        dao.insertAll(remoteTodos)
    }

    private fun enqueueSyncWorker() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = OneTimeWorkRequestBuilder<TodoSyncWorker>()
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context)
            .enqueueUniqueWork(
                "todo_sync_work",
                ExistingWorkPolicy.KEEP,
                request
            )
    }

    suspend fun cleanupDeletedTodos(days: Int = 7) {
        val thresholdTime = System.currentTimeMillis() -
                (days * 24 * 60 * 60 * 1000L)
        dao.hardDeleteTodos(thresholdTime)
    }
}


===== FILE: ./app/src/main/java/com/raza/notesapp01/TodoSyncWorker.kt =====
package com.raza.notesapp01

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.firestore.FirebaseFirestore
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import javax.inject.Inject

@HiltWorker
class TodoSyncWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: TodoRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            repository.syncFromFirebase()
            repository.cleanupDeletedTodos(7)

            Log.d("TAG", "worker success")
            Result.success()
        } catch (e: Exception) {

            e.printStackTrace()

            Log.d("TAG", "worker exception")
            Result.retry()
        }
    }
}


===== FILE: ./app/src/main/java/com/raza/notesapp01/ui/theme/Color.kt =====
package com.raza.notesapp01.ui.theme

import androidx.compose.ui.graphics.Color

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)


===== FILE: ./app/src/main/java/com/raza/notesapp01/ui/theme/Theme.kt =====
package com.raza.notesapp01.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun NotesApp01Theme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}


===== FILE: ./app/src/main/java/com/raza/notesapp01/ui/theme/Type.kt =====
package com.raza.notesapp01.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Set of Material typography styles to start with
val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )
    /* Other default text styles to override
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
    */
)


===== FILE: ./app/src/test/java/com/raza/notesapp01/ExampleUnitTest.kt =====
package com.raza.notesapp01

import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }
}


