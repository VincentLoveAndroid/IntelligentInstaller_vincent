package vincent.intelligentinstaller;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void install(View view) {
        String Filepath = Environment.getExternalStorageDirectory() + "/" + "new.apk";
        File file = new File(Filepath);
        InputStream is = null;
        FileOutputStream fos = null;
        try {
            is = getAssets().open("new.apk");
            fos = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int len;
            while ((len = is.read(buf)) != -1) {
                fos.write(buf, 0, len);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (is != null) try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (fos != null) try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        AutoInstaller autoInstaller = new AutoInstaller(this, file);
        autoInstaller.runInstaller();
    }
}
