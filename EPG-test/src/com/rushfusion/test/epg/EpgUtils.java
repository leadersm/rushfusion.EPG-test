package com.rushfusion.test.epg;

import android.net.Uri;

public class EpgUtils
{
  public static final String AUTHORITY = "com.iss.epg";
  public static final String TABLE_PLAY_HISTORY_INFO = "play_history_info";
  public static final String TABLE_PROGRAM_INFO = "program_info";
  public static final String TABLE_RESERVATION_INFO = "reservation_info";

  public static final class PlayHistoryInfo
  {
    public static final String CHANNEL_NAME = "channel_name";
    public static final Uri CONTENT_URI = Uri.parse("content://com.iss.epg/play_history_info");
    public static final String ORIGINAL_NETWORK_ID = "original_network_id";
    public static final String PLAY_DATE = "play_date";
    public static final String PLAY_DURATION = "play_duration";
    public static final String PROGRAM_NUMBER = "program_number";
    public static final String START_TIME = "start_time";
    public static final String TRANSPORT_STREAM_ID = "transport_stream_id";
    public static final String _ID = "_id";
  }

  public static final class ReservationInfo
  {
    public static final String CHANNEL_NAME = "channel_name";
    public static final Uri CONTENT_URI = Uri.parse("content://com.iss.epg/reservation_info");
    public static final String DURATION = "duration";
    public static final String EVENT_NAME = "event_name";
    public static final String ORIGINAL_NETWORK_ID = "original_network_id";
    public static final String PROGRAM_NUMBER = "program_number";
    public static final String SHORT_DESCRIPTOR = "short_descriptor";
    public static final String START_TIME = "start_time";
    public static final String TRANSPORT_STREAM_ID = "transport_stream_id";
    public static final String _ID = "_id";
  }

  public static final class ProgramInfo
  {
    public static final Uri CONTENT_URI = Uri.parse("content://com.iss.epg/program_info");
    public static final String FAVORITE = "favorite";
    public static final String FREE_CA_MODE = "free_ca_mode";
    public static final String FREQUENCY = "frequency";
    public static final String LATEST_PROGRAM = "latest_program";
    public static final String MODULATION = "modulation";
    public static final String ORIGINAL_NETWORK_ID = "original_network_id";
    public static final String PARENT_CONTROL = "parent_control";
    public static final String PROGRAM_NUMBER = "program_number";
    public static final String SERVICE_NAME = "service_name";
    public static final String SERVICE_PROVIDER_NAME = "service_provider_name";
    public static final String SYMBOL_RATE = "symbol_rate";
    public static final String TRANSPORT_STREAM_ID = "transport_stream_id";
    public static final String VOLUME = "volume";
    public static final String _ID = "_id";
  }
  public static final class Epg
  {

      public static final Uri CONTENT_URI = Uri.parse("content://dvb/eit");
      public static final String DURATION = "duration";
      public static final String EVENT_ID = "event_id";
      public static final String EVENT_NAME = "event_name";
      public static final String FREE_CA_MODE = "free_ca_mode";
      public static final String ORIGINAL_NETWORK_ID = "original_network_id";
      public static final String RUNNING_STATUS = "running_status";
      public static final String SERVICE_ID = "service_id";
      public static final String SHORT_DESCRIPTOR = "short_descriptor";
      public static final String START_TIME = "start_time";
      public static final String TS_ID = "transport_stream_id";
      public static final String _ID = "_id";
  }
  
}