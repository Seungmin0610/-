package kr.ac.dankook.ecg_dating;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import kr.ac.dankook.ecg_dating.db.AppDatabase;
import kr.ac.dankook.ecg_dating.db.EcgData;
import kr.ac.dankook.ecg_dating.db.EcgDataDao;

public class ConversationActivity extends AppCompatActivity {

    private static final String TAG = "BPM_TEST";
    private static final UUID BT_MODULE_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // UI
    private Chronometer timerConversation;
    private View cardQuestion, cardBalance, cardIdealType;
    private Button btnEndDating;

    // 블루투스
    private BluetoothAdapter bluetoothAdapter;
    private ConnectedThread threadMale, threadFemale;

    // [데이터] 실시간 수신된 심박수
    private volatile double currentMaleBpm = 0;
    private volatile double currentFemaleBpm = 0;

    // 로직
    private Timer monitoringTimer;
    private int vibCountMale = 0;
    private int vibCountFemale = 0;

    // [수정됨] 최고점 갱신을 위한 변수 (Max Record)
    private double maxBpmMale = 0;
    private double maxBpmFemale = 0;
    private boolean isBaseSet = false;

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);

        initUI();

        if (savedInstanceState == null) {
            initSession();
            timerConversation.setBase(SystemClock.elapsedRealtime());
            timerConversation.start();
        }

        String maleMac = getIntent().getStringExtra("MALE_MAC");
        String femaleMac = getIntent().getStringExtra("FEMALE_MAC");

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        Log.d(TAG, ">>> MAC 주소: 남(" + maleMac + "), 여(" + femaleMac + ")");

        if (maleMac != null && femaleMac != null) {
            connectDevice(maleMac, true);
            connectDevice(femaleMac, false);
        } else {
            Toast.makeText(this, "기기 주소 오류 (테스트 모드)", Toast.LENGTH_SHORT).show();
        }

        startHeartRateMonitoring();
        setupButtonListeners();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (timerConversation != null) timerConversation.start();
    }

    private void initUI() {
        timerConversation = findViewById(R.id.timer_conversation);
        cardQuestion = findViewById(R.id.card_question);
        cardBalance = findViewById(R.id.card_balance);
        cardIdealType = findViewById(R.id.card_ideal_type);
        btnEndDating = findViewById(R.id.btn_end_dating);
    }

    private void initSession() {
        maxBpmMale = 0;
        maxBpmFemale = 0;
        isBaseSet = false;
        vibCountMale = 0;
        vibCountFemale = 0;
        new Thread(() -> AppDatabase.getDBInstance(this).ecgDataDao().deleteAll()).start();
    }

    // [핵심] 3초마다 체크 -> 최고점 갱신 시 진동 명령 전송
    private void startHeartRateMonitoring() {
        if (monitoringTimer != null) monitoringTimer.cancel();

        monitoringTimer = new Timer();
        AppDatabase db = AppDatabase.getDBInstance(this);

        monitoringTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                // 1. 데이터 가져오기
                double curMale = currentMaleBpm;
                double curFemale = currentFemaleBpm;

                long now = System.currentTimeMillis();

                // 2. DB 저장
                EcgData dataMale = new EcgData();
                dataMale.setUserId("user_male");
                dataMale.setHrv(curMale);
                dataMale.setMeasuredAt(now);
                db.ecgDataDao().insert(dataMale);

                EcgData dataFemale = new EcgData();
                dataFemale.setUserId("user_female");
                dataFemale.setHrv(curFemale);
                dataFemale.setMeasuredAt(now);
                db.ecgDataDao().insert(dataFemale);

                Log.d(TAG, "저장됨: 남(" + (int)curMale + ") 여(" + (int)curFemale + ")");

                // 3. [수정됨] 최고점 갱신 판단 로직
                if (!isBaseSet) {
                    // 첫 데이터는 기준값이자 현재의 최고값
                    maxBpmMale = curMale;
                    maxBpmFemale = curFemale;
                    isBaseSet = true;
                    Log.d(TAG, ">>> 초기값 설정 완료");
                } else {
                    // [남성] 신기록 갱신?
                    if (curMale > maxBpmMale) {
                        maxBpmMale = curMale; // 기록 갱신
                        vibCountMale++;

                        // 아두이노로 'V' 전송 (진동!)
                        if (threadMale != null) threadMale.write("V");
                        // 아두이노에서 받았을때 진동 울리게 코드 만들어줘야 되여

                    }

                    // [여성] 신기록 갱신?
                    if (curFemale > maxBpmFemale) {
                        maxBpmFemale = curFemale; // 기록 갱신
                        vibCountFemale++;

                        // 아두이노로 'V' 전송
                        if (threadFemale != null) threadFemale.write("V");
                        // 아두이노에서 받았을때 진동 울리게 코드 만들어줘야 되여
                    }
                }
            }
        }, 0, 3000);
    }

    private void setupButtonListeners() {
        cardQuestion.setOnClickListener(v -> startActivity(new Intent(this, QuestionListActivity.class)));
        cardBalance.setOnClickListener(v -> startActivity(new Intent(this, Balancegame.class)));
        cardIdealType.setOnClickListener(v -> startActivity(new Intent(this, ChoiceActivity.class)));

        btnEndDating.setOnClickListener(v -> {
            timerConversation.stop();
            if (monitoringTimer != null) monitoringTimer.cancel();
            showEyeContactPopup(timerConversation.getText().toString());
        });
    }

    private void showEyeContactPopup(final String duration) {
        LayoutInflater inflater = getLayoutInflater();
        View popupView = inflater.inflate(R.layout.popup_eye_contact, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(popupView);
        builder.setCancelable(false);

        final TextView tvCountdown = popupView.findViewById(R.id.tv_countdown);
        final AlertDialog dialog = builder.create();
        dialog.show();

        new CountDownTimer(10000, 1000) {
            public void onTick(long millisUntilFinished) {
                tvCountdown.setText(String.valueOf(millisUntilFinished / 1000 + 1));
            }
            public void onFinish() {
                dialog.dismiss();
                processResultAndFinish(duration);
            }
        }.start();
    }

    // 점수 계산
    // DB에서 측정된 모든 ECG 레코드를 가져와서 HRV (여기서는 RMSSD 기반) 변화량을 계산하여 finalScore를 산출합니다.
    private void processResultAndFinish(String duration) {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getDBInstance(this);
            EcgDataDao dao = db.ecgDataDao();

            // DB에서 사용자별로 데이터 가져오기
            List<EcgData> maleRecords = dao.getAllDataForUser("user_male");
            List<EcgData> femaleRecords = dao.getAllDataForUser("user_female");

            // 분리: 남성과 여성 BPM 리스트 추출 (null/비정상 값 필터링)
            List<Double> maleBpm = new java.util.ArrayList<>();
            List<Double> femaleBpm = new java.util.ArrayList<>();
            if (maleRecords != null) {
                for (EcgData r : maleRecords) {
                    if (r != null) {
                        double v = r.getHrv();
                        if (v > 0) maleBpm.add(v);
                    }
                }
            }
            if (femaleRecords != null) {
                for (EcgData r : femaleRecords) {
                    if (r != null) {
                        double v = r.getHrv();
                        if (v > 0) femaleBpm.add(v);
                    }
                }
            }

            // BPM 리스트 -> RR(ms) 리스트
            java.util.function.Function<List<Double>, List<Double>> toRR = (bpmList) -> {
                List<Double> rr = new java.util.ArrayList<>();
                if (bpmList == null) return rr;
                for (Double b : bpmList) {
                    if (b == null || b <= 0) continue;
                    rr.add(60000.0 / b);  // bpm -> ms
                }
                return rr;
            };

            // RMSSD 계산 (RR 리스트가 최소 2개 이상이어야 의미 있음)
            java.util.function.Function<List<Double>, Double> calcRMSSD = (rrList) -> {
                if (rrList == null || rrList.size() < 2) return 0.0;
                double sumSqDiff = 0.0;
                int count = 0;
                for (int i = 1; i < rrList.size(); i++) {
                    double diff = rrList.get(i) - rrList.get(i - 1);
                    sumSqDiff += diff * diff;
                    count++;
                }
                if (count == 0) return 0.0;
                double meanSq = sumSqDiff / count;
                return Math.sqrt(meanSq);
            };

            //  초기 기준 윈도우 RMSSD - 안전하게 범위 체크
            java.util.function.BiFunction<List<Double>, Integer, Double> calcBaselineRMSSD = (rrList, windowSize) -> {
                if (rrList == null || rrList.size() < 2) return 0.0;
                int size = Math.min(windowSize, rrList.size());
                // 처음 size개의 RR로 RMSSD 계산 (실제로는 sliding window 사용 가능)
                List<Double> sub = rrList.subList(0, size);
                return calcRMSSD.apply(sub);
            };

            // Pearson 상관계수 (두 리스트 길이 다르면 짧은 쪽에 맞춤)
            java.util.function.BiFunction<List<Double>, List<Double>, Double> pearsonCorr = (a, b) -> {
                if (a == null || b == null) return 0.0;
                int n = Math.min(a.size(), b.size());
                if (n < 2) return 0.0;
                double meanA = 0.0, meanB = 0.0;
                for (int i = 0; i < n; i++) { meanA += a.get(i); meanB += b.get(i); }
                meanA /= n; meanB /= n;
                double num = 0.0, denA = 0.0, denB = 0.0;
                for (int i = 0; i < n; i++) {
                    double d1 = a.get(i) - meanA;
                    double d2 = b.get(i) - meanB;
                    num += d1 * d2;
                    denA += d1 * d1;
                    denB += d2 * d2;
                }
                double denom = Math.sqrt(denA * denB);
                if (denom == 0) return 0.0;
                double corr = num / denom;
                if (Double.isNaN(corr)) return 0.0;
                return Math.max(0.0, corr);  // 음수는 0 처리
            };

            // 변환 및 계산
            List<Double> rrMale = toRR.apply(maleBpm);
            List<Double> rrFemale = toRR.apply(femaleBpm);

            // 초기 기준 윈도우 크기 (샘플 개수) — 센서 전송 주기 (예: 3초)와 연동해 조절 가능
            int baselineWindow = 10; // 예: 처음 10개 샘플을 기준으로 (샘플이 적으면 가능한 범위로 자동 축소)

            double rmssdMaleBaseline = calcBaselineRMSSD.apply(rrMale, baselineWindow);
            double rmssdMaleOverall = calcRMSSD.apply(rrMale);

            double rmssdFemaleBaseline = calcBaselineRMSSD.apply(rrFemale, baselineWindow);
            double rmssdFemaleOverall = calcRMSSD.apply(rrFemale);

            // 변화량 비율 (pct) 계산 - 절대값의 백분율, baseline이 0일 경우 처리
            double pctChangeMale = 0.0;
            if (rmssdMaleBaseline > 1e-6) {
                pctChangeMale = Math.abs((rmssdMaleOverall - rmssdMaleBaseline) / rmssdMaleBaseline); // 비율 (0.0 ~)
            }

            double pctChangeFemale = 0.0;
            if (rmssdFemaleBaseline > 1e-6) {
                pctChangeFemale = Math.abs((rmssdFemaleOverall - rmssdFemaleBaseline) / rmssdFemaleBaseline);
            }

            // 서로 간의 동기성 (상관계수) 계산
            double sync = pearsonCorr.apply(rrMale, rrFemale);

            // 점수화 방식: 변화량 (pctChange) 평균에 스케일링을 적용하고 동기성 보너스 추가
            // 변화량 평균에 100을 곱해 0..(큰값) 스케일로 만든 뒤 클램프
            // 동기성은 0..1 이므로 0..20 점 보너스
            // 여기서는 간단한 가중치 설정 (임의 조정 가능) - 직접 해보면서 맞춰야 될 거 같아요 이건
            double changeMean = (pctChangeMale + pctChangeFemale) / 2.0;
            double scoreFromChange = changeMean * 100.0;  // 예: 변화 비율 0.2 -> 20점
            double syncBonus = sync * 20.0;  // 동기성이 1.0이면 +20점
            double rawScore = scoreFromChange + syncBonus;

            // 정규화: 0..100 범위로 클램프
            double finalScore = Math.max(0.0, Math.min(100.0, rawScore));

            // Debug 로그
            Log.d(TAG, String.format("HRV(RMSSD) 남: baseline=%.2f overall=%.2f pct=%.3f | 여: baseline=%.2f overall=%.2f pct=%.3f",
                    rmssdMaleBaseline, rmssdMaleOverall, pctChangeMale, rmssdFemaleBaseline, rmssdFemaleOverall, pctChangeFemale));
            Log.d(TAG, "sync=" + sync + " scoreFromChange=" + scoreFromChange + " syncBonus=" + syncBonus + " final=" + finalScore);

            // 최종 점수 반영 - ResultActivity로 남/여 구성요소도 전달해 디버깅/리포트에 좋음
            Intent intent = new Intent(ConversationActivity.this, ResultActivity.class);
            intent.putExtra("DURATION", duration);
            intent.putExtra("HRV_SCORE", finalScore);
            intent.putExtra("VIBRATION_MALE", vibCountMale);
            intent.putExtra("VIBRATION_FEMALE", vibCountFemale);

            // 추가 정보 (디버깅/표시용)
            intent.putExtra("RMSSD_MALE_BASE", rmssdMaleBaseline);
            intent.putExtra("RMSSD_MALE_OVERALL", rmssdMaleOverall);
            intent.putExtra("RMSSD_FEMALE_BASE", rmssdFemaleBaseline);
            intent.putExtra("RMSSD_FEMALE_OVERALL", rmssdFemaleOverall);
            intent.putExtra("HRV_PCT_MALE", pctChangeMale);
            intent.putExtra("HRV_PCT_FEMALE", pctChangeFemale);
            intent.putExtra("HRV_SYNC", sync);

            startActivity(intent);
            finish();
        }).start();
    }

    @SuppressLint("MissingPermission")
    private void connectDevice(String address, boolean isMale) {
        new Thread(() -> {
            try {
                BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
                BluetoothSocket socket = device.createRfcommSocketToServiceRecord(BT_MODULE_UUID);
                socket.connect();

                ConnectedThread thread = new ConnectedThread(socket, isMale);
                thread.start();

                if (isMale) threadMale = thread;
                else threadFemale = thread;

                runOnUiThread(() -> Toast.makeText(this, (isMale ? "남성" : "여성") + " 연결 성공", Toast.LENGTH_SHORT).show());

            } catch (IOException e) {
                Log.e(TAG, "연결 실패", e);
                runOnUiThread(() -> Toast.makeText(this, "연결 실패", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    // 데이터 수신 및 송신 스레드
    private class ConnectedThread extends Thread {
        private final BluetoothSocket socket;
        private final OutputStream mmOutStream; // [추가] 송신용
        private final BufferedReader reader;
        private final boolean isMaleThread;

        public ConnectedThread(BluetoothSocket socket, boolean isMale) {
            this.socket = socket;
            this.isMaleThread = isMale;
            InputStream tmpIn;
            OutputStream tmpOut;

            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream(); // [추가]
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            InputStream mmInStream = tmpIn;
            mmOutStream = tmpOut;
            reader = new BufferedReader(new InputStreamReader(mmInStream));
        }

        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    String line = reader.readLine();
                    if (line != null) {
                        String digits = line.replaceAll("[^0-9.]", "");
                        if (!digits.isEmpty()) {
                            try {
                                double bpm = Double.parseDouble(digits);
                                if (isMaleThread) currentMaleBpm = bpm;
                                else currentFemaleBpm = bpm;
                                Log.d(TAG, (isMaleThread ? "남" : "여") + " 수신: " + bpm);
                            } catch (NumberFormatException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                } catch (IOException e) { break; }
            }
        }

        // [추가] 데이터 전송 메서드
        public void write(String message) {
            try {
                if (mmOutStream != null) {
                    mmOutStream.write(message.getBytes());
                }
            } catch (IOException e) {
                Log.e(TAG, "전송 실패", e);
            }
        }

        public void cancel() { try { socket.close(); } catch (IOException e) {
            throw new RuntimeException(e);
        }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (threadMale != null) threadMale.cancel();
        if (threadFemale != null) threadFemale.cancel();
        if (timerConversation != null) timerConversation.stop();
        if (monitoringTimer != null) monitoringTimer.cancel();
    }
}