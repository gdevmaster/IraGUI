package com.iragui.objects;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import static org.lwjgl.openal.AL10.*;

import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.stb.STBVorbis.*;
import static org.lwjgl.system.libc.LibCStdlib.free;

public class Sound implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -6697134496838567975L;
	private int bufferId;
	private int sourceId;
	private boolean isPlaying = false;
	private boolean playOnStart;
	private boolean loops;
	
	public Sound(ByteBuffer bytes,boolean loops, boolean playOnStart, String name) {
		this.playOnStart=playOnStart;
		
		stackPush();
		IntBuffer channelsBuffer = stackMallocInt(1);
		stackPush();
		IntBuffer sampleRateBuffer = stackMallocInt(1);
		
		ShortBuffer rawAudioBuffer = stb_vorbis_decode_memory(bytes,channelsBuffer,sampleRateBuffer);
		
		
		if(rawAudioBuffer == null) {
			System.out.println("Could not load sound '"+name+"'");
			stackPop();
			stackPop();
			return;
		}
		
		
		int channels = channelsBuffer.get();
		int sampleRate = sampleRateBuffer.get();
		
		stackPop();
		stackPop();
		
		int format = -1;
		
		if(channels==1) {
			format = AL_FORMAT_MONO16;
		} else if (channels==2) {
			format = AL_FORMAT_STEREO16;
		}
		
		bufferId = alGenBuffers();
		alBufferData(bufferId,format,rawAudioBuffer,sampleRate);
		
		sourceId = alGenSources();
		
		alSourcei(sourceId,AL_BUFFER,bufferId);
		alSourcei(sourceId,AL_LOOPING,loops ? 1 : 0);
		alSourcei(sourceId,AL_POSITION,0);
		alSourcef(sourceId,AL_GAIN,1.0f);
		
		free(rawAudioBuffer);
	}
	
	public boolean loops() {
		return this.loops;
	}
	
	public void delete() {
		alDeleteSources(sourceId);
		alDeleteBuffers(bufferId);
	}
	
	public void stopAndPlay() {
		stop();
		int state = alGetSourcei(sourceId,AL_SOURCE_STATE);
		if(state == AL_STOPPED) {
			isPlaying=false;
			alSourcei(sourceId,AL_POSITION,0);
		}
		
		if(!isPlaying) {
			alSourcePlay(sourceId);
			isPlaying=true;
		}
	}
	
	public void play() {
		int state = alGetSourcei(sourceId,AL_SOURCE_STATE);
		if(state == AL_STOPPED) {
			isPlaying=false;
			alSourcei(sourceId,AL_POSITION,0);
		}
		
		if(!isPlaying) {
			alSourcePlay(sourceId);
			isPlaying=true;
		}
	}
	
	
	public void stop() {
		if(isPlaying) {
			alSourceStop(sourceId);
			isPlaying=false;
		}
	}
	
	public void stopAtNextLoop() {
		if(isPlaying) {
			alSourcei(sourceId,AL_LOOPING,0);
		}
	}
	
	public void playForceLoop() {
		alSourcei(sourceId,AL_LOOPING,1);
		this.play();
	}
	
	public boolean playOnStart() {
		return playOnStart;
	}
	
	public boolean isPlaying() {
		int state = alGetSourcei(sourceId,AL_SOURCE_STATE);
		if(state == AL_STOPPED) {
			isPlaying=false;
		}
		return isPlaying;
	}

	public void pause() {
		stop();
	}

	public void resume() {
		if(!isPlaying) {
			alSourcePlay(sourceId);
			isPlaying=true;
		}
	}
	
}
