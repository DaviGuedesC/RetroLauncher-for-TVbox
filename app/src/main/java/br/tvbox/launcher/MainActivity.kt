package br.tvbox.launcher

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.View

class MainActivity : Activity() {

    private lateinit var tela: LauncherView
    private val handler = Handler(Looper.getMainLooper())
    private val relogio = object : Runnable {
        override fun run() {
            tela.atualizarHora()
            handler.postDelayed(this, 15_000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tela = LauncherView(this)
        val salvo = getSharedPreferences("cfg", Context.MODE_PRIVATE).getString("tema", null)
        tela.tema = TEMAS.firstOrNull { it.id == salvo } ?: TEMAS[0]
        setContentView(tela)
        esconderBarras()
    }

    override fun onResume() {
        super.onResume()
        esconderBarras()
        tela.atualizarInstalados()
        relogio.run()
    }

    override fun onPause() {
        handler.removeCallbacks(relogio)
        super.onPause()
    }

    @Suppress("DEPRECATION")
    private fun esconderBarras() {
        tela.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN or
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
    }

    private fun proximoTema() {
        val i = TEMAS.indexOf(tela.tema)
        val novo = TEMAS[(i + 1) % TEMAS.size]
        tela.tema = novo
        getSharedPreferences("cfg", Context.MODE_PRIVATE).edit().putString("tema", novo.id).apply()
    }

    private fun ehOk(k: Int) =
        k == KeyEvent.KEYCODE_DPAD_CENTER || k == KeyEvent.KEYCODE_ENTER || k == KeyEvent.KEYCODE_NUMPAD_ENTER

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        // Easter egg aberto: OK ou VOLTAR fecham; o resto e ignorado.
        if (tela.egg) {
            if (ehOk(keyCode) || keyCode == KeyEvent.KEYCODE_BACK) tela.fecharEgg()
            return true
        }
        val primeira = event.repeatCount == 0
        return when {
            keyCode == KeyEvent.KEYCODE_DPAD_UP -> { if (primeira) tela.sequencia(0); true }
            keyCode == KeyEvent.KEYCODE_DPAD_DOWN -> { if (primeira) tela.sequencia(1); true }
            keyCode == KeyEvent.KEYCODE_DPAD_LEFT -> { if (primeira) tela.sequencia(2); tela.mover(-1); true }
            keyCode == KeyEvent.KEYCODE_DPAD_RIGHT -> { if (primeira) tela.sequencia(3); tela.mover(1); true }
            ehOk(keyCode) -> { event.startTracking(); true }
            keyCode == KeyEvent.KEYCODE_MENU || keyCode == KeyEvent.KEYCODE_SETTINGS -> {
                if (primeira) proximoTema()
                true
            }
            else -> super.onKeyDown(keyCode, event)
        }
    }

    override fun onKeyLongPress(keyCode: Int, event: KeyEvent): Boolean {
        if (ehOk(keyCode) && !tela.egg) {
            proximoTema()
            return true
        }
        return super.onKeyLongPress(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        if (ehOk(keyCode) && event.isTracking && !event.isCanceled && !tela.egg) {
            tela.abrir()
            return true
        }
        return super.onKeyUp(keyCode, event)
    }

    // Launcher nao deve fechar com o botao voltar.
    @Deprecated("Launcher fica sempre aberto")
    override fun onBackPressed() {
        if (tela.egg) tela.fecharEgg()
    }
}
