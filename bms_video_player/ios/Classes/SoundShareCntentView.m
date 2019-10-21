//
//  SoundShareCntentView.m
//  bms_video_player
//
//  Created by liuming on 2019/9/27.
//

#import "SoundShareCntentView.h"
#import <Masonry/Masonry.h>

@interface SoundShareCntentView ()

@property(nonatomic,strong) UIButton *soundButton;
@property (nonatomic, strong) SJVideoPlayer *player;

@property (nonatomic, assign) BOOL _currentMuted;

@end

@implementation SoundShareCntentView

-(id)initWithFrame:(CGRect)frame xvideoPlayer:(SJVideoPlayer *)xvideoPlayer
{
    if (self = [super initWithFrame:frame])
    {
        self.player = xvideoPlayer;
        
        self.soundButton = [UIButton buttonWithType:UIButtonTypeCustom];
        self.soundButton.tag = 0;
        
        [self.soundButton setImage:[UIImage imageNamed:@"ic-round-volume-up"] forState:UIControlStateNormal];
        [self.soundButton setImage:[UIImage imageNamed:@"ic-round-volume-off"] forState:UIControlStateSelected];
        [self.soundButton addTarget:self action:@selector(changeVolume:) forControlEvents:UIControlEventTouchUpInside];
         [self addSubview:self.soundButton];
        [self.soundButton mas_makeConstraints:^(MASConstraintMaker *make) {
            make.width.equalTo(@(24*[UIScreen mainScreen].bounds.size.width/375.0));
        make.height.equalTo(@(24*[UIScreen mainScreen].bounds.size.width/375.0));
            make.centerX.equalTo(self);
        make.bottom.equalTo(self.mas_bottom).offset(-57*[UIScreen mainScreen].bounds.size.width/375.0);
            
        }];
        
        
    }
    
    return self;
    
}


-(void)changeVolume:(UIButton *)btn{
    
    btn.selected = !btn.selected;
    if (btn.selected) {
        self.player.playerVolume = 0.0;
    }else{
        self.player.playerVolume = 1.0;
       
    }
}
-(void)setVolum:(float)volum{
    
    if (volum>0) {
        self.soundButton.selected = NO;
    }else{
         self.soundButton.selected = YES;
    }
   
}


@end
