package br.com.terramistica.launcher;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class MainActivity extends Activity {
    private TextView status, players, gmx, event, updates;
    private final Handler ui = new Handler(Looper.getMainLooper());

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_main);
        status = findViewById(R.id.status);
        players = findViewById(R.id.players);
        gmx = findViewById(R.id.gmx);
        event = findViewById(R.id.event);
        updates = findViewById(R.id.updates);
        Button play = findViewById(R.id.play);
        play.setOnClickListener(v -> openSamp());
        refresh();
    }

    @Override protected void onResume() {
        super.onResume();
        refresh();
    }

    private void refresh() {
        new Thread(this::queryServer).start();
        if (!BuildConfig.CONTENT_URL.isEmpty()) new Thread(this::loadContent).start();
    }

    private void queryServer() {
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setSoTimeout(3500);
            String[] parts = BuildConfig.SERVER_IP.split("\\.");
            ByteArrayOutputStream request = new ByteArrayOutputStream();
            request.write(new byte[]{'S','A','M','P'});
            for (String part : parts) request.write(Integer.parseInt(part));
            request.write(BuildConfig.SERVER_PORT & 0xFF);
            request.write((BuildConfig.SERVER_PORT >> 8) & 0xFF);
            request.write('i');
            byte[] bytes = request.toByteArray();
            InetAddress address = InetAddress.getByName(BuildConfig.SERVER_IP);
            socket.send(new DatagramPacket(bytes, bytes.length, address, BuildConfig.SERVER_PORT));
            byte[] response = new byte[2048];
            DatagramPacket packet = new DatagramPacket(response, response.length);
            socket.receive(packet);
            if (packet.getLength() < 16 || response[10] != 'i') throw new Exception("Resposta inválida");
            int online = (response[12] & 0xFF) | ((response[13] & 0xFF) << 8);
            int max = (response[14] & 0xFF) | ((response[15] & 0xFF) << 8);
            ui.post(() -> {
                status.setText("● SERVIDOR ONLINE");
                status.setTextColor(Color.rgb(40, 220, 125));
                players.setText("Jogadores: " + online + "/" + max);
            });
        } catch (Exception ignored) {
            ui.post(() -> {
                status.setText("● SERVIDOR OFFLINE");
                status.setTextColor(Color.rgb(255, 85, 100));
                players.setText("Jogadores: 0/--");
            });
        }
    }

    private void loadContent() {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(BuildConfig.CONTENT_URL).openConnection();
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setRequestProperty("Accept", "application/json");
            try (InputStream in = connection.getInputStream()) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                byte[] buffer = new byte[2048];
                for (int read; (read = in.read(buffer)) != -1;) out.write(buffer, 0, read);
                JSONObject json = new JSONObject(out.toString(StandardCharsets.UTF_8.name()));
                String g = json.optString("gmx", "Todos os dias às 06:00");
                String e = json.optString("evento", "Sem evento programado.");
                String u = json.optString("atualizacoes", "Versão 1.0 • Launcher oficial");
                ui.post(() -> { gmx.setText(g); event.setText(e); updates.setText(u); });
            }
        } catch (Exception ignored) {
            // Mantém o último conteúdo visível quando o painel estiver indisponível.
        } finally {
            if (connection != null) connection.disconnect();
        }
    }

    private void openSamp() {
        Uri server = Uri.parse("samp://" + BuildConfig.SERVER_IP + ":" + BuildConfig.SERVER_PORT);
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, server));
        } catch (ActivityNotFoundException ex) {
            Toast.makeText(this, "Instale um cliente SA-MP compatível para jogar.", Toast.LENGTH_LONG).show();
            try {
                startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("market://search?q=SA-MP launcher&c=apps")));
            } catch (ActivityNotFoundException ignored) {
                startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/search?q=SA-MP%20launcher&c=apps")));
            }
        }
    }
}
