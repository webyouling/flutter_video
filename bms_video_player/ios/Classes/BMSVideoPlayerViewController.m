
//
//  BMSVideoPlayerViewController.m
//  bms_video_player
//
//  Created by liuming on 2019/9/23.
//
#define WidthRatio [UIScreen mainScreen].bounds.size.width/375.0
#define HeightRatio [UIScreen mainScreen].bounds.size.height/667.0


#import <Flutter/Flutter.h>
#import "BMSVideoPlayerViewController.h"
#import <SJVideoPlayer/SJVideoPlayer.h>
#import <Masonry/Masonry.h>
#import "SJRotationManager.h"
#import "SoundShareCntentView.h"

static SJEdgeControlButtonItemTag const SJTestItemTag1 = 100;

@interface BMSVideoPlayerViewController ()<FlutterStreamHandler>
@property (nonatomic, strong) SJVideoPlayer *player;
@property (nonatomic, strong) SoundShareCntentView *soundContentView;
@end

@implementation BMSVideoPlayerViewController {
    UIView * _videoView;
    int64_t _viewId;
    
    FlutterMethodChannel* _channel;
    FlutterEventSink _eventSink;
    NSInteger _comeFrom;
    NSInteger _currentVideoID;
}

#pragma mark - life cycle

- (instancetype)initWithWithFrame:(CGRect)frame
                   viewIdentifier:(int64_t)viewId
                        arguments:(id _Nullable)args
                  binaryMessenger:(NSObject<FlutterBinaryMessenger>*)messenger {
    if ([super init]) {
        _viewId = viewId;
        _videoView = [UIView new];
        _videoView.backgroundColor = [UIColor blackColor];
        CGFloat x = 0;
        CGFloat y = 0;
        CGFloat width = [UIScreen mainScreen].bounds.size.width;
        CGFloat height = [UIScreen mainScreen].bounds.size.width*9/16;
        _videoView.frame = CGRectMake(x, y, width, height);
        _player = [SJVideoPlayer player];
      
        //playerVolumeDidChangeExeBlock
        [_player.defaultEdgeControlLayer.bottomAdapter exchangeItemForTag:SJEdgeControlLayerBottomItem_DurationTime withItemForTag:SJEdgeControlLayerBottomItem_Progress];
        [_player.defaultEdgeControlLayer.bottomAdapter removeItemForTag:SJEdgeControlLayerBottomItem_Separator];
        _player.defaultEdgeControlLayer.bottomHeight = 39*HeightRatio;
        [_player.defaultEdgeControlLayer.bottomAdapter reload];
        [_videoView addSubview:_player.view];
        [_player.view mas_makeConstraints:^(MASConstraintMaker *make) {
            make.edges.offset(0);
        }];
        
       
        self.soundContentView = [[SoundShareCntentView alloc]initWithFrame:CGRectMake(0, 0, 49*WidthRatio, height) xvideoPlayer:_player];

        SJEdgeControlButtonItem *item = [[SJEdgeControlButtonItem alloc] initWithCustomView:self.soundContentView tag:SJTestItemTag1];
        [_player.defaultEdgeControlLayer.rightAdapter addItem:item];
        [_player.defaultEdgeControlLayer.rightAdapter reload];
        
        _player.fastForwardViewController.enabled = YES; // 开启左右边缘快进快退功能
        _player.showMoreItemToTopControlLayer = NO;
        _player.defaultEdgeControlLayer.hiddenBackButtonWhenOrientationIsPortrait = YES;
      _player.defaultEdgeControlLayer.hiddenBottomProgressIndicator = YES;
         _player.enabledFilmEditing = YES;
        
        
        NSString* channelName = [NSString stringWithFormat:@"bms_video_player_%lld", viewId];
        _channel = [FlutterMethodChannel methodChannelWithName:channelName binaryMessenger:messenger];
        __weak __typeof__(self) weakSelf = self;
        
        [_channel setMethodCallHandler:^(FlutterMethodCall* call, FlutterResult result) {
            
            NSLog(@"%@",[NSThread currentThread]);
            
           [weakSelf onMethodCall:call result:result];
           
        }];
        
        
        FlutterEventChannel *eventChannel = [FlutterEventChannel eventChannelWithName:@"flutter_bms_video_player_event" binaryMessenger:messenger];
        [eventChannel setStreamHandler:self];
        
         [self registerObserver];

    }
    return self;
}

- (nonnull UIView *)view {
    return _videoView;
}
-(void)openMutedsound{
    
}

- (void)onMethodCall:(FlutterMethodCall*)call result:(FlutterResult)result {
    
    if ([[call method] isEqualToString:@"loadUrl"]) {
        [self onLoadUrl:call result:result];
    }else if ([[call method] isEqualToString:@"destroyPlayer"]){
        _player = nil;
        [_player.view removeFromSuperview];
    }
    else {
        result(FlutterMethodNotImplemented);
    }
   
}

- (void)onLoadUrl:(FlutterMethodCall*)call result:(FlutterResult)result {
    NSString* url = call.arguments[@"curentUrl"];
    _comeFrom = [call.arguments[@"comeFrom"]integerValue];
    _currentVideoID = [call.arguments[@"xvideoID"]integerValue];
    
    if (![self loadUrl:url]) {
        result([FlutterError errorWithCode:@"loadUrl_failed"
                                   message:@"Failed parsing the URL"
                                   details:[NSString stringWithFormat:@"URL was: '%@'", url]]);
    } else {
        result(nil);
    }
}

- (bool)loadUrl:(NSString*)url {
    
    NSURL *nsUrl;
    if(_comeFrom == 0){
       nsUrl  = [NSURL URLWithString:@"https://crazynote.v.netease.com/2019/0811/6bc0a084ee8655bfb2fa31757a0570f4qt.mp4"];
    }else{
         nsUrl  = [NSURL fileURLWithPath:url];
    }
    if (!nsUrl) {
        return false;
    }
    _player.assetURL = nsUrl;
    return true;
}



- (void)registerObserver {
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(loadStateDidChange)
                                                 name:@"playStatus"
                                               object:nil];
    
  
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(currentVolume:)
                                                 name:@"currentPlayerVolume"
                                        object:nil];

}


-(void)loadStateDidChange{
    
    _eventSink(@{@"action":@"readyToPlay",
                 @"comeFrom":@(_comeFrom),
                 @"xvideoID":@(_currentVideoID)
                 });
    
}
-(void)currentVolume:(NSNotification *)notification{
    
    float _currentVolume =   [notification.userInfo[@"volume"]floatValue];
    
    self.soundContentView.volum = _currentVolume;
    
    
   
}
- (void)dispose {
    _channel = nil;
    [self unregisterObservers];
}

- (void)unregisterObservers {
    [[NSNotificationCenter defaultCenter] removeObserver:self
                                                    name:@"playStatus"
                                                  object:nil];
    
    [[NSNotificationCenter defaultCenter] removeObserver:self
                                                    name:@"currentPlayerVolume"
                                                  object:nil];
}


- (FlutterError* _Nullable)onListenWithArguments:(id _Nullable)arguments
                                       eventSink:(FlutterEventSink)events {
    _eventSink = events;
    return nil;
}

// flutter不再接收
- (FlutterError* _Nullable)onCancelWithArguments:(id _Nullable)arguments {
   _eventSink = nil;
    return nil;
}



@end


