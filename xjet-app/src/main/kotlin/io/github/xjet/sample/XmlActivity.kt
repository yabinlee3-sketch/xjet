package io.github.xjet.sample

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.compose.material3.Text
import androidx.compose.ui.platform.ComposeView
import io.github.xjet.annotation.XRoute
import io.github.xjet.compose.XJetTheme
import io.github.xjet.xml.XJetActivity

@XRoute(path = "xmlScreen", group = "xml", title = "XML + embedded Compose")
class XmlActivity : XJetActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 96, 32, 96)
            setBackgroundColor(Color.WHITE)
        }

        root.addView(TextView(this).apply {
            text = "Classic XML screen. XJet routes launched me via @XRoute + SimpleRouterProvider."
            textSize = 18f
            gravity = Gravity.CENTER
        })

        root.addView(ComposeView(this).apply {
            setContent {
                XJetTheme {
                    Text("I am a ComposeView embedded inside an XML activity.")
                }
            }
        })

        root.addView(Button(this).apply {
            text = "Back"
            setOnClickListener { finish() }
        })

        setContentView(root)
    }
}

