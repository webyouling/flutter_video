import 'dart:async';
import 'dart:convert';
import 'dart:io';
import 'package:flutter/services.dart';
import 'package:flutter_xvideos/model/MovieInfo.dart';
import 'package:flutter_xvideos/utiles/RequestManager.dart';
import 'package:flutter_xvideos/app/app_singleton.dart';
import 'package:flutter_xvideos/utiles/NormalTool.dart';
import 'package:flutter_xvideos/utiles/EventManager.dart';

class BmsVideoPlayerController {
  MethodChannel _channel;
  EventChannel _eventChannel;
  StreamSubscription<dynamic> _eventSubscription;
  int _currentViewId = 0;

  BmsVideoPlayerController.init(int id) {
    _currentViewId = id;
    _channel = new MethodChannel('bms_video_player_$id');
    _eventChannel = EventChannel('flutter_bms_video_player_event_$id');
    _eventSubscription = _eventChannel
        .receiveBroadcastStream()
        .listen(_eventListener, onError: _errorListener);
  }

//接受本地发送信息
  void _eventListener(dynamic event) {
    final Map<dynamic, dynamic> _videoInfoMap = event;
    String action = _videoInfoMap['action'];
    if (Platform.isIOS) {
      int _comeFrom = _videoInfoMap["comeFrom"];
      int _movie_id = _videoInfoMap["xvideoID"];
      if (_comeFrom == 1) {
        AppSingleton.downloadbloc.onUpdateVideoLocalVmiews(_movie_id);
      } else {
        if (!NormalTool.xVideoIDSet.contains(_movie_id)) {
          RequestManager.watch_movie();
        }
        NormalTool.xVideoIDSet.add(_movie_id); //在发生改变时传服务器
        //修正的时候
        if (NormalTool.leftPlayNumber >
            NormalTool.totalViewTimes - NormalTool.xVideoIDSet.length) {
          NormalTool.leftPlayNumber =
              NormalTool.totalViewTimes - NormalTool.xVideoIDSet.length;
        }
      }
    } else {
      if (action == "prepared") {
        //播放准备完成
        int _comeFrom = _videoInfoMap["comeFrom"];
        int _movie_id = _videoInfoMap["movie_id"];
        if (_comeFrom == 1) {
          AppSingleton.downloadbloc.onUpdateVideoLocalVmiews(_movie_id);
        } else {
          if (NormalTool.limit) {
            if (!NormalTool.xVideoIDSet.contains(_movie_id)) {
              RequestManager.watch_movie();
            }
            NormalTool.xVideoIDSet.add(_movie_id); //在发生改变时传服务器
            //修正的时候
            if (NormalTool.leftPlayNumber >
                NormalTool.totalViewTimes - NormalTool.xVideoIDSet.length) {
              NormalTool.leftPlayNumber =
                  NormalTool.totalViewTimes - NormalTool.xVideoIDSet.length;
            }
          }
        }
      } else if (action == "SelectClarity") {
        //选择的分辨率
        int _clarity = _videoInfoMap["clarity"];
        EventManager.eventBus.fire(BadgeNoEvent('selectClarity', _clarity));
      } else if (action == "SelectVideo") {
        //选择的本地视频
        final map1 = Map<String, dynamic>.from(_videoInfoMap);
        String info = json.encode(map1);
        EventManager.eventBus.fire(BadgeNoEvent('SelectVideo', info));
      } else if (action == "AndroidCopy") {
        EventManager.eventBus.fire(BadgeNoEvent('AndroidCopy', ''));
      }
    }
  }

  void _errorListener(Object obj) {}

  Future<void> loadUrlAndroid(
      MovieInfo currentMoveInfo, String playUrl, String resolution) async {
    assert(currentMoveInfo != null);
    Map<String, dynamic> _currentPlayerInfoMap = {
      "movieInfo": json.encode(currentMoveInfo.toJson()),
      "playUrl": playUrl,
      "currentResolution": resolution,
      "resolutions": json.encode(currentMoveInfo.HLS),
      "comeFrom": currentMoveInfo.comeFrom
    };
    return _channel.invokeMethod('loadUrl', _currentPlayerInfoMap);
  }

  Future<void> loadUrl(
      String url, String resolution, int comeFrom, int xvideoID) async {
    Map<String, dynamic> videoInfo = {
      "curentUrl": url,
      "currentResolution": resolution,
      "comeFrom": comeFrom,
      "xvideoID": xvideoID
    };
    assert(url != null);
    return _channel.invokeMethod('loadUrl', videoInfo);
  }

  Future<void> destroyPlayer() async {
    _channel = new MethodChannel('bms_video_player_$_currentViewId');
    return _channel.invokeMethod('destroyPlayer');
  }

  Future<void> androidBack() async {
    _channel = new MethodChannel('bms_video_player_$_currentViewId');
    return _channel.invokeMethod('androidBack');
  }
}
