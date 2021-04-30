package com.fluidtouch.bookshelf;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.graphics.drawable.DrawerArrowDrawable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MainActivity extends AppCompatActivity {

    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerArrowDrawable drawerArrow;
    private DrawerLayout mDrawerLayout;
    private Toolbar toolbar;
    private ImageView ivAddBook;
    private TextView tvCreateFolder;
    private RecyclerView rvBookShelfs;
    private ListView lvFolders;
    private ShelfAdapter shelfAdapter;
    private ArrayList<BookShelfDo> bookShelfDos;
    private static final ArrayList<Integer> imagesList = new ArrayList<>();
    private File defaultDirectory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        loadImages();
        initialiseControls();
        setMenu();
        shelfAdapter = new ShelfAdapter(MainActivity.this, bookShelfDos);
        rvBookShelfs.setAdapter(shelfAdapter);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 121);
        }
        else {
            createDefaultFolder();
        }
        ivAddBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeDrawer();
                addNoteBook();
                saveBooks(defaultDirectory, bookShelfDos);
                shelfAdapter.refreshAdapter(bookShelfDos);
                rvBookShelfs.post(new Runnable() {
                    @Override
                    public void run() {
                        if(shelfAdapter.getItemCount() > 1){
                            rvBookShelfs.smoothScrollToPosition(shelfAdapter.getItemCount()- 1);
                        }
                    }
                });
            }
        });

        tvCreateFolder.setOnClickListener(v -> {
            closeDrawer();
            Dialog dialog = new Dialog(MainActivity.this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view                  = inflater.inflate(R.layout.add_folder_dialog, null, false);
            EditText etFolderName      = view.findViewById(R.id.etFolderName);
            TextView tvDone            = view.findViewById(R.id.tvDone);
            TextView tvCancel          = view.findViewById(R.id.tvCancel);
            dialog.setContentView(view);
            final Window window = dialog.getWindow();
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
            window.setBackgroundDrawableResource(android.R.color.white);
            window.setGravity(Gravity.CENTER);
            dialog.show();
            tvCancel.setOnClickListener(v1 -> {
                hideKeyboard(etFolderName);
                dialog.dismiss();
            });
            tvDone.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(etFolderName.getText().toString().trim().equalsIgnoreCase("")){
                        Toast.makeText(MainActivity.this, "Please enter folder!", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        hideKeyboard(etFolderName);
                        defaultDirectory = createCategory(etFolderName.getText().toString().trim());
                        dialog.dismiss();
                    }
                }
            });
        });

        lvFolders.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                closeDrawer();
                for (int i = 0; i < lvFolders.getChildCount(); i++) {
                    if(position == i ){
                        lvFolders.getChildAt(i).setBackgroundColor(getResources().getColor(R.color.lightGray));
                    }else{
                        lvFolders.getChildAt(i).setBackgroundColor(Color.TRANSPARENT);
                    }
                }

                String folderName = ((TextView)view).getText().toString();
                File rootFolder = new File(getCacheDir(), getString(R.string.app_name));
                if(position != 0){
                    defaultDirectory = new File(rootFolder, folderName);
                    shelfAdapter.refreshAdapter(getBooks(defaultDirectory));
                }
                else {
                    defaultDirectory = rootFolder;
                    shelfAdapter.refreshAdapter(getBooks(rootFolder));
                }
            }
        });
    }

    private static void loadImages(){
        imagesList.add(R.drawable.shelf_baseball);
        imagesList.add(R.drawable.shelf_cornered_brown);
        imagesList.add(R.drawable.shelf_cover_yellow);
        imagesList.add(R.drawable.shelf_guitar);
        imagesList.add(R.drawable.shelf_journal);
        imagesList.add(R.drawable.shelf_leather_black);
        imagesList.add(R.drawable.shelf_leather_yellow);
        imagesList.add(R.drawable.shelf_piano);
        imagesList.add(R.drawable.shelf_plain_blue);
        imagesList.add(R.drawable.shelf_plain_brown);
        imagesList.add(R.drawable.shelf_plain_green);
        imagesList.add(R.drawable.shelf_plain_purple);
        imagesList.add(R.drawable.shelf_plain_red);
        imagesList.add(R.drawable.shelf_plain_yellow);
        imagesList.add(R.drawable.shelf_shopping);
        imagesList.add(R.drawable.shelf_yellow_striped);
    }

    private void initialiseControls(){
        mDrawerLayout                   = findViewById(R.id.mDrawerLayout);
        toolbar                         = findViewById(R.id.toolbar);
        ivAddBook                       = findViewById(R.id.ivAddBook);
        tvCreateFolder                  = findViewById(R.id.tvCreateFolder);
        lvFolders                       = findViewById(R.id.lvFolders);
        rvBookShelfs                    = findViewById(R.id.rvBookShelfs);
        setGridSpanCount();
    }

    private void setGridSpanCount() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;
        int colSpan = width / (230);//200 book width + 30 margin px as req
        Log.e("ColSpan Count : ", "height : "+height+", width : "+width+", colSpan : "+colSpan);
        rvBookShelfs.setLayoutManager(new GridLayoutManager(MainActivity.this, colSpan, GridLayoutManager.VERTICAL, false));
    }

    private void setMenu() {
        drawerArrow = new DrawerArrowDrawable(this);
        drawerArrow.setColor(getResources().getColor(R.color.white));
        toolbar.setNavigationIcon(drawerArrow);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close){
            @Override
            public void onDrawerOpened(View drawerView) {
                invalidateOptionsMenu();
                super.onDrawerOpened(drawerView);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                invalidateOptionsMenu();
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);
            }

            @Override
            public void onDrawerStateChanged(int newState) {
                super.onDrawerStateChanged(newState);
            }
        };
        mDrawerToggle.setDrawerArrowDrawable(drawerArrow);
        mDrawerLayout.addDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            Log.v("MainActivity","Permission: "+permissions[0]+ "was "+grantResults[0]);
            createDefaultFolder();
        }
        else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 121);
        }
    }

    private void createDefaultFolder() {
        try {
            defaultDirectory = new File(getCacheDir(), getString(R.string.app_name));
            if(defaultDirectory.exists()) {
                if(delete(defaultDirectory)) {
                    defaultDirectory.mkdirs();
                }
            }
            else {
                defaultDirectory.mkdirs();
            }
            getFolderList();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getFolderList() {
        ArrayList<String> foldersList = new ArrayList<>();
        File rootDirectory = new File(getCacheDir(), getString(R.string.app_name));

        if(rootDirectory.exists()) {
            if (rootDirectory.isDirectory()) {
                File[] files = rootDirectory.listFiles();
                if (files != null && files.length > 0) {
                    for (File file : files) {
                        if (file.isDirectory()) {
                            foldersList.add(file.getName());
                        }
                    }
                }
            }
        }
        Collections.sort(foldersList);
        foldersList.add(0, rootDirectory.getName());
        lvFolders.setAdapter(new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, android.R.id.text1, foldersList));
    }

    private boolean delete(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File f : files) {
                    delete(f);
                }
            }
        }
        return file.delete();
    }

    private File createCategory(String folderName) {
        File myDirectory = new File(getCacheDir(), getString(R.string.app_name));
        if(!myDirectory.exists()) {
            myDirectory.mkdirs();
        }
        File newFolder = new File(myDirectory, folderName);
        if(!folderName.equalsIgnoreCase("") && !newFolder.exists()) {
            if(newFolder.mkdirs()){
                defaultDirectory = newFolder;
                getFolderList();
                shelfAdapter.refreshAdapter(getBooks(defaultDirectory));
            }
        }
        return newFolder;
    }

    private void addNoteBook() {
        bookShelfDos = getBooks(defaultDirectory);
        BookShelfDo bookShelfDo = new BookShelfDo();
        bookShelfDo.setName("Book-"+(bookShelfDos.size()+1));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
        bookShelfDo.setDate(sdf.format(new Date()));
        bookShelfDo.setCover(getRandomCover());
        bookShelfDos.add(bookShelfDo);
    }

    private int getRandomCover() {
        return imagesList.get(new Random().nextInt(imagesList.size()));
    }

    private void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void closeDrawer() {
        mDrawerLayout.closeDrawer(Gravity.LEFT);
    }

    private class ShelfAdapter extends RecyclerView.Adapter<ShelfHolder> {

        private Context context;
        private ArrayList<BookShelfDo> bookShelfDos;

        public ShelfAdapter(Context context, ArrayList<BookShelfDo> bookShelfDos) {
            this.context = context;
            this.bookShelfDos = bookShelfDos;
        }
        private void refreshAdapter(ArrayList<BookShelfDo> bookShelfDos){
            this.bookShelfDos = bookShelfDos;
            notifyDataSetChanged();
        }
        @NonNull
        @Override
        public ShelfHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View convertView = LayoutInflater.from(context).inflate(R.layout.book_shelf_cell, parent, false);
            return new ShelfHolder(convertView);
        }

        @Override
        public void onBindViewHolder(@NonNull ShelfHolder holder, int position) {
            holder.ivNoteBook.setImageResource(bookShelfDos.get(position).getCover());
            holder.tvBookName.setText(bookShelfDos.get(position).getName());
        }

        @Override
        public int getItemCount() {
            return bookShelfDos!=null ? bookShelfDos.size() : 0;
        }
    }

    private class ShelfHolder extends RecyclerView.ViewHolder {

        private TextView tvBookName;
        private ImageView ivNoteBook;
        public ShelfHolder(@NonNull View itemView) {
            super(itemView);
            tvBookName      = itemView.findViewById(R.id.tvBookName);
            ivNoteBook      = itemView.findViewById(R.id.ivNoteBook);
        }
    }

    public void saveBooks(File folder, ArrayList<BookShelfDo> bookShelfDos) {
        try {
            File file = new File(folder, folder.getName()+".txt");
            FileOutputStream fos = new FileOutputStream(file);
            ObjectOutputStream os = new ObjectOutputStream(fos);
            os.writeObject(bookShelfDos);
            os.close();
            fos.close();
        }
        catch (Exception e) {
            Log.e("MainActivity", "saveBooks() : " + e.getMessage());
        }

    }

    public ArrayList<BookShelfDo> getBooks(File folder) {
        ArrayList<BookShelfDo> bookShelfDos = new ArrayList<>();
        try {
            File file = new File(folder, folder.getName()+".txt");
            if(file.exists()){
                FileInputStream fis = new FileInputStream(file);
                ObjectInputStream ois = new ObjectInputStream(fis);
                bookShelfDos = (ArrayList<BookShelfDo>) ois.readObject();
                ois.close();
                fis.close();
            }
        }
        catch (Exception e) {
            Log.e("MainActivity", "saveBooks() : " + e.getMessage());
        }
        return bookShelfDos;
    }
}