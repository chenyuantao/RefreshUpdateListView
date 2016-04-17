package com.cyt.mylistview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final RefreshUpdateListView listView = (RefreshUpdateListView)findViewById(R.id.listView);
        listView.setOnRefreshUpdateListener(new RefreshUpdateListView.OnRefreshUpdateListener() {
            @Override
            public void onRefresh() {

            }

            @Override
            public void onUpdate() {

            }
        });
        ArrayList<String> list = new ArrayList<String>();
        for(int i=0;i<30;i++){
            list.add("test"+i);
        }
        ArrayAdapter<String> adapter =  new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,list);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                position=position-1;
                if(position<0){
                    return;
                }
                showToast("pos:"+position);
                listView.setRefreshComplete();
                listView.setUpdateComplete();
            }
        });
    }

    private void showToast(String str){
        Toast.makeText(this,str,Toast.LENGTH_SHORT).show();
    }
}
