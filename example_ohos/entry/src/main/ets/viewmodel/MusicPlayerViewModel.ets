// MusicPlayerViewModel.ets
// Music playback + watch linkage ViewModel

import { media } from '@kit.MediaKit';
import { Cmds, IDODeviceNotificationModel } from 'protocol_channel';
import { IDOMusicControlParamModel } from 'protocol_channel/src/main/ets/components/model/IDOMusicControlParamModel';
import { IDOMusicOnOffParamModel } from 'protocol_channel/src/main/ets/components/model/IDOMusicOnOffParamModel';
import { IDOBleVoiceParamModel } from 'protocol_channel/src/main/ets/components/model/IDOBleVoiceParamModel';
import BleManager, { AppDelegate } from '../bluetooth/BleManager';
import { common } from '@kit.AbilityKit';

// https://idoosmart.github.io/Native_GitBook/zh/doc/model/IDODeviceNotificationModel.html#05
// Control event constants
const CTRL_MUSIC_PLAY = 551;
const CTRL_MUSIC_PAUSE = 552;
const CTRL_MUSIC_STOP = 553;
const CTRL_MUSIC_PREV = 554;
const CTRL_MUSIC_NEXT = 555;
const CTRL_VOLUME_UP = 558;
const CTRL_VOLUME_DOWN = 559;
const CTRL_DEV_VOLUME_CHANGED = 565;

/** Song info */
export interface SongInfo {
  name: string;
  singer: string;
  rawFileName: string;
}

/** Playlist */
const PLAYLIST: SongInfo[] = [
  { name: 'Demo Song 1', singer: 'Artist A', rawFileName: 'demo_song_1.mp3' },
  { name: 'Demo Song 2', singer: 'Artist B', rawFileName: 'demo_song_2.mp3' },
  { name: 'Demo Song 3', singer: 'Artist C', rawFileName: 'demo_song_3.mp3' },
];

@ObservedV2
export class MusicPlayerViewModel {
  // UI state
  @Trace isPlaying: boolean = false;
  @Trace currentIndex: number = 0;
  @Trace currentSongName: string = PLAYLIST[0].name;
  @Trace currentSinger: string = PLAYLIST[0].singer;
  @Trace currentTimeMs: number = 0;
  @Trace totalTimeMs: number = 0;
  @Trace consoleLog: string = '';
  @Trace volume: number = 50;
  @Trace maxVolume: number = 15;

  private avPlayer: media.AVPlayer | null = null;
  private context: common.UIAbilityContext;
  private appDelegate: AppDelegate;
  private progressTimer: number = -1;
  private pendingPlay: boolean = false;
  private volumeDebounceTimer: number = -1;

  constructor(context: common.UIAbilityContext) {
    this.context = context;
    // Register the device notification listener
    this.appDelegate = {
      listenDeviceNotification: (model: IDODeviceNotificationModel): void => {
        this.onDeviceNotification(model);
      }
    };
    BleManager.registerBleDelegate(this.appDelegate);
    this.initVolume();
    this.initPlayer();
  }

  /** Initialize AVPlayer */
  private async initPlayer(): Promise<void> {
    try {
      console.info('[MusicVM] initPlayer start');
      this.avPlayer = await media.createAVPlayer();
      console.info('[MusicVM] AVPlayer created, state: ' + this.avPlayer.state);
      this.setupPlayerCallbacks();
      this.log('AVPlayer initialization succeeded');
      // Notify the watch that the music feature is on
      this.sendMusicOnOff(true);
      await this.loadCurrentSong();
    } catch (e) {
      console.error('[MusicVM] initPlayer error: ' + JSON.stringify(e));
      this.log(`AVPlayer initialization failed: ${e}`);
    }
  }

  /** Set up player callbacks */
  private setupPlayerCallbacks(): void {
    if (!this.avPlayer) return;

    this.avPlayer.on('stateChange', (state: string) => {
      console.info('[MusicVM] stateChange: ' + state + ', pendingPlay: ' + this.pendingPlay);
      this.log(`Player state: ${state}`);
      switch (state) {
        case 'playing':
          this.isPlaying = true;
          this.totalTimeMs = this.avPlayer?.duration ?? 0;
          this.startProgressTimer();
          this.syncToWatch();
          break;
        case 'paused':
          this.isPlaying = false;
          this.stopProgressTimer();
          this.syncToWatch();
          break;
        case 'stopped':
        case 'idle':
          console.info('[MusicVM] state idle/stopped, resetting');
          this.isPlaying = false;
          this.currentTimeMs = 0;
          this.stopProgressTimer();
          break;
        case 'completed':
          this.isPlaying = false;
          this.stopProgressTimer();
          this.playNext();
          break;
        case 'initialized':
          console.info('[MusicVM] initialized, calling prepare()');
          this.avPlayer?.prepare();
          break;
        case 'prepared':
          this.totalTimeMs = this.avPlayer?.duration ?? 0;
          console.info('[MusicVM] prepared, duration: ' + this.totalTimeMs + ', pendingPlay: ' + this.pendingPlay);
          this.log(`Prepared, duration: ${Math.floor(this.totalTimeMs / 1000)}s`);
          if (this.pendingPlay) {
            this.pendingPlay = false;
            console.info('[MusicVM] auto-play after prepared');
            this.play();
          }
          break;
        default:
          break;
      }
    });

    this.avPlayer.on('error', (err) => {
      console.error('[MusicVM] player error: ' + err.message);
      this.log(`Playback error: ${err.message}`);
      this.isPlaying = false;
    });
  }

  /** Load the current song */
  private async loadCurrentSong(): Promise<void> {
    if (!this.avPlayer) {
      console.error('[MusicVM] loadCurrentSong: avPlayer is null');
      return;
    }

    const song = PLAYLIST[this.currentIndex];
    this.currentSongName = song.name;
    this.currentSinger = song.singer;
    this.currentTimeMs = 0;

    try {
      console.info('[MusicVM] loadCurrentSong: resetting player, current state: ' + this.avPlayer.state);
      // Reset the player
      await this.avPlayer.reset();
      console.info('[MusicVM] reset done, state: ' + this.avPlayer.state);

      // Load from rawfile
      const resourceManager = this.context.resourceManager;
      const fd = await resourceManager.getRawFd(song.rawFileName);
      console.info('[MusicVM] getRawFd ok: fd=' + fd.fd + ', offset=' + fd.offset + ', length=' + fd.length);

      this.avPlayer.fdSrc = {
        fd: fd.fd,
        offset: fd.offset,
        length: fd.length,
      };
      console.info('[MusicVM] fdSrc set, waiting for initialized -> prepared');

      this.log(`Loading song: ${song.name} - ${song.singer}`);
    } catch (e) {
      console.error('[MusicVM] loadCurrentSong error: ' + JSON.stringify(e));
      this.log(`Failed to load song: ${e}`);
    }
  }

  // ======================== Playback control ========================

  /** Play */
  async play(): Promise<void> {
    if (!this.avPlayer) return;
    try {
      console.info('[MusicVM] play() called, current state: ' + this.avPlayer.state);
      await this.avPlayer.play();
      this.log('▶ Play');
    } catch (e) {
      console.error('[MusicVM] play() error: ' + JSON.stringify(e));
      this.log(`Failed to play: ${e}`);
    }
  }

  /** Pause */
  async pause(): Promise<void> {
    if (!this.avPlayer) return;
    try {
      await this.avPlayer.pause();
      this.log('⏸ Pause');
    } catch (e) {
      this.log(`Failed to pause: ${e}`);
    }
  }

  /** Toggle play/pause */
  async togglePlay(): Promise<void> {
    if (this.isPlaying) {
      await this.pause();
    } else {
      await this.play();
    }
  }

  /** Next song */
  async playNext(): Promise<void> {
    this.currentIndex = (this.currentIndex + 1) % PLAYLIST.length;
    this.pendingPlay = true;
    await this.loadCurrentSong();
    this.log('⏭ Next');
  }

  /** Previous song */
  async playPrev(): Promise<void> {
    this.currentIndex = (this.currentIndex - 1 + PLAYLIST.length) % PLAYLIST.length;
    this.pendingPlay = true;
    await this.loadCurrentSong();
    this.log('⏮ Previous');
  }

  /** Seek (progress drag) */
  async seekTo(timeMs: number): Promise<void> {
    if (!this.avPlayer) return;
    try {
      await this.avPlayer.seek(timeMs);
      this.currentTimeMs = timeMs;
    } catch (e) {
      this.log(`Seek failed: ${e}`);
    }
  }

  // ======================== Watch linkage ========================

  /** Sync the current playback state to the watch */
  private syncToWatch(): void {
    const status = this.isPlaying ? 1 : 2; // 1=play, 2=pause
    const curSec = Math.floor(this.currentTimeMs / 1000);
    const totalSec = Math.floor(this.totalTimeMs / 1000);

    const param = new IDOMusicControlParamModel(
      status,
      curSec,
      totalSec,
      this.currentSongName,
      this.currentSinger
    );

    Cmds.musicControl(param, (result) => {
      if (result.success) {
        this.log(`→ Watch sync: ${this.isPlaying ? 'playing' : 'paused'} ${this.currentSongName}`);
      }
    });
  }

  /** Set the watch music switch */
  private sendMusicOnOff(isOn: boolean): void {
    const param = new IDOMusicOnOffParamModel(isOn ? 1 : 0, isOn ? 1 : 0);
    Cmds.setMusicOnOff(param, (result) => {
      this.log(`→ Watch music ${isOn ? 'on' : 'off'}: ${result.success}`);
    });
  }

  /** Notify the watch of music start/stop */
  private sendMusicStartStop(start: boolean): void {
    if (start) {
      Cmds.musicStart((result) => {
        this.log(`→ Watch MusicStart: ${result.success}`);
      });
    } else {
      Cmds.musicStop((result) => {
        this.log(`→ Watch MusicStop: ${result.success}`);
      });
    }
  }

  /** Handle control events sent by the watch */
  private onDeviceNotification(model: IDODeviceNotificationModel): void {
    const evt = model.controlEvt;
    if (evt == null || evt === 0) return;

    this.log(`← Watch event: controlEvt=${evt}`);

    switch (evt) {
      case CTRL_MUSIC_PLAY:
        this.play();
        break;
      case CTRL_MUSIC_PAUSE:
        this.pause();
        break;
      case CTRL_MUSIC_STOP:
        this.pause(); // Treat stop as pause
        break;
      case CTRL_MUSIC_PREV:
        this.playPrev();
        break;
      case CTRL_MUSIC_NEXT:
        this.playNext();
        break;
      case CTRL_VOLUME_UP:
        this.volumeUp();
        break;
      case CTRL_VOLUME_DOWN:
        this.volumeDown();
        break;
      case CTRL_DEV_VOLUME_CHANGED:
        try {
          const json: Record<string, number> = JSON.parse(model.controlJson ?? '{}');
          const pct = json['volume_percentage'] ?? this.volume;
          this.log(`← Watch volume: ${pct}%`);
          this.setVolume(pct);
        } catch (e) {
          console.error('[MusicVM] parse volume json error: ' + e);
        }
        break;
      default:
        break;
    }
  }

  // ======================== Volume control ========================

  /** Initialize volume (AVPlayer native volume is 0~1) */
  private initVolume(): void {
    this.maxVolume = 100;
    this.volume = 100; // Full volume by default
    this.log(`Volume: ${this.volume}%`);
  }

  /** Volume+ */
  volumeUp(): void {
    this.setVolume(Math.min(this.volume + 10, this.maxVolume));
  }

  /** Volume- */
  volumeDown(): void {
    this.setVolume(Math.max(this.volume - 10, 0));
  }

  /** Set volume (0~100) */
  setVolume(val: number): void {
    this.volume = val;
    if (this.avPlayer) {
      this.avPlayer.setVolume(val / 100);
    }
    // Sync volume to the device
    // Debounce: wait 300ms after dragging stops before sending over BLE
    if (this.volumeDebounceTimer !== -1) {
      clearTimeout(this.volumeDebounceTimer);
    }
    this.volumeDebounceTimer = setTimeout(() => {
      this.volumeDebounceTimer = -1;
      const param = new IDOBleVoiceParamModel(this.maxVolume, val);
      Cmds.setBleVoice(param, (result) => {
        console.info(`[MusicVM] setBleVoice: ${result.success}`);
      });
    }, 300);
    console.info(`[MusicVM] setVolume: ${val}%`);
    this.log(`Volume: ${val}%`);
  }

  // ======================== Helper methods ========================

  /** Start the progress timer */
  private startProgressTimer(): void {
    this.stopProgressTimer();
    this.progressTimer = setInterval(() => {
      if (this.avPlayer && this.isPlaying) {
        this.currentTimeMs = this.avPlayer.currentTime ?? 0;
      }
    }, 1000);
  }

  /** Stop the progress timer */
  private stopProgressTimer(): void {
    if (this.progressTimer !== -1) {
      clearInterval(this.progressTimer);
      this.progressTimer = -1;
    }
  }

  /** Format time (ms -> mm:ss) */
  formatTime(ms: number): string {
    const totalSec = Math.floor(ms / 1000);
    const min = Math.floor(totalSec / 60);
    const sec = totalSec % 60;
    return `${min.toString().padStart(2, '0')}:${sec.toString().padStart(2, '0')}`;
  }

  /** Append a log line */
  private log(msg: string): void {
    const now = new Date();
    const time = `${now.getHours().toString().padStart(2, '0')}:${now.getMinutes().toString()
      .padStart(2, '0')}:${now.getSeconds().toString().padStart(2, '0')}`;
    this.consoleLog = `[${time}] ${msg}\n${this.consoleLog}`;
  }

  /** Release resources */
  async destroy(): Promise<void> {
    this.stopProgressTimer();
    BleManager.unregisterBleDelegate(this.appDelegate);
    // Notify the watch that music is off
    this.sendMusicOnOff(false);
    this.sendMusicStartStop(false);
    if (this.avPlayer) {
      await this.avPlayer.release();
      this.avPlayer = null;
    }
    this.log('Resources released');
  }

  /** Get the playlist */
  getPlaylist(): SongInfo[] {
    return PLAYLIST;
  }
}
