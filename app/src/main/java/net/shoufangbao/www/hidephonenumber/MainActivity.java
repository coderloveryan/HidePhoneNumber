package net.shoufangbao.www.hidephonenumber;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.Selection;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

public class MainActivity extends ActionBarActivity {

    private EditText et;
    private CheckBox cb;
    private TextWatcher noHideWatcher;//未隐藏手机号
    private String phoneNumber;
    private TextWatcher hideWatcher;//隐藏手机号

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        /**
         * //禁用粘贴等功能（粘贴剪切隐藏实现有问题提），貌似api14以后只禁用长按没用所以加上setCustomSelectionActionModeCallback
         */
        et.setLongClickable(false);
        et.setCustomSelectionActionModeCallback(new ActionMode.Callback() {

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {

            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                return false;
            }
        });
        //未隐藏的监听phoneNumber直接赋值
        noHideWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                phoneNumber = s.toString();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        };
        et.addTextChangedListener(noHideWatcher);
        cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    et.removeTextChangedListener(noHideWatcher);//移除未隐藏条件的监听事件并且为EditText赋值
                    String str = phoneNumber;
                    StringBuffer s  = new StringBuffer();
                    if(!TextUtils.isEmpty(str)){
                        for(int i=0;i<str.length();i++){
                            if(i == 3 || i== 4 || i==5 || i==6){
                                s.append("*");
                            }else{
                                s.append(str.charAt(i));
                            }
                        }
                        et.setText(s.toString());
                        et.setSelection(s.length());
                    }
                    changePhoneNumber(et);
                }else{
                    et.removeTextChangedListener(hideWatcher);//移除隐藏条件下的监听并且为EditText赋值
                    et.setText(phoneNumber);
                    et.setSelection(phoneNumber.length());
                    et.addTextChangedListener(noHideWatcher);
                }
            }
        });
    }
   private void initView(){
       et = (EditText)findViewById(R.id.et);
       cb = (CheckBox)findViewById(R.id.cb);
   }

    /**
     * 隐藏手机号中间四位
     *根据onTextLength与beforeTextLength、pos判断用户在对应位置进行的操作，如果onTextLength不等于beforeTextLength证明内容发生变化了，把isChange置为true
     * （这里一定要有一个isChange 的boolean值判断因为我们在afterTextChanged对Edittext进行赋值它就会再次触发TextWatcher监听事件，没有isChange标志位就会死循环造成vStackOverFlowError错误！）
     * 我们可以根据长度判读是增加或者是删除操作从而改变phoneNumber内容
     * @param mEditText
     */
    protected void changePhoneNumber(final EditText mEditText) {
        hideWatcher = new TextWatcher() {
            int beforeTextLength = 0;
            int onTextLength = 0;
            boolean isChanged = false;
            int location = 0;//记录光标的位置
            private StringBuffer buffer = new StringBuffer();
            int pos = 0;
            int action = 0;//0没操作1：添加2：删除（用作改对应光标位置如：添加光标位置pos+1）

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                beforeTextLength = s.length();
                if (buffer.length() > 0) {
                    buffer.delete(0, buffer.length());
                }
                pos = mEditText.getSelectionStart();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                onTextLength = s.length();
                buffer.append(s.toString());
                if(pos == 0){
                    if(onTextLength > beforeTextLength){
                        action = 1;
                    }else if(onTextLength < beforeTextLength){
                        action = 2;
                    }else{
                        action = 0;
                    }
                    if(phoneNumber == null){
                        phoneNumber = s.toString();
                    }else{
                        phoneNumber = s.charAt(0)+phoneNumber;
                    }
                }else if(pos == phoneNumber.length()){
                    if(phoneNumber.length() < s.length()){//增加
                        action = 1;
                        phoneNumber = phoneNumber+s.toString().substring(phoneNumber.length(),s.length());
                    }else if(phoneNumber.length() > s.length()){//删除
                        action = 2;
                        phoneNumber = phoneNumber.substring(0, s.length());
                    }else{//无任何操作
                        action = 0;
                    }
                }else{
                    if(s.length() > phoneNumber.length()){//增加
                        action = 1;
                        phoneNumber = phoneNumber.substring(0,pos)+s.charAt(pos)+phoneNumber.substring(pos,phoneNumber.length());
                    }else if(s.length() < phoneNumber.length()){//删除
                        action = 2;
                        phoneNumber = phoneNumber.substring(0,pos-1)+phoneNumber.substring(pos,phoneNumber.length());
                    }else{//无人和操作
                        action = 0;
                    }
                }
                buffer = new StringBuffer();
                buffer.append(phoneNumber);
                if(beforeTextLength != onTextLength){
                    isChanged = true;
                }else{
                    isChanged = false;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (isChanged) {
                    location = mEditText.getSelectionEnd();
                    int index = 0;
                    while (index < buffer.length()) {
                        if ((index == 3 || index == 4 || index == 5 || index == 6)) {
                            buffer.setCharAt(index, '*');
                        }
                        index++;
                    }
                    String str = buffer.toString();
                    if(pos > str.length()){
                        pos = str.length();
                    }else if(pos < 0){
                        pos = 0;
                    }
                    mEditText.setText(str);
                    Editable etable = mEditText.getText();
                    if(action == 0){//无操作
                        Selection.setSelection(etable, pos);
                    }else if(action == 1){//增加
                        if(pos + 1 > str.length()){
                            Selection.setSelection(etable, pos);
                        }else{
                            Selection.setSelection(etable, pos+1);
                        }
                    }else{//删除
                        if(pos - 1 < 0){
                            Selection.setSelection(etable, pos);
                        }else{
                            Selection.setSelection(etable, pos-1);
                        }
                    }
                    isChanged = false;
                }

            }
        };
        mEditText.addTextChangedListener(hideWatcher);
    }
}
