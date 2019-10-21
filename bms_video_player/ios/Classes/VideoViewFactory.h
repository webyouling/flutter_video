//
//  VideoViewFactory.h
//  bms_video_player
//
//  Created by liuming on 2019/9/23.
//

#import <Foundation/Foundation.h>
#import <Flutter/Flutter.h>

NS_ASSUME_NONNULL_BEGIN

@interface VideoViewFactory : NSObject<FlutterPlatformViewFactory>
- (instancetype)initWithMessenger:(NSObject<FlutterBinaryMessenger
>*)messenger;

@end

NS_ASSUME_NONNULL_END
