package io.github.xjet.sample

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.viewModels
import androidx.compose.material3.Text
import androidx.compose.ui.platform.ComposeView
import io.github.xjet.compose.XJetTheme
import io.github.xjet.core.UiState
import io.github.xjet.xml.XJetActivity
import io.github.xjet.xml.setUiState
import kotlinx.coroutines.flow.StateFlow

class XmlActivity : XJetActivity<XmlViewModel>() {

    override val viewModel: XmlViewModel by viewModels()
    override val uiState: StateFlow<UiState> get() = viewModel.uiState

    private lateinit var progressBar: ProgressBar
    private lateinit var errorText: TextView
    private lateinit var content: LinearLayout
    private lateinit var greetingText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        buildViews()
        setContentView(buildRoot())
    }

    override fun onUiState(state: UiState) {
        setUiState(
            state = state,
            loading = progressBar,
            error = errorText,
            content = content,
            onError = { message -> errorText.text = message ?: "出错了，点击重试" },
        )
        if (state is UiState.Content) {
            greetingText.text = viewModel.data.value.greeting
        }
    }

    private fun buildViews() {
        progressBar = ProgressBar(this)
        errorText = TextView(this).apply {
            visibility = android.view.View.GONE
            gravity = Gravity.CENTER
            setTextColor(Color.DKGRAY)
        }

        content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(16), dp(16), dp(16))
        }

        greetingText = TextView(this).apply {
            textSize = 18f
            gravity = Gravity.CENTER
        }
        content.addView(greetingText)

        content.addView(ComposeView(this).apply {
            setContent {
                XJetTheme {
                    Text("I am a ComposeView embedded inside an XML MVVM screen.")
                }
            }
        })

        content.addView(Button(this).apply {
            text = "Back"
            setOnClickListener { finish() }
        })

        content.addView(Button(this).apply {
            text = "Retry"
            setOnClickListener { viewModel.retry() }
        })
    }

    private fun buildRoot(): FrameLayout {
        val root = FrameLayout(this).apply {
            setBackgroundColor(Color.WHITE)
        }

        root.addView(
            progressBar,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER
            )
        )
        root.addView(
            errorText,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT,
                Gravity.CENTER
            )
        )
        root.addView(content, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT))
        return root
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}

