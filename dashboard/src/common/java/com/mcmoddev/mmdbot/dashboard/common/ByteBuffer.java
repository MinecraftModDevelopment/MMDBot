/*
 * MMDBot - https://github.com/MinecraftModDevelopment/MMDBot
 * Copyright (C) 2016-2022 <MMD - MinecraftModDevelopment>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
 * USA
 * https://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 */
package com.mcmoddev.mmdbot.dashboard.common;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import io.netty.util.ByteProcessor;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.ScatteringByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

// TODO rename all of the parameters
public class ByteBuffer extends ByteBuf {

    private final ByteBuf source;

    public ByteBuffer(final ByteBuf source) {
        this.source = source;
    }

    @Override
    public int capacity() {
        return this.source.capacity();
    }

    @Override
    public ByteBuf capacity(int p_130120_) {
        return this.source.capacity(p_130120_);
    }

    @Override
    public int maxCapacity() {
        return this.source.maxCapacity();
    }

    @Override
    public ByteBufAllocator alloc() {
        return this.source.alloc();
    }

    @Override
    public ByteOrder order() {
        return this.source.order();
    }

    @Override
    public ByteBuf order(ByteOrder p_130280_) {
        return this.source.order(p_130280_);
    }

    @Override
    public ByteBuf unwrap() {
        return this.source.unwrap();
    }

    @Override
    public boolean isDirect() {
        return this.source.isDirect();
    }

    @Override
    public boolean isReadOnly() {
        return this.source.isReadOnly();
    }

    @Override
    public ByteBuf asReadOnly() {
        return this.source.asReadOnly();
    }

    @Override
    public int readerIndex() {
        return this.source.readerIndex();
    }

    @Override
    public ByteBuf readerIndex(int p_130343_) {
        return this.source.readerIndex(p_130343_);
    }

    @Override
    public int writerIndex() {
        return this.source.writerIndex();
    }

    @Override
    public ByteBuf writerIndex(int p_130527_) {
        return this.source.writerIndex(p_130527_);
    }

    @Override
    public ByteBuf setIndex(int p_130417_, int p_130418_) {
        return this.source.setIndex(p_130417_, p_130418_);
    }

    @Override
    public int readableBytes() {
        return this.source.readableBytes();
    }

    @Override
    public int writableBytes() {
        return this.source.writableBytes();
    }

    @Override
    public int maxWritableBytes() {
        return this.source.maxWritableBytes();
    }

    @Override
    public boolean isReadable() {
        return this.source.isReadable();
    }

    @Override
    public boolean isReadable(int p_130254_) {
        return this.source.isReadable(p_130254_);
    }

    @Override
    public boolean isWritable() {
        return this.source.isWritable();
    }

    @Override
    public boolean isWritable(int p_130257_) {
        return this.source.isWritable(p_130257_);
    }

    @Override
    public ByteBuf clear() {
        return this.source.clear();
    }

    @Override
    public ByteBuf markReaderIndex() {
        return this.source.markReaderIndex();
    }

    @Override
    public ByteBuf resetReaderIndex() {
        return this.source.resetReaderIndex();
    }

    @Override
    public ByteBuf markWriterIndex() {
        return this.source.markWriterIndex();
    }

    @Override
    public ByteBuf resetWriterIndex() {
        return this.source.resetWriterIndex();
    }

    @Override
    public ByteBuf discardReadBytes() {
        return this.source.discardReadBytes();
    }

    @Override
    public ByteBuf discardSomeReadBytes() {
        return this.source.discardSomeReadBytes();
    }

    @Override
    public ByteBuf ensureWritable(int p_130139_) {
        return this.source.ensureWritable(p_130139_);
    }

    @Override
    public int ensureWritable(int p_130141_, boolean p_130142_) {
        return this.source.ensureWritable(p_130141_, p_130142_);
    }

    @Override
    public boolean getBoolean(int p_130159_) {
        return this.source.getBoolean(p_130159_);
    }

    @Override
    public byte getByte(int p_130161_) {
        return this.source.getByte(p_130161_);
    }

    @Override
    public short getUnsignedByte(int p_130225_) {
        return this.source.getUnsignedByte(p_130225_);
    }

    @Override
    public short getShort(int p_130221_) {
        return this.source.getShort(p_130221_);
    }

    @Override
    public short getShortLE(int p_130223_) {
        return this.source.getShortLE(p_130223_);
    }

    @Override
    public int getUnsignedShort(int p_130235_) {
        return this.source.getUnsignedShort(p_130235_);
    }

    @Override
    public int getUnsignedShortLE(int p_130237_) {
        return this.source.getUnsignedShortLE(p_130237_);
    }

    @Override
    public int getMedium(int p_130217_) {
        return this.source.getMedium(p_130217_);
    }

    @Override
    public int getMediumLE(int p_130219_) {
        return this.source.getMediumLE(p_130219_);
    }

    @Override
    public int getUnsignedMedium(int p_130231_) {
        return this.source.getUnsignedMedium(p_130231_);
    }

    @Override
    public int getUnsignedMediumLE(int p_130233_) {
        return this.source.getUnsignedMediumLE(p_130233_);
    }

    @Override
    public int getInt(int p_130209_) {
        return this.source.getInt(p_130209_);
    }

    @Override
    public int getIntLE(int p_130211_) {
        return this.source.getIntLE(p_130211_);
    }

    @Override
    public long getUnsignedInt(int p_130227_) {
        return this.source.getUnsignedInt(p_130227_);
    }

    @Override
    public long getUnsignedIntLE(int p_130229_) {
        return this.source.getUnsignedIntLE(p_130229_);
    }

    @Override
    public long getLong(int p_130213_) {
        return this.source.getLong(p_130213_);
    }

    @Override
    public long getLongLE(int p_130215_) {
        return this.source.getLongLE(p_130215_);
    }

    @Override
    public char getChar(int p_130199_) {
        return this.source.getChar(p_130199_);
    }

    @Override
    public float getFloat(int p_130207_) {
        return this.source.getFloat(p_130207_);
    }

    @Override
    public double getDouble(int p_130205_) {
        return this.source.getDouble(p_130205_);
    }

    @Override
    public ByteBuf getBytes(int p_130163_, ByteBuf p_130164_) {
        return this.source.getBytes(p_130163_, p_130164_);
    }

    @Override
    public ByteBuf getBytes(int p_130166_, ByteBuf p_130167_, int p_130168_) {
        return this.source.getBytes(p_130166_, p_130167_, p_130168_);
    }

    @Override
    public ByteBuf getBytes(int p_130170_, ByteBuf p_130171_, int p_130172_, int p_130173_) {
        return this.source.getBytes(p_130170_, p_130171_, p_130172_, p_130173_);
    }

    @Override
    public ByteBuf getBytes(int p_130191_, byte[] p_130192_) {
        return this.source.getBytes(p_130191_, p_130192_);
    }

    @Override
    public ByteBuf getBytes(int p_130194_, byte[] p_130195_, int p_130196_, int p_130197_) {
        return this.source.getBytes(p_130194_, p_130195_, p_130196_, p_130197_);
    }

    @Override
    public ByteBuf getBytes(final int index, final java.nio.ByteBuffer dst) {
        return this.source.getBytes(index, dst);
    }

    @Override
    public ByteBuf getBytes(int p_130175_, OutputStream p_130176_, int p_130177_) throws IOException {
        return this.source.getBytes(p_130175_, p_130176_, p_130177_);
    }

    @Override
    public int getBytes(int p_130187_, GatheringByteChannel p_130188_, int p_130189_) throws IOException {
        return this.source.getBytes(p_130187_, p_130188_, p_130189_);
    }

    @Override
    public int getBytes(int p_130182_, FileChannel p_130183_, long p_130184_, int p_130185_) throws IOException {
        return this.source.getBytes(p_130182_, p_130183_, p_130184_, p_130185_);
    }

    @Override
    public CharSequence getCharSequence(int p_130201_, int p_130202_, Charset p_130203_) {
        return this.source.getCharSequence(p_130201_, p_130202_, p_130203_);
    }

    @Override
    public ByteBuf setBoolean(int p_130362_, boolean p_130363_) {
        return this.source.setBoolean(p_130362_, p_130363_);
    }

    @Override
    public ByteBuf setByte(int p_130365_, int p_130366_) {
        return this.source.setByte(p_130365_, p_130366_);
    }

    @Override
    public ByteBuf setShort(int p_130438_, int p_130439_) {
        return this.source.setShort(p_130438_, p_130439_);
    }

    @Override
    public ByteBuf setShortLE(int p_130441_, int p_130442_) {
        return this.source.setShortLE(p_130441_, p_130442_);
    }

    @Override
    public ByteBuf setMedium(int p_130432_, int p_130433_) {
        return this.source.setMedium(p_130432_, p_130433_);
    }

    @Override
    public ByteBuf setMediumLE(int p_130435_, int p_130436_) {
        return this.source.setMediumLE(p_130435_, p_130436_);
    }

    @Override
    public ByteBuf setInt(int p_130420_, int p_130421_) {
        return this.source.setInt(p_130420_, p_130421_);
    }

    @Override
    public ByteBuf setIntLE(int p_130423_, int p_130424_) {
        return this.source.setIntLE(p_130423_, p_130424_);
    }

    @Override
    public ByteBuf setLong(int p_130426_, long p_130427_) {
        return this.source.setLong(p_130426_, p_130427_);
    }

    @Override
    public ByteBuf setLongLE(int p_130429_, long p_130430_) {
        return this.source.setLongLE(p_130429_, p_130430_);
    }

    @Override
    public ByteBuf setChar(int p_130404_, int p_130405_) {
        return this.source.setChar(p_130404_, p_130405_);
    }

    @Override
    public ByteBuf setFloat(int p_130414_, float p_130415_) {
        return this.source.setFloat(p_130414_, p_130415_);
    }

    @Override
    public ByteBuf setDouble(int p_130411_, double p_130412_) {
        return this.source.setDouble(p_130411_, p_130412_);
    }

    @Override
    public ByteBuf setBytes(int p_130368_, ByteBuf p_130369_) {
        return this.source.setBytes(p_130368_, p_130369_);
    }

    @Override
    public ByteBuf setBytes(int p_130371_, ByteBuf p_130372_, int p_130373_) {
        return this.source.setBytes(p_130371_, p_130372_, p_130373_);
    }

    @Override
    public ByteBuf setBytes(int p_130375_, ByteBuf p_130376_, int p_130377_, int p_130378_) {
        return this.source.setBytes(p_130375_, p_130376_, p_130377_, p_130378_);
    }

    @Override
    public ByteBuf setBytes(int p_130396_, byte[] p_130397_) {
        return this.source.setBytes(p_130396_, p_130397_);
    }

    @Override
    public ByteBuf setBytes(int p_130399_, byte[] p_130400_, int p_130401_, int p_130402_) {
        return this.source.setBytes(p_130399_, p_130400_, p_130401_, p_130402_);
    }

    @Override
    public ByteBuf setBytes(final int index, final java.nio.ByteBuffer src) {
        return source.setBytes(index, src);
    }

    @Override
    public int setBytes(int p_130380_, InputStream p_130381_, int p_130382_) throws IOException {
        return this.source.setBytes(p_130380_, p_130381_, p_130382_);
    }

    @Override
    public int setBytes(int p_130392_, ScatteringByteChannel p_130393_, int p_130394_) throws IOException {
        return this.source.setBytes(p_130392_, p_130393_, p_130394_);
    }

    @Override
    public int setBytes(int p_130387_, FileChannel p_130388_, long p_130389_, int p_130390_) throws IOException {
        return this.source.setBytes(p_130387_, p_130388_, p_130389_, p_130390_);
    }

    @Override
    public ByteBuf setZero(int p_130444_, int p_130445_) {
        return this.source.setZero(p_130444_, p_130445_);
    }

    @Override
    public int setCharSequence(int p_130407_, CharSequence p_130408_, Charset p_130409_) {
        return this.source.setCharSequence(p_130407_, p_130408_, p_130409_);
    }

    @Override
    public boolean readBoolean() {
        return this.source.readBoolean();
    }

    @Override
    public byte readByte() {
        return this.source.readByte();
    }

    @Override
    public short readUnsignedByte() {
        return this.source.readUnsignedByte();
    }

    @Override
    public short readShort() {
        return this.source.readShort();
    }

    @Override
    public short readShortLE() {
        return this.source.readShortLE();
    }

    @Override
    public int readUnsignedShort() {
        return this.source.readUnsignedShort();
    }

    @Override
    public int readUnsignedShortLE() {
        return this.source.readUnsignedShortLE();
    }

    @Override
    public int readMedium() {
        return this.source.readMedium();
    }

    @Override
    public int readMediumLE() {
        return this.source.readMediumLE();
    }

    @Override
    public int readUnsignedMedium() {
        return this.source.readUnsignedMedium();
    }

    @Override
    public int readUnsignedMediumLE() {
        return this.source.readUnsignedMediumLE();
    }

    @Override
    public int readInt() {
        return this.source.readInt();
    }

    @Override
    public int readIntLE() {
        return this.source.readIntLE();
    }

    @Override
    public long readUnsignedInt() {
        return this.source.readUnsignedInt();
    }

    @Override
    public long readUnsignedIntLE() {
        return this.source.readUnsignedIntLE();
    }

    @Override
    public long readLong() {
        return this.source.readLong();
    }

    @Override
    public long readLongLE() {
        return this.source.readLongLE();
    }

    @Override
    public char readChar() {
        return this.source.readChar();
    }

    @Override
    public float readFloat() {
        return this.source.readFloat();
    }

    @Override
    public double readDouble() {
        return this.source.readDouble();
    }

    @Override
    public ByteBuf readBytes(int p_130287_) {
        return this.source.readBytes(p_130287_);
    }

    @Override
    public ByteBuf readSlice(int p_130332_) {
        return this.source.readSlice(p_130332_);
    }

    @Override
    public ByteBuf readRetainedSlice(int p_130328_) {
        return this.source.readRetainedSlice(p_130328_);
    }

    @Override
    public ByteBuf readBytes(ByteBuf p_130289_) {
        return this.source.readBytes(p_130289_);
    }

    @Override
    public ByteBuf readBytes(ByteBuf p_130291_, int p_130292_) {
        return this.source.readBytes(p_130291_, p_130292_);
    }

    @Override
    public ByteBuf readBytes(ByteBuf p_130294_, int p_130295_, int p_130296_) {
        return this.source.readBytes(p_130294_, p_130295_, p_130296_);
    }

    @Override
    public ByteBuf readBytes(byte[] p_130310_) {
        return this.source.readBytes(p_130310_);
    }

    @Override
    public ByteBuf readBytes(byte[] p_130312_, int p_130313_, int p_130314_) {
        return this.source.readBytes(p_130312_, p_130313_, p_130314_);
    }

    @Override
    public ByteBuf readBytes(final java.nio.ByteBuffer dst) {
        return source.readBytes(dst);
    }

    @Override
    public ByteBuf readBytes(OutputStream p_130298_, int p_130299_) throws IOException {
        return this.source.readBytes(p_130298_, p_130299_);
    }

    @Override
    public int readBytes(GatheringByteChannel p_130307_, int p_130308_) throws IOException {
        return this.source.readBytes(p_130307_, p_130308_);
    }

    @Override
    public CharSequence readCharSequence(int p_130317_, Charset p_130318_) {
        return this.source.readCharSequence(p_130317_, p_130318_);
    }

    @Override
    public int readBytes(FileChannel p_130303_, long p_130304_, int p_130305_) throws IOException {
        return this.source.readBytes(p_130303_, p_130304_, p_130305_);
    }

    @Override
    public ByteBuf skipBytes(int p_130447_) {
        return this.source.skipBytes(p_130447_);
    }

    @Override
    public ByteBuf writeBoolean(boolean p_130468_) {
        return this.source.writeBoolean(p_130468_);
    }

    @Override
    public ByteBuf writeByte(int p_130470_) {
        return this.source.writeByte(p_130470_);
    }

    @Override
    public ByteBuf writeShort(int p_130520_) {
        return this.source.writeShort(p_130520_);
    }

    @Override
    public ByteBuf writeShortLE(int p_130522_) {
        return this.source.writeShortLE(p_130522_);
    }

    @Override
    public ByteBuf writeMedium(int p_130516_) {
        return this.source.writeMedium(p_130516_);
    }

    @Override
    public ByteBuf writeMediumLE(int p_130518_) {
        return this.source.writeMediumLE(p_130518_);
    }

    @Override
    public ByteBuf writeInt(int p_130508_) {
        return this.source.writeInt(p_130508_);
    }

    @Override
    public ByteBuf writeIntLE(int p_130510_) {
        return this.source.writeIntLE(p_130510_);
    }

    @Override
    public ByteBuf writeLong(long p_130512_) {
        return this.source.writeLong(p_130512_);
    }

    @Override
    public ByteBuf writeLongLE(long p_130514_) {
        return this.source.writeLongLE(p_130514_);
    }

    @Override
    public ByteBuf writeChar(int p_130499_) {
        return this.source.writeChar(p_130499_);
    }

    @Override
    public ByteBuf writeFloat(float p_130506_) {
        return this.source.writeFloat(p_130506_);
    }

    @Override
    public ByteBuf writeDouble(double p_130504_) {
        return this.source.writeDouble(p_130504_);
    }

    @Override
    public ByteBuf writeBytes(ByteBuf p_130472_) {
        return this.source.writeBytes(p_130472_);
    }

    @Override
    public ByteBuf writeBytes(ByteBuf p_130474_, int p_130475_) {
        return this.source.writeBytes(p_130474_, p_130475_);
    }

    @Override
    public ByteBuf writeBytes(ByteBuf p_130477_, int p_130478_, int p_130479_) {
        return this.source.writeBytes(p_130477_, p_130478_, p_130479_);
    }

    @Override
    public ByteBuf writeBytes(byte[] p_130493_) {
        return this.source.writeBytes(p_130493_);
    }

    @Override
    public ByteBuf writeBytes(byte[] p_130495_, int p_130496_, int p_130497_) {
        return this.source.writeBytes(p_130495_, p_130496_, p_130497_);
    }

    @Override
    public ByteBuf writeBytes(final java.nio.ByteBuffer src) {
        return source.writeBytes(src);
    }

    @Override
    public int writeBytes(InputStream p_130481_, int p_130482_) throws IOException {
        return this.source.writeBytes(p_130481_, p_130482_);
    }

    @Override
    public int writeBytes(ScatteringByteChannel p_130490_, int p_130491_) throws IOException {
        return this.source.writeBytes(p_130490_, p_130491_);
    }

    @Override
    public int writeBytes(FileChannel p_130486_, long p_130487_, int p_130488_) throws IOException {
        return this.source.writeBytes(p_130486_, p_130487_, p_130488_);
    }

    @Override
    public ByteBuf writeZero(int p_130524_) {
        return this.source.writeZero(p_130524_);
    }

    @Override
    public int writeCharSequence(CharSequence p_130501_, Charset p_130502_) {
        return this.source.writeCharSequence(p_130501_, p_130502_);
    }

    @Override
    public int indexOf(int p_130244_, int p_130245_, byte p_130246_) {
        return this.source.indexOf(p_130244_, p_130245_, p_130246_);
    }

    @Override
    public int bytesBefore(byte p_130108_) {
        return this.source.bytesBefore(p_130108_);
    }

    @Override
    public int bytesBefore(int p_130110_, byte p_130111_) {
        return this.source.bytesBefore(p_130110_, p_130111_);
    }

    @Override
    public int bytesBefore(int p_130113_, int p_130114_, byte p_130115_) {
        return this.source.bytesBefore(p_130113_, p_130114_, p_130115_);
    }

    @Override
    public int forEachByte(ByteProcessor p_130150_) {
        return this.source.forEachByte(p_130150_);
    }

    @Override
    public int forEachByte(int p_130146_, int p_130147_, ByteProcessor p_130148_) {
        return this.source.forEachByte(p_130146_, p_130147_, p_130148_);
    }

    @Override
    public int forEachByteDesc(ByteProcessor p_130156_) {
        return this.source.forEachByteDesc(p_130156_);
    }

    @Override
    public int forEachByteDesc(int p_130152_, int p_130153_, ByteProcessor p_130154_) {
        return this.source.forEachByteDesc(p_130152_, p_130153_, p_130154_);
    }

    @Override
    public ByteBuf copy() {
        return this.source.copy();
    }

    @Override
    public ByteBuf copy(int p_130128_, int p_130129_) {
        return this.source.copy(p_130128_, p_130129_);
    }

    @Override
    public ByteBuf slice() {
        return this.source.slice();
    }

    @Override
    public ByteBuf retainedSlice() {
        return this.source.retainedSlice();
    }

    @Override
    public ByteBuf slice(int p_130450_, int p_130451_) {
        return this.source.slice(p_130450_, p_130451_);
    }

    @Override
    public ByteBuf retainedSlice(int p_130359_, int p_130360_) {
        return this.source.retainedSlice(p_130359_, p_130360_);
    }

    @Override
    public ByteBuf duplicate() {
        return this.source.duplicate();
    }

    @Override
    public ByteBuf retainedDuplicate() {
        return this.source.retainedDuplicate();
    }

    @Override
    public int nioBufferCount() {
        return this.source.nioBufferCount();
    }

    @Override
    public java.nio.ByteBuffer nioBuffer() {
        return this.source.nioBuffer();
    }

    @Override
    public java.nio.ByteBuffer nioBuffer(int p_130270_, int p_130271_) {
        return this.source.nioBuffer(p_130270_, p_130271_);
    }

    @Override
    public java.nio.ByteBuffer internalNioBuffer(int p_130248_, int p_130249_) {
        return this.source.internalNioBuffer(p_130248_, p_130249_);
    }

    @Override
    public java.nio.ByteBuffer[] nioBuffers() {
        return this.source.nioBuffers();
    }

    @Override
    public java.nio.ByteBuffer[] nioBuffers(int p_130275_, int p_130276_) {
        return this.source.nioBuffers(p_130275_, p_130276_);
    }

    @Override
    public boolean hasArray() {
        return this.source.hasArray();
    }

    @Override
    public byte[] array() {
        return this.source.array();
    }

    @Override
    public int arrayOffset() {
        return this.source.arrayOffset();
    }

    @Override
    public boolean hasMemoryAddress() {
        return this.source.hasMemoryAddress();
    }

    @Override
    public long memoryAddress() {
        return this.source.memoryAddress();
    }

    @Override
    public String toString(Charset p_130458_) {
        return this.source.toString(p_130458_);
    }

    @Override
    public String toString(int p_130454_, int p_130455_, Charset p_130456_) {
        return this.source.toString(p_130454_, p_130455_, p_130456_);
    }

    public int hashCode() {
        return this.source.hashCode();
    }

    public boolean equals(Object p_130144_) {
        return this.source.equals(p_130144_);
    }

    @Override
    public int compareTo(ByteBuf p_130123_) {
        return this.source.compareTo(p_130123_);
    }

    public String toString() {
        return this.source.toString();
    }

    @Override
    public ByteBuf retain(int p_130353_) {
        return this.source.retain(p_130353_);
    }

    @Override
    public ByteBuf retain() {
        return this.source.retain();
    }

    @Override
    public ByteBuf touch() {
        return this.source.touch();
    }

    @Override
    public ByteBuf touch(Object p_130462_) {
        return this.source.touch(p_130462_);
    }

    @Override
    public int refCnt() {
        return this.source.refCnt();
    }

    @Override
    public boolean release() {
        return this.source.release();
    }

    @Override
    public boolean release(int p_130347_) {
        return this.source.release(p_130347_);
    }

    public int readVarInt() {
        int i = 0;
        int j = 0;

        byte b0;
        do {
            b0 = this.readByte();
            i |= (b0 & 127) << j++ * 7;
            if (j > 5) {
                throw new RuntimeException("VarInt too big");
            }
        } while ((b0 & 128) == 128);

        return i;
    }

    public ByteBuffer writeVarInt(int value) {
        while((value & -128) != 0) {
            this.writeByte(value & 127 | 128);
            value >>>= 7;
        }

        this.writeByte(value);
        return this;
    }

    /**
     * Calculates the number of bytes ({@code [1-5]}) required to fit the supplied int if it were to be read/written
     * using readVarInt/writeVarInt
     */
    public static int getVarIntSize(int pInput) {
        for(int i = 1; i < 5; ++i) {
            if ((pInput & -1 << i * 7) == 0) {
                return i;
            }
        }

        return 5;
    }

    /**
     * Reads a string from the buffer with a maximum length of {@code Short.MAX_VALUE}.
     *
     * @see #readUtf(int)
     * @see #writeUtf
     */
    public String readUtf() {
        return readUtf(32767);
    }

    /**
     * Reads a string from the buffer with a maximum length from this buffer.
     *
     * @see #writeUtf
     */
    public String readUtf(int maxLength) {
        int i = this.readVarInt();
        if (i > maxLength * 4) {
            throw new DecoderException("The received encoded string buffer length is longer than maximum allowed (" + i + " > " + maxLength * 4 + ")");
        } else if (i < 0) {
            throw new DecoderException("The received encoded string buffer length is less than zero! Weird string!");
        } else {
            String s = this.toString(this.readerIndex(), i, StandardCharsets.UTF_8);
            this.readerIndex(this.readerIndex() + i);
            if (s.length() > maxLength) {
                throw new DecoderException("The received string length is longer than maximum allowed (" + i + " > " + maxLength + ")");
            } else {
                return s;
            }
        }
    }

    /**
     * Writes a string to the buffer with a maximum length of {@code Short.MAX_VALUE}.
     *
     * @see #readUtf
     */
    public ByteBuf writeUtf(String str) {
        return writeUtf(str, 32767);
    }

    /**
     * Writes a string to the buffer with a maximum length.
     *
     * @see #readUtf
     */
    public ByteBuffer writeUtf(String str, int maxLength) {
        byte[] abyte = str.getBytes(StandardCharsets.UTF_8);
        if (abyte.length > maxLength) {
            throw new EncoderException("String too big (was " + abyte.length + " bytes encoded, max " + maxLength + ")");
        } else {
            this.writeVarInt(abyte.length);
            this.writeBytes(abyte);
            return this;
        }
    }

    /**
     * Writes an enum of the given type to the buffer
     * using the ordinal encoded.
     *
     * @see #readEnum
     */
    public ByteBuffer writeEnum(Enum<?> value) {
        return writeVarInt(value.ordinal());
    }

    /**
     * Reads an enum of the given type {@code T} from the buffer
     * using the ordinal encoded.
     *
     * @see #writeEnum
     */
    public <T extends Enum<T>> T readEnum(Class<T> enumClass) {
        return (enumClass.getEnumConstants())[this.readVarInt()];
    }

}
