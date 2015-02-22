package com.appspot.realpeachrails.IndianPoker2;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}

	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		private static final String DEFAULT_URL = "http://realpeachrails.appspot.com/indianpoker2";
		public Button startButton = null;
		public Button configButton = null;
		public EditText edtInput = null;
		private String url = null;

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);

			SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
			url = pref.getString("URL", "");
			if(url.length() == 0){
				url = DEFAULT_URL;
		        Editor editor = pref.edit();
		        editor.putString("URL", url);
		        editor.commit();
			}

			// ボタン追加
			startButton = (Button) rootView.findViewById(R.id.button1);
			startButton.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					startGame();
				}
			});

			configButton = (Button) rootView.findViewById(R.id.button2);
			configButton.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v) {
					configGame();
				}
			});

			return rootView;
		}

		private void startGame(){
			Intent intent = new Intent(getActivity(), Card.class);
			startActivity(intent);
			getActivity().finish();
		}

		private void configGame(){

			// Create EditText
			edtInput = new EditText(getActivity());
			edtInput.setText(url);

			// Show Dialog
			new AlertDialog.Builder(getActivity())
			.setIcon(R.drawable.ic_launcher)
			.setTitle("カード取得先URLを指定してください。空白を指定すると初期状態に戻ります。")
			.setView(edtInput)
			.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					/* OKボタンをクリックした時の処理 */
					url = edtInput.getText().toString();

					SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
					if(url.length() == 0){
						url = DEFAULT_URL;
					}
			        Editor editor = pref.edit();
			        editor.putString("URL", url);
			        editor.commit();

					showToast(url + "\nを設定しました。", getActivity());
				}

			})
			.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					/* Cancel ボタンをクリックした時の処理 */

					showToast(url + "\nのまま変更しませんでした。", getActivity());
				}
			})
			.show();

		}

		public static void showToast(String text, Context context) {
			Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
		}

	}

}
