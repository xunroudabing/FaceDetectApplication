package com.hanzheng.facedetectapplication.api;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

//inner class(for little endian)
class byte_buffer{
	byte[] data;
	int cap,loff,roff;
	
	void check_size(int newsize) {
		if(newsize<cap) return;
		newsize=align2n(newsize);
		byte[] newdt=new byte[newsize];
		System.arraycopy(data, 0, newdt,0, cap);
		data=newdt;
		cap=newsize;
	}
	
	static int align2n(int sz) {
		int a=16;
		for(;a<sz;a<<=1);
		return a;
	}
	
	static void arrPutInt(byte[] arr,int off,int val) {
		arr[off+3]= (byte)((val>>24) & 0xFF);
		arr[off+2]= (byte)((val>>16) & 0xFF);
		arr[off+1]= (byte)((val>>8) & 0xFF);
		arr[off]= (byte)((val) & 0xFF);
	}
	
	static void arrPutLong(byte[] arr,int off,long val) {
		arr[off+7]= (byte)((val>>56) & 0xFF);
		arr[off+6]= (byte)((val>>48) & 0xFF);
		arr[off+5]= (byte)((val>>40) & 0xFF);
		arr[off+4]= (byte)((val>>32) & 0xFF);
		arr[off+3]= (byte)((val>>24) & 0xFF);
		arr[off+2]= (byte)((val>>16) & 0xFF);
		arr[off+1]= (byte)((val>>8) & 0xFF);
		arr[off]= (byte)((val) & 0xFF);
	}
	
	static void arrPutBytes(byte[] arr,int off,byte[] val) {
		System.arraycopy(val, 0, arr, off, val.length);
	}
	
	static final String def_charset="utf-8";
	public static final int INT_BYTES = 4;
	public static final int LONG_BYTES = 8;
//--------------------------	
	byte_buffer() {
		data=new byte[cap=32];
		loff=roff=0;
	}
	
	byte_buffer(int cap) {
		cap=align2n(cap);
		data=new byte[cap=32];
		loff=roff=0;
	}
	
	public int capacity() {
		return cap;
	}
	
	public int lpos() {
		return loff;
	}
	
	public int lset(int off) {
		int old = loff;
		if((loff=off)>roff) loff=roff;
		return old;
	}
	
	public int rset(int off) {
		int old = roff;
		if((roff=off)<loff) roff=loff;
		else if(roff>cap) roff=cap;
		return old;
	}
	
	public void bset(int off) {
		rset(off); lset(off);
	}
	
	public int data_size() {
		return roff-loff;
	}
	
	public byte[] raw_data() {
		return data;
	}
	
	public byte_buffer put(int val) {
		check_size(roff+INT_BYTES);
		arrPutInt(data, roff, val);
		roff+=INT_BYTES;
		return this;
	}
	
	public byte_buffer putraw(byte[] arr) {
		check_size(roff+arr.length);
		arrPutBytes(data, roff, arr);
		roff+=arr.length;
		return this;
	}
	
	public byte_buffer put(byte[] arr) {
		check_size(roff+4+arr.length);
		arrPutInt(data, roff, arr.length);
		roff+=INT_BYTES;
		arrPutBytes(data, roff, arr);
		roff+=arr.length;
		return this;
	}
	
	public byte_buffer put(String str) throws IOException {
		return put(str.getBytes(def_charset));
	}
	
	public byte_buffer put(float val) {
		return put(Float.floatToRawIntBits(val));
	}
	
	public byte_buffer put(long val) {
		check_size(roff+LONG_BYTES);
		arrPutLong(data, roff, val);
		roff+=LONG_BYTES;
		return this;
	}
	
	public static void wrap_outbb(byte_buffer bb) throws IllegalArgumentException {
		int lp=bb.lpos();
		if(lp<4) 
			throw new IllegalArgumentException("byte_buffer.lpos()<4!");
		int oblen=bb.data_size();
		bb.lset(lp-4);
		//bb.bb.putInt(lp-4,oblen);
		arrPutInt(bb.data, bb.loff, oblen);
	}
};


class CharSeqReader{
	byte[] arr;
	int off;
	
	public static int ba2int(byte[] arr,int off) {
		return ((arr[off+3]&0xFF)<<24)|((arr[off+2]&0xFF)<<16)|((arr[off+1]&0xFF)<<8)|(arr[off]&0xFF);
	}
	static long ba2long(byte[] arr,int off) {
		return ((arr[off+7]&0xFFL)<<56)|((arr[off+6]&0xFFL)<<48)|((arr[off+5]&0xFFL)<<40)|((arr[off+4]&0xFF)<<32)|
				((arr[off+3]&0xFFL)<<24)|((arr[off+2]&0xFFL)<<16)|((arr[off+1]&0xFFL)<<8)|((arr[off]&0xFF));
	}
	static float ba2float(byte[] arr,int off) {
		return Float.intBitsToFloat(ba2int(arr,off));
	}
	static double ba2double(byte[] arr,int off) {
		return Double.longBitsToDouble(ba2long(arr,off));
	}
//================
	CharSeqReader(byte[] ba) {
		arr=ba;
		off=0;
	}
	
	public int remaining() {
		return arr.length-off;
	}
	
	public int readInt() throws IOException {
		if(remaining()<4) throw new IOException("read int failed! left-byte<4!");
		int v=ba2int(arr,off);
		off+=byte_buffer.INT_BYTES;
		return v;
	}
	
	public byte[] readBytes(int sz) throws IOException {
		if(remaining()<sz) throw new IOException("read array[] failed! left-byte<"+sz);
		byte[] ba=new byte[sz];
		System.arraycopy(arr, off, ba, 0, sz);
		off+=sz;
		return ba;
	}
	
	public String readString(String charset) throws IOException {
		int slen=readInt();
		if(slen<0||slen>1000000000) throw new IOException("invalid string-length: "+slen);
		byte[] sarr=readBytes(slen);
		return new String(sarr,charset);
	}
	
	public String readString() throws IOException {
		return readString("utf-8");
	}
	
	public long readLong() throws IOException {
		if(remaining()<8) throw new IOException("read long failed! left-byte<8!");
		long v=ba2long(arr,off);
		off+=byte_buffer.LONG_BYTES;
		return v;
	}
	
	public float readFloat() throws IOException {
		return Float.intBitsToFloat(readInt());
	}
}


public class Api {
	static final int CMD_EXTRACT_FACE_FEATURE=1302;
	static final int CMD_PERSON_RECOGNIZE=1311;
	
	Socket ConnAndSock(InetSocketAddress addr) throws IOException {
		Socket sock=new Socket();
		sock.connect(addr, CONN_TIME_OUT);
		sock.setTcpNoDelay(true);
		if(RECV_TIME_OUT>0)
			sock.setSoTimeout(RECV_TIME_OUT);
		return sock;
	}
	
	static int readAllbytes(InputStream istm,byte[] ba) throws IOException {
		for(int off=0;off<ba.length;) {
			int rt=istm.read(ba,off,ba.length-off);
			if(rt<0) return off;
			off+=rt;
		}
		return ba.length;
	}
	
	static byte[] readStreamAll(InputStream istm) throws IOException {
		byte[] ba4=new byte[4];
		int rt=readAllbytes(istm, ba4);
		if(rt!=4) throw new IOException("read stream length failed!!");
		int len=CharSeqReader.ba2int(ba4, 0);
		//System.out.println("cmdlen="+len);
		byte[] ret=new byte[len];
		rt=readAllbytes(istm, ret);
		if(rt!=len) throw new IOException("read stream content failed!!");
		return ret;
	}
///------------------------------	
	//for config
	public int CONN_TIME_OUT=3000;
	public int RECV_TIME_OUT=0;
	InetSocketAddress addr;
	
	public Api(String hostname,int port) {
		addr=new InetSocketAddress(hostname, port);
	}
	
	public void setRemoteAddr(String hostname,int port) {
		addr=new InetSocketAddress(hostname, port);
	}
	
	private CharSeqReader _rt_cmd_comm(byte_buffer bb) throws IOException  {
		Socket sock=ConnAndSock(addr);
		CharSeqReader chr;
		try{
			//send
			sock.getOutputStream().write(bb.raw_data(), bb.lpos(), bb.data_size());
			//recv
			chr=new CharSeqReader(readStreamAll(sock.getInputStream()));
		}finally {
			sock.close();
		}
		int retc= chr.readInt();
		if(retc!=0) {
			String errstr=chr.readString();
			throw new IOException("remote-error:"+errstr);
		} 
		return chr;
	}


	/**
	 * 通过图像数据，在服务端提取（人脸）特征
	 * @param image_data 	图像编码文件数据（如jpeg，png流）
	 * @return 特征数据
	 * @throws IOException IO异常信息
	 */
	public float[] faceFeatureExtract(byte[] image_data) throws IOException {
		byte_buffer bb=new byte_buffer();
		bb.bset(8);
		bb.put(CMD_EXTRACT_FACE_FEATURE);
		bb.put("face-raw").put(image_data);
		byte_buffer.wrap_outbb(bb);
		CharSeqReader chrd=_rt_cmd_comm(bb);
		int nsz=chrd.readInt();
		float[] feature=new float[nsz/4];
		for(int i=0;i<feature.length;i++) {
			feature[i]=chrd.readFloat();
		}
		return feature;
	}
	
	/**
	 * �������ʶ��
	 * @param feature ��������
	 * @param device_id	�����豸ΨһID
	 * @return ���ʶ��json����
	 * @throws IOException IO�쳣��Ϣ
	 */
	public String personIdRecognize(float[] feature,String device_id) throws IOException {
		byte_buffer bb=new byte_buffer();
		bb.bset(8);
		bb.put(CMD_PERSON_RECOGNIZE);
		bb.put(feature.length*4);
		for(int i=0;i<feature.length;i++) {
			bb.put(feature[i]);
		}
		bb.put(device_id);
		byte_buffer.wrap_outbb(bb);
		CharSeqReader chrd=_rt_cmd_comm(bb);
		return chrd.readString();
	}
	
	
	//only support filesize<2GB!
	public static byte[] readFileToByteArray(String filename) throws IOException {
		FileInputStream fis=new FileInputStream(filename);
		try{
			byte[] bts=new byte[fis.available()];
			for(int off=0;off<bts.length;) {
				int rt=fis.read(bts,off,bts.length-off);
				if(rt<=0) throw new IOException("reach unexpected file end!");
				off+=rt;
			}
			return bts;
		}finally{
			fis.close();
		}
	}
}
