package com.example.kk.flyingmusic;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CopyActivity extends AppCompatActivity {

    private static final String TAG = "copy";

    ListView fileList_copy;
    TextView path_copy;


    //记录当前路径下 的所有文件的数组
    File currentParent;
    //记录当前路径下的所有文件的文件数组
    File[] currentFiles;
    private LinearLayout linearLayout_copy;
    private Button bt_cancel_cpoy;
    private Button bt_copy;
    private ArrayList<String> copyfiles;

    private List<Map<String,Object>> listItems;// 数据

    private SimpleAdapter adapter;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_copy);
        fileList_copy = findViewById(R.id.filelist_copy);
        path_copy = findViewById(R.id.path_copy);
        bt_cancel_cpoy = findViewById(R.id.bt_cancel_copy);
        bt_copy = findViewById(R.id.bt_copy);

        linearLayout_copy = findViewById(R.id.linearLayout_copy);
        copyfiles = new ArrayList<>();






        File root = this.getFilesDir();
        Log.i(TAG, "onCreate: toor"+root.getPath());
        if (root.exists()) {
            currentParent = root;
            currentFiles = root.listFiles();//获取root目录下的所有文件
            //Log.i(TAG, "onCreatecfiles: "+currentFiles);

            inflateListView(currentFiles);
        }


        fileList_copy.setEmptyView(findViewById(R.id.nofile_cpoy));
        fileList_copy.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //用户点击了文件，则调用手机已安装软件操作该文件
                if (currentFiles[position].isFile()) {
                    Toast.makeText(CopyActivity.this, "这是个文件夹！", Toast.LENGTH_SHORT).show();
                } else {
                    //获取currentFiles[position]路径下的所有文件
                    File[] tmp = currentFiles[position].listFiles();
                    if (tmp == null || tmp.length == 0) {
                        //Toast.makeText(MainActivity.this, "空文件夹!", Toast.LENGTH_SHORT).show();
                        //获取用户单击的列表项对应的文件夹，设为当前的父文件夹
                        currentParent = currentFiles[position];
                        //保存当前文件夹内的全部问价和文件夹
                        currentFiles = tmp;
                        inflateListView(currentFiles);
                    }//if
                    else {
                        //获取用户单击的列表项对应的文件夹，设为当前的父文件夹
                        currentParent = currentFiles[position];
                        //保存当前文件夹内的全部问价和文件夹
                        currentFiles = tmp;
                        inflateListView(currentFiles);
                    }
                }
            }
        });

        bt_copy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                copyfiles.clear();
                Intent intent = getIntent();
                copyfiles = intent.getStringArrayListExtra("copyfiles");
                for (String s:copyfiles){
                    Log.i(TAG, "onClick: intentfiles: "+s);
                    File file = new File(s);
                    try {
                        copyAll(file,currentParent);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                setResult(2,intent);
                finish();
            }
        });
        bt_cancel_cpoy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(CopyActivity.this)
                        .setIcon(R.mipmap.ic_launcher)
                        .setTitle("提示")
                        .setMessage("确定要取消本次复制吗？")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .create()
                        .show();
            }
        });
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.menu,menu);
//        return true;
//    }

    private void onbey() {
        try {
            Log.i(TAG, "onbey: this"+this.getFilesDir().getPath());
            Log.i(TAG, "onbey:currentParent "+currentParent.getPath());
            if (!this.getFilesDir().getPath().equals(currentParent.getPath())) {
                //获取上一层目录
                currentParent = currentParent.getParentFile();
                //列出当前目录下的所有文件
                currentFiles = currentParent.listFiles();
                //再次更新ListView
                inflateListView(currentFiles);
            }
            else{
                new AlertDialog.Builder(this)
                        .setIcon(R.mipmap.ic_launcher)
                        .setTitle("提示")
                        .setMessage("确定要取消本次复制吗？")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .create()
                        .show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }//catch
    }

//    public static void deleteAll(File file){
//        if(file.isFile() || file.list().length ==0){
//            file.delete();
//        }else{
//            File[] files = file.listFiles();
//            for (int i = 0; i < files.length; i++) {
//                deleteAll(files[i]);
//                files[i].delete();
//            }
//            if(file.exists())         //如果文件本身就是目录 ，就要删除目录
//                file.delete();
//        }
//    }



    private void inflateListView(File[] files) {
//        if (files.length == 0){
//            Toast.makeText(MainActivity.this, "没有文件", Toast.LENGTH_SHORT).show();
////            currentParent=currentParent.getParentFile();
////            currentFiles = currentParent.listFiles();
////            inflateListView(currentFiles);
//        }

//        else {
        //创建一个List集合,List集合的元素是Map
        listItems = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < files.length; i++) {
            Map<String, Object> listItem = new HashMap<String, Object>();
            //如果当前File是文件夹，使用folder图标；否则使用file图标
            if (files[i].isDirectory()) listItem.put("icon", R.mipmap.folder);
                //else if(files[i].isFile)
            else listItem.put("icon", R.mipmap.file);
            listItem.put("fileName", files[i].getName());
            //listItem.put("checkBox",new CheckBox(MainActivity.this));
            listItems.add(listItem);
        }
        //创建一个SimpleAdapter
        adapter = new SimpleAdapter(this, listItems, R.layout.list_item, new String[]{"icon", "fileName","checkBox"},
                new int[]{R.id.icon, R.id.filename,R.id.checkBox});
        //位ListView设置Adpter
        fileList_copy.setAdapter(adapter);
        path_copy.setText("当前路径为：" + currentParent.getPath());
//        }
    }
    public void copyAll(File src,File dest) throws IOException {
        File file = src;
        if (file.isFile()){
            File temp = new File(dest.getPath()+"/"+file.getName());
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(temp));
            int b;
            while ((b=bis.read())!=-1){
                bos.write(b);
            }
            bis.close();
            bos.close();
        }else {
            File subfile[] = file.listFiles();
            file = new File(dest.getPath()+"/"+file.getName());
            Log.i(TAG, "copyAll: filename"+file.getName());
            Log.i(TAG, "copyAll: destpath"+dest.getPath());
            if (dest.getPath().contains(file.getName())){
                Toast.makeText(CopyActivity.this, "不能复制在子文件夹下！", Toast.LENGTH_SHORT).show();
            }else {
                if(file.mkdirs()) {
                    for (File f : subfile) {
                        copyAll(f, file);
                    }
                }else {
                    Toast.makeText(CopyActivity.this, "创建失败，已有此文件夹！", Toast.LENGTH_SHORT).show();
                }
            }

        }
    }
    @Override
    public void onBackPressed() {
        onbey();
    }
}
