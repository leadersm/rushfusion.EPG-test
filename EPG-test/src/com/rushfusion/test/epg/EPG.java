package com.rushfusion.test.epg;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.xmlpull.v1.XmlPullParserException;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.XmlResourceParser;
import android.database.Cursor;
import android.epg.EpgManager;
import android.epg.IEpgService;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.provider.DvbUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.rushfusion.tvpearls2.sandbox.IInteractiveService;

public class EPG extends Activity implements OnItemClickListener{
    /** Called when the activity is first created. */
	private static final String TAG = "EPG-test";
	
	private boolean isLoading = false;
	
	private static final int PREV = 101;
	private static final int NEXT = 102;
	private static final int VOICE_PLUS = 201;
	private static final int VOICE_MINUS = 202;
	
	private static final int loadingDelay = 300;
	private static final int infoTime = 3000;

	private Uri currentUrl;
	private int currentIndex = 0;
	private ChannelInfo currentInfo;
	
	private String currentChannel_name = "";
	private int currentChannel_num = 0;
	private String currentChannel_event = "";
	
	private VideoView player;
	private LinearLayout menu;
	private LinearLayout info;
	private ListView programList;
	
	private EpgManager mEpgManager;
	private IEpgService mEpgService;
	private static ContentResolver resolver;
	
	private static List<HashMap<String,Object>> mChannelNameList = new ArrayList<HashMap<String,Object>>();
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        init();
    }
    
	private void init() {
		mEpgManager = new EpgManager(EPG.this);
		resolver = getContentResolver();
		bindService(new Intent("com.rushfusion.tvpearls2.sandbox.INTERACTIVE"), 
				interactConnection,Context.BIND_AUTO_CREATE);
		player = (VideoView) findViewById(R.id.videoView1);
		info = (LinearLayout) findViewById(R.id.info);
		menu = (LinearLayout) findViewById(R.id.menu);
		programList = (ListView) findViewById(R.id.listView1);
		programList.requestFocus();
		setChannelList();
		BaseAdapter ba = new EpgAdapter();
		programList.setAdapter(ba);
		programList.setOnItemClickListener(this);
		currentInfo = getChannelInfo(this, getLastChannel());
		if(info!=null)
		{
			menu.setVisibility(View.GONE);
			resetAndPlay(currentInfo);
			showChannelInfo(currentInfo.getProgramNumber(),currentInfo.getServiceName());
		}else{
			menu.setVisibility(View.VISIBLE);
			Toast.makeText(this, "没有找到上次播放的节目,请从列表中选择！", 1).show();
		}
	}
	
	public static void saveLastChannel(Context context, int lastPos,ChannelInfo channelInfo) {
		if ((channelInfo == null) || (context == null))
			return;
		SharedPreferences.Editor editor = context.getSharedPreferences("epg_preference", 0).edit();
		editor.putInt("last_watch_position", lastPos);
		editor.putInt("original_network_id", channelInfo.getOriginalNetworkId());
		editor.putInt("transport_stream_id", channelInfo.getTransportStreamId());
		editor.putInt("program_number", channelInfo.getProgramNumber());
		editor.putInt("frequency", channelInfo.getFrequency());
		editor.putInt("modulation", channelInfo.getModulation());
		editor.putInt("symbol_rate", channelInfo.getSymbolRate());
		editor.commit();
	}
	
	
	private int getLastChannel(){
		SharedPreferences sp = getSharedPreferences("epg_preference", Context.MODE_WORLD_READABLE);
		int programNumber = sp.getInt("program_number", 121);//default cctv-1  121
		Log.d(TAG, "get last play program programNumber--->"+programNumber);
		return programNumber; 
	}
	
	
	private String getCurrentEventName(ChannelInfo info){
		System.out.println("查询info-->"+info.toString());
		String result="";
		Uri uri = EpgUtils.Epg.CONTENT_URI;
		String [] cols = new String []{"start_time","event_name"};
		String where = "original_network_id=" + info.getOriginalNetworkId() 
				+ " and " + "service_id" + "=" + info.getProgramNumber() 
				+ " and " + "transport_stream_id" + "=" + info.getTransportStreamId() 
				+ " and " + "start_time" + "<=" + System.currentTimeMillis();
		Cursor c = resolver.query(uri, cols, where, null, "start_time DESC");
		System.out.println("查询结果---c.size-->"+c.getCount());
		if(c!=null&&c.getCount()>0){
			c.moveToFirst();
			result = c.getString(c.getColumnIndex("event_name"));
		}
		System.out.println("当前频道==>"+info.getServiceName()+"---当前节目====>"+result);
		return result;
	}
	
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		Log.d("EPG-test", "keyCode:"+keyCode);
		switch (keyCode) {
		case KeyEvent.KEYCODE_DPAD_LEFT:
			voice(VOICE_MINUS);
			break;
		case KeyEvent.KEYCODE_DPAD_RIGHT:
			voice(VOICE_PLUS);
			break;
		case KeyEvent.KEYCODE_DPAD_UP:
			playNextOrPrev(NEXT);
			break;
		case KeyEvent.KEYCODE_DPAD_DOWN:
			playNextOrPrev(PREV);
			break;
		case KeyEvent.KEYCODE_MENU:
			showMenu();
			break;
		case KeyEvent.KEYCODE_BACK:
			if(menu.isShown()){
				menu.setVisibility(View.GONE);
				return true;
			}else
				break;
		case 165:
			showInteractMenu();
			break;
		default:
			break;
		}
		return super.onKeyDown(keyCode, event);
	}


	private void showInteractMenu() {
		// TODO Auto-generated method stub
		Log.d(TAG,"--------->打开互动应用列表<-------");
		loadLst();
	}

	private void showMenu() {
		// TODO Auto-generated method stub
		if(menu.isShown()){
			menu.setVisibility(View.GONE);
		}else{
			menu.setVisibility(View.VISIBLE);
			menu.requestFocus();
		}
	}

	private void voice(int voice) {
		
	}

	private void play() {
		// TODO Auto-generated method stub
		if(isLoading){
			player.removeCallbacks(loadVideoRunnable);
			isLoading = false;
		}else{
			isLoading = true;
			player.postDelayed(loadVideoRunnable, loadingDelay);
		}
			
	}

	private void showChannelInfo(final int num,final String channel){
		info.setVisibility(View.VISIBLE);
		final TextView info_num = (TextView) info.findViewById(R.id.info_number);
		final TextView info_channel = (TextView) info.findViewById(R.id.info_channelname);
		final TextView info_program = (TextView) info.findViewById(R.id.info_programname);
		//===========
		new AsyncTask<ChannelInfo, Void, String>(){

			@Override
			protected String doInBackground(ChannelInfo... params) {
				return getCurrentEventName(currentInfo);
			}

			@Override
			protected void onPostExecute(String result) {
				// TODO Auto-generated method stub
				super.onPostExecute(result);
				currentChannel_event = result;
				info_program.setText(result);
			}

			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				info_num.setText(num+"");
				info_channel.setText(channel);
			}
		}.execute();
	}
	
	Handler handler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			try {
				Thread.sleep(infoTime);
				info.setVisibility(View.GONE);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	};
	
	boolean mLoading = false;
	private synchronized void playNextOrPrev(int param) {
		// TODO Auto-generated method stub
		switch (param) {
		case PREV:
			currentIndex--;
			if(currentIndex<0)currentIndex = mChannelNameList.size()-1;
			Log.d(TAG,"currentIndex-->"+currentIndex);
			break;
		case NEXT:
			currentIndex++;
			if(currentIndex==mChannelNameList.size())currentIndex=0;
			Log.d(TAG,"currentIndex-->"+currentIndex);
			break;
		default:
			break;
		}
		int program_number;
		program_number =  (Integer) mChannelNameList.get(currentIndex).get("program_number");
		currentInfo = getChannelInfo(EPG.this,program_number);
		showChannelInfo(currentInfo.getProgramNumber(), currentInfo.getServiceName());
		if(mLoading){
			player.removeCallbacks(resetRunnable);
			mLoading = false;
		}else{
			player.postDelayed(resetRunnable, loadingDelay);
		}
	}

	
	Runnable resetRunnable = new Runnable() {
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			mLoading = true;
			resetAndPlay(currentInfo);
			mLoading = false;
		}
	};
	

	Runnable loadVideoRunnable = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			System.out.println("playing-->"+currentUrl);
			player.setVideoURI(currentUrl);
			player.setOnPreparedListener(new OnPreparedListener() {
				
				public void onPrepared(MediaPlayer mp) {
					System.out.println("---------OnPreparedListener--------");
					player.start();
					handler.sendEmptyMessage(1);
					isLoading = false;
				}
			});
		}
	};
    
	
	ServiceConnection tunerConnection = new ServiceConnection() {
		
		@Override
		public void onServiceDisconnected(ComponentName name) {
			// TODO Auto-generated method stub
			Log.d("EPG-test", "onServiceDisconnected--->tunerConnection");
		}
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			// TODO Auto-generated method stub
			mEpgService = android.epg.IEpgService.Stub.asInterface(service);
			Log.d("EPG-test", "onServiceConnected--->tunerConnection service-->" + mEpgService);
		}
	};
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, final int position,long id) {
		currentIndex = position;
		int program_number =  (Integer) mChannelNameList.get(position).get("program_number");
		currentInfo = getChannelInfo(this,program_number);
		System.out.println("position==>"+position+"  channelInfo.name==>"+currentInfo.getServiceName());
		System.out.println(info.toString());
		//???????????????????????????????????????????
		showChannelInfo(currentInfo.getProgramNumber(), currentInfo.getServiceName());
		resetAndPlay(currentInfo);
		//===========================================
	}

	private void resetAndPlay(final ChannelInfo info) {
		new Thread(new Runnable() {
			public void run() {
				currentChannel_name = info.getServiceName();
				currentChannel_num = info.getProgramNumber();
				saveLastChannel(EPG.this, currentIndex, info);
				mEpgManager.bindEpgService(tunerConnection);
				mEpgManager.startEpgService();
				while(mEpgService==null){
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
		        int i = info.getProgramNumber();
		        int j = info.getTransportStreamId();
		        int k = info.getOriginalNetworkId();
		        Log.d(TAG, (new StringBuilder()).append("setTunerInfo serviceId=").append(i).append(", tsId=").append(j).append(", networkId=").append(k).toString());
		        try {
//		        	mEpgService.getEpgTable(i, j, k);
		        	mEpgService.epgReportPlayingProgram(k, j, i);
		        	mEpgService.epgCamodSetTunerInfo(i, j, k);
		        	mEpgService.epgTunerLock(0, info.getFrequency(), info.getModulation(), info.getSymbolRate());
				} catch (RemoteException e) {
					e.printStackTrace();
				}
		        Uri[] uris = getVideoUri(EPG.this, info);
//		        System.out.println("uri[].size()==>"+uris.length);
		        if(uris.length>=1){
		        	currentUrl = uris[0];
		        	play();
		        }
			}
		}).start();
	}
	
	public static Uri[] getVideoUri(Context context, ChannelInfo channelinfo) {
		Uri auri[];
		if (context == null || channelinfo == null) {
			auri = null;
		} else {
			ArrayList<ArrayList<Integer>> arraylist = getPlayPids(context,channelinfo);
			Log.d("ChannelList",(new StringBuilder()).append("pids =").append(arraylist).toString());
			if (arraylist != null && arraylist.size() == 2) {
				Log.d(TAG, (new StringBuilder()).append("pids.size =").append(arraylist.size()).toString());
				ArrayList<Integer> arraylist1 = arraylist.get(0);
				ArrayList<Integer> arraylist2 = arraylist.get(1);
				int i = arraylist1.size();
				int j = arraylist2.size();
				int k;
				Uri auri1[];
				int l;
				if (i > 0 && j > 0)
					k = Math.min(i, j);
				else
					k = Math.max(i, j);
				auri1 = new Uri[k];
				l = 0;
				while (l < k) {
					int i1;
					int j1;
					int k1;
					int l1;
					StringBuilder stringbuilder;
					if (i != 0)
						i1 = 65535 & ((Integer) arraylist1.get(l)).intValue();
					else
						i1 = 0;
					if (i != 0)
						j1 = ((Integer) arraylist1.get(l)).intValue() >> 16;
					else
						j1 = 2;
					if (j != 0)
						k1 = 65535 & ((Integer) arraylist2.get(l)).intValue();
					else
						k1 = 0;
					if (j != 0)
						l1 = ((Integer) arraylist2.get(l)).intValue() >> 16;
					else
						l1 = 4;
					Log.d(TAG, (new StringBuilder()).append("videoPid =").append(i1).toString());
					Log.d(TAG,(new StringBuilder()).append("videoStream =").append(j1).toString());
					Log.d(TAG, (new StringBuilder()).append("audioPid =").append(k1).toString());
					Log.d(TAG,(new StringBuilder()).append("audioStream =").append(l1).toString());
					stringbuilder = new StringBuilder("demux-ts://");
					stringbuilder.append("demuxid=").append(0);
					stringbuilder.append("&vpid=").append(i1);
					stringbuilder.append("&vformat=").append(convertStreamTypeFormat(j1));
					stringbuilder.append("&apid=").append(k1);
					stringbuilder.append("&aformat=").append(convertStreamTypeFormat(l1));
					auri1[l] = Uri.parse(stringbuilder.toString());
					l++;
				}
				auri = auri1;
			} else {
				auri = null;
			}
		}
		return auri;
	}

	private static String convertStreamTypeFormat(int paramInt) {
		String str = "";
		switch (paramInt) {
		case 1:
			str = "mpeg2";
			break;
		case 2:
			str = "mpeg2";
			break;
		case 3:
			str = "mp2";
			break;
		case 4:
			str = "mp2";
			break;
		case 16:
			str = "mpeg4";
			break;
		case 27:
			str = "h264";
			break;
		case 15:
			str = "aac";
			break;
		case 17:
			str = "mp3";
			break;
		case 6:
			str = "ac3";
			break;
		case 129:
			str = "ac3";
			break;
		default:
			break;
		}
		return str;
	}
	
	private static int getStreamType(int i) {
		int result;
		switch (i) {
		case 1:
			result = 0;
			break;
		case 2:
			result = 0;
			break;
		case 3:
			result = 1;
			break;
		case 4:
			result = 1;
			break;
		case 6:
			result = 1;
			break;
		case 15:
			result = 1;
			break;
		case 16:
			result = 0;
			break;
		case 17:
			result = 1;
			break;
		case 27:
			result = 0;
			break;
		case 129:
			result = 1;
			break;
		default:
			result = -1;
			break;
		}
//		System.out.println("getStreamType i===>"+i+"  result  k==>"+result);
		return result;
	}

	public static Cursor getProgramCursor(Context context, int network_id,
			int program_number, int stream_id) {
		String str = "original_network_id=" + network_id + " and "
				+ "program_number" + "=" + program_number + " and "
				+ "transport_stream_id" + "=" + stream_id;
		return context.getContentResolver().query(
				EpgUtils.ProgramInfo.CONTENT_URI, null, str, null, null);
	}

	public static ChannelInfo getChannelInfo(Context context, int program_number) {
		Cursor cursor = context.getContentResolver().query(DvbUtils.Program.CONTENT_URI, null, "program_number="+program_number, null,
				"frequency ASC, program_number ASC");
		ChannelInfo channelinfo;
		if (cursor != null) {
			cursor.moveToFirst();
			ChannelInfo channelinfo1 = new ChannelInfo();
			int j = cursor.getInt(cursor.getColumnIndex("original_network_id"));
			int k = cursor.getInt(cursor.getColumnIndex("program_number"));
			int l = cursor.getInt(cursor.getColumnIndex("transport_stream_id"));
			int i1 = cursor.getInt(cursor.getColumnIndex("frequency"));
			int j1 = cursor.getInt(cursor.getColumnIndex("modulation"));
			int k1 = cursor.getInt(cursor.getColumnIndex("symbol_rate"));
			int l1 = cursor.getInt(cursor.getColumnIndex("pmt_pid"));
			String s = cursor.getString(cursor.getColumnIndex("service_name"));
			channelinfo1.setOriginalNetworkId(j);
			channelinfo1.setProgramNumber(k);
			channelinfo1.setTransportStreamId(l);
			channelinfo1.setFrequency(i1);
			channelinfo1.setModulation(j1);
			channelinfo1.setSymbolRate(k1);
			channelinfo1.setPmtPid(l1);
			channelinfo1.setServiceName(s);
			channelinfo = channelinfo1;
		} else {
			channelinfo = null;
		}
		if (cursor != null && !cursor.isClosed())
			cursor.close();
		return channelinfo;
	}

	public static ArrayList<ArrayList<Integer>> getPlayPids(Context context,ChannelInfo channelinfo) {
		ArrayList<ArrayList<Integer>> arraylist;
		if (context == null || channelinfo == null)
			arraylist = null;
		else {
			ArrayList<ArrayList<Integer>> arraylist1 = new ArrayList<ArrayList<Integer>>();
			ArrayList<Integer> arraylist2 = new ArrayList<Integer>();
			ArrayList<Integer> arraylist3 = new ArrayList<Integer>();
			arraylist1.add(arraylist2);
			arraylist1.add(arraylist3);
			String s = (new StringBuilder()).append("original_network_id=")
					.append(channelinfo.getOriginalNetworkId()).append(" and ")
					.append("program_number").append("=")
					.append(channelinfo.getProgramNumber()).append(" and ")
					.append("transport_stream_id").append("=")
					.append(channelinfo.getTransportStreamId()).toString();
			ContentResolver contentresolver = context.getContentResolver();
			Uri uri = DvbUtils.ProgramMap.CONTENT_URI;
			String as[] = new String[2];
			as[0] = "stream_type";
			as[1] = "elementary_pid";
			Cursor cursor = contentresolver.query(uri, as, s, null, null);
			if (cursor != null && cursor.moveToFirst()) {
				Log.d(TAG, (new StringBuilder()).append("c.getCount = ").append(cursor.getCount()).toString());
				int i = cursor.getCount();
				int ai[] = new int[i];
				int ai1[] = new int[i];
				int j = 0;
				do {
					ai[j] = cursor.getInt(cursor
							.getColumnIndexOrThrow("stream_type"));
					ai1[j] = cursor.getInt(cursor
							.getColumnIndexOrThrow("elementary_pid"));
//					System.out.println("ai["+j+"]===>"+ai[j]+"  ai1["+j+"]===>"+ai1[j]);
					int k = getStreamType(ai[j]);
//					System.out.println("k===>"+k);
					if (k == 0)
						arraylist2.add(Integer.valueOf(ai1[j] | ai[j] << 16));
					else if (k == 1)
						arraylist3.add(Integer.valueOf(ai1[j] | ai[j] << 16));
					j++;
				} while (cursor.moveToNext());
			}
			if (cursor != null)
				cursor.close();
			arraylist = arraylist1;
		}
		return arraylist;
	}

    public static ArrayList<Program> getProgramInfo(Context context, int i)
    {
        ArrayList<Program> arraylist = new ArrayList<Program>();
        try
        {
            XmlResourceParser xmlresourceparser = context.getResources().getXml(i);
            do
            {
                int j = xmlresourceparser.next();
                if(j == 1)
                    break;
                if(j == 2 && "program".equals(xmlresourceparser.getName()))
                {
                    Program program = new Program();
                    program.mTime = getXmlAttribute(context, xmlresourceparser, "time");
                    program.mLabel = getXmlAttribute(context, xmlresourceparser, "label");
                    arraylist.add(program);
                }
            } while(true);
        }
        catch(XmlPullParserException e) { e.printStackTrace();}
        catch(IOException e) { e.printStackTrace();}
        return arraylist;
    }

	private static String getXmlAttribute(Context context,
			XmlResourceParser xmlresourceparser, String s) {
		int i = xmlresourceparser.getAttributeResourceValue(null, s, 0);
		String s1;
		if (i == 0)
			s1 = xmlresourceparser.getAttributeValue(null, s);
		else
			s1 = context.getString(i);
		return s1;
	}

	public static void setChannelList() {
		//查免费的 ecm_pid = 8191;
		Uri mapUri = DvbUtils.ProgramMap.CONTENT_URI;
		String [] cols = new String []{"program_number"};
		Cursor c0 = resolver.query(mapUri, cols, "ecm_pid=8191", null, null);
//		System.out.println("c0.size=========>"+c0.getCount());
		int temp=-1;
		if(c0!=null)
		while(c0.moveToNext()){
			int program_number = c0.getInt(c0.getColumnIndex("program_number"));
			if(program_number==temp)continue;
			temp = program_number;
			HashMap<String,Object> data = new HashMap<String, Object>();
			//查节目号为 program_number = xx 的行出来；
			Uri uri = DvbUtils.Program.CONTENT_URI;
			String[] args = new String[] { "service_name" ,"free_ca_mode","original_network_id"};
			Cursor c = resolver.query(uri, args, "program_number="+program_number,  null,"frequency ASC, program_number ASC");
			if (c != null) {
//				System.out.println("c.size====>"+c.getCount());
				while (c.moveToNext()) {
					int original_network_id = c.getInt(c.getColumnIndex("original_network_id"));
					if(original_network_id==1)continue;
					String channelName = c.getString(c.getColumnIndex("service_name"));
					data.put("channelName", channelName);
					data.put("program_number", program_number);
					mChannelNameList.add(data);
				}
				c.close();
			}
		}
		c0.close();
	}

	public static class Program {
		public String mLabel;
		public String mTime;
	}

	public static class Channel {
		public String label;
		public String program;
	}

	class EpgAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return mChannelNameList.size();
		}

		@Override
		public HashMap<String,Object> getItem(int position) {
			// TODO Auto-generated method stub
			return mChannelNameList.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			TextView tv = new TextView(EPG.this);
			tv.setTextSize(24);
			tv.setTextColor(Color.WHITE);
			int number =  (Integer) mChannelNameList.get(position).get("program_number");
			tv.setText((String) mChannelNameList.get(position).get("channelName"));
			return tv;
		}

	}

	
	private IInteractiveService mService;
	
    private void loadLst() {
    	try {
			//mService.loadInteractiveList("vod", getString(R.string.title));
    		String param = currentChannel_name.equals("")? "西游记":currentChannel_event;
    		Log.d(TAG, "currentChannel_event===>"+param);
    		mService.loadInteractiveList("vod", param);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
    }
    
	
	private ServiceConnection interactConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mService = IInteractiveService.Stub.asInterface(service);
			Log.d(TAG, "onServiceConnected--->interactConnection");
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mService = null;
			Log.d(TAG, "onServiceDisconnected--->interactConnection");
		}
	};
	
    @Override
    public void onPause() {
    	super.onPause();
    	if(tunerConnection!=null)
    		unbindService(tunerConnection);
    	if(interactConnection!=null)
    		unbindService(interactConnection);
//    	if(menu!=null)menu.removeAllViews();
    	mChannelNameList.clear();
    }
	
    
}