package com.zhengyuan.emcarsplan;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonObject;

import com.zhengyuan.baselib.constants.EMProApplicationDelegate;
import com.zhengyuan.baselib.listener.NetworkCallbacks;
import com.zhengyuan.baselib.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


//getJuDuan();预留的接口

public class CarsPlanActivity extends Activity implements View.OnClickListener {
    //获取操作者的工号
    String sId = EMProApplicationDelegate.userInfo.getUserId();
    String sname=EMProApplicationDelegate.userInfo.getUserName();
    //布局
    private LinearLayout carListLayout = null;
    private LinearLayout addItemLayout = null;
    //控件
    private Button submitButton = null;
    EditText cxEdit = null;
    EditText chEdit = null;
    AutoCompleteTextView juEdit = null;
    EditText PracticeCarsEdit = null;
    EditText carsPlanEdit = null;
    EditText managerEdit = null;
    private ImageView backImage = null;

    //一个item包含的记录数目
    private int itemLength = 6;

    //暂存录入数据，计数.超过100条先提交再继续
    private String contents[][] = new String[100][7];
    private int count = 0;

    private Button addButton = null;
    private View enableItem = null;
    private Spinner rolespinner = null;
    private List<String> role_list = null;
    private ArrayAdapter<String> role_adapter = null;
    private ArrayAdapter<String> JuDuanadapter = null;
    private ImageButton backBtn;

    String result1 = "";//提交返回的结果
    public String res; //代表提交的所有的数据

    private boolean submitButtomState = true;

    Handler handler1=null;
    //最后一个数据是否有效
    private boolean addLastInfo=false;

   /* //测试扫码拍照的Activity
    private Button testActivityButton=null;*/



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cars_plan);
        TextView textView = findViewById(R.id.title_tv);
        String titleText = "进车计划";
        if (sId != null) {
            titleText = titleText + "_" + sId;
        }
        textView.setText(titleText);

        backBtn = findViewById(R.id.title_back_btn);
        //backBtn.setOnClickListener(this);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        carListLayout = (LinearLayout) findViewById(R.id.carListLayout);
        addItemLayout = (LinearLayout) findViewById(R.id.AddItem_Layout);
        submitButton = (Button) findViewById(R.id.submitCarInfo);

        enableItem = View.inflate(this, R.layout.cars_item, null);
        addItemLayout.addView(enableItem);

        //绑定控件
        init();

        //绑定监听事件
        addButton.setOnClickListener(this);
        submitButton.setOnClickListener(this);

        //该edit的监听用于自动完成局段信息的添加
        cxEdit.setOnFocusChangeListener(new editOnFocusChangeListener());
        chEdit.setOnFocusChangeListener(new editOnFocusChangeListener());
        //init局段自动填充
        initJuDuan();
        //自动填充负责人，默认登录用户
        ((EditText)enableItem.findViewById(R.id.Manager_Edit)).setText(sname+"("+sId+")");

        //通过提交返回的结果更新数据
        handler1 = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                // TODO Auto-generated method stub
                super.handleMessage(msg);

                //result1 = (String) msg.obj;
                if (result1.equals("true")) {
                    Toast.makeText(CarsPlanActivity.this, "提交成功!", Toast.LENGTH_SHORT).show();
                    submitButtomState = true;
                    submitButton.setBackgroundResource(R.drawable.shape_rectangle_radius_theme);
                    submitButton.setEnabled(true);

                    Log.d("******result1*****", "******result1*****");
                } else {
                    Toast.makeText(CarsPlanActivity.this, "提交失败，请重新操作!", Toast.LENGTH_SHORT).show();
                    submitButtomState = true;
                    submitButton.setTextColor(0xFFFFFFFF);
                    submitButton.setEnabled(true);
                }
            }
        };
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.Add_Button://点击添加按钮就动态添加Item
                if (InfoIsOkAddToArrayAndShow()) {
                    Toast.makeText(CarsPlanActivity.this, "添加成功", Toast.LENGTH_SHORT).show();
                    sortCartPlanItem();
                } else {
                }
                break;
            case R.id.submitCarInfo:
                AddEnableItemInfo();
                break;
            case R.id.title_back_btn:
                finish();
                break;
        }
    }

    //添加一条item/进行了前端的验证。还需进行后台验证
    private boolean InfoIsOkAddToArrayAndShow() {
        //临时变量
        String cxText = cxEdit.getText().toString().trim();
        String chText = chEdit.getText().toString().trim();
        String juText = juEdit.getText().toString().trim();
        String duanText = PracticeCarsEdit.getText().toString().trim();
        String carsPlanText = carsPlanEdit.getText().toString().trim();
        String managerText = managerEdit.getText().toString().trim();

        //如果所有数据填充完毕，则添加条目
        if ((!"".equals(cxText)) & (null != cxText) & (!"".equals(chText)) & (null != chText)
                & ((!"".equals(juText)) & (null != juText)) & ((!"".equals(duanText)) & (null != duanText))
                & ((!"".equals(carsPlanText)) & (null != carsPlanText)) & ((!"".equals(managerText)) & (null != managerText))) {

            if (count < 100) {
                //“负责人”的检验数据格式正确
                if (chackmanager(managerText)) {
                    //添加一条数据暂存
                    contents[count][0] = cxText;
                    contents[count][1] = chText;
                    contents[count][2] = juText;
                    contents[count][3] = duanText;
                    contents[count][4] = carsPlanText;
                    contents[count][5] = managerText;
                    //显示一个item
                    View UnableItem = View.inflate(this, R.layout.cars_unable_item, null);
                    ((EditText) UnableItem.findViewById(R.id.CX_Edit)).setText(contents[count][0]);
                    ((EditText) UnableItem.findViewById(R.id.CH_Edit)).setText(contents[count][1]);
                    ((EditText) UnableItem.findViewById(R.id.JU_Edit)).setText(contents[count][2]);
                    ((EditText) UnableItem.findViewById(R.id.PracticeCars_Edit)).setText(contents[count][3]);
                    ((EditText) UnableItem.findViewById(R.id.CarsPlan_Edit)).setText(contents[count][4]);
                    ((EditText) UnableItem.findViewById(R.id.Manager_Edit)).setText(contents[count][5]);
                    carListLayout.addView(UnableItem);
                    count++;
                    //清空输入框内容，只保留负责人栏目
                    cxEdit.setText("");
                    chEdit.setText("");
                    PracticeCarsEdit.setText("");
                    juEdit.setText("");
                    carsPlanEdit.setText("");
                    return true;
                } else {
                    Toast.makeText(CarsPlanActivity.this, "请按照提示输入“责任人”部分，如：张三(12345)", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(CarsPlanActivity.this, "记录数大于100，请先提交", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(CarsPlanActivity.this, "插入前请填入完整信息！", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    private boolean chackmanager(String manegerText) {
        int first = manegerText.indexOf("(");
        int second = manegerText.indexOf(")");
        if ((first != -1) && (second != -1) && (first < second)) {
            return true;
        }
        first = manegerText.indexOf("（");//中文符号
        second = manegerText.indexOf("）");

        if ((first != -1) && (second != -1) && (first < second)) {
            return true;
        }
        return false;
    }

    private void AddEnableItemInfo(){
        //临时变量
        String cxText = cxEdit.getText().toString().trim();
        String chText = chEdit.getText().toString().trim();
        String juText = juEdit.getText().toString().trim();
        String duanText = PracticeCarsEdit.getText().toString().trim();
        String carsPlanText = carsPlanEdit.getText().toString().trim();
        String managerText = managerEdit.getText().toString().trim();

        if((!"".equals(cxText)) & (null != cxText) & (!"".equals(chText)) & (null != chText)
                & ((!"".equals(juText)) & (null != juText)) & ((!"".equals(duanText)) & (null != duanText))
                & ((!"".equals(carsPlanText)) & (null != carsPlanText)) &
                ((!"".equals(managerText)) & (null != managerText))){

            if(chackmanager(managerText)){
                //输入框数据有效，是否添加到上传列表
                checkLastInfoDialog();
            }
            else{
                submitDialog(count);
            }
        }else{
            submitDialog(count);
        }
    }


    //遍历为item的button添加删除的监听器
    private void sortCartPlanItem() {
        Log.v("sortCartPlanItem", "startSortCartPlanItem");
        if (carListLayout != null) {
            for (int i = 0; i < carListLayout.getChildCount(); i++) {
                final View view = carListLayout.getChildAt(i);
                final int position = i;
                final Button deleteButton = (Button) view.findViewById(R.id.Delete_Button);
                deleteButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        deleteDialog(position);
                    }
                });
            }
        } else {
            Log.v("carListLayout", "null");
        }
        Log.v("sortCartPlanItem", "endSortCartPlanItem");
    }

    //确认的对话框删除
    private void deleteDialog(final int position) {
        Log.v("deleteDialog", "startDeleteDialog");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.mipmap.tip);
        builder.setTitle("删除提示");
        builder.setMessage("确认删除该项");
        builder.setPositiveButton("确认删除",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //刷新显示，删除数组中的记录
                        carListLayout.removeViewAt(position);
                        deleteFromContents(position);
                        Log.v("确认删除", "确认删除");
                    }
                });
        builder.setNegativeButton("取消",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Log.v("取消删除", "取消删除");
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
        Log.v("deleteDialog", "endDeleteDialog");
    }
    //提交对话框
    private void submitDialog(int count) {
        AlertDialog.Builder submitDialog = new AlertDialog.Builder(this);
        submitDialog.setIcon(R.mipmap.tip);
        submitDialog.setTitle("提交提示");
        submitDialog.setMessage("将提交" + count + "条记录，请确认录入数据无误！");
        submitDialog.setPositiveButton("确认提交",
                 new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //提交数据
                        res=dataToString(contents);
                        submitData(res,sId);
                        submitButton.setBackgroundResource(R.drawable.shape_rectangle_radius_gray);
                        submitButton.setEnabled(false);
                        submitButtomState = false;
                    }
                });
        submitDialog.setNegativeButton("取消",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                });
        AlertDialog dialog = submitDialog.create();
        dialog.show();
    }
    //验证最后输入的有效数据是否提交
    private void checkLastInfoDialog() {
        AlertDialog.Builder Dialog = new AlertDialog.Builder(this);
        Dialog.setIcon(R.mipmap.tip);
        Dialog.setTitle("提交提示");
        Dialog.setMessage("输入框数据有效，是否添加到上传列表");
        Dialog.setPositiveButton("确认添加",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //添加数据
                        addLastInfo=true;
                        InfoIsOkAddToArrayAndShow();
                        submitDialog(count);
                    }
                });
        Dialog.setNegativeButton("忽略",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //清空输入框
                        cxEdit.setText("");
                        chEdit.setText("");
                        juEdit.setText("");
                        PracticeCarsEdit.setText("");
                        carsPlanEdit.setText("");
                        submitDialog(count);
                    }
                });
        AlertDialog dialog = Dialog.create();
        dialog.show();
    }

    private void deleteFromContents(int position) {
        for (int i = position; i < count; i++) {
            if (i + 1 < count) {
                contents[i] = contents[i + 1];
            }
        }
        count--;
        return;
    }

    //获取id控件
    private void init() {
        cxEdit = (EditText) enableItem.findViewById(R.id.CX_Edit);
        chEdit = (EditText) enableItem.findViewById(R.id.CH_Edit);
        juEdit = (AutoCompleteTextView) enableItem.findViewById(R.id.JU_Edit);
        //自动填充
        initJuDuan();

        PracticeCarsEdit = (EditText) enableItem.findViewById(R.id.PracticeCars_Edit);
        carsPlanEdit = (EditText) enableItem.findViewById(R.id.CarsPlan_Edit);
        managerEdit = (EditText) enableItem.findViewById(R.id.Manager_Edit);
        addButton = (Button) enableItem.findViewById(R.id.Add_Button);
        //rolespinner=(Spinner)findViewById(R.id.role);

       /* //测试扫码和拍照的Activity
        testActivityButton=(Button)findViewById(R.id.testActivityButton);
        testActivityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent =new Intent(CarsPlanActivity.this, testActivity.class);
                startActivity(intent);
            }
        });*/
    }

    //自动填充局段，预留
    private void initJuDuan() {
        //获取自动填充的数组
        JuDuanadapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, getDataSource());
        juEdit.setAdapter(JuDuanadapter);
    }

    private List<String> getDataSource() {
        List<String> data = new ArrayList<String>();
        data.add("北京");
        data.add("株洲");
        data.add("武昌");
        return data;
    }

    //失去焦点时候自动填充局段
    class editOnFocusChangeListener implements View.OnFocusChangeListener {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            //失去焦点
            if (!hasFocus) {
                if ((!(cxEdit.getText().toString().trim()).equals("")) && (!((chEdit.getText().toString().trim()).equals("")))) {
                    //调用局段查询函数，显示局段.
                    getJuDuan();
                }
            }
        }
    }

    //预留自动显示局段接口
    public void getJuDuan() {
        //在填完车型车号获取局段信息，并显示。
        Toast.makeText(this, "自动显示局段暂未实现", Toast.LENGTH_SHORT).show();
    }


    //打印显示数据
    private void printData() {
        Toast.makeText(this, "插入的数据条数" + contents.length + "最后一个数据" + contents[contents.length][6], Toast.LENGTH_SHORT).show();
    }

    //将数据转化成字符串
    private String dataToString(String data[][]) {

        String result = new String("");
        if (data == null || data.length <= 0) {
            return null;
        }
        for (int i = 0; i < count; i++) {
            for (int j = 0; j < itemLength; j++) {
                result += data[i][j];
                if(j<(itemLength-1)){
                    result += ",";
                }
            }
            if(i<(count-1)){
                result += ";";
            }
        }
        //数据格式**,**,**;**,**,**;**,**,**
        return result;
    }

    //将json转化成Sting
    private String jsonToString(String data[][]) {
        return null;
    }

    //将数据转化成json对象
    private String dataToJson(String data[][]) {
        String result = "";
        Map<String, String> map = null;
        JsonObject jsonObject = new JsonObject();
        for (int i = 0; i < data.length; i++) {
            map = new HashMap<String, String>();
            map.put("1", data[i][0]);
            map.put("2", data[i][1]);
            map.put("3", data[i][2]);
            map.put("4", data[i][3]);
            map.put("5", data[i][4]);
            map.put("6", data[i][5]);

        }
        return null;
    }

    private void submitData(String s1,String sname) {
        DataObtainer.INSTANCE.sendCarsPlanMessage(s1,sname,
                new NetworkCallbacks.SimpleDataCallback() {
                    @Override
                    public void onFinish(boolean b, String s, Object o) {
                        if (o.equals("")) {
                            Utils.showToast("没有找到");
                            return;
                        }
                        result1 = (String) o;
                        Message m = handler1.obtainMessage();
                        handler1.sendMessage(m);
                    }
                }
        );
    }
}