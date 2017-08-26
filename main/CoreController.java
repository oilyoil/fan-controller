package main;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.SortedMap;

import device.ArduinoClient;
import device.OpenHardwareMonitorClient;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.UnsupportedCommOperationException;
import xml.SettingReader;
import xml.Settings;

/**
 * ファンコントローラの中核となるクラスです。
 * PC情報取得とArduino制御を管理します
 * @author oilyoil
 * @since 2017/04/29
 * @version v0.1
 */
public class CoreController extends Thread{

	/** 設定ファイル */
	private static final String CONFIG_FILE_NAME = "FanControllerSettings.xml";

	/** Arduino通信レート */
	private static final int ARDUINO_RATE = 9600;

	/** ファンの数 */
	private static final int FAN_NUM = 4;

	/** データ送信間隔(秒) */
	private static final int SEND_INTERVAL_TIME = 10;

	/** 接続確認間隔(秒) */
	private static final int CONNECTION_CHECK_INTERVAL = 15;

	/** コントローラーインスタンス */
	private static CoreController instance = null;

	/** PCモニター情報取得クラス */
	private OpenHardwareMonitorClient monitor;

	/** Arduino通信クラス */
	private ArduinoClient arduino;

	/** 設定格納クラス */
	private Settings setting = null;

	/** 前データ送信からの時間 */
	private int intervalTime = 0;

	/**
	 * コンストラクタ.
	 * シングルトンです
	 * @throws InterruptedException
	 * @throws UnsupportedCommOperationException
	 * @throws NoSuchPortException
	 * @throws PortInUseException
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	private CoreController()
			throws PortInUseException, NoSuchPortException, UnsupportedCommOperationException, InterruptedException, FileNotFoundException, IOException{
		SettingReader reader = new SettingReader(CONFIG_FILE_NAME);
		this.setting = reader.getSetting(false);

		this.monitor = new OpenHardwareMonitorClient(
				setting.getPort(), setting.getCpuName(), setting.getGpuName(), setting.getCpuCore() );

		this.arduino = ArduinoClient.getInstance(setting.getComPort(), ARDUINO_RATE);
	}

	/**
	 * CoreControllerインスタンス取得メソッド
	 * @return CoreControllerのインスタンス
	 * @throws InterruptedException
	 * @throws UnsupportedCommOperationException
	 * @throws NoSuchPortException
	 * @throws PortInUseException
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public static CoreController getInstance()
			throws PortInUseException, NoSuchPortException, UnsupportedCommOperationException, InterruptedException, FileNotFoundException, IOException{

		System.out.println("CoreController is setting up...");

		if( instance == null ){
			instance = new CoreController();
		}

		System.out.println("CoreController ready.");

		return instance;
	}

	/**
	 * コントローラ本体実行メソッド。
	 * monitorIntervalに設定された間隔で
	 * PCを監視しArduinoを制御します。
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run(){
		HashMap<String,HashMap<String,String>> temperature;
		int[] sendData;
		boolean isChange = false;
		int connectionCheckInterval = 0;

		System.out.println("CoreController is started.");

		while(true){
			try {
				Thread.sleep( setting.getMonitorInterval() * 1000 );

				connectionCheckInterval = connectionCheckInterval + 1;

				if( intervalTime < Integer.MAX_VALUE - 1 ){
					intervalTime = intervalTime + 1;
				} else {
					intervalTime = SEND_INTERVAL_TIME;
				}

				temperature = monitor.getTemperature();

				System.out.print("CurrentSpeed:");
				for(int val: arduino.getFanSettings()){
					System.out.print(val + " ");
				}
				System.out.println();

				sendData = arduino.getFanSettings();

				//ファン設定
				if( temperature.containsKey("CPU_Temp")
						&& temperature.get("CPU_Temp").containsKey("Package")
						&& temperature.containsKey("GPU_Temp")
						&& temperature.get("GPU_Temp").containsKey("0")){
					try{
					for(int i = 0 ; i < FAN_NUM ; i++){
						SortedMap<Integer,Integer> fanSettingMap = setting.getFanSpeedSettings().get(i);
						Integer targetSpeedCPUKey;
						Integer targetSpeedGPUKey;

						if( fanSettingMap
							.headMap( (int) Math.ceil( Double.parseDouble(temperature.get("CPU_Temp").get("Package")) ) )
							.size() > 0
								&& fanSettingMap
								.headMap( (int) Math.ceil( Double.parseDouble(temperature.get("GPU_Temp").get("0")) ) )
								.size() > 0){

						targetSpeedCPUKey = fanSettingMap
								.headMap( (int) Math.ceil( Double.parseDouble(temperature.get("CPU_Temp").get("Package")) ) )
								.lastKey();
						targetSpeedGPUKey = fanSettingMap
								.headMap( (int) Math.ceil( Double.parseDouble(temperature.get("GPU_Temp").get("0")) ) )
								.lastKey();

						}else{

							targetSpeedCPUKey = fanSettingMap.firstKey();
							targetSpeedGPUKey = fanSettingMap.firstKey();
						}

						Integer targetSpeedKey = Math.max(targetSpeedCPUKey.intValue(), targetSpeedGPUKey.intValue());

						if(arduino.getFanSettings()[i] != fanSettingMap.get(targetSpeedKey)){
							sendData[i] = fanSettingMap.get(targetSpeedKey);
							isChange = true;
						}
					}
					}catch(Exception e){
						int i = 0;
						for(SortedMap<Integer, Integer> map: setting.getFanSpeedSettings()){
							for(Entry<Integer, Integer> entry: map.entrySet()){
								System.out.println("fan" + i + "-setting-key:" + entry.getKey() + ",value:" + entry.getValue());
							}
							i++;
						}
						System.out.println("CPU:" + temperature.get("CPU_Temp").get("Package") + ",GPU:" + temperature.get("GPU_Temp").get("0"));
					}
				}

				if(isChange){
					isChange = false;
					setData(sendData);
				}

				if( connectionCheckInterval > CONNECTION_CHECK_INTERVAL ){
					arduino.sendConnection();
					connectionCheckInterval = 0;
				}

			} catch (InterruptedException | IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void setData(int[] data){
		if( intervalTime >= SEND_INTERVAL_TIME ){
			intervalTime = 0;
			try {
				System.out.println("send:" + data[0] + "," + data[1] + "," + data[2] + "," + data[3]);
				arduino.setFanSpeed(data);
			} catch (IOException e) {
				// TODO 自動生成された catch ブロック
				e.printStackTrace();
			}
		}
	}
}
