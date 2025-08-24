package com.iragui.objects;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import static org.lwjgl.openal.AL10.*;

import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.stb.STBVorbis.*;
import static org.lwjgl.system.libc.LibCStdlib.free;

/**
 * Represents a sound resource loaded from a {@link ByteBuffer}.
 * 
 * <p>This class wraps OpenAL buffer and source handling, making it easy to 
 * load, play, stop, and manage sounds (such as music or sound effects). 
 * Sounds can be configured to loop and optionally start playing immediately.</p>
 *
 * <p>Remember to call {@link #delete()} when you are finished with the sound to free
 * OpenAL resources.</p>
 */
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
	
	/**
     * Loads a new sound from Vorbis-encoded data.
     *
     * @param bytes        The raw audio data in Vorbis format.
     * @param loops        Whether this sound should loop when played.
     * @param playOnStart  Whether this sound should start playing automatically when created.
     * @param name         The name of the sound (used in error messages).
     */
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
	
	/**
     * @return whether this sound is configured to loop.
     */
	public boolean loops() {
		return this.loops;
	}
	
	/**
     * Deletes this sound and frees its OpenAL buffer and source.
     * <p>Must be called when the sound is no longer needed.</p>
     */
	public void delete() {
		alDeleteSources(sourceId);
		alDeleteBuffers(bufferId);
	}
	
	/**
     * Stops the sound if it's playing, then plays it again from the start.
     */
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
	
	/**
     * Plays the sound if it is not already playing.
     * If the sound was stopped, it restarts from the beginning.
     */
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
	
	/**
     * Stops the sound immediately if it is playing.
     */
	public void stop() {
		if(isPlaying) {
			alSourceStop(sourceId);
			isPlaying=false;
		}
	}
	
	 /**
     * Allows the sound to finish its current loop, then stop.
     */
	public void stopAtNextLoop() {
		if(isPlaying) {
			alSourcei(sourceId,AL_LOOPING,0);
		}
	}
	
	/**
     * Forces the sound to loop and starts playing.
     */
	public void playForceLoop() {
		alSourcei(sourceId,AL_LOOPING,1);
		this.play();
	}
	
	 /**
     * @return whether this sound should be flagged as playOnStart.
     */
	public boolean playOnStart() {
		return playOnStart;
	}
	
	/**
     * @return whether this sound is currently playing.
     */
	public boolean isPlaying() {
		int state = alGetSourcei(sourceId,AL_SOURCE_STATE);
		if(state == AL_STOPPED) {
			isPlaying=false;
		}
		return isPlaying;
	}

	/**
     * Pauses the sound (implemented by stopping playback).
     */
	public void pause() {
		stop();
	}

	 /**
     * Resumes the sound if it was paused or stopped.
     */
	public void resume() {
		if(!isPlaying) {
			alSourcePlay(sourceId);
			isPlaying=true;
		}
	}
	
}
