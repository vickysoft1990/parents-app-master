package in.codeomega.parents.fragments;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnErrorListener;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import java.io.File;
import java.io.InputStream;

import in.codeomega.parents.AppController;
import in.codeomega.parents.R;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link Fees.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link Fees#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Fees extends Fragment implements OnPageChangeListener, OnLoadCompleteListener, OnErrorListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;
    private TextView pagenumber;

    public Fees() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Fees.
     */
    // TODO: Rename and change types and number of parameters
    public static Fees newInstance(String param1, String param2) {
        Fees fragment = new Fees();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    PDFView pdfView;
    Integer pageNumber = 0;
    TextView page_number, msg, error;
    int n = 0;
    Button go;
    EditText page_input;
    Dialog goToDialog;

    InputStream input = null;

    ProgressDialog progressDialog = null;
    ProgressDialog pDialog;
    File currentFile;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_fees, container, false);
        this.pagenumber = (TextView) view.findViewById(R.id.page_number);

        progressDialog = AppController.getInstance().createProgressDialog(progressDialog, getActivity());
        progressDialog.setCancelable(true);

        pDialog = new ProgressDialog(getActivity());
        pDialog.setTitle("Message");
        pDialog.setMessage("Loading");
        pDialog.setIndeterminate(false);
        pDialog.setMax(100);
        pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pDialog.setCancelable(true);

        goToDialog = new Dialog(getActivity());
        goToDialog.setTitle("Jump To Page");
        goToDialog.setContentView(R.layout.gotopage);

        pdfView = (PDFView) view.findViewById(R.id.pdfView);

        page_number = (TextView) view.findViewById(R.id.page_number);

        go = (Button) goToDialog.findViewById(R.id.go);
        msg = (TextView) goToDialog.findViewById(R.id.msg);
        error = (TextView) goToDialog.findViewById(R.id.error);
        page_input = (EditText) goToDialog.findViewById(R.id.page_input);

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

                try {
                    int go_to_page = Integer.parseInt(page_input.getText().toString());
                    go_to_page--;
                    Log.e("go_to_page", go_to_page + " n " + n);

                    if (go_to_page <= n) {
                        error.setVisibility(View.GONE);
                        goToDialog.hide();
                        Log.e("flag", "A");

                        pdfView.fromFile(currentFile)
                                .defaultPage(go_to_page)
                                .enableDoubletap(true)
                                .enableAntialiasing(true)
                                .onPageChange(Fees.this)
                                .onLoad(Fees.this)
                                .spacing(10)
                                .onError(Fees.this)
                                .load();
                    } else {
                        error.setVisibility(View.VISIBLE);
                        Log.e("flag", "B");
                        Toast.makeText(getActivity(), "Invalid Page Number", Toast.LENGTH_LONG);
                    }

                } catch (Exception e) {
                    error.setVisibility(View.VISIBLE);
                    Log.e("flag", "C");

                    Toast.makeText(getActivity(), "Invalid Page Number", Toast.LENGTH_LONG);
                    Log.e("Exception", e.toString() + "");
                }

            }
        });

        File file = new File(Environment.getExternalStorageDirectory() + "/School/");

        if (!(file.exists())) {
            file.mkdirs();
        }

        currentFile = new File(Environment.getExternalStorageDirectory() + "/School/fees.pdf");
        currentFile.delete();

        Ion.with(getActivity())
                .load("https://www.comega.in/schools/sdsbkd/billing/fees.php?refid=" + AppController.getInstance().preferences.getString("user_id", "") + "&academic=" + AppController.getInstance().preferences.getString("academic_year", "") + "&fees")
                .write(currentFile)
                .setCallback(new FutureCallback<File>() {

                    @Override
                    public void onCompleted(Exception e, File file) {

                        progressDialog.dismiss();

                        pdfView.fromFile(currentFile)
                                .enableDoubletap(true)
                                .enableAntialiasing(true)
                                .onPageChange(Fees.this)
                                .spacing(10)
                                .onError(Fees.this)
                                .onLoad(Fees.this)
                                .load();
                    }

                });

        progressDialog.show();
        return view;
    }

    @Override
    public void onPageChanged(int page, int pageCount) {
        pageNumber = page;

        Log.e("page", page + 1 + " " + pageCount);

        page_number.setText((page + 1) + "/" + pageCount);
    }

    @Override
    public void loadComplete(int nbPages) {

        Log.e("Load Completed", nbPages + "");
        n = nbPages;
        msg.setText("Enter a page number between 1 - " + n);

    }

    @Override
    public void onError(Throwable t) {

        Log.e("t", t.toString());

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Unable to open this document").setCancelable(false).setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {

                dialog.dismiss();

            }
        });
        final AlertDialog alert = builder.create();
        alert.setTitle("Message");
        alert.show();
        alert.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.primaryColor));

    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;

    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    @Override
    public void onResume() {
        super.onResume();


    }
}
