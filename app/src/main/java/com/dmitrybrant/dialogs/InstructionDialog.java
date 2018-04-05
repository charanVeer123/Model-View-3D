package com.dmitrybrant.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import com.dmitrybrant.activities.ImagesGridActivity_3;
import com.dmitrybrant.modelviewer.R;

public class InstructionDialog extends Dialog{


    public InstructionDialog(@NonNull Context context) {
        super(context);
    }

    CheckBox checkboxAccept;
    Button btnAccept;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCanceledOnTouchOutside(false);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setContentView(R.layout.instruction_dialog);



        btnAccept = findViewById(R.id.btnAccept);
        checkboxAccept = findViewById(R.id.checkboxAccept);

        btnAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(checkboxAccept.isChecked()) {

                    getContext().startActivity(new Intent(getContext(), ImagesGridActivity_3.class));
                    dismiss();

                }
                else {
                    Toast.makeText(getContext(), "Accept criteria", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
