package device;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import net.arnx.jsonic.JSON;

/**
 * OpenHardwareMonitorのHTTPサーバからJSONデータを取得
 * 解析しCPU,GPU情報を読みだすクラスです。
 * @author oilyoil
 * @since 2017/04/29
 * @version v0.1
 */
public class OpenHardwareMonitorClient{

	/** OpenHardwareMonitorのサーバデータ取得URL */
	private final String OHM_GET_DATA_URL;

	/** CPU名 */
	private final String CPU_NAME;

	/** GPU名 */
	private final String GPU_NAME;

	/** CPU物理コア数 */
	private final int CPU_CORE;

	/** JSON探索用キュー */
	private static LinkedList<ArrayList<HashMap<String, Object>>> queue =
															new LinkedList<ArrayList<HashMap<String, Object>>>();

	/** OpenHardwareMonitorデータ取得用クラス
	 * @param port OpenHardwareMonitorのHTTPサーバポート番号
	 * @param cpuName OpenHardwareMonitorに表示されているCPU名(部分一致可)
	 * @param gpuName OpenHardwareMonitorに表示されているGPU名(部分一致可)
	 * @param cpuCore CPU物理コア数
	 * */
	public OpenHardwareMonitorClient(String port, String cpuName, String gpuName ,int cpuCore){
		if( port != null ){
			OHM_GET_DATA_URL = "http://localhost:" + port + "/data.json";
		} else {
			OHM_GET_DATA_URL = "http://localhost:8085/data.json";
		}

		if( cpuName != null ){
			CPU_NAME = cpuName;
		} else {
			CPU_NAME = "3770K";
		}

		if( gpuName != null ){
			GPU_NAME = gpuName;
		} else {
			GPU_NAME = "GTX 1080";
		}

		if( cpuCore > 0 ){
			CPU_CORE = cpuCore;
		} else {
			CPU_CORE = 4;
		}
	}

	/**
	 * CPU,GPU温度情報を取得します。
	 * @return <デバイス名,<モジュール名,温度>>, エラー時:null
	 */
	public HashMap<String, HashMap<String, String>> getTemperature(){
		try{
			/** アクセスURL */
			URL url = new URL(OHM_GET_DATA_URL);

			/** HTTPコネクション */
			HttpURLConnection connection = null;

			/** レスポンスデータ */
			String responseData;

			/** レスポンスデータをJSONにパースしたもの */
			HashMap<String, Object> responseJSON = null;

				try{
					connection = (HttpURLConnection) url.openConnection();
					connection.setRequestMethod("GET");

					//正常のレスポンスコードならばJSONパース
					if( connection.getResponseCode() == HttpURLConnection.HTTP_OK ){
						try( BufferedReader br = new BufferedReader(
								new InputStreamReader( connection.getInputStream(),
										StandardCharsets.UTF_8 ) ) ){

							while( ( responseData = br.readLine() ) == null ){}

							if( responseData != null ){
								responseJSON = JSON.decode( responseData );
							}
						}
					}

					if( responseJSON != null ){
						return(extractData(responseJSON));
					}
				} catch (IOException e) {
					e.printStackTrace();
				}finally{
					if( connection != null ){
						connection.disconnect();
					}
				}
		}catch(MalformedURLException e){
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * CPU,GPU温度をJSONから解析し取得します
	 * @param rowData 解析対象JSON
	 * @return <デバイス名,<モジュール名,温度>>
	 */
	@SuppressWarnings("unchecked")
	private HashMap<String, HashMap<String, String>> extractData( HashMap<String, Object> rowData ){
		queue.clear();
		HashMap<String, HashMap<String, String>> extractData = new HashMap<String, HashMap<String, String>>();
		HashMap<String ,String> detailData = new HashMap<String, String>();
		ArrayList<HashMap<String, Object>> tmpHardwareData;


		//CPU情報代入
		tmpHardwareData = getJSONObject((ArrayList<HashMap<String, Object>>) rowData.get("Children"), CPU_NAME);
		for( int i = 0 ; i < CPU_CORE ; i++ ){
			detailData.put(String.valueOf(i), ((String) tmpHardwareData.get(i).get("Value")).split(" ")[0]);
		}

		detailData.put("Package", ((String) tmpHardwareData.get(CPU_CORE).get("Value")).split(" ")[0]);
		extractData.put("CPU_Temp", detailData);


		detailData = new HashMap<String, String>();

		//GPU情報代入
		tmpHardwareData = getJSONObject((ArrayList<HashMap<String, Object>>) rowData.get("Children"), GPU_NAME);
		detailData.put("0", ((String) tmpHardwareData.get(0).get("Value")).split(" ")[0]);

		extractData.put("GPU_Temp", detailData);

		return extractData;
	}

	/**
	 * JSONから指定したデバイス名のモジュールの温度を取得します。
	 */
	@SuppressWarnings("unchecked")
	private ArrayList<HashMap<String, Object>> getJSONObject(
							ArrayList<HashMap<String, Object>> targetData, String deviceName ){
		//ひたすらに再帰でJSONのパース

		if(targetData != null){
			queue.add(targetData);
		}

		for(int i = 0 ; i < queue.size(); i++){
			for(HashMap<String, Object> node : queue.peek()){
				if( ((String) node.get("Text")).contains(deviceName) ){
					for(HashMap<String, Object> detailData : (ArrayList<HashMap<String, Object>>)node.get("Children")){

						if( detailData.get("Text").equals("Temperatures") ){
							return (ArrayList<HashMap<String, Object>>) detailData.get("Children");
						}

					}
				}
			}
		}

		int size = queue.size();

		for(int i = 0 ; i < size ; i++){
			for(HashMap<String, Object> node : queue.poll()){
				if( node.containsKey("Children") ){
					queue.add((ArrayList<HashMap<String, Object>>) node.get("Children"));
				}
			}
		}

		if( queue.size() != 0 ){
			return getJSONObject(null , deviceName);
		}

		return null;

	}

}
