package de.rub.pherbers.behindthetables.activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.constraint.solver.ArrayLinkedVariables;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import de.rub.pherbers.behindthetables.BehindTheTables;
import de.rub.pherbers.behindthetables.R;
import de.rub.pherbers.behindthetables.data.TableFile;
import de.rub.pherbers.behindthetables.util.FileManager;
import timber.log.Timber;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

	public static final int PERMISSION_REQUEST_CODE = BehindTheTables.APP_TAG.hashCode() % (int) Math.pow(2, 15) - 1;

	private ArrayList<TableFile> foundTables;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		//FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
		//fab.setOnClickListener(new View.OnClickListener() {
		//	@Override
		//	public void onClick(View view) {
		//		Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
		//				.setAction("Action", null).show();
		//	}
		//});


		DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
				this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
		drawer.setDrawerListener(toggle);
		toggle.syncState();
		((NavigationView) findViewById(R.id.nav_view)).setNavigationItemSelectedListener(this);

		//TODO Code to request permission
		//Timber.i("My files dir: " + new FileManager(this).getJSONTableDir());
		//if (ContextCompat.checkSelfPermission(this,
		//		Manifest.permission.WRITE_EXTERNAL_STORAGE)
		//		!= PackageManager.PERMISSION_GRANTED) {
		//
		//	Timber.i("This app does not have permission to write the external storage!");
		//	Timber.i("Asking for permissions now.");
		//	ActivityCompat.requestPermissions(this,
		//			new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
		//			PERMISSION_REQUEST_CODE);
		//}
	}

	private void discoverTables() {
		String tableIdentifier = getString(R.string.const_table_identifier).toLowerCase();

		Timber.i("Just to be sure. Known raw file ID: " + R.raw.table_4y5pl2);
		Field[] fields = R.raw.class.getFields();
		for (int i = 0; i < fields.length - 1; i++) {
			String name = fields[i].getName();
			Timber.v("Found this file in the raw data: " + name);

			if (name.toLowerCase().startsWith(tableIdentifier)) {
				Timber.v("Found this raw data to be a valid table: " + name);
				foundTables.add(new TableFile(getResources().getIdentifier(name, "raw", getPackageName())));
			}
		}

		Timber.i("List of discovered tables: " + Arrays.toString(foundTables.toArray()));
	}

	@Override
	public void onBackPressed() {
		DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		if (drawer.isDrawerOpen(GravityCompat.START)) {
			drawer.closeDrawer(GravityCompat.START);
		} else {
			super.onBackPressed();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		foundTables = new ArrayList<>();
		discoverTables();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_home, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_settings:
				//TODO Settings?
				break;
			default:
				Timber.w("Unknown menu item selected.");
				break;
		}

		return super.onOptionsItemSelected(item);
	}

	@SuppressWarnings("StatementWithEmptyBody")
	@Override
	public boolean onNavigationItemSelected(MenuItem item) {
		Timber.i("Nav item selected: " + item.getTitle());

		// Handle navigation view item clicks here.
		switch (item.getItemId()) {
			default:
				Timber.w("Unknown nav item selected.");
				break;
		}

		DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		drawer.closeDrawer(GravityCompat.START);
		return true;
	}
}
