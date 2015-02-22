package com.appspot.realpeachrails.IndianPoker2;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

public class Card extends ActionBarActivity implements SensorEventListener {

	private static final int MATRIX_SIZE = 16;

	private String urlString = null;
	private static int trumpNum = 0;
	private static SensorManager manager;
	private boolean mIsMagSensor = false;
	private boolean mIsAccSensor = false;
	private boolean mIsGravity = false;

	private boolean first = false;

	private long mPrevTime;

	private final static int[] imageList = new int[]{
		R.drawable.err, R.drawable.trump001, R.drawable.trump002, R.drawable.trump003, R.drawable.trump004,
		R.drawable.trump005, R.drawable.trump006, R.drawable.trump007, R.drawable.trump008, R.drawable.trump009,
		R.drawable.trump010, R.drawable.trump011, R.drawable.trump012, R.drawable.trump013, R.drawable.trump014,
		R.drawable.trump015, R.drawable.trump016, R.drawable.trump017, R.drawable.trump018, R.drawable.trump019,
		R.drawable.trump020, R.drawable.trump021, R.drawable.trump022, R.drawable.trump023, R.drawable.trump024,
		R.drawable.trump025, R.drawable.trump026, R.drawable.trump027, R.drawable.trump028, R.drawable.trump029,
		R.drawable.trump030, R.drawable.trump031, R.drawable.trump032, R.drawable.trump033, R.drawable.trump034,
		R.drawable.trump035, R.drawable.trump036, R.drawable.trump037, R.drawable.trump038, R.drawable.trump039,
		R.drawable.trump040, R.drawable.trump041, R.drawable.trump042, R.drawable.trump043, R.drawable.trump044,
		R.drawable.trump045, R.drawable.trump046, R.drawable.trump047, R.drawable.trump048, R.drawable.trump049,
		R.drawable.trump050, R.drawable.trump051, R.drawable.trump052
	};



	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_card);

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
			.add(R.id.container, new PlaceholderFragment()).commit();
		}


		// 戻るボタンを付ける
		android.app.ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);

		// バックライトを保持する（常時点灯）
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
		urlString = pref.getString("URL", "");

		trumpNum = 0;
		manager = null;

		// いつでもBougaに登録しているか？
		AsyncTask<Void, Void, Integer> task = new AsyncTask<Void, Void, Integer>() {


			@Override
			protected Integer doInBackground(Void... params) {

				// 登録
				HttpURLConnection con = null;

				// URLの作成
				URL url;
				try {
					url = new URL(urlString);

					// 接続用HttpURLConnectionオブジェクト作成
					con = (HttpURLConnection)url.openConnection();

					// パラメタ設定
					con.setDoInput(true);

					// リクエストメソッドの設定
					con.setRequestMethod("GET");

					// リダイレクトを自動で許可しない設定
					con.setInstanceFollowRedirects(false);

					// ヘッダーの設定(複数設定可能)
					con.setRequestProperty("Accept-Language", "jp");

					// 接続
					con.connect();

					InputStream is = con.getInputStream();

					// Outputを用意
					ByteArrayOutputStream out = new ByteArrayOutputStream();

					//InputStreamからOutputStreamに出力
					try {
						byte[] buf = new byte[1024];
						int len = 0;

						while ((len = is.read(buf)) > 0) {  //終わるまで書き込み
							out.write(buf, 0, len);
						}

						out.flush();
					} finally {
						out.close();//ストリームをクローズすることを忘れずに
						is.close();
					}

					// outをバイト配列に
					byte[] image = out.toByteArray();
					con.disconnect();

					int num = 0;
					for(byte check : image){
						if(check >= 0x30 && check <= 0x39){
							num = num * 10 + (check - 0x30);
						} else {
							break;
						}
					}

					return num;

				} catch (Throwable e) {
					// TODO 自動生成された catch ブロック
					return 0;
				}
			}

			@Override
			protected void onPostExecute(Integer result) {

				int intResult = result;
				if(intResult < 1 || intResult > 52){
					intResult = 0;
				}

				((PlaceholderFragment)getSupportFragmentManager().getFragments().get(0)).isCompleteNumber = true;
				trumpNum = intResult;

				return;
			}
		};

		task.execute();

	}

	@Override
	protected void onStop() {
		super.onStop();

		//センサーマネージャのリスナ登録破棄
		if ((mIsMagSensor || mIsAccSensor || mIsGravity) && manager != null) {
			manager.unregisterListener(this);
			mIsMagSensor = false;
			mIsAccSensor = false;
			mIsGravity = false;
		}

	}

	@Override
	protected void onResume() {
		super.onResume();
		// センサの取得
		List<Sensor> sensors = manager.getSensorList(Sensor.TYPE_ALL);

		// センサマネージャへリスナーを登録(implements SensorEventListenerにより、thisで登録する)
		for (Sensor sensor : sensors) {

			if( sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD){
				manager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI);
				mIsMagSensor = true;
			}

			if( sensor.getType() == Sensor.TYPE_ACCELEROMETER){
				manager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_FASTEST);
				mIsAccSensor = true;
			}

			//			if( sensor.getType() == Sensor.TYPE_GRAVITY){
			//				manager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI);
			//				mIsGravity = true;
			//			}
		}

	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		/* 回転行列 */
		float[]  inR = new float[MATRIX_SIZE];
		float[] outR = new float[MATRIX_SIZE];
		float[]    I = new float[MATRIX_SIZE];

		/* センサーの値 */
		float[] orientationValues   = new float[3];
		float[] magneticValues      = new float[3];
		float[] accelerometerValues = new float[3];

		boolean isCompleteGravity = false;
		boolean isCompleteNumber = false;
		boolean isAlreadySetImage = false;


		private static final String DEFAULT_URL = "http://realpeachrails.appspot.com/indianpoker2";
		public Button restartButton = null;
		private String url = null;

		public PlaceholderFragment() {
		}

		private void init(){
			orientationValues   = new float[3];
			magneticValues      = new float[3];
			accelerometerValues = new float[3];

			isCompleteGravity = false;
			isCompleteNumber = false;
			isAlreadySetImage = false;
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_card, container,
					false);

			// ボタン追加
			restartButton = (Button) rootView.findViewById(R.id.button3);
			restartButton.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					restartGame();
				}
			});

			manager = (SensorManager)getActivity().getSystemService(SENSOR_SERVICE);
			init();

			return rootView;
		}


		private void restartGame(){

		}

		public synchronized void setImage(){

			if(isAlreadySetImage){
				return;
			}

			if(isCompleteGravity && isCompleteNumber){
				ImageView imageView = (ImageView)getActivity().findViewById(R.id.imageView2);
				imageView.setImageResource(imageList[trumpNum]);
				isAlreadySetImage = true;

				Vibrator vibrator = (Vibrator) getActivity().getSystemService(VIBRATOR_SERVICE);
	            vibrator.vibrate(100);
			}
		}

	}


	@Override
	public void onSensorChanged(SensorEvent event) {
		// TODO 自動生成されたメソッド・スタブ
		if (event.accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE) return;

		long nowTime = System.currentTimeMillis();
		long diffTime = nowTime - mPrevTime;
//		if(diffTime < 400) return;

		PlaceholderFragment frag = (PlaceholderFragment)getSupportFragmentManager().getFragments().get(0);


		switch (event.sensor.getType()) {
		case Sensor.TYPE_MAGNETIC_FIELD:
			frag.magneticValues = event.values.clone();
			break;
		case Sensor.TYPE_ACCELEROMETER:
			frag.accelerometerValues = event.values.clone();
			break;
		}

		if (frag.magneticValues != null && frag.accelerometerValues != null) {

			SensorManager.getRotationMatrix(frag.inR, frag.I, frag.accelerometerValues, frag.magneticValues);

			//Activityの表示が縦固定の場合。横向きになる場合、修正が必要です
			SensorManager.remapCoordinateSystem(frag.inR, SensorManager.AXIS_X, SensorManager.AXIS_Z, frag.outR);
			SensorManager.getOrientation(frag.outR, frag.orientationValues);

			if(	(radianToDegree(frag.orientationValues[2]) < -150 || radianToDegree(frag.orientationValues[2]) > 150)
//					&& (radianToDegree(frag.orientationValues[1]) < -150 || radianToDegree(frag.orientationValues[1]) > 150)
					){
				frag.isCompleteGravity = true;
				manager.unregisterListener(this);
				manager = null;
				mIsGravity = false;

				frag.setImage();
			}


//			if(!first || diffTime > 2000){
//				first = true;
//				showToast(
//						String.valueOf( radianToDegree(frag.orientationValues[0]) ) + ", " + //Z軸方向,azmuth
//								String.valueOf( radianToDegree(frag.orientationValues[1]) ) + ", " + //X軸方向,pitch
//								String.valueOf( radianToDegree(frag.orientationValues[2]) ), this );       //Y軸方向,roll
//
//			}

		}



		mPrevTime = nowTime;

		/*
		if(event.sensor.getType() == Sensor.TYPE_GRAVITY) {
			float z = event.values[SensorManager.DATA_Z];

			if(z < 0){
				((PlaceholderFragment)getSupportFragmentManager().getFragments().get(0)).isCompleteGravity = true;
				manager.unregisterListener(this);
				mIsGravity = false;

				((PlaceholderFragment)getSupportFragmentManager().getFragments().get(0)).setImage();
			}


			String str = "重力センサー値:"
					+ "\nX軸:" + event.values[SensorManager.DATA_X]
					+ "\nY軸:" + event.values[SensorManager.DATA_Y]
					+ "\nZ軸:" + event.values[SensorManager.DATA_Z];
			showToast(str, this);

		}
		 */
	}

	int radianToDegree(float rad){
		return (int) Math.floor( Math.toDegrees(rad) ) ;
	}


	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		//NOP
	}

	public static void showToast(String text, Context context) {
		Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

	    switch(item.getItemId()) {
	        case android.R.id.home:
				Intent intent = new Intent(this, MainActivity.class);
				startActivity(intent);
	            finish();
	            return true;
	    }
	    return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if(keyCode == KeyEvent.KEYCODE_BACK) {
	        // 戻るボタンの処理
			Intent intent = new Intent(this, MainActivity.class);
			startActivity(intent);
            finish();
            return true;
	    } else {
	        return super.onKeyDown(keyCode, event);
	    }
	}



}
