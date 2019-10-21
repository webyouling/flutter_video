#import "BmsVideoPlayerPlugin.h"
#import "VideoViewFactory.h"
#import <SJVideoPlayer/SJVideoPlayer.h>


@implementation BmsVideoPlayerPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  
    VideoViewFactory* factory =
    [[VideoViewFactory alloc] initWithMessenger:registrar.messenger];
    [registrar registerViewFactory:factory withId:@"plugins.bms_video_player/view"];
    
 
    SJVideoPlayer.update(^(SJVideoPlayerSettings * _Nonnull common) {
        common.progress_trackColor = [UIColor colorWithRed:26 / 256.0 green:26 / 256.0 blue:26 / 256.0 alpha:1];
        common.progress_traceColor = [UIColor colorWithRed:228 / 256.0 green:34 / 256.0 blue:24 / 256.0 alpha:1];
        common.pauseBtnImage = [UIImage imageNamed:@"ic-round-pause"];
        common.playBtnImage = [UIImage imageNamed:@"ic-round-play-arrow"];
        common.replayBtnImage = [UIImage imageNamed:@"ic-round-replay"];
        common.lockBtnImage = [UIImage imageNamed:@"ic-round-lock"];
        common.unlockBtnImage = [UIImage imageNamed:@"ic-round-lock-open"];
        common.titleFont = [UIFont fontWithName:@"Hiragino Sans W6" size:10];
        common.backBtnImage = [UIImage imageNamed:@"ic-top-round-back"];
        common.fullBtnImage = [UIImage imageNamed:@"ic-round-fullscreen"];
        common.shrinkscreenImage = [UIImage imageNamed:@"ic-round-fullscreen-exit"];
    });

    

}

- (void)handleMethodCall:(FlutterMethodCall*)call result:(FlutterResult)result {
  if ([@"getPlatformVersion" isEqualToString:call.method]) {
    result([@"iOS " stringByAppendingString:[[UIDevice currentDevice] systemVersion]]);
  } else {
    result(FlutterMethodNotImplemented);
  }
}

@end
