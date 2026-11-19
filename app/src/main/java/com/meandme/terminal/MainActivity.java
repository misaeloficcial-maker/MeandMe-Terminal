package com.meandme.terminal;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.termux.terminal.TerminalColors;
import com.termux.terminal.TerminalSession;
import com.termux.terminal.TerminalSessionClient;
import com.termux.view.TerminalView;
import com.termux.view.TerminalViewClient;

import java.io.File;

public class MainActivity extends Activity implements TerminalViewClient, TerminalSessionClient {

    private TerminalView mTerminalView;
    private TerminalSession mSession;
    private boolean mCtrlDown = false;
    private boolean mAltDown = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTerminalView = findViewById(R.id.terminal_view);
        mTerminalView.setTerminalViewClient(this);
        mTerminalView.setTextSize(32);
        try {
            mTerminalView.setTypeface(Typeface.MONOSPACE);
        } catch (Exception ignored) {}

        // Extra keys bar
        LinearLayout extraKeys = findViewById(R.id.extra_keys);
        String[] keys = {"Esc", "Tab", "Ctrl", "Alt", "-", "/", "Home", "Up", "Down", "Left", "Right", "PgUp", "PgDn", "End"};
        for (String k : keys) {
            Button b = new Button(this);
            b.setText(k);
            b.setTextColor(Color.parseColor("#CCFFCC"));
            b.setBackgroundColor(Color.parseColor("#1A2E1F"));
            b.setTextSize(12);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(2, 2, 2, 2);
            b.setLayoutParams(lp);
            b.setOnClickListener(v -> onExtraKey(k));
            extraKeys.addView(b);
        }

        // Start session with Android shell
        String shell = "/system/bin/sh";
        File f = new File(shell);
        if (!f.exists()) shell = "/bin/sh";
        String cwd = getFilesDir().getAbsolutePath();
        // Ensure cwd exists
        new File(cwd).mkdirs();
        mSession = new TerminalSession(shell, cwd, null, null, null, this);
        mTerminalView.attachSession(mSession);
        mSession.initializeEmulator(80, 24);
        mTerminalView.requestFocus();
    }

    private void onExtraKey(String key) {
        if (mSession == null) return;
        switch (key) {
            case "Esc": mSession.write("\u001b"); break;
            case "Tab": mSession.write("\t"); break;
            case "Ctrl":
                mCtrlDown = !mCtrlDown;
                Toast.makeText(this, "Ctrl " + (mCtrlDown ? "ON" : "OFF"), Toast.LENGTH_SHORT).show();
                break;
            case "Alt":
                mAltDown = !mAltDown;
                Toast.makeText(this, "Alt " + (mAltDown ? "ON" : "OFF"), Toast.LENGTH_SHORT).show();
                break;
            case "-": mSession.write("-"); break;
            case "/": mSession.write("/"); break;
            case "Home": mSession.write("\u001b[H"); break;
            case "End": mSession.write("\u001b[F"); break;
            case "Up": mSession.write("\u001b[A"); break;
            case "Down": mSession.write("\u001b[B"); break;
            case "Right": mSession.write("\u001b[C"); break;
            case "Left": mSession.write("\u001b[D"); break;
            case "PgUp": mSession.write("\u001b[5~"); break;
            case "PgDn": mSession.write("\u001b[6~"); break;
        }
        mTerminalView.requestFocus();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mTerminalView != null) mTerminalView.requestFocus();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mSession != null) {
            try { mSession.finishIfRunning(); } catch (Exception ignored) {}
        }
    }

    // TerminalViewClient
    @Override
    public float onScale(float scale) { return scale; }

    @Override
    public void onSingleTapUp(MotionEvent e) {
        // Show keyboard on tap
        if (mTerminalView != null) mTerminalView.requestFocus();
    }

    @Override
    public boolean shouldBackButtonBeMappedToEscape() { return false; }

    @Override
    public void copyModeChanged(boolean copyMode) {}

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent e, TerminalSession session) { return false; }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent e) { return false; }

    @Override
    public boolean readControlKey() {
        boolean result = mCtrlDown;
        mCtrlDown = false;
        return result;
    }

    @Override
    public boolean readAltKey() {
        boolean result = mAltDown;
        mAltDown = false;
        return result;
    }

    @Override
    public boolean onCodePoint(int codePoint, boolean ctrlDown, TerminalSession session) { return false; }

    @Override
    public boolean onLongPress(MotionEvent event) { return false; }

    // TerminalSessionClient
    @Override
    public void onTextChanged(TerminalSession session) {
        if (mTerminalView != null) mTerminalView.onTextChanged(session);
    }

    @Override
    public void onTitleChanged(TerminalSession session) {}

    @Override
    public void onSessionFinished(TerminalSession session) {
        runOnUiThread(() -> {
            Toast.makeText(this, "Sesión terminada. Cierra y vuelve a abrir MeandMe.", Toast.LENGTH_LONG).show();
        });
    }

    @Override
    public void onCopyTextToClipboard(TerminalSession session, String text) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null) {
            clipboard.setPrimaryClip(ClipData.newPlainText("terminal", text));
        }
    }

    @Override
    public void onPasteTextFromClipboard(TerminalSession session) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard == null) return;
        ClipData clip = clipboard.getPrimaryClip();
        if (clip != null && clip.getItemCount() > 0) {
            CharSequence text = clip.getItemAt(0).getText();
            if (text != null && mSession != null) {
                mSession.write(text.toString());
            }
        }
    }

    @Override
    public void onBell(TerminalSession session) {}

    @Override
    public void onColorsChanged(TerminalSession session) {}

    @Override
    public void onTerminalCursorStateChange(boolean state) {}

    @Override
    public Integer getTerminalCursorStyle() { return null; }
}
