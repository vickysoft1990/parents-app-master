package in.codeomega.parents;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnErrorListener;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;

/**
 * Created by HP on 01-Aug-17.
 */

public class PdfReader extends AppCompatActivity  implements OnPageChangeListener, OnLoadCompleteListener, OnErrorListener {

    PDFView pdfView;
    Integer pageNumber = 0;
    TextView page_number,msg,error;
    int n=0;
    Button go;
    EditText page_input;
    Dialog goToDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pdftest);

        goToDialog= new Dialog(this);
        goToDialog.setTitle("Jump To Page");
        goToDialog.setContentView(R.layout.gotopage);

        pdfView=(PDFView)findViewById(R.id.pdfView);

        page_number=(TextView)findViewById(R.id.page_number);

        go=(Button) goToDialog.findViewById(R.id.go);
        msg=(TextView)goToDialog. findViewById(R.id.msg);
        error=(TextView)goToDialog. findViewById(R.id.error);
        page_input=(EditText)goToDialog. findViewById(R.id.page_input);

        pdfView.fromAsset("sample.pdf")
                .enableDoubletap(true)
                .enableAntialiasing(true)
                .onPageChange(this)
                .spacing(10)
                .onError(this)
                .onLoad(this)
                .load();

        page_number.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                error.setVisibility(View.GONE);
                page_input.setText("");
                goToDialog.show();

            }
        });

        go.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                try
                {
                    int go_to_page =  Integer.parseInt(page_input.getText().toString());
                    go_to_page--;
                    Log.e("go_to_page",go_to_page+" n "+n);

                    if(go_to_page<=n)
                    {
                        error.setVisibility(View.GONE);
                        goToDialog.hide();
                        Log.e("flag","A");

                        pdfView.fromAsset("sample.pdf")
                                .defaultPage(go_to_page)
                                .enableDoubletap(true)
                                .enableAntialiasing(true)
                                .onPageChange(PdfReader.this)
                                .onLoad(PdfReader.this)
                                .spacing(10)
                                .onError(PdfReader.this)
                                .load();
                    }else
                    {
                        error.setVisibility(View.VISIBLE);
                        Log.e("flag","B");
                        Toast.makeText(PdfReader.this,"Invalid Page Number",Toast.LENGTH_LONG);
                    }

                }catch (Exception e)
                {
                    error.setVisibility(View.VISIBLE);
                    Log.e("flag","C");

                    Toast.makeText(PdfReader.this,"Invalid Page Number",Toast.LENGTH_LONG);
                    Log.e("Exception",e.toString()+"");
                }

            }
        });

    }


    @Override
    public void onPageChanged(int page, int pageCount) {
        pageNumber = page;

        Log.e("page",page + 1+" "+pageCount);

        page_number.setText((page+1)+"/"+pageCount);
    }

    @Override
    public void loadComplete(int nbPages) {

        Log.e("Load Completed",nbPages+"");
        n = nbPages;
        msg.setText("Enter a page number between 1 - "+n);

    }

    @Override
    public void onError(Throwable t) {

        final AlertDialog.Builder builder = new AlertDialog.Builder(PdfReader.this);
        builder.setMessage("Unable to open this document").setCancelable(false).setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {

                dialog.dismiss();
                finish();
            }
        });
        final AlertDialog alert = builder.create();
        alert.setTitle("Message");
        alert.show();
        alert.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.primaryColor));

    }

}
