

import 'package:flutter/cupertino.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

import 'bms_video_player.dart';


typedef void BmsVideoPlayerCreatedCallback(BmsVideoPlayerController controller);



class BmsVideoPlayer extends StatefulWidget {

  final BmsVideoPlayerCreatedCallback onCreated;


  BmsVideoPlayer({
    Key key,
    @required this.onCreated,
  });

  @override
  State<StatefulWidget> createState() => _VideoPlayerState();

}

class _VideoPlayerState extends State<BmsVideoPlayer> {

  @override
  void initState() {
    super.initState();
  }
  @override
  Widget build(BuildContext context) {
    double ratio = 16 / 9;
      return AspectRatio(
        aspectRatio: ratio,
        child: Container(
          color: Colors.black,
          child:nativeView(),
        ),
      );
  }
  nativeView() {
    if (defaultTargetPlatform == TargetPlatform.android) {
      return AndroidView(
        viewType: 'plugins.bms_video_player/view',
        onPlatformViewCreated: onPlatformViewCreated,
        creationParamsCodec: const StandardMessageCodec(),
      );
    } else {
      return UiKitView(
        viewType: 'plugins.bms_video_player/view',
        onPlatformViewCreated: onPlatformViewCreated,
        creationParamsCodec: const StandardMessageCodec(),
      );
    }
  }

  Future<void> onPlatformViewCreated(id) async {

    //这儿是回调回来的ID
    if (widget.onCreated == null) {
      return;
    }
    widget.onCreated(new BmsVideoPlayerController.init(id));
  }
}