package gr.teicm.informatics.selfdrivegps.Utilities;

import android.content.SharedPreferences;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import gr.teicm.informatics.selfdrivegps.Controller.Controller;
import gr.teicm.informatics.selfdrivegps.R;

public class DialogUtilities {
    private static Controller controller = new Controller();

    public static void chooseTerrainBasedOfRadioBtn(String mName){
        switch (mName) {
            case "Normal":
                controller.getGoogleMap().setMapType(1); //To make it normal
                break;
            case "Satellite":
                controller.getGoogleMap().setMapType(2); //To make it satellite
                break;
            case "Terrain":
                controller.getGoogleMap().setMapType(3); //To make it terrain
                break;
        }
    }

    public static void chooseWhichDialogWillAppear(int etFieldName, int tvSaveLine, View mView){
        EditText editTextToSaveNameOfField = mView.findViewById(R.id.et_pop_name_DB_ET);
        TextView textViewToSaveLine = mView.findViewById(R.id.tv_line_saving);

        // Visible = 0 || Invisible = 4
        editTextToSaveNameOfField.setVisibility(etFieldName);
        textViewToSaveLine.setVisibility(tvSaveLine);
    }

    //Click Listener for buttons of DialogCenterOfAntenna to make counter works
    public static void setListenerForClickBtn(final Button button, final TextView front, final TextView back, final TextView right, final TextView left){
        final int mId = button.getId();
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(mId == R.id.btn_antenna_front_plus || mId == R.id.btn_antenna_front_sub) {
                    composeFunctionOfAntennaCounter(mId, back, right, left, front);
                }else if(mId == R.id.btn_antenna_back_plus || mId == R.id.btn_antenna_back_sub) {
                    composeFunctionOfAntennaCounter(mId, front, right, left, back);
                }else if(mId == R.id.btn_antenna_left_plus || mId == R.id.btn_antenna_left_sub) {
                    composeFunctionOfAntennaCounter(mId, back, right, front, left);
                }else if(mId == R.id.btn_antenna_right_plus || mId == R.id.btn_antenna_right_sub){
                    composeFunctionOfAntennaCounter(mId, back, front, left, right);
                }
            }
        });
    }

    //Take 3 textView to set them 0 and the one which we focused
    private static void composeFunctionOfAntennaCounter(int mId, TextView aMeter, TextView bMeter, TextView cMeter, TextView focusedMeter){
        aMeter.setText("0");
        bMeter.setText("0");
        cMeter.setText("0");
        int counter = Integer.parseInt(focusedMeter.getText().toString());
        int resultOfCounter = counterMeterForAntenna(counter, mId);
        focusedMeter.setText(String.valueOf(resultOfCounter));
    }

    //Give an number and id of button and increase or decrease the number
    private static int counterMeterForAntenna(int mCounter, int idButton) {
        if((idButton == R.id.btn_antenna_front_plus || idButton == R.id.btn_antenna_back_plus ||
                idButton == R.id.btn_antenna_left_plus || idButton == R.id.btn_antenna_right_plus) && mCounter < 10){
            mCounter = mCounter + 1; //Increase it

        }else if((idButton == R.id.btn_antenna_front_sub || idButton == R.id.btn_antenna_back_sub ||
                idButton == R.id.btn_antenna_left_sub || idButton == R.id.btn_antenna_right_sub) && mCounter > 0){
            mCounter = mCounter - 1; //Decrease it
        }
        return mCounter;
    }

    //Set antenna sides from settings with SharePreferences
    public static void setUpAntennaNewPosition(Boolean chooseReset, TextView mFront, TextView mBack, TextView mLeft, TextView mRight){

        SharedPreferences.Editor editor = controller.getSharePreferences().edit();
        editor.putInt("front", chooseReset ? 0 : Integer.parseInt(mFront.getText().toString()));
        editor.putInt("back", chooseReset ? 0 : Integer.parseInt(mBack.getText().toString()));
        editor.putInt("left", chooseReset ? 0 :Integer.parseInt(mLeft.getText().toString()));
        editor.putInt("right", chooseReset ? 0 : Integer.parseInt(mRight.getText().toString()));
        editor.apply();

        controller.setAntennaFront(chooseReset ? 0 : Integer.parseInt(mFront.getText().toString()));
        controller.setAntennaBack(chooseReset ? 0 : Integer.parseInt(mBack.getText().toString()));
        controller.setAntennaLeft(chooseReset ? 0 :Integer.parseInt(mLeft.getText().toString()));
        controller.setAntennaRight(chooseReset ? 0 : Integer.parseInt(mRight.getText().toString()));
    }
}