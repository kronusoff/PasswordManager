package com.example.passwordmanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.passwordmanager.data.AppDatabase
import com.example.passwordmanager.data.PasswordEntry
import com.example.passwordmanager.ui.PasswordViewModel
import androidx.room.Room
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "password_database"
        ).build()

        setContent {
            PasswordGeneratorApp(database)
        }
    }
}

@Composable
fun PasswordGeneratorApp(database: AppDatabase) {
    // Поддержка тёмной/светлой темы
    val isDarkTheme = isSystemInDarkTheme()
    MaterialTheme(
        colorScheme = if (isDarkTheme) darkColorScheme() else lightColorScheme()
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            // Определяем ориентацию
            val configuration = LocalConfiguration.current
            val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

            if (isLandscape) {
                LandscapeLayout(database)
            } else {
                PortraitLayout(database)
            }
        }
    }
}

@Composable
fun PortraitLayout(database: AppDatabase) {
    val viewModel: PasswordViewModel = viewModel(factory = PasswordViewModelFactory(database))
    var accountName by remember { mutableStateOf(TextFieldValue("")) }
    var generatedPassword by remember { mutableStateOf("") }
    var length by remember { mutableStateOf(12) }
    var useLowercase by remember { mutableStateOf(true) }
    var useUppercase by remember { mutableStateOf(true) }
    var useDigits by remember { mutableStateOf(true) }
    var useSpecial by remember { mutableStateOf(true) }
    var showDeleteDialog by remember { mutableStateOf<PasswordEntry?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Заголовок с иконкой
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(8.dp))
            Text("Генератор паролей", style = MaterialTheme.typography.headlineSmall)
        }

        // Ввод названия аккаунта
        OutlinedTextField(
            value = accountName,
            onValueChange = { accountName = it },
            label = { Text("Название аккаунта") },
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp)),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        )

        // Длина пароля
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("Длина пароля: $length", style = MaterialTheme.typography.bodyLarge)
                Slider(
                    value = length.toFloat(),
                    onValueChange = { length = it.toInt() },
                    valueRange = 8f..32f,
                    steps = 23,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Типы символов
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
                Row {
                    Checkbox(checked = useLowercase, onCheckedChange = { useLowercase = it })
                    Text("Строчные буквы", style = MaterialTheme.typography.bodyMedium)
                }
                Row {
                    Checkbox(checked = useUppercase, onCheckedChange = { useUppercase = it })
                    Text("Заглавные буквы", style = MaterialTheme.typography.bodyMedium)
                }
                Row {
                    Checkbox(checked = useDigits, onCheckedChange = { useDigits = it })
                    Text("Цифры", style = MaterialTheme.typography.bodyMedium)
                }
                Row {
                    Checkbox(checked = useSpecial, onCheckedChange = { useSpecial = it })
                    Text("Спецсимволы", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        // Кнопка генерации
        Button(
            onClick = {
                generatedPassword = viewModel.generatePassword(
                    length, useLowercase, useUppercase, useDigits, useSpecial
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Сгенерировать пароль", style = MaterialTheme.typography.labelLarge)
        }

        // Отображение сгенерированного пароля с анимацией
        AnimatedVisibility(
            visible = generatedPassword.isNotEmpty(),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = generatedPassword,
                    onValueChange = { },
                    label = { Text("Сгенерированный пароль") },
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                )
                Button(
                    onClick = {
                        if (accountName.text.isNotEmpty() && generatedPassword.isNotEmpty()) {
                            viewModel.savePassword(accountName.text, generatedPassword)
                            accountName = TextFieldValue("")
                            generatedPassword = ""
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Сохранить пароль", style = MaterialTheme.typography.labelLarge)
                }
            }
        }

        // История паролей
        Text("История паролей", style = MaterialTheme.typography.titleMedium)
        val passwords by viewModel.passwords.collectAsState()
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(passwords) { password ->
                PasswordCard(
                    password = password,
                    onDeleteClick = { showDeleteDialog = password }
                )
            }
        }

        // Диалог подтверждения удаления
        showDeleteDialog?.let { password ->
            AlertDialog(
                onDismissRequest = { showDeleteDialog = null },
                title = { Text("Удалить пароль?") },
                text = { Text("Вы уверены, что хотите удалить пароль для ${password.accountName}?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.deletePassword(password)
                            showDeleteDialog = null
                        }
                    ) {
                        Text("Удалить")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = null }) {
                        Text("Отмена")
                    }
                }
            )
        }
    }
}

@Composable
fun LandscapeLayout(database: AppDatabase) {
    val viewModel: PasswordViewModel = viewModel(factory = PasswordViewModelFactory(database))
    var accountName by remember { mutableStateOf(TextFieldValue("")) }
    var generatedPassword by remember { mutableStateOf("") }
    var length by remember { mutableStateOf(12) }
    var useLowercase by remember { mutableStateOf(true) }
    var useUppercase by remember { mutableStateOf(true) }
    var useDigits by remember { mutableStateOf(true) }
    var useSpecial by remember { mutableStateOf(true) }
    var showDeleteDialog by remember { mutableStateOf<PasswordEntry?>(null) }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Панель управления (генерация пароля)
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text("Генератор паролей", style = MaterialTheme.typography.headlineSmall)
            }

            OutlinedTextField(
                value = accountName,
                onValueChange = { accountName = it },
                label = { Text("Название аккаунта") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Длина пароля: $length", style = MaterialTheme.typography.bodyLarge)
                    Slider(
                        value = length.toFloat(),
                        onValueChange = { length = it.toInt() },
                        valueRange = 8f..32f,
                        steps = 23,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row {
                        Checkbox(checked = useLowercase, onCheckedChange = { useLowercase = it })
                        Text("Строчные буквы", style = MaterialTheme.typography.bodyMedium)
                    }
                    Row {
                        Checkbox(checked = useUppercase, onCheckedChange = { useUppercase = it })
                        Text("Заглавные буквы", style = MaterialTheme.typography.bodyMedium)
                    }
                    Row {
                        Checkbox(checked = useDigits, onCheckedChange = { useDigits = it })
                        Text("Цифры", style = MaterialTheme.typography.bodyMedium)
                    }
                    Row {
                        Checkbox(checked = useSpecial, onCheckedChange = { useSpecial = it })
                        Text("Спецсимволы", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            Button(
                onClick = {
                    generatedPassword = viewModel.generatePassword(
                        length, useLowercase, useUppercase, useDigits, useSpecial
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Сгенерировать пароль", style = MaterialTheme.typography.labelLarge)
            }

            AnimatedVisibility(
                visible = generatedPassword.isNotEmpty(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = generatedPassword,
                        onValueChange = { },
                        label = { Text("Сгенерированный пароль") },
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                    )
                    Button(
                        onClick = {
                            if (accountName.text.isNotEmpty() && generatedPassword.isNotEmpty()) {
                                viewModel.savePassword(accountName.text, generatedPassword)
                                accountName = TextFieldValue("")
                                generatedPassword = ""
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Сохранить пароль", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }

        // История паролей
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("История паролей", style = MaterialTheme.typography.titleMedium)
            val passwords by viewModel.passwords.collectAsState()
            LazyColumn(
                modifier = Modifier.fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(passwords) { password ->
                    PasswordCard(
                        password = password,
                        onDeleteClick = { showDeleteDialog = password }
                    )
                }
            }
        }

        // Диалог подтверждения удаления
        showDeleteDialog?.let { password ->
            AlertDialog(
                onDismissRequest = { showDeleteDialog = null },
                title = { Text("Удалить пароль?") },
                text = { Text("Вы уверены, что хотите удалить пароль для ${password.accountName}?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.deletePassword(password)
                            showDeleteDialog = null
                        }
                    ) {
                        Text("Удалить")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = null }) {
                        Text("Отмена")
                    }
                }
            )
        }
    }
}

@Composable
fun PasswordCard(password: PasswordEntry, onDeleteClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Аккаунт: ${password.accountName}", style = MaterialTheme.typography.bodyLarge)
                Text("Пароль: ${password.password}", style = MaterialTheme.typography.bodyMedium)
                Text(
                    "Дата: ${
                        SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
                            .format(Date(password.timestamp))
                    }",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            IconButton(onClick = onDeleteClick) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Удалить пароль",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

class PasswordViewModelFactory(private val database: AppDatabase) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PasswordViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PasswordViewModel(database) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}