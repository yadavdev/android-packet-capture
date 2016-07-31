package devilrx.smartpacket;

import android.content.res.AssetManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        int exitcode = loadTcpdumpFromAssets();
        if(exitcode != 0) throw new RuntimeException("Copying tcpdump binary failed.");
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

    private int loadTcpdumpFromAssets(){
        int retval = 0;
        String rootDataPath = getApplicationInfo().dataDir + "/files";
        String filePath = rootDataPath + "/tcpdump.bin";
        File file = new File(filePath);
        AssetManager assetManager = getAssets();

        try{
            if (file.exists()) {
                ((TextView)findViewById(R.id.main_tv)).setText("App is already configured!");
                return retval;
            }
            new File(rootDataPath).mkdirs();
            retval = copyFileFromAsset(assetManager, "tcpdump.bin", filePath);

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
