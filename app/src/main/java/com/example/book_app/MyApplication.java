package com.example.book_app;

import static com.example.book_app.Constants.MAX_BYTES_PDF;

import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Environment;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.book_app.adapter.PdfAdminAdapter;
import com.example.book_app.models.PdfModel;
import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnErrorListener;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

public class MyApplication extends Application {

    private static final String TAG_DOWNLOAD = "DOWNLOAD_TAG";
    @Override
    public void onCreate() {
        super.onCreate();
    }

    public static final String formatTimestamp (long timestamp){
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(timestamp);
        String date = DateFormat.format("dd/MM/yy",cal).toString();

        return date;
    }

    public static void deleteBook(Context context,String bookTitle, String bookId, String bookUrl) {
        String TAG = "DELETE BOOK TAG";

        Log.d(TAG,"deleteBook :Deleting");
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Please wait");
        progressDialog.setMessage("Deleting" + bookTitle);
        progressDialog.show();


        StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(bookUrl);
        storageReference.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {

                        Log.d(TAG, "onSuccess : Deleted Storage");

                        Log.d(TAG, "onSuccess : Deleting from in DB");
                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
                        ref.child(bookId).removeValue()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Log.d(TAG, "onSuccess : Deleted from Db too" );
                                        progressDialog.dismiss();
                                        Toast.makeText(context, "Book Deleted Successfully", Toast.LENGTH_SHORT).show();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d(TAG, "onFailure: failed to delete from storage due to" + e.getMessage());
                                        Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: failed to delete from storage due to" + e.getMessage());
                        Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                });
    }

    public static void loadPdfSize(String pdfUrl , String pdfTitle, TextView sizeTv) {
        String TAG = "PDF_SIZE_TAG";

        StorageReference ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl);

        ref.getMetadata()
                .addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
            @Override
            public void onSuccess(StorageMetadata storageMetadata) {
                double bytes = storageMetadata.getSizeBytes();
                Log.d(TAG, "onSuccess " + pdfTitle + "" + bytes);
                double kb = bytes/1024;
                double mb = kb/1024;

                if (kb >= 1){
                    sizeTv.setText(String.format("%.2f", mb)+ "KB");
                } else if (mb >= 1) {
                    sizeTv.setText(String.format("%.2f", mb)+ "MB");
                }else{
                    sizeTv.setText(String.format("%.2f", mb)+ "Bytes");
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }

    public static void loadPdfFromUrl(String pdfUrl, String Title, PDFView pdfView, ProgressBar progressBar) {

        String TAG = "PDF_LOAD_SINGLE_TAG";

        StorageReference ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl);

        ref.getBytes(MAX_BYTES_PDF).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Log.d(TAG,"onSuccess" + Title + "Successfully got the file");

                pdfView.fromBytes(bytes)
                        .pages(0)
                        .spacing(0)
                        .swipeHorizontal(false)
                        .enableSwipe(false)
                        .onError(new OnErrorListener() {
                            @Override
                            public void onError(Throwable t) {
                                Log.d(TAG,"onError" + t.getMessage());
                            }
                        }).onPageError(new OnPageErrorListener() {
                            @Override
                            public void onPageError(int page, Throwable t) {
                                progressBar.setVisibility(View.INVISIBLE);
                                Log.d(TAG,"onPageError" +t.getMessage());
                            }
                        }).onLoad(new OnLoadCompleteListener() {
                            @Override
                            public void loadComplete(int nbPages) {
                                progressBar.setVisibility(View.INVISIBLE);
                            }
                        }).load();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressBar.setVisibility(View.INVISIBLE);
                Log.d(TAG,"onFailure : failed getting file from url due to"+ e.getMessage());
            }
        });

    }

    public static void loadCategory(String categoryId, TextView categoryTv) {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories");

        ref.child(categoryId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String category = ""+ snapshot.child("category").getValue();
                categoryTv.setText(category);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    public static void incrementBookViewCount (String bookId){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.child(bookId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String viewCount = ""+ snapshot.child("viewCount").getValue();
                        if(viewCount.equals("") || viewCount.equals("null")){
                            viewCount = "0";
                        }
                        Long newViewCount = Long.parseLong(viewCount)+1;
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("viewCount",newViewCount);

                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
                        ref.child(bookId).updateChildren(hashMap);
                    }


                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }


    public static void downloadBook (Context context,String bookId, String bookUrl,String bookTitle){
        Log.d(TAG_DOWNLOAD,"Downloading books...");
        String nameExtension = bookTitle + ".pdf";
        Log.d(TAG_DOWNLOAD,"Downloading book: NAME:"+ nameExtension);
        ProgressDialog progressDialog =new ProgressDialog(context);
        progressDialog.setTitle("Please wait");
        progressDialog.setMessage("Downloading" + nameExtension +"...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        StorageReference ref = FirebaseStorage.getInstance().getReferenceFromUrl(bookUrl);
        ref.getBytes(MAX_BYTES_PDF)
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        Log.d(TAG_DOWNLOAD,"Book Downloaded");
                        Log.d(TAG_DOWNLOAD,"Saving Book");
                        saveDownloadedBook(context, bookId, bytes, progressDialog, nameExtension);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG_DOWNLOAD,"onFailure download book due to" + e.getMessage());
                        progressDialog.dismiss();
                        Toast.makeText(context, "onFailure download book due to"+ e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private static void saveDownloadedBook(Context context, String bookId, byte[] bytes, ProgressDialog progressDialog, String nameExtension) {
        Log.d(TAG_DOWNLOAD,"saveDownloadedBook : Saving downloaded book");
        try{
            File downloadFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            downloadFolder.mkdirs();

            String filePath = downloadFolder.getPath() + "/" + nameExtension;

            FileOutputStream fileOutputStream = new FileOutputStream(filePath);
            fileOutputStream.write(bytes);
            fileOutputStream.close();

            Toast.makeText(context, "Saved download to folder", Toast.LENGTH_SHORT).show();
            Log.d(TAG_DOWNLOAD,"saveDownloadedBook : Saved to Download Folder :" );
            progressDialog.dismiss();

            incrementBookDownloadCount(bookId);

        }catch (Exception e){
            Log.d(TAG_DOWNLOAD,"saveDownloadBook: Failed saving Download Folder due to"+ e.getMessage());
            Toast.makeText(context, "Failed saving to download folder due to"+e.getMessage(), Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();
        }
    }

    public static void incrementBookDownloadCount(String bookId){
        Log.d(TAG_DOWNLOAD,"incrementBookDownloadCount: Incrementing Book Download Count");
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");

        ref.child(bookId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String downloadCount = ""+ snapshot.child("downloadsCount").getValue();
                        Log.d(TAG_DOWNLOAD,"onDataChange: Download Count"+ downloadCount);
                        if( downloadCount.equals("") || downloadCount.equals("null")){
                            downloadCount = "0";
                        }
                        long newDownloadCount = Long.parseLong(downloadCount) + 1;
                        Log.d(TAG_DOWNLOAD,"onDataChane : New Download Count "+ newDownloadCount);
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("downloadsCount",newDownloadCount);

                        DatabaseReference ref1 = FirebaseDatabase.getInstance().getReference("Books");
                        ref1.child(bookId).updateChildren(hashMap)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Log.d(TAG_DOWNLOAD,"onSuccess : Downloads Count updated...");
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d(TAG_DOWNLOAD,"onFailure : Failed to update Count due to"+ e.getMessage());
                                    }
                                });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

}
