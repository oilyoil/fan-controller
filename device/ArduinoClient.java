package device;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;

import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

/**
 * Arduinoとの通信を制御するクラスです。
 * @author oilyoil
 * @since 2017/05/04
 * @version v0.1
 */

public class ArduinoClient extends Thread{

	/** ポートハンドラ名 */
	private static final String APPLICATION_NAME = "ArduinoClient";

	/** ポートタイムアウト(ms) */
	private static final int PORT_TIMEOUT = 10000;

	/** 送信ブロック開始文字 */
	private static final int WRITE_BLOCK_START = 1;

	/** 受信ブロック開始文字 */
	private static final int READ_BLOCK_START = 2;

	/** 接続チェック文字 */
	private static final int CONNECTION_CHECK = 3;

	/** インスタンス */
	private static ArduinoClient instance = null;

	/** シリアルポート */
	private SerialPort port = null;

	/** Arduinoファン設定データ */
	private int[] fanSettings = new int[4];

	/** スレッド制御フラグ */
	private boolean alive = true;


	/** インスタンス生成時にArduinoとの通信を確立します。
	 * @Param comPort シリアルポート名(ex. COM3)
	 * @Param pollingRate 通信レート(300, 1200, 2400, 4800, 9600, 14400, 19200, 28800, 38400, 57600, 115200のいずれか)
	 * */
	private ArduinoClient(String comPort, int pollingRate)
			throws PortInUseException, NoSuchPortException, UnsupportedCommOperationException{
		//ポートのオープン
		port = (SerialPort) CommPortIdentifier.getPortIdentifier(comPort)
				.open(APPLICATION_NAME, PORT_TIMEOUT);

		/** ポートの設定
		 * Arduinoシリアル通信に準拠した以下の設定
		 * 通信レート:300, 1200, 2400, 4800, 9600, 14400, 19200, 28800, 38400, 57600, 115200
		 * データビット:8ビット
		 * ストップビット:1
		 * パリティチェック:なし
		 * */
		port.setSerialPortParams(pollingRate, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
	}

	/** インスタンスを取得します。既に存在するならばそれを返します。
	 * インスタンス生成時にArduinoとの通信を確立します
	 * @Param comPort シリアルポート名(ex. COM3)
	 * @Param pollingRate 通信レート(300, 1200, 2400, 4800, 9600, 14400, 19200, 28800, 38400, 57600, 115200のいずれか)
	 * @throws PortInUseException　既にこのポートは使用中です
	 * @throws NoSuchPortException 指定されたポート名は存在しません
	 * @throws UnsupportedCommOperationException ポート設定エラーです
	 * @throws InterruptedException
	 * @return ArduinoClient - インスタンス。
	 * */
	public static ArduinoClient getInstance(String comPort, int pollingRate)
			throws PortInUseException, NoSuchPortException, UnsupportedCommOperationException, InterruptedException{

		System.out.println("ArduinoClient is setting up...");

		if( instance == null ){
			instance = new ArduinoClient( comPort, pollingRate );
		}

		Thread.sleep(1000);

		System.out.println("ArduinoClient ready.");

		instance.start();

		return instance;
	}

	/** インスタンスを再取得します.
	 * インスタンス生成時にArduinoとの通信を確立します
	 * @Param comPort シリアルポート名(ex. COM3)
	 * @Param pollingRate 通信レート(300, 1200, 2400, 4800, 9600, 14400, 19200, 28800, 38400, 57600, 115200のいずれか)
	 * @throws PortInUseException　既にこのポートは使用中です
	 * @throws NoSuchPortException 指定されたポート名は存在しません
	 * @throws UnsupportedCommOperationException ポート設定エラーです
	 * @throws InterruptedException
	 * @return ArduinoClient - インスタンス。エラー時はnull
	 * */
	public static ArduinoClient reOpen(String comPort, int pollingRate)
			throws PortInUseException, NoSuchPortException, UnsupportedCommOperationException, InterruptedException{
		final int TIMEOUT = 100000;
		if(instance != null){

			instance.stopClient();

			System.out.println("Waiting for closing client...");
			int waitTime = 0;
			while( instance.getState() != Thread.State.TERMINATED ){
				if( waitTime > TIMEOUT ){
					return null;
				}
				Thread.sleep(100);
				waitTime = waitTime + 100;
			}

			System.out.println("ArduinoClient is reopen...");

			instance = new ArduinoClient( comPort, pollingRate );

			Thread.sleep(1000);

			System.out.println("ArduinoClient ready.");
		}

		instance.start();

		return instance;
	}

	/** Arduinoからの受信データをポーリングし格納します。*/
	@Override
	public void run(){
		/** 受信データバッファ */
		char[] data = new char[100];

		/** 受信サイズ */
		int readStatus = 0;

		/** 受信済みデータブロックサイズ */
		int dataCount = 0;

		/** データブロック開始フラグ */
		boolean blockFlag = false;

		int i = 0;

		System.out.println("ArduinoClient is started.");

		try( InputStreamReader isr = new InputStreamReader( port.getInputStream() ) ){
			while(alive){
				if( isr.ready() ){
					readStatus = isr.read(data);
				}

				if( readStatus > 0 ){
					//データを受信した場合
					if( data[0] == READ_BLOCK_START){
						//データブロック開始文字
						blockFlag = true;
						dataCount = 0;

						if( readStatus > 1 ){
							//既にデータブロック中にデータが存在する
							for(i = 0 ; i < readStatus - 1 && dataCount < 4; i++){
								if( data[i + 1] != READ_BLOCK_START ){
									fanSettings[dataCount] = data[i + 1];

									dataCount++;
								}
							}
						}
					}else if( blockFlag ){
						//データブロックのデータの場合
						for(i = dataCount ; i < readStatus && dataCount < 4 ; i++){
							fanSettings[i] = data[i];
							dataCount++;
						}

						if(dataCount >= 4){
							blockFlag = false;
						}
					}
				}
				Thread.sleep(500);
			}
		} catch (IOException | InterruptedException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
	}

	/** 引数の数値をすべてArduinoへ送信します。その際にASCIIコードへは変換しません。
	 * @param data 送信する数値配列(0-127)
	 * @throws IOException 送信時例外
	 * */
	public void setFanSpeed(int[] data) throws IOException{
		try ( OutputStream out = port.getOutputStream() ){
			out.write( WRITE_BLOCK_START );
			for(int var: data){
				out.write( var );
			}
			out.flush();
			out.close();
		}
	}

	/**
	 * Arduino接続維持用データ送信
	 * @throws IOException
	 */
	public void sendConnection() throws IOException{
		try ( OutputStream out = port.getOutputStream() ){
			out.write( CONNECTION_CHECK );
			out.flush();
			out.close();
		}
	}

	/** Arduinoからのファン設定データをint配列として読み出します。
	 * @return 受信データ
	 * */
	public int[] getFanSettings(){
		return this.fanSettings;
	}

	private void stopClient(){
		alive = false;
	}
}
