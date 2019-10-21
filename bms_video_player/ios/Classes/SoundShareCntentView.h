//
//  SoundShareCntentView.h
//  bms_video_player
//
//  Created by liuming on 2019/9/27.
//

#import <UIKit/UIKit.h>
#import <SJVideoPlayer/SJVideoPlayer.h>
NS_ASSUME_NONNULL_BEGIN

@interface SoundShareCntentView : UIView

@property (nonatomic ,assign) float volum;

-(id)initWithFrame:(CGRect)frame xvideoPlayer:(SJVideoPlayer *)xvideoPlayer;


@end

NS_ASSUME_NONNULL_END
