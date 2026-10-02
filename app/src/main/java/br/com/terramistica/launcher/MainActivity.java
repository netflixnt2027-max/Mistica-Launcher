package br.com.terramistica.launcher;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.ProgressBar;
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
    private static final String DISCORD_URL="https://discord.gg/KKgsh3n8Mz";
    private TextView terraStatus,terraPlayers,nevoraStatus,nevoraPlayers,gmx,event,updates,bannerServer,loadingText;
    private final Handler ui=new Handler(Looper.getMainLooper());
    private boolean terraOnline=false,nevoraOnline=false;
    private String selectedIp=BuildConfig.TERRA_IP; private int selectedPort=BuildConfig.TERRA_PORT;

    @Override protected void onCreate(Bundle state){
        super.onCreate(state); setContentView(R.layout.activity_main);
        terraStatus=findViewById(R.id.terra_status); terraPlayers=findViewById(R.id.terra_players);
        nevoraStatus=findViewById(R.id.nevora_status); nevoraPlayers=findViewById(R.id.nevora_players);
        gmx=findViewById(R.id.gmx); event=findViewById(R.id.event); updates=findViewById(R.id.updates);
        bannerServer=findViewById(R.id.banner_server); loadingText=findViewById(R.id.loading_text);
        Button terra=findViewById(R.id.terra_play),nevora=findViewById(R.id.nevora_play),play=findViewById(R.id.play_now);
        Button refresh=findViewById(R.id.refresh_button),discord=findViewById(R.id.discord_button);
        terra.setOnClickListener(v->{selectServer(BuildConfig.TERRA_IP,BuildConfig.TERRA_PORT,"Terra Mística RP");openSamp(selectedIp,selectedPort);});
        nevora.setOnClickListener(v->{selectServer(BuildConfig.NEVORA_IP,BuildConfig.NEVORA_PORT,"Nevora RPG");openSamp(selectedIp,selectedPort);});
        play.setOnClickListener(v->openSamp(selectedIp,selectedPort));
        refresh.setOnClickListener(v->refresh()); discord.setOnClickListener(v->openUrl(DISCORD_URL));
        startLoadingAnimation(); refresh();
    }

    private void startLoadingAnimation(){
        AlphaAnimation a=new AlphaAnimation(0.35f,1f); a.setDuration(850); a.setRepeatMode(AlphaAnimation.REVERSE); a.setRepeatCount(AlphaAnimation.INFINITE);
        loadingText.startAnimation(a);
        new Handler(Looper.getMainLooper()).postDelayed(()->loadingText.setText("LAUNCHER PRONTO • ESCOLHA UM SERVIDOR"),2200);
    }

    private void selectServer(String ip,int port,String name){
        selectedIp=ip; selectedPort=port; bannerServer.setText(name+" • selecionado");
    }

    @Override protected void onResume(){super.onResume();refresh();}

    private void refresh(){
        loadingText.setText("VERIFICANDO SERVIDORES...");
        new Thread(()->queryServer(BuildConfig.TERRA_IP,BuildConfig.TERRA_PORT,terraStatus,terraPlayers,true)).start();
        new Thread(()->queryServer(BuildConfig.NEVORA_IP,BuildConfig.NEVORA_PORT,nevoraStatus,nevoraPlayers,false)).start();
        if(!BuildConfig.CONTENT_URL.isEmpty())new Thread(this::loadContent).start();
    }

    private void queryServer(String ip,int port,TextView status,TextView players,boolean terra){
        try(DatagramSocket socket=new DatagramSocket()){
            socket.setSoTimeout(3500); String[] parts=ip.split("\\."); ByteArrayOutputStream req=new ByteArrayOutputStream();
            req.write(new byte[]{'S','A','M','P'}); for(String p:parts)req.write(Integer.parseInt(p));
            req.write(port&255);req.write((port>>8)&255);req.write('i'); byte[] data=req.toByteArray();
            socket.send(new DatagramPacket(data,data.length,InetAddress.getByName(ip),port));
            byte[] response=new byte[2048]; DatagramPacket packet=new DatagramPacket(response,response.length);socket.receive(packet);
            if(packet.getLength()<16||response[10]!='i')throw new Exception();
            int online=(response[12]&255)|((response[13]&255)<<8),max=(response[14]&255)|((response[15]&255)<<8);
            ui.post(()->{status.setText("● SERVIDOR ONLINE");status.setTextColor(Color.rgb(40,220,125));players.setText("Jogadores: "+online+"/"+max);if(terra)terraOnline=true;else nevoraOnline=true;loadingText.setText("SERVIDORES VERIFICADOS • PRONTO PARA JOGAR");});
        }catch(Exception e){ui.post(()->{status.setText("● SERVIDOR OFFLINE");status.setTextColor(Color.rgb(255,85,100));players.setText("Jogadores: 0/--");});}
    }

    private void loadContent(){
        HttpURLConnection c=null;try{
            c=(HttpURLConnection)new URL(BuildConfig.CONTENT_URL).openConnection();c.setConnectTimeout(5000);c.setReadTimeout(5000);c.setRequestProperty("Accept","application/json");
            try(InputStream in=c.getInputStream()){ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] b=new byte[2048];for(int n;(n=in.read(b))!=-1;)out.write(b,0,n);
                JSONObject j=new JSONObject(out.toString(StandardCharsets.UTF_8.name()));String gv=j.optString("gmx","Todos os dias às 06:00"),ev=j.optString("evento","Sem evento programado."),up=j.optString("atualizacoes","Versão 1.1.0 • Launcher oficial");
                ui.post(()->{gmx.setText(gv);event.setText(ev);updates.setText(up);});
            }
        }catch(Exception ignored){}finally{if(c!=null)c.disconnect();}
    }

    private void openSamp(String ip,int port){
        Uri server=Uri.parse("samp://"+ip+":"+port);try{startActivity(new Intent(Intent.ACTION_VIEW,server));}
        catch(ActivityNotFoundException ex){Toast.makeText(this,"Instale um cliente SA-MP compatível para jogar.",Toast.LENGTH_LONG).show();
            try{startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse("market://search?q=SA-MP launcher&c=apps")));}
            catch(ActivityNotFoundException ignored){openUrl("https://play.google.com/store/search?q=SA-MP%20launcher&c=apps");}}
    }
    private void openUrl(String url){try{startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(url)));}catch(ActivityNotFoundException ignored){Toast.makeText(this,"Não foi possível abrir o link.",Toast.LENGTH_SHORT).show();}}
}