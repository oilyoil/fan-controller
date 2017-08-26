/* シリアル通信ポーリングレート */
const int POLLING_RATE = 9600;

/* 通信バイト数 */
const int INPUT_BYTES = 4;

/* ファン番号該当バイト配列インデックス */
const int FAN1_SPEED = 0;
const int FAN2_SPEED = 1;
const int FAN3_SPEED = 2;
const int FAN4_SPEED = 3;

/* ファン接続ピン番号 */
const int FAN1 = 3;
const int FAN2 = 9;
const int FAN3 = 10;
const int FAN4 = 11;

//送信データブロック開始文字
const int WRITE_BLOCK_START = 2;

//受信データブロック開始文字
const int READ_BLOCK_START = 1;

//データブロックを示すフラグ
boolean blockFlag = false;

//入力データ数
int inputCount = 0;

//ファン速度
int fanSpeed[INPUT_BYTES] = { 80, 80, 80, 80 } ;

//通信間隔計測値
int connectionInterval = 0;

/*
 * FAN制御用通信はFAN1,FAN2,FAN3,FAN4の順に0,30-255数値合計4Byteの固定長通信
 */
void setup() {
  //シリアル初期化
  Serial.begin( POLLING_RATE );
  
  //タイマ1(9,10番ピン)分周比1(31.4KHz)
  TCCR1B=(TCCR1B & 0b11111000)| 0x01;

  //タイマ2(3,11番ピン)分周比1(31.4KHz)
  TCCR2B=(TCCR2B & 0b11111000)| 0x01;

  //ファン初期速度
  analogWrite( FAN1, fanSpeed[FAN1_SPEED] );
  analogWrite( FAN2, fanSpeed[FAN2_SPEED] );
  analogWrite( FAN3, fanSpeed[FAN3_SPEED] );
  analogWrite( FAN4, fanSpeed[FAN4_SPEED] );
}

void loop() {
  int inputData; 
  
  if(Serial.available() > 0){
    //PCからファンスピード受信
    inputData = Serial.read();

    if(inputData > -1){

      connectionInterval = 0;
      
      if( blockFlag && inputCount < INPUT_BYTES && (inputData == 0 || inputData >= 10) ){
        fanSpeed[inputCount] = inputData * 2;
        inputCount++;
        
        if( inputCount == INPUT_BYTES ){
          blockFlag = false;
        }
      }else if( !blockFlag && inputData == READ_BLOCK_START ){
        blockFlag = true;
        inputCount = 0;
      }else{
        blockFlag = false;
      }
    }
  }

  if( inputCount == INPUT_BYTES ){
    //4バイト正常に受信した場合ファンスピード変更
    analogWrite( FAN1, fanSpeed[FAN1_SPEED] );
    analogWrite( FAN2, fanSpeed[FAN2_SPEED] );
    analogWrite( FAN3, fanSpeed[FAN3_SPEED] );
    analogWrite( FAN4, fanSpeed[FAN4_SPEED] );
  }

  if( connectionInterval > 5 * 60){
    //5分PCからの通信が途切れた場合ファンを緩く回す
    fanSpeed[FAN1_SPEED] = 60;
    fanSpeed[FAN2_SPEED] = 60;
    fanSpeed[FAN3_SPEED] = 60;
    fanSpeed[FAN4_SPEED] = 60;
    
    analogWrite( FAN1, fanSpeed[FAN1_SPEED] );
    analogWrite( FAN2, fanSpeed[FAN2_SPEED] );
    analogWrite( FAN3, fanSpeed[FAN3_SPEED] );
    analogWrite( FAN4, fanSpeed[FAN4_SPEED] );
  }
  
  if( blockFlag ){
    delay(100);
  }else{
    Serial.write(WRITE_BLOCK_START);
    Serial.write( fanSpeed[FAN1_SPEED] / 2 );
    Serial.write( fanSpeed[FAN2_SPEED] / 2);
    Serial.write( fanSpeed[FAN3_SPEED] / 2);
    Serial.write( fanSpeed[FAN4_SPEED] / 2);
    delay(1000);
    if( connectionInterval < 30000 ){
      connectionInterval += 1;
    }
  }
}
