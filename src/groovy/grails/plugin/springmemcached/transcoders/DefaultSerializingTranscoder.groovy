package grails.plugin.springmemcached.transcoders

import net.spy.memcached.transcoders.SerializingTranscoder
import net.spy.memcached.CachedData;
import net.spy.memcached.compat.CloseUtil;
import net.spy.memcached.compat.SpyObject;

public class DefaultSerializingTranscoder extends SerializingTranscoder {

	def classLoader

	public DefaultSerializingTranscoder() {
		super()
	}
	public DefaultSerializingTranscoder(classLoader) {
		super()
		this.classLoader = classLoader
	}

	protected Object deserialize(byte[] inBytes) {
		Object rv=null;
		ByteArrayInputStream bis = null;
		ObjectInputStream is = null;
		try {
			if(inBytes != null) {
				bis=new ByteArrayInputStream(inBytes);
				//is=new ObjectInputStream(bis);
				is = bis.newObjectInputStream(classLoader);
				rv=is.readObject();
				is.close();
				bis.close();
			}
		} catch (IOException e) {
			getLogger().warn("Caught IOException decoding %d bytes of data",
							inBytes == null ? 0 : inBytes.length, e);
		} catch (ClassNotFoundException e) {
			getLogger().warn("Caught CNFE decoding %d bytes of data",
							inBytes == null ? 0 : inBytes.length, e);
		} finally {
			CloseUtil.close(is);
			CloseUtil.close(bis);
		}
		return rv;
	}
}