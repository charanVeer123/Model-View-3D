package com.dmitrybrant.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;

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


                dismiss();
            }
        });
    }
}
