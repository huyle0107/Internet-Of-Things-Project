package bku.project_iot;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.github.angads25.toggle.interfaces.OnToggledListener;
import com.github.angads25.toggle.model.ToggleableView;
import com.github.angads25.toggle.widget.LabeledSwitch;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;

import java.nio.charset.Charset;

public class MainActivity extends AppCompatActivity
{
    MQTTHelper mqttHelper;
    TextView txtTemp, txtHumi, txtAI;
    LabeledSwitch btnLIGHT, btnPUMP;

    int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();   //This line hides the action bar

        setContentView(R.layout.activity_main);

        txtTemp = findViewById(R.id.txtTemperature);
        txtHumi = findViewById(R.id.txtHumidity);
        txtAI = findViewById(R.id.txtAI);
        btnLIGHT = findViewById(R.id.btnLIGHT);
        btnPUMP = findViewById(R.id.btnBUMP);

        btnPUMP.setOnToggledListener(new OnToggledListener()
        {
            @Override
            public void onSwitched(ToggleableView toggleableView, boolean isOn)
            {
                if (isOn == true)
                {
                    sendDataMQTT("HCMUT_IOT/feeds/pump-button","1");
                }
                else
                {
                    sendDataMQTT("HCMUT_IOT/feeds/pump-button","0");
                }
            }
        });

        btnLIGHT.setOnToggledListener(new OnToggledListener()
        {
            @Override
            public void onSwitched(ToggleableView toggleableView, boolean isOn)
            {
                if (isOn == true)
                {
                    sendDataMQTT("HCMUT_IOT/feeds/light-button","1");
                }
                else
                {
                    sendDataMQTT("HCMUT_IOT/feeds/light-button","0");
                }
            }
        });
        startMQTT();

        Button btnChart = findViewById(R.id.btnCHART);

        btnChart.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent intent = new Intent(MainActivity.this, SecondActivity.class);
                startActivity(intent);
            }
        });
    }

    public void sendDataMQTT(String topic, String value)
    {
        MqttMessage msg = new MqttMessage();
        msg.setId(1234);
        msg.setQos(0);
        msg.setRetained(false);

        byte[] b = value.getBytes(Charset.forName("UTF-8"));
        msg.setPayload(b);

        try
        {
            mqttHelper.mqttAndroidClient.publish(topic, msg);
        }
        catch (MqttException e){
        }
    }

    public void startMQTT()
    {
        mqttHelper = new MQTTHelper(this);
        mqttHelper.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {

            }

            @Override
            public void connectionLost(Throwable cause) {

            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                Log.d("TEST", topic + "***" + message.toString());
                if (topic.contains("temp-sensor"))
                {
                    txtTemp.setText(message.toString() + "°C");
                }
                else if (topic.contains("humid-sensor"))
                {
                    txtHumi.setText(message.toString() + "%");
                }
                else if (topic.contains("ai"))
                {
                    txtAI.setText(message.toString());
                }
                else if (topic.contains("light-button"))
                {
                    if (message.toString().equals("1"))
                    {
                        btnLIGHT.setOn(true);
                    }
                    else
                    {
                        btnLIGHT.setOn(false);
                    }
                }
                else if (topic.contains("pump-button"))
                {
                    if (message.toString().equals("1"))
                    {
                        btnPUMP.setOn(true);
                    }
                    else
                    {
                        btnPUMP.setOn(false);
                    }
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });
    }
}