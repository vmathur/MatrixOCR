package com.SYDEprojects.matrixocr;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import java.lang.String;


public class MainActivity extends Activity {
	
	protected Button button;
	protected EditText elementA;
	protected EditText elementB;
	protected EditText elementC;
	protected EditText elementD;
	
	protected EditText lambda1;
	protected EditText lambda2;
	
	protected EditText determinent;
	
	int A;
	int B;
	int C;
	int D;

	protected String result;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		final Intent intent = new Intent(this, OCR.class);
		
		elementA = (EditText) findViewById(R.id.editTextA);
		elementB = (EditText) findViewById(R.id.editTextB);
		elementC = (EditText) findViewById(R.id.EditTextC);
		elementD = (EditText) findViewById(R.id.EditTextD);
		
		determinent = (EditText) findViewById(R.id.editTextDeterminent);
		lambda1 = (EditText) findViewById(R.id.editTextLambda1);
		lambda2 = (EditText) findViewById(R.id.editTextLambda2);
		
		button = (Button) findViewById(R.id.button);
		button.setOnClickListener(new OnClickListener() {
		    public void onClick(View v)
		    {
		        System.out.println("hello");
		        startActivityForResult(intent, 1);
		    } 
		});
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		  if (requestCode == 1) {

		     if(resultCode == RESULT_OK){      
		         result=data.getStringExtra("result");     
		         System.out.println("Data is "+result);
		         updateMatrix();
		         doCalculations();
		     }
		     if (resultCode == RESULT_CANCELED) {    
		    	 System.out.println("fail");
		     }
		  }
		}


	private void updateMatrix()
	{
		System.out.println("Inside the update Matrix function");
		
		char a = result.charAt(0);
		char b = result.charAt(1);
		char c = result.charAt(3);
		char d = result.charAt(4);
		
		A = (a-48);
		B = (b-48);
		C = (c-48);
		D = (d-48);
		
		System.out.println(A);
		System.out.println(B);
		System.out.println(C);
		System.out.println(D);

		
		elementA.setText(String.valueOf(a));
		elementB.setText(String.valueOf(b));
		elementC.setText(String.valueOf(c));
		elementD.setText(String.valueOf(d));
	}
	
	private void doCalculations() {
		updateDeterminent();
		updateEigenvalue();
	}
	
	private void updateDeterminent(){
		int det = ((A*D)-(B*C));
		determinent.setText(Integer.toString(det));
	}
	
	private void updateEigenvalue() {
		int a = 1;
		int b = -1*(A+D);
		int c = ((A*D)-(B*C));
		
		System.out.println("a is "+a);
		System.out.println("b is "+b);
		System.out.println("c is "+c);
		
		String l1 = null;
		String l2 = null;
		
		if ((b*b-4*a*c)>0)
		{
			l1 = Double.toString((-1.0*b+Math.sqrt(b*b-4.0*a*c))/2.0*a);
			l2 = Double.toString((-1.0*b-Math.sqrt(b*b-4.0*a*c))/2.0*a);
		}
		else if ((b*b-4.0*a*c)<0)
		{
			l1 = Double.toString(((-1.0*b)/(2*a)))+" "+Math.sqrt(4.0*a*c-b*b)+"i";
			l2 = Double.toString(((-1.0*b)/(2*a)))+" "+(-1.0*Math.sqrt(4.0*a*c-b*b))+"i";
		}	
		else
		{
			l1=Double.toString((-1.0*b)/(2.0*a));
			l2=Double.toString((-1.0*b)/(2.0*a));
		}
		
		lambda1.setText(l1);
		lambda2.setText(l2);
		
		
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
