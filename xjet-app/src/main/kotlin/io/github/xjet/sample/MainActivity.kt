package io.github.xjet.sample

import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.xjet.compose.XJetTheme
import io.github.xjet.compose.collectAsEffect

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            XJetTheme {
                HomeScreen()
            }
        }
    }
}

@Composable
fun HomeScreen(vm: HomeViewModel = viewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    vm.toasts.collectAsEffect { message ->
        android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_SHORT).show()
    }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("XJet 2.0 sample", style = MaterialTheme.typography.headlineSmall)
            Text(state.greeting)
            Text(state.config)
            Button(onClick = { vm.onToast() }, modifier = Modifier.fillMaxWidth()) {
                Text("Emit one-shot SharedFlow event")
            }
            Button(onClick = { vm.onOpenXml() }, modifier = Modifier.fillMaxWidth()) {
                Text("Open XML screen via @XRoute router")
            }
            AndroidView(
                factory = { lContext ->
                    LayoutInflater.from(lContext).inflate(R.layout.embedded_xml, null)
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

