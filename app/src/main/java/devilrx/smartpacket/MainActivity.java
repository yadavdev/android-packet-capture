package devilrx.smartpacket;

import android.app.ProgressDialog;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import eu.chainfire.libsuperuser.Shell;

public class MainActivity extends AppCompatActivity {

    private ProgressDialog progressbox;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final Button bt = (Button)findViewById(R.id.button_packet_capture);
        progressbox = new ProgressDialog(this);
        progressbox.setTitle("Initialising");
        progressbox.setMessage("Requesting root permissions..");
        progressbox.setIndeterminate(true);
        progressbox.setCancelable(false);
        progressbox.show();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                final Boolean isRootAvailable = Shell.SU.available();
                Boolean processExists = false;
                String pid = null;
                if(isRootAvailable) {
                    List<String> out = Shell.SH.run("ps | grep tcpdump.bin");
                    if(out.size() == 1) {
                        processExists = true;
                        pid = (out.get(0).split("\\s+"))[1];
                    }
                    else if(out.size() == 0) {
                        if (loadTcpdumpFromAssets() != 0)
                            throw new RuntimeException("Copying tcpdump binary failed.");
                    }
                    else
                        throw new RuntimeException("Searching for running process returned unexpected result.");
                }

                final Boolean processExistsFinal = processExists;
                final String pidFinal = pid;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!isRootAvailable) {
                            ((TextView)findViewById(R.id.main_tv)).setText("Root permission denied or phone is not rooted!");
                            (findViewById(R.id.button_packet_capture)).setEnabled(false);
                        }
                        else {
                            if(processExistsFinal){
                                ((TextView)findViewById(R.id.main_tv)).setText("Tcpdump already running at pid: " + pidFinal );
                                bt.setText("Stop  Capture");
                                bt.setTag(1);
                            }
                            else {
                                ((TextView)findViewById(R.id.main_tv)).setText("Initialization Successful.");
                                bt.setTag(0);
                            }
                        }
                    }
                });
                progressbox.dismiss();
            }
        };
        new Thread(runnable).start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void  startCapture(View v) {
        Button bt = (Button)findViewById(R.id.button_packet_capture);
        bt.setEnabled(false);
        if((int)bt.getTag() == 1){
            //Using progress dialogue from main. See comment in: TcpdumpPacketCapture.stopTcpdumpCapture
            progressbox.setMessage("Killing Tcpdump process.");
            progressbox.show();
            TcpdumpPacketCapture.stopTcpdumpCapture(this);
            bt.setText("Start Capture");
            bt.setTag(0);
            ((TextView)findViewById(R.id.main_tv)).setText("Packet capture stopped. Output stored in sdcard/0001.pcap.");
            progressbox.dismiss();
        }
        else if ((int)bt.getTag() == 0){
            TcpdumpPacketCapture.initialiseCapture(this);
            bt.setText("Stop  Capture");
            bt.setTag(1);
        }
        bt.setEnabled(true);
    }

    public void stopAndExitActivity(View v) {
        TcpdumpPacketCapture.stopTcpdumpCapture(this);
        finish();
    }

    private int loadTcpdumpFromAssets(){
        int retval = 0;
        // updating progress message from other thread causes exception.
        // progressbox.setMessage("Setting up data..");
        String rootDataPath = getApplicationInfo().dataDir + "/files";
        String filePath = rootDataPath + "/tcpdump.bin";
        File file = new File(filePath);
        AssetManager assetManager = getAssets();

        try{
            if (file.exists()) {
                Shell.SH.run("chmod 755 " + filePath);
                return retval;
            }
            new File(rootDataPath).mkdirs();
            retval = copyFileFromAsset(assetManager, "tcpdump.bin", filePath);
            // Mark the binary executable
            Shell.SH.run("chmod 755 " + filePath);
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
            retval = -1;
        }
        return retval;
    }

    private int copyFileFromAsset(AssetManager assetManager, String sourcePath, String destPath) {
        byte[] buff = new byte[1024];
        int len;
        InputStream in;
        OutputStream out;
        try {
            in = assetManager.open(sourcePath);
            new File(destPath).createNewFile();
            out = new FileOutputStream(destPath);
            // write file
            while((len = in.read(buff)) != -1){
                out.write(buff, 0, len);
            }
            in.close();
            out.flush();
            out.close();
        }
        catch(Exception ex) {
            ex.printStackTrace();
            return -1;
        }
        return 0;
    }
}
