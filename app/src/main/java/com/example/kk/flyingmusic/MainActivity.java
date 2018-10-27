package com.example.kk.flyingmusic;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
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



    //记录当前路径下 的所有文件的数组
    File currentParent;
    //记录当前路径下的所有文件的文件数组
    File[] currentFiles;
    private LinearLayout linearLayout;
    private Button bt_cancel;
    private Button bt_delete;
    private Button bt_copy2other;
    private ArrayList<String> copyfiles;

    private List<Map<String,Object>> listItems;// 数据
    private List<Integer> list_delete = new ArrayList<>();// 需要删除的数据
    private boolean isMultiSelect = false;// 是否处于多选状态
    private SimpleAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fileList = findViewById(R.id.filelist);
        path = findViewById(R.id.path);
        bt_cancel = findViewById(R.id.bt_cancel);
        bt_delete = findViewById(R.id.bt_delete);

        linearLayout = findViewById(R.id.linearLayout);
        bt_copy2other = findViewById(R.id.bt_copy2other);
        copyfiles = new ArrayList<String>();








        File root = this.getFilesDir();
        File myfolder = new File("/data/user/0/com.example.kk.flyingmusic/files/myFolder");
        myfolder.mkdirs();
        File myfile = new File("/data/user/0/com.example.kk.flyingmusic/files/myFolder/myfile.txt");
        try {
            myfile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.i(TAG, "onCreate: toor"+root.getPath());
        if (root.exists()) {
            currentParent = root;
            currentFiles = root.listFiles();//获取root目录下的所有文件
            //Log.i(TAG, "onCreatecfiles: "+currentFiles);

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
        fileList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                isMultiSelect = true;
                list_delete.clear();
                for (int i = parent.getChildCount()-1 ; i >= 0 ; i--) {
                    View lv = (View) parent.getChildAt(i);
                    lv.findViewById(R.id.checkBox).setVisibility(View.VISIBLE);
                    lv.findViewById(R.id.checkBox).setFocusable(true);
                    lv.findViewById(R.id.checkBox).setClickable(true);
                }
                linearLayout.setVisibility(View.VISIBLE);
                return true;
            }
        });
        bt_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ListView lv = findViewById(R.id.filelist);
                for (int i=lv.getChildCount()-1;i>=0;i--){
                    View item = (View)lv.getChildAt(i);
                    CheckBox cb = item.findViewById(R.id.checkBox);
                    TextView textView = (TextView)item.findViewById(R.id.filename);

                    if (cb.isChecked()){
                        Log.i(TAG, "onClickfilename: "+currentParent.getPath()+"/"+textView.getText());
                        Log.i(TAG, "onClickdelete: delete "+i);
                        File deletefile = new File(currentParent.getPath()+"/"+textView.getText());
                        deleteAll(deletefile);
                    }
                }
                currentFiles = currentParent.listFiles();
                inflateListView(currentFiles);
                linearLayout.setVisibility(View.GONE);
            }
        });
        bt_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isMultiSelect = false;// 退出多选模式
                list_delete.clear();// 清楚选中的数据
                // 重新加载Adapter
                adapter = new SimpleAdapter(MainActivity.this, listItems, R.layout.list_item, new String[]{"icon", "fileName"}, new int[]{R.id.icon, R.id.filename});
                fileList.setAdapter(adapter);
                
                linearLayout.setVisibility(View.GONE);

            }
        });
        bt_copy2other.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                copyfiles.clear();
                ListView lv = findViewById(R.id.filelist);
                for (int i=lv.getChildCount()-1;i>=0;i--){
                    View item = (View)lv.getChildAt(i);
                    CheckBox cb = item.findViewById(R.id.checkBox);
                    TextView textView = (TextView)item.findViewById(R.id.filename);

                    if (cb.isChecked()){
                        Log.i(TAG, "onClickfilename: "+currentParent.getPath()+"/"+textView.getText());
                        Log.i(TAG, "onClickcopy: copy "+i);
                        //File copyfile = new File(currentParent.getPath()+"/"+textView.getText());
                        copyfiles.add(currentParent.getPath()+"/"+textView.getText());
                    }
                }
                for (String s:copyfiles){
                Log.i(TAG, "onClickArray: "+s);
                }
                Intent intent = new Intent(MainActivity.this,CopyActivity.class);
                intent.putStringArrayListExtra("copyfiles",copyfiles);
                startActivityForResult(intent,1);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_root:
                File root = this.getFilesDir();
                if (root.exists()) {
                    currentParent = root;
                    currentFiles = root.listFiles();//获取root目录下的所有文件
                    //Log.i(TAG, "onCreatecfiles: "+currentFiles);

                    inflateListView(currentFiles);
                }
                linearLayout.setVisibility(View.GONE);


                break;
            case R.id.doc:
                Log.i(TAG, "onOptionsItemSelected: doc");
                break;
            case R.id.picture:
                Log.i(TAG, "onOptionsItemSelected: picture");
                break;
            case R.id.music:
                Log.i(TAG, "onOptionsItemSelected: music");
                break;
            case R.id.newfolder:
                Log.i(TAG, "onOptionsItemSelected: newfolder");
                final EditText newfoldername = new EditText(this);
                new AlertDialog.Builder(this).setTitle("请输入新建文件夹名:").setIcon(
                        android.R.drawable.ic_dialog_info).setView(
                        newfoldername).setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.i(TAG, "onClick: newfoldername "+newfoldername.getText());
                        File newfolder = new File(currentParent.getPath()+"/"+newfoldername.getText());
                        if (newfolder.mkdirs()){
                            Log.i(TAG, "onClicknewFolder: 创建文件夹成功！");
                            currentFiles = currentParent.listFiles();
                            inflateListView(currentFiles);
                        }

                    }
                })
                        .setNegativeButton("取消", null).show();
                break;
            default:
                break;
        }
        return true;
    }

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode==1&&resultCode==2){
            linearLayout.setVisibility(View.GONE);
            inflateListView(currentFiles);

        }
    }

    public static void deleteAll(File file){
        if(file.isFile() || file.list().length ==0){
            file.delete();
        }else{
            File[] files = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                deleteAll(files[i]);
                files[i].delete();
            }
            if(file.exists())         //如果文件本身就是目录 ，就要删除目录
                file.delete();
        }
    }



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
            fileList.setAdapter(adapter);
            path.setText("当前路径为：" + currentParent.getPath());
//        }
    }

    @Override
    public void onBackPressed() {
        onbey();
    }
}
