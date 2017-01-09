package com.hexaflake.chatapp;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

//seperated list adapter from Jeff Sharkey (http://jsharkey.org/blog/2008/08/18/separating-lists-with-headers-in-android-09/)
//used to create different style of messages sent and messages received for a single ListView
class SeparatedListAdapter extends BaseAdapter {

    public final Map<String,Adapter> sections = new LinkedHashMap<String,Adapter>();
    public final ArrayAdapter<String> headers;
    public final static int TYPE_SECTION_HEADER = 0;

    public SeparatedListAdapter(Context context) {
        headers = new ArrayAdapter<String>(context, R.layout.list_header);
    }

    public void addSection(String section, Adapter adapter) {
        this.headers.add(section);
        this.sections.put(section, adapter);
    }

    public Object getItem(int position) {
        for(Object section : this.sections.keySet()) {
            Adapter adapter = sections.get(section);
            int size = adapter.getCount() + 1;

            // check if position inside this section
            if(position == 0) return section;
            if(position < size) return adapter.getItem(position - 1);

            // otherwise jump into next section
            position -= size;
        }
        return null;
    }

    public int getCount() {
        // total together all sections, plus one for each section header
        int total = 0;
        for(Adapter adapter : this.sections.values())
            total += adapter.getCount() + 1;
        return total;
    }

    public int getViewTypeCount() {
        // assume that headers count as one, then total all sections
        int total = 1;
        for(Adapter adapter : this.sections.values())
            total += adapter.getViewTypeCount();
        return total;
    }

    public int getItemViewType(int position) {
        int type = 1;
        for(Object section : this.sections.keySet()) {
            Adapter adapter = sections.get(section);
            int size = adapter.getCount() + 1;

            // check if position inside this section
            if(position == 0) return TYPE_SECTION_HEADER;
            if(position < size) return type + adapter.getItemViewType(position - 1);

            // otherwise jump into next section
            position -= size;
            type += adapter.getViewTypeCount();
        }
        return -1;
    }

    public boolean areAllItemsSelectable() {
        return false;
    }

    public boolean isEnabled(int position) {
        return (getItemViewType(position) != TYPE_SECTION_HEADER);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        int sectionnum = 0;
        for(Object section : this.sections.keySet()) {
            Adapter adapter = sections.get(section);
            int size = adapter.getCount() + 1;

            // check if position inside this section
            if(position == 0) return headers.getView(sectionnum, convertView, parent);
            if(position < size) return adapter.getView(position - 1, convertView, parent);

            // otherwise jump into next section
            position -= size;
            sectionnum++;
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}


public class MainActivity extends AppCompatActivity {

    public final static String ITEM_TITLE = "title";
    public final static String ITEM_CAPTION = "caption";
    public final static String ITEM_TIME = "time";
    public final static String USER_FROM = "Hank H.";
    public final static String USER_TO = "Bobby H.";


    //initializing list for Hanks generic responses to his son
    public final static String [] GENERIC_HANK_REPSONSES = {
            "D-minus! Dangit, Bobby, I expected better from someone who doesn't have any extracurricular activities.",
            "Dangit, Bobby!!!",
            "No got dang way, Bobby!",
            "Bobby, if you weren't my son, I'd hug you.",
            "You, uh, you're my son, you know, with everything that entails... feelings of fondness and more... You know what I mean, don’t you, boy?",
            "Yep",
            "I always say - if you plan ahead, then when things happen, you're prepared for them.",
            "It's called the double standard, Bobby. Don't knock it — we got the long end of the stick on that one.",
            "The only woman I'm pimping is sweet lady propane! And I'm tricking her out all over this town."
    };
    //initializing random number generaotr to get random responses randomly from list
    Random random = new Random();

    //initializing adapter for list view
    SeparatedListAdapter adapter;
    ListView list;
    Calendar c = Calendar.getInstance();
    SimpleDateFormat sdf = new SimpleDateFormat("h:mm a");

    //creates new message called item, used by the seperated list adapter
    public Map<String,?> createItem(String title, String caption) {
        Map<String,String> item = new HashMap<String,String>();
        //add username to message
        item.put(ITEM_TITLE, title);
        //add content to message
        item.put(ITEM_CAPTION, caption);
        //add time to message
        String curr_time = sdf.format(c.getTime());
        item.put(ITEM_TIME, curr_time);
        return item;
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.activity_main);
        RelativeLayout layout =(RelativeLayout)findViewById(R.id.layout);
        final EditText input_box = (EditText) findViewById(R.id.editText);
        final Button send_button = (Button) findViewById(R.id.button);

        //making sure text doesn't scroll horizontally into end of input text box while typing
        input_box.setHorizontallyScrolling(false);

        //making layout clickable to disappear keyboard later on
        layout.setClickable(true);

        //setting send button to gone by default
        send_button.setVisibility(View.GONE);

        //creating preloaded messages
        List<Map<String,?>> preloaded_messages_received;
        preloaded_messages_received = new LinkedList<Map<String,?>>();
        preloaded_messages_received.add(createItem(USER_FROM, "I sell propane and propane accessories"));
        preloaded_messages_received.add(createItem(USER_FROM, "Dangit, Bobby!!!"));


        // create our list and custom adapter
        adapter = new SeparatedListAdapter(this);
        //inputting preloaded mussages into list view
        adapter.addSection("hank", new SimpleAdapter(this, preloaded_messages_received, R.layout.receiver,
                new String[] { ITEM_TITLE, ITEM_CAPTION, ITEM_TIME }, new int[] { R.id.username_receiver, R.id.received_message_contents, R.id.time_receiver}));

        list = (ListView) findViewById(R.id.listView);
        list.setAdapter(adapter);


        //set onclick listener for send_button
        send_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                addItems( input_box.getText().toString() );
                input_box.setText("", TextView.BufferType.EDITABLE);
            }
        });

        //set on text change listener for input_box to make send button visible when something is entered
        input_box.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {
                //if length of text in input box is 0 set send button view to gone
                if(editable.length() == 0 ){
                    send_button.setVisibility(View.GONE);
                }
                else send_button.setVisibility(View.VISIBLE);
            }
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

        });

        //set onclick listener for listView to disappear send button and keyboard
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //disappear keyboard when not using input_box
                InputMethodManager inputManager = (InputMethodManager) MainActivity.this.getSystemService(MainActivity.this.INPUT_METHOD_SERVICE);
                try {
                    inputManager.hideSoftInputFromWindow(MainActivity.this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                } catch (Throwable e) {}

            }
        });

        //set onclick listener for layout view to disappear keyoboard and send button
        layout.setOnClickListener(new RelativeLayout.OnClickListener() {
            public void onClick(View v){
                //disappear keyboard when not using input_box
                InputMethodManager inputManager = (InputMethodManager) MainActivity.this.getSystemService(MainActivity.this.INPUT_METHOD_SERVICE);
                try {
                    inputManager.hideSoftInputFromWindow(MainActivity.this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                } catch (Throwable e) {}
            }
        });
    }

    //append message to listView
    public void addItems(String new_item) {
        //setting list to null while I add new section into adapter (app behaves unpredictably if list stays set when adding to it)
        list.setAdapter(null);
        //creating temporary list to house new message and pass into simple adapter
        LinkedList<Map<String,?>> new_message = new LinkedList<Map<String,?>>();
        new_message.add(createItem(USER_TO, new_item));
        adapter.addSection(Integer.toString(adapter.getCount()), new SimpleAdapter(this, new_message, R.layout.sender,
                new String[] { ITEM_TITLE, ITEM_CAPTION, ITEM_TIME }, new int[] { R.id.username_to, R.id.sending_message_contents, R.id.time_to }));
        //resetting list to our updated adapter
        list.setAdapter(adapter);
        //prompting timer to simulate a response from another user after 1.5 seconds
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                simulate_response();
            }
        }, 1500);
    }

    //doing same thing as addItems except with layouts from receiver.xml
    public void simulate_response() {
        //setting list to null while I add new section into adapter (app behaves unpredictably if list stays set when adding to it)
        list.setAdapter(null);
        //creating temporary list to house new message and pass into simple adapter
        LinkedList<Map<String,?>> new_message = new LinkedList<Map<String,?>>();
        //getting random response from string array to send to user
        int index = random.nextInt(GENERIC_HANK_REPSONSES.length);
        new_message.add(createItem(USER_FROM,GENERIC_HANK_REPSONSES[index]));
        adapter.addSection(Integer.toString(adapter.getCount()), new SimpleAdapter(this, new_message, R.layout.receiver,
                new String[] { ITEM_TITLE, ITEM_CAPTION, ITEM_TIME }, new int[] { R.id.username_receiver, R.id.received_message_contents, R.id.time_receiver }));
        //resetting list to our updated adapter
        list.setAdapter(adapter);
    }
}








