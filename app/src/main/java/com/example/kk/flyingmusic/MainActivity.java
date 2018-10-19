package com.example.kk.flyingmusic;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "root";

    ListView fileList;
    TextView path;
    Button parent;

    //记录当前路径下 的所有文件的数组
    File currentParent;
    //记录当前路径下的所有文件的文件数组
    File[] currentFiles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fileList = findViewById(R.id.filelist);
        path = findViewById(R.id.path);
        parent = findViewById(R.id.parent);



        File root = new File("/mnt");
        if (root.exists()) {
            currentParent = root;
            currentFiles = root.listFiles();//获取root目录下的所有文件

            inflateListView(currentFiles);
        }


        fileList.setEmptyView(findViewById(R.id.nofile));
        fileList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //用户点击了文件，则调用手机已安装软件操作该文件
                if (currentFiles[position].isFile()) {
                    Intent intent = OpenFile.openFile(currentFiles[position].getPath());
                    startActivity(intent);
                } else {
                    //获取currentFiles[position]路径下的所有文件
                    File[] tmp = currentFiles[position].listFiles();
                    if (tmp == null || tmp.length == 0) {
                        Toast.makeText(MainActivity.this, "空文件夹!", Toast.LENGTH_SHORT).show();
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

        parent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onbey();
            }
        });
    }

    private void onbey() {
        try {
            if (!"/mnt".equals(currentParent.getCanonicalPath())) {
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
                        .setMessage("确定要退出吗？")
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


    private void inflateListView(File[] files) {
        if (files.length == 0)
            Toast.makeText(MainActivity.this, "sd卡不存在", Toast.LENGTH_SHORT).show();
        else {
            //创建一个List集合,List集合的元素是Map
            List<Map<String, Object>> listItems = new ArrayList<Map<String, Object>>();
            for (int i = 0; i < files.length; i++) {
                Map<String, Object> listItem = new HashMap<String, Object>();
                //如果当前File是文件夹，使用folder图标；否则使用file图标
                if (files[i].isDirectory()) listItem.put("icon", R.mipmap.folder);
                    //else if(files[i].isFi)
                else listItem.put("icon", R.mipmap.file);
                listItem.put("fileName", files[i].getName());
                listItems.add(listItem);
            }
            //创建一个SimpleAdapter
            SimpleAdapter simpleAdapter = new SimpleAdapter(this, listItems, R.layout.list_item, new String[]{"icon", "fileName"},
                    new int[]{R.id.icon, R.id.filename});
            //位ListView设置Adpter
            fileList.setAdapter(simpleAdapter);
            try {
                path.setText("当前路径为：" + currentParent.getCanonicalPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onBackPressed() {
        onbey();
    }
}
