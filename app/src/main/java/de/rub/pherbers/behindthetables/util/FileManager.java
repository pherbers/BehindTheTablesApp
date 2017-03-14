package de.rub.pherbers.behindthetables.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;

import java.io.File;
import java.io.IOException;

import de.rub.pherbers.behindthetables.R;
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
		//TODO does this parent file thing work?

		//if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
		//	f = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
		//}
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
			Timber.i("Failed to delete the cache dir: " + dir.getAbsolutePath(), e);
			return false;
		}
	}

	public File getCacheDir() {
		return context.getCacheDir();
	}

	public File getJSONTableDir() {
		File f = new File(getOriginDir(), context.getString(R.string.const_dir_json_resource));
		if (!f.exists()) f.mkdirs();
		createNoMediaFile(f);

		return f;
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
			return f.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
			Timber.e(e, "Wanted to create 'NomediaFile' in " + parent.getAbsolutePath() + " but it failed!");
			return false;
		}
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
