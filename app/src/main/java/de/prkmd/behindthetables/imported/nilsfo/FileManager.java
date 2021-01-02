package de.prkmd.behindthetables.imported.nilsfo;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import androidx.core.content.ContextCompat;
import android.text.format.DateUtils;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Date;

import de.prkmd.behindthetables.R;
import timber.log.Timber;

/**
 * Created by Nils on 13.03.2017.
 */

public class FileManager {

    public static final String NO_MEDIA = ".nomedia";

    private Context context;

    public FileManager(Context context) {
        this.context = context;
    }

    public File getOriginDir() {
        File f = Environment.getExternalStorageDirectory();

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            f = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        }
        f = new File(f, context.getString(R.string.app_name));

        Timber.v("Requested Origin File. Location: " + f.getAbsolutePath());
        if (!f.exists()) {
            if (!f.mkdirs()) {
                Timber.e("Origin directory not created");
            } else {
                Timber.v("Origin Dir was created without problems.");
            }
        } else {
            Timber.v("Origin Dir already exists. No need to create.");
        }
        createNoMediaFile(f);

        return f;
    }

    public boolean deleteCache() {
        File dir = getCacheDir();
        try {
            return deleteDir(dir);
        } catch (Exception e) {
            e.printStackTrace();
            Timber.i(e,"Failed to delete the cache dir: " + dir.getAbsolutePath());
            return false;
        }
    }

    public File getCacheDir() {
        return context.getCacheDir();
    }

    public void browseFolder(File file) {
        String folderPath = file.getAbsolutePath();

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        Uri myUri = Uri.parse(folderPath);
        intent.setDataAndType(myUri, "file/*");
        context.startActivity(intent);
    }

    public boolean createNoMediaFile(File parent) {
        if (!parent.exists() || !parent.isDirectory()) return false;

        File f = new File(parent, NO_MEDIA);
        if (f.exists()) return true;

        try {
            boolean newFile = f.createNewFile();
            MediaScannerConnection.scanFile(context, new String[]{f.toString()}, null, null);
            return newFile;
        } catch (IOException e) {
            e.printStackTrace();
            Timber.e(e, "Wanted to create 'NomediaFile' in " + parent.getAbsolutePath() + " but it failed!");
            return false;
        }
    }

    public boolean hasPermission(Activity activity) {
        return ContextCompat.checkSelfPermission(activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;
    }

    public boolean hasPermission() {
        return hasPermission((Activity) context);
    }

    public File getExternalTableDir() {
        File f = new File(getOriginDir(), context.getString(R.string.const_dir_json_resource));
        if (!f.exists()) f.mkdirs();
        createNoMediaFile(f);

        return f;
    }

    public long getFileSize(File f) {
        return f.length();
    }

    public String getFileSizeFormated(File f) {
        long size = getFileSize(f);
        if (size <= 0) return "0";

        String b = context.getString(R.string.unit_b);
        String kb = context.getString(R.string.unit_kb);
        String mb = context.getString(R.string.unit_mb);
        String gb = context.getString(R.string.unit_gb);
        String tb = context.getString(R.string.unit_tb);
        final String[] units = new String[]{b, kb, mb, gb, tb};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    public String getFileDateRelative(File f) {
        return DateUtils.getRelativeTimeSpanString(f.lastModified(), new Date().getTime(), 0L, DateUtils.FORMAT_ABBREV_ALL).toString();
    }

    public boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (String aChildren : children) {
                boolean success = deleteDir(new File(dir, aChildren));
                if (!success) {
                    return false;
                }
            }
            return dir.delete();
        } else
            return dir != null && dir.isFile() && dir.delete();
    }
}
